#!/usr/bin/env python3
"""
pusher.py — Automatizador de flujo Git.

Uso:
    python3 pusher.py

Hace: git add . → git commit → git push origin main

Requisitos:
    - Variable de entorno GITHUB_PERSONAL_ACCESS_TOKEN con un token de GitHub válido.
    - El remoto 'origin' apuntando a un repositorio de GitHub (HTTPS o SSH).

El script auto-configura todo lo que puede (usuario Git, conversión SSH→HTTPS)
y muestra mensajes claros ante cualquier problema.
"""

import os
import re
import subprocess
import sys
from datetime import datetime


# ─── Helpers de salida ────────────────────────────────────────────────────────

SEP = "─" * 56

def header(title):
    print(f"\n{SEP}")
    print(f"  {title}")
    print(SEP)

def ok(msg):   print(f"  ✓  {msg}")
def info(msg): print(f"  →  {msg}")
def warn(msg): print(f"  ⚠  {msg}")
def fail(msg):
    print(f"\n  ✗  ERROR: {msg}\n")


# ─── Ejecución de comandos ────────────────────────────────────────────────────

def run(cmd, cwd=None):
    """
    Ejecuta un comando. Devuelve (returncode, stdout, stderr).
    Siempre captura la salida; muestra stderr solo si hay error.
    """
    result = subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        cwd=cwd,
    )
    return result.returncode, result.stdout.strip(), result.stderr.strip()


# ─── Verificaciones y preparación ────────────────────────────────────────────

def check_git_repo():
    """El directorio actual debe ser (o estar dentro de) un repo Git."""
    code, _, _ = run(["git", "rev-parse", "--is-inside-work-tree"])
    if code != 0:
        fail("El directorio actual NO es un repositorio Git.")
        info("Inicialízalo con:  git init")
        sys.exit(1)
    ok("Repositorio Git detectado.")


def ensure_git_user():
    """
    Configura user.name y user.email si no están definidos.
    Git no permite hacer commits sin ellos.
    """
    _, name, _  = run(["git", "config", "user.name"])
    _, email, _ = run(["git", "config", "user.email"])

    if not name:
        run(["git", "config", "user.name", "pusher-bot"])
        ok("user.name configurado automáticamente como 'pusher-bot'.")
    else:
        ok(f"user.name: {name}")

    if not email:
        run(["git", "config", "user.email", "pusher@noreply.local"])
        ok("user.email configurado automáticamente.")
    else:
        ok(f"user.email: {email}")


def get_remote_url():
    """Obtiene la URL del remoto 'origin'. Sale si no existe."""
    code, url, _ = run(["git", "remote", "get-url", "origin"])
    if code != 0 or not url:
        fail("No hay ningún remoto 'origin' configurado.")
        info("Agrégalo con:  git remote add origin https://github.com/USUARIO/REPO.git")
        sys.exit(1)
    ok(f"Remoto origin: {url}")
    return url


def build_authenticated_url(remote_url):
    """
    Construye una URL HTTPS autenticada inyectando el token.
    Convierte automáticamente URLs SSH a HTTPS cuando hay token disponible.

    Formatos reconocidos:
      HTTPS : https://github.com/user/repo.git
      SSH   : git@github.com:user/repo.git
    """
    token = os.environ.get("GITHUB_PERSONAL_ACCESS_TOKEN", "").strip()

    if not token:
        warn("GITHUB_PERSONAL_ACCESS_TOKEN no está definido.")
        warn("El push usará la autenticación por defecto del sistema (puede fallar).")
        return None, remote_url  # sin URL autenticada

    # ── Caso HTTPS ────────────────────────────────────────────────────────────
    if remote_url.startswith("https://") and "github.com" in remote_url:
        # Eliminar cualquier token previo incrustado en la URL
        clean = re.sub(r"https://[^@]+@", "https://", remote_url)
        auth_url = clean.replace("https://", f"https://{token}@", 1)
        ok("Token GITHUB_PERSONAL_ACCESS_TOKEN inyectado en URL HTTPS.")
        return auth_url, remote_url

    # ── Caso SSH ──────────────────────────────────────────────────────────────
    # git@github.com:usuario/repo.git  →  https://TOKEN@github.com/usuario/repo.git
    ssh_match = re.match(r"git@github\.com[:/](.+?)(?:\.git)?$", remote_url)
    if ssh_match:
        repo_path = ssh_match.group(1)
        auth_url = f"https://{token}@github.com/{repo_path}.git"
        ok(f"URL SSH convertida a HTTPS con token: https://***@github.com/{repo_path}.git")
        return auth_url, remote_url

    warn(f"No se reconoció el formato de la URL del remoto: {remote_url}")
    warn("El push usará la URL original sin autenticación automática.")
    return None, remote_url


