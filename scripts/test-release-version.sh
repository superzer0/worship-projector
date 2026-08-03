#!/usr/bin/env bash
set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
version_script="$script_dir/release-version.sh"

[[ $("$version_script" 42) == '1.0.42' ]]

if "$version_script" 0 >/dev/null 2>&1; then
    echo "Run number zero must be rejected" >&2
    exit 1
fi

if "$version_script" invalid >/dev/null 2>&1; then
    echo "A non-numeric run number must be rejected" >&2
    exit 1
fi

printf 'Release version tests passed\n'
