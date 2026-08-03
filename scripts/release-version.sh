#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 || ! "$1" =~ ^[1-9][0-9]*$ ]]; then
    echo "Usage: $0 POSITIVE_GITHUB_RUN_NUMBER" >&2
    exit 2
fi

printf '1.0.%s\n' "$1"
