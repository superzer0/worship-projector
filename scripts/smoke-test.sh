#!/usr/bin/env sh
set -eu

if ! command -v xvfb-run >/dev/null 2>&1; then
    echo "xvfb-run is required for the Swing smoke test" >&2
    exit 1
fi

if [ ! -d target/classes ] || [ ! -d target/dependency ]; then
    echo "Compile the application and copy runtime dependencies before running this script" >&2
    exit 1
fi

home_dir=$(mktemp -d)
trap 'rm -rf "$home_dir"' EXIT INT TERM

app_dir="$home_dir/jWorship"
log_file="$home_dir/startup.log"
mkdir -p "$app_dir/pictures" "$app_dir/songs" "$app_dir/videos" "$app_dir/settings" "$app_dir/thumbnailCache"
cp target/classes/sk/calvary/misc/lang.lng "$app_dir/settings/lang.lng"

set +e
xvfb-run -a timeout 15s java \
    -Duser.home="$home_dir" \
    -cp 'target/classes:target/dependency/*' \
    sk.calvary.worship.App -testmode >"$log_file" 2>&1
status=$?
set -e

if [ "$status" -eq 124 ] && grep -Fqx 'JWORSHIP_UI_READY' "$log_file"; then
    echo "Swing smoke test passed: the operator UI became visible and remained alive for 15 seconds"
    exit 0
fi

cat "$log_file" >&2

if [ "$status" -eq 0 ]; then
    echo "The application exited before the smoke-test observation window ended" >&2
else
    echo "The application failed during startup (exit $status)" >&2
fi
exit 1
