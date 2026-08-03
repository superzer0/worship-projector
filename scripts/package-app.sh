#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 VERSION PLATFORM ARCH" >&2
    exit 2
fi

version="$1"
platform="$2"
arch="$3"

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Version must use X.Y.Z format: $version" >&2
    exit 2
fi
if (( 10#${version%%.*} < 1 )); then
    echo "Version major component must be positive for cross-platform packaging: $version" >&2
    exit 2
fi

case "$platform" in
    linux|macos) ;;
    *)
        echo "Unsupported Unix platform: $platform" >&2
        exit 2
        ;;
esac

case "$arch" in
    x64|arm64) ;;
    *)
        echo "Unsupported architecture: $arch" >&2
        exit 2
        ;;
esac

command -v jpackage >/dev/null 2>&1 || {
    echo "jpackage from JDK 21 is required" >&2
    exit 1
}

jar_file="target/worship-1.0-SNAPSHOT.jar"
dependency_dir="target/dependency"
if [[ ! -f "$jar_file" || ! -d "$dependency_dir" ]]; then
    echo "Run Maven package and dependency:copy-dependencies before packaging" >&2
    exit 1
fi

release_root="target/release"
input_dir="$release_root/input"
app_dir="$release_root/app"
dist_dir="dist"
artifact_name="jWorship-${version}-${platform}-${arch}"

rm -rf "$release_root"
mkdir -p "$input_dir/lib" "$app_dir" "$dist_dir"
cp "$jar_file" "$input_dir/jWorship.jar"
cp "$dependency_dir"/*.jar "$input_dir/lib/"

jpackage \
    --type app-image \
    --name jWorship \
    --app-version "$version" \
    --vendor "jWorship contributors" \
    --description "Worship lyrics projection application" \
    --input "$input_dir" \
    --dest "$app_dir" \
    --main-jar jWorship.jar \
    --main-class sk.calvary.worship.App \
    --add-modules ALL-MODULE-PATH \
    --java-options "-Dfile.encoding=UTF-8"

if [[ "$platform" == "linux" ]]; then
    archive="$dist_dir/${artifact_name}.tar.gz"
    tar -C "$app_dir" -czf "$archive" jWorship
    (
        cd "$dist_dir"
        sha256sum "$(basename "$archive")" >"$(basename "$archive").sha256"
    )
else
    archive="$dist_dir/${artifact_name}.zip"
    ditto -c -k --sequesterRsrc --keepParent "$app_dir/jWorship.app" "$archive"
    (
        cd "$dist_dir"
        shasum -a 256 "$(basename "$archive")" >"$(basename "$archive").sha256"
    )
fi

printf 'Created %s\n' "$archive"
