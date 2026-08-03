#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 PATH_TO_MACOS_ZIP" >&2
    exit 2
fi

archive=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
if [[ ! -f "$archive" ]]; then
    echo "Release archive was not found: $archive" >&2
    exit 1
fi

smoke_root=$(mktemp -d)
extract_root="$smoke_root/extract"
user_home="$smoke_root/home"
app_data="$user_home/jWorship"
log_file="$smoke_root/startup.log"
ready_file="$smoke_root/ui-ready.txt"
app_pid=''
cleanup() {
    if [[ -n "$app_pid" ]]; then
        kill "$app_pid" 2>/dev/null || true
        wait "$app_pid" 2>/dev/null || true
    fi
    rm -rf "$smoke_root"
}
trap cleanup EXIT INT TERM

mkdir -p "$extract_root" "$app_data/pictures" "$app_data/songs" "$app_data/videos" \
    "$app_data/settings" "$app_data/thumbnailCache"
ditto -x -k "$archive" "$extract_root"

launcher="$extract_root/jWorship.app/Contents/MacOS/jWorship"
packaged_jar="$extract_root/jWorship.app/Contents/app/jWorship.jar"
if [[ ! -x "$launcher" ]]; then
    echo "Packaged launcher was not found or executable: $launcher" >&2
    exit 1
fi
if [[ ! -f "$packaged_jar" ]]; then
    echo "Packaged application JAR was not found: $packaged_jar" >&2
    exit 1
fi
unzip -p "$packaged_jar" sk/calvary/misc/lang.lng >"$app_data/settings/lang.lng"

JAVA_TOOL_OPTIONS="-Duser.home=$user_home" JWORSHIP_TEST_READY_FILE="$ready_file" \
    "$launcher" -testmode >"$log_file" 2>&1 &
app_pid=$!

ready=false
for _ in {1..90}; do
    if grep -Fqx 'JWORSHIP_UI_READY' "$ready_file" 2>/dev/null; then
        ready=true
        break
    fi
    if ! kill -0 "$app_pid" 2>/dev/null; then
        cat "$log_file" >&2
        echo "Packaged macOS application exited before reporting readiness" >&2
        exit 1
    fi
    sleep 1
done
if [[ "$ready" != true ]]; then
    cat "$log_file" >&2
    echo "Packaged macOS application did not report readiness within 90 seconds" >&2
    exit 1
fi

for _ in {1..15}; do
    if ! kill -0 "$app_pid" 2>/dev/null; then
        cat "$log_file" >&2
        echo "Packaged macOS application exited during the observation window" >&2
        exit 1
    fi
    sleep 1
done

echo "macOS packaged application smoke test passed"
