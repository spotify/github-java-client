#!/usr/bin/python3
import re
import subprocess
import sys


def get_arg(arg_idx) -> str:
    return sys.argv[arg_idx]


def get_current_version() -> str:
    current_version_cmd = subprocess.run(
        "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
        shell=True, capture_output=True, text=True)
    return current_version_cmd.stdout


def get_release_version(release_type: str, current_version: str) -> str:
    major, minor, patch = determine_new_version(current_version, release_type)
    return str(major) + "." + str(minor) + "." + str(patch)


def get_snapshot_version(release_type: str, current_version: str) -> str:
    major, minor, patch = determine_new_version(current_version, release_type)
    patch += 1
    return str(major) + "." + str(minor) + "." + str(patch) + "-SNAPSHOT"


def get_version_tag(release_type: str, current_version: str) -> str:
    major, minor, patch = determine_new_version(current_version, release_type)
    return "v" + str(major) + "." + str(minor) + "." + str(patch)


def determine_new_version(current_version, release_type):
    major, minor, patch = dissect_version(current_version)

    match release_type:
        case "MAJOR":
            major, minor, patch = get_major_release_version(major)
        case "MINOR":
            major, minor, patch = get_minor_release_version(major, minor)
        case "PATCH":
            major, minor, patch = get_patch_release_version(major, minor, patch)
        case _:
            print("Second arg has to be `MAJOR`, `MINOR` or `PATCH`")
            sys.exit()

    return major, minor, patch


def dissect_version(current_version) -> (int, int, int):
    version_regex = re.compile(
        r'^(?P<major>[0-9]+)\.(?P<minor>[0-9+])\.(?P<patch>[0-9+])(-SNAPSHOT)*')
    regex_match = version_regex.search(current_version)
    major: int = int(regex_match.groupdict().get("major"))
    minor: int = int(regex_match.groupdict().get("minor"))
    patch: int = int(regex_match.groupdict().get("patch"))
    return major, minor, patch


def get_major_release_version(major):
    major += 1
    minor = 0
    patch = 0
    return major, minor, patch


def get_minor_release_version(major, minor):
    minor += 1
    patch = 0
    return major, minor, patch


def get_patch_release_version(major, minor, patch):
    # Leave values as is because current version without `-SNAPSHOT` is new patch version
    return major, minor, patch


if __name__ == "__main__":

    version_type = get_arg(1)
    release_type = get_arg(2)

    current_version = get_current_version()

    match version_type:
        case "release-version":
            new_version = get_release_version(release_type, current_version)
        case "version-tag":
            new_version = get_version_tag(release_type, current_version)
        case "snapshot-version":
            new_version = get_snapshot_version(release_type, current_version)
        case _:
            print(
                "First arg has to be `release-version`, `version-tag` or `snapshot-version`.")
            sys.exit()

    print(new_version)