def ensure_main_branch():
    """
    Asegura que la rama activa sea 'main'.
    Intenta cambiar a 'main' si existe; informa si la rama actual no es 'main'.
    """
    _, current, _ = run(["git", "branch", "--show-current"])

    if not current:
        # Sin commits aún — inicializa la rama como 'main'
        run(["git", "checkout", "-b", "main"])
        ok("Rama 'main' creada (repo sin commits previos).")
        return

    if current == "main":
        ok("Rama activa: main")
        return

    info(f"Rama activa: '{current}'. Buscando rama 'main'...")

    # ¿Existe 'main' localmente?
    _, branch_list, _ = run(["git", "branch", "--list", "main"])
    if branch_list.strip():
        code, _, err = run(["git", "checkout", "main"])
        if code != 0:
            fail(f"No se pudo cambiar a 'main': {err}")
            sys.exit(1)
        ok("Cambiado a rama 'main'.")
        return

    # 'main' no existe → ofrecer renombrar la rama actual
    warn(f"La rama 'main' no existe. La rama actual es '{current}'.")
    respuesta = input(f"  ¿Renombrar '{current}' → 'main' y continuar? [s/N]: ").strip().lower()
    if respuesta in ("s", "si", "sí", "yes", "y"):
        code, _, err = run(["git", "branch", "-m", current, "main"])
        if code != 0:
            fail(f"No se pudo renombrar la rama: {err}")
            sys.exit(1)
        ok("Rama renombrada a 'main'.")
    else:
        fail(f"Detención por solicitud del usuario. La rama activa sigue siendo '{current}'.")
        sys.exit(1)


def has_changes():
    """True si hay archivos modificados, nuevos o eliminados sin commitear."""
    _, out, _ = run(["git", "status", "--porcelain"])
    return bool(out.strip())


# ─── Flujo Git ────────────────────────────────────────────────────────────────

def git_add():
    info("Ejecutando: git add .")
    code, _, err = run(["git", "add", "."])
    if code != 0:
        fail(f"git add falló:\n    {err}")
        sys.exit(1)
    ok("Todos los cambios añadidos al área de staging.")


def git_commit():
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M")
    message = f"chore: actualización automática [{timestamp}]"
    info(f'Creando commit: "{message}"')

    code, out, err = run(["git", "commit", "-m", message])
    combined = (out + "\n" + err).strip()

    if code != 0:
        if "nothing to commit" in combined:
            warn("No hay cambios nuevos tras el staging. El commit no se creó.")
            return False
        fail(f"git commit falló:\n    {combined}")
        sys.exit(1)

    ok("Commit creado.")
    info(out.split("\n")[0] if out else "")
    return True


def git_push(auth_url, original_url):
    """
    Hace push a 'main'. Intenta primero con la URL autenticada,
    y si falla porque no hay upstream configurado, añade --set-upstream.
    """
    push_url = auth_url if auth_url else "origin"
    display  = original_url if auth_url else "origin"
    info(f"Ejecutando: git push {display} main")

    code, out, err = run(["git", "push", push_url, "main"])
    combined = (out + "\n" + err).strip()

    # ── Si falla por no tener upstream, reintenta con --set-upstream ──────────
    if code != 0 and ("no upstream" in combined or "has no upstream" in combined
                      or "set-upstream" in combined):
        info("Primer push al remoto, estableciendo upstream...")
        code, out, err = run(["git", "push", "--set-upstream", push_url, "main"])
        combined = (out + "\n" + err).strip()

    if code != 0:
        if "Authentication failed" in combined or "could not read Username" in combined:
            fail("Autenticación fallida.")
            info("Verifica que GITHUB_PERSONAL_ACCESS_TOKEN esté definido y sea válido.")
            info("En Replit: Secrets → GITHUB_PERSONAL_ACCESS_TOKEN → <tu token>")
        elif "non-fast-forward" in combined or "rejected" in combined:
            fail("Push rechazado: el remoto tiene commits que no tienes localmente.")
            info("Ejecuta primero:  git pull origin main --rebase")
        elif "Repository not found" in combined or "does not exist" in combined:
            fail("Repositorio no encontrado. Verifica la URL del remoto y tus permisos.")
        else:
            fail(f"git push falló:\n    {combined}")
        sys.exit(1)

    ok("Push completado con éxito.")
    if combined:
        for line in combined.split("\n"):
            if line.strip():
                info(line.strip())


# ─── Punto de entrada ─────────────────────────────────────────────────────────

def main():
    header("PUSHER — Flujo Git Automatizado")

    # 1. Verificar entorno
    check_git_repo()
    ensure_git_user()
    remote_url = get_remote_url()
    ensure_main_branch()

    print()

    # 2. Comprobar si hay algo que hacer
    if not has_changes():
        warn("No hay cambios en el proyecto. El repo ya está al día.")
        print(f"\n{SEP}\n")
        sys.exit(0)

    # 3. Construir URL autenticada
    auth_url, original_url = build_authenticated_url(remote_url)

    print()

    # 4. Flujo Git
    git_add()
    committed = git_commit()
    if not committed:
        print(f"\n{SEP}\n")
        sys.exit(0)

    git_push(auth_url, original_url)

    print(f"\n{SEP}")
    ok("¡Todo listo! Los cambios ya están en GitHub.")
    print(f"{SEP}\n")


if __name__ == "__main__":
    main()
