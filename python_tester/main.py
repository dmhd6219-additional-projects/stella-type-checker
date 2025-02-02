import subprocess
import os
import sys


def run_stella_exe(content):
    try:
        result = subprocess.run(["stella.exe", "typecheck"], input=content, capture_output=True, text=True, timeout=5)
        if "extension?" in result.stdout:
            return "NO_EXTENSION"
        elif "Type Error Tag:" in result.stdout:
            return result.stdout.split("Type Error Tag: [", 2)[1].split("]", 2)[0]
        elif "Input program is well-typed!" in result.stdout:
            return "OK"
        return result.stdout.strip()

    except Exception as e:
        return str(e)


def run_stella_jar(content):
    try:
        result = subprocess.run(["java", "-jar", "stella-implementation-in-java-1.0-SNAPSHOT.jar"], input=content, capture_output=True, text=True)
        return 'OK' if result.returncode == 0 else result.stderr.strip().split(':')[1].strip().split(' ')[0] if result.stderr.strip() else ""

    except Exception as e:
        return str(e)


def main(folder):
    for file in os.listdir(folder):
        if file.endswith(".stella"):
            filepath = os.path.join(folder, file)
            with open(filepath, "r", encoding="utf8") as f:
                content = '\n'.join(f.readlines())
                exe_output = run_stella_exe(content)
                jar_output = run_stella_jar(content)

            if exe_output != jar_output:
                print(f"Mismatch in {file}:\n  stella.exe: {exe_output}\n  stella.jar: {jar_output}\n", file=sys.stderr,
                      flush=True)


if __name__ == "__main__":
    test_folder = 'week-1/main/public'

    main(test_folder)
