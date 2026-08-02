#!/usr/bin/env sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 PATH_TO_JWORSHIP_LAUNCHER" >&2
    exit 2
fi

launcher_dir=$(CDPATH='' cd -- "$(dirname "$1")" && pwd)
launcher="$launcher_dir/$(basename "$1")"
if [ ! -x "$launcher" ]; then
    echo "Packaged launcher is not executable: $launcher" >&2
    exit 1
fi

if ! command -v xvfb-run >/dev/null 2>&1; then
    echo "xvfb-run is required for the packaged Swing smoke test" >&2
    exit 1
fi
if ! command -v xdotool >/dev/null 2>&1; then
    echo "xdotool is required for the packaged Swing smoke test" >&2
    exit 1
fi

home_dir=$(mktemp -d)
trap 'rm -rf "$home_dir"' EXIT INT TERM

app_data_dir="$home_dir/jWorship"
log_file="$home_dir/startup.log"
mkdir -p "$app_data_dir/pictures" "$app_data_dir/songs" "$app_data_dir/videos" \
    "$app_data_dir/settings" "$app_data_dir/thumbnailCache"

app_root=$(CDPATH='' cd -- "$launcher_dir/.." && pwd)
packaged_jar="$app_root/lib/app/jWorship.jar"
if [ ! -f "$packaged_jar" ]; then
    echo "Packaged application JAR was not found: $packaged_jar" >&2
    exit 1
fi
unzip -p "$packaged_jar" sk/calvary/misc/lang.lng >"$app_data_dir/settings/lang.lng"

set +e
# The single-quoted program is evaluated by the nested shell, not this one.
# shellcheck disable=SC2016
JAVA_TOOL_OPTIONS="-Duser.home=$home_dir" timeout 40s xvfb-run -a sh -c '
    launcher=$1
    log_file=$2
    "$launcher" -testmode >"$log_file" 2>&1 &
    app_pid=$!
    cleanup() {
        kill "$app_pid" 2>/dev/null || true
        wait "$app_pid" 2>/dev/null || true
    }
    trap cleanup EXIT INT TERM

    count=0
    while [ "$count" -lt 15 ]; do
        if xdotool search --onlyvisible --name "^jWorship " >/dev/null 2>&1; then
            break
        fi
        kill -0 "$app_pid" 2>/dev/null || exit 1
        count=$((count + 1))
        sleep 1
    done
    [ "$count" -lt 15 ] || exit 1

    count=0
    while [ "$count" -lt 15 ]; do
        kill -0 "$app_pid" 2>/dev/null || exit 1
        count=$((count + 1))
        sleep 1
    done
' sh "$launcher" "$log_file"
status=$?
set -e

if [ "$status" -eq 0 ]; then
    echo "Packaged application smoke test passed: its window was visible and alive for 15 seconds"
    exit 0
fi

cat "$log_file" >&2
echo "The packaged application failed its startup check (exit $status)" >&2
exit 1
