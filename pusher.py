#!/usr/bin/env python3
"""
pusher.py - Automatizador de flujo Git para el proyecto.
Uso: python3 pusher.py

Hace: git add . → git commit → git push origin main
Sin comandos destructivos, sin forzar pushes, sin borrar nada.
"""

import subprocess
import sys
import os
from datetime import datetime


# ─── Utilidades ──────────────────────────────────────────────────────────────

def run(cmd, capture=True, cwd=None):
    """Ejecuta un comando y devuelve (returncode, stdout, stderr)."""
    result = subprocess.run(
        cmd,
        capture_output=capture,
        text=True,
        cwd=cwd,
    )
    return result.returncode, result.stdout.strip(), result.stderr.strip()


def ok(msg):
    print(f"  ✓  {msg}")


def info(msg):
    print(f"  →  {msg}")


def warn(msg):
    print(f"  ⚠  {msg}")


def error(msg):
    print(f"  ✗  {msg}")


def separator():
    print("─" * 52)


# ─── Verificaciones ───────────────────────────────────────────────────────────

def check_git_repo():
    """Verifica que el directorio actual sea un repositorio Git."""
    code, out, err = run(["git", "rev-parse", "--is-inside-work-tree"])
    if code != 0:
        error("El directorio actual NO es un repositorio Git.")
        info("Inicializa uno con: git init")
        sys.exit(1)
    ok("Repositorio Git detectado.")


def check_git_user():
    """Verifica que git tenga user.name y user.email configurados."""
    _, name, _ = run(["git", "config", "user.name"])
    _, email, _ = run(["git", "config", "user.email"])
    missing = []
    if not name:
        missing.append("user.name")
    if not email:
        missing.append("user.email")
    if missing:
        error("Falta configuración de usuario en Git:")
        for field in missing:
            info(f"  git config --global {field} \"tu valor\"")
        sys.exit(1)
    ok(f"Usuario Git: {name} <{email}>")


def check_remote():
    """Verifica que exista un remoto 'origin'."""
    code, out, err = run(["git", "remote", "get-url", "origin"])
    if code != 0:
        error("No hay ningún remoto 'origin' configurado.")
        info("Agrégalo con: git remote add origin <URL>")
        sys.exit(1)
    ok(f"Remoto origin: {out}")
    return out


def configure_auth(remote_url):
    """
    Si existe GITHUB_PERSONAL_ACCESS_TOKEN, inyecta el token en la URL
    del remoto para autenticación HTTPS sin interacción manual.
    No modifica el remoto permanentemente.
    Devuelve la URL autenticada (o la original si no aplica).
    """
    token = os.environ.get("GITHUB_PERSONAL_ACCESS_TOKEN", "").strip()
    if not token:
        return remote_url

    if remote_url.startswith("https://") and "github.com" in remote_url:
        # Forma segura: https://TOKEN@github.com/usuario/repo.git
        authenticated = remote_url.replace(
            "https://", f"https://{token}@", 1
        )
        ok("Token de GitHub detectado y configurado para esta sesión.")
        return authenticated

    # SSH o URL no-HTTPS: el token no aplica
    return remote_url


def ensure_main_branch():
    """
    Verifica que la rama activa sea 'main'.
    Si no lo es, intenta cambiar a 'main' si existe.
    """
    _, current, _ = run(["git", "branch", "--show-current"])
    if current == "main":
        ok("Rama activa: main")
        return

    info(f"Rama activa: '{current}'. Intentando cambiar a 'main'...")

    # ¿Existe la rama 'main' localmente?
    code, out, _ = run(["git", "branch", "--list", "main"])
    if out.strip():
        code2, _, err2 = run(["git", "checkout", "main"])
        if code2 != 0:
            error(f"No se pudo cambiar a 'main': {err2}")
            sys.exit(1)
        ok("Cambiado a rama 'main'.")
    else:
        error("La rama 'main' no existe en este repositorio.")
        info("Créala con: git checkout -b main")
        info(f"Actualmente estás en la rama: '{current}'")
        sys.exit(1)


def check_changes():
    """
    Devuelve True si hay cambios para commitear (staged o unstaged).
    Devuelve False si el árbol de trabajo está limpio.
    """
    code, out, _ = run(["git", "status", "--porcelain"])
    if not out:
        return False
    return True


# ─── Flujo principal ──────────────────────────────────────────────────────────

def git_add():
    info("Ejecutando: git add .")
    code, _, err = run(["git", "add", "."])
    if code != 0:
        error(f"Falló git add: {err}")
        sys.exit(1)
    ok("Todos los cambios añadidos al staging.")


def git_commit():
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M")
    message = f"chore: actualización automática [{timestamp}]"
    info(f"Commit: \"{message}\"")
    code, out, err = run(["git", "commit", "-m", message])
    if code != 0:
        # Puede que no haya nada nuevo después del add (e.g. archivos ignorados)
        if "nothing to commit" in out or "nothing to commit" in err:
            warn("No hay cambios nuevos para commitear.")
            return False
        error(f"Falló git commit:\n{err or out}")
        sys.exit(1)
    ok("Commit creado correctamente.")
    return True


def git_push(authenticated_url):
    info("Ejecutando: git push origin main")
    if authenticated_url:
        cmd = ["git", "push", authenticated_url, "main"]
    else:
        cmd = ["git", "push", "origin", "main"]

    code, out, err = run(cmd)
    combined = (out + "\n" + err).strip()

    if code != 0:
        # Intentar dar mensajes de error comprensibles
        if "Authentication failed" in combined or "could not read Username" in combined:
            error("Autenticación fallida.")
            info("Verifica que GITHUB_PERSONAL_ACCESS_TOKEN esté configurado y sea válido.")
        elif "rejected" in combined and "non-fast-forward" in combined:
            error("Push rechazado: la rama remota tiene cambios que no tienes localmente.")
            info("Haz primero: git pull origin main --rebase")
        elif "does not exist" in combined or "not found" in combined:
            error("El repositorio remoto no existe o no tienes acceso.")
        else:
            error(f"Falló git push:\n{combined}")
        sys.exit(1)

    ok("Push a 'origin main' completado con éxito.")
    if combined:
        info(combined)


# ─── Punto de entrada ─────────────────────────────────────────────────────────

def main():
    separator()
    print("          PUSHER — Flujo Git Automatizado")
    separator()

    check_git_repo()
    check_git_user()
    remote_url = check_remote()
    ensure_main_branch()

    separator()

    if not check_changes():
        warn("No hay cambios en el proyecto. Nada que hacer.")
        separator()
        sys.exit(0)

    authenticated_url = configure_auth(remote_url)

    git_add()
    committed = git_commit()

    if not committed:
        separator()
        sys.exit(0)

    git_push(authenticated_url)

    separator()
    ok("Todo listo. Los cambios están en GitHub.")
    separator()


if __name__ == "__main__":
    main()
