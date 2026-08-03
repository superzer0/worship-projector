#!/usr/bin/env bash
set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
verifier="$script_dir/verify-release-tag.sh"
tmp_dir=$(mktemp -d)
trap 'rm -rf "$tmp_dir"' EXIT

cat >"$tmp_dir/gh" <<'MOCK'
#!/usr/bin/env bash
set -euo pipefail
endpoint=$2
if [[ "$endpoint" == */git/ref/tags/* ]]; then
    count=0
    [[ ! -f "$MOCK_STATE" ]] || count=$(<"$MOCK_STATE")
    count=$((count + 1))
    printf '%s\n' "$count" >"$MOCK_STATE"
    if (( count <= MOCK_ERROR_COUNT )); then
        echo "$MOCK_ERROR_MESSAGE" >&2
        exit 1
    fi
    printf '{"object":{"type":"%s","sha":"%s"}}\n' "$MOCK_REF_TYPE" "$MOCK_REF_SHA"
elif [[ "$endpoint" == 'repos/owner/repo/git/tags/tag-object' ]]; then
    printf '{"object":{"type":"commit","sha":"%s"}}\n' "$MOCK_COMMIT_SHA"
else
    echo "Unexpected mock endpoint: $endpoint" >&2
    exit 3
fi
MOCK
chmod +x "$tmp_dir/gh"
export PATH="$tmp_dir:$PATH"
export MOCK_STATE="$tmp_dir/count"
export MOCK_ERROR_COUNT=1 MOCK_ERROR_MESSAGE='gh: Not Found (HTTP 404)'
export MOCK_REF_TYPE=commit MOCK_REF_SHA=abc MOCK_COMMIT_SHA=abc

RELEASE_TAG_MAX_ATTEMPTS=3 RELEASE_TAG_RETRY_DELAY=0 "$verifier" owner/repo v1.0.7 abc
[[ $(<"$MOCK_STATE") == 2 ]]

rm -f "$MOCK_STATE"
export MOCK_ERROR_COUNT=0 MOCK_REF_TYPE=tag MOCK_REF_SHA=tag-object MOCK_COMMIT_SHA=abc
RELEASE_TAG_RETRY_DELAY=0 "$verifier" owner/repo v1.0.7 abc

export MOCK_REF_TYPE=commit MOCK_REF_SHA=wrong
if RELEASE_TAG_RETRY_DELAY=0 "$verifier" owner/repo v1.0.7 abc >/dev/null 2>&1; then
    echo 'Expected a moved tag to be rejected' >&2
    exit 1
fi

rm -f "$MOCK_STATE"
export MOCK_ERROR_COUNT=10 MOCK_ERROR_MESSAGE='gh: Not Found (HTTP 404)'
if RELEASE_TAG_MAX_ATTEMPTS=3 RELEASE_TAG_RETRY_DELAY=0 "$verifier" owner/repo v1.0.7 abc 2>"$tmp_dir/exhausted"; then
    echo 'Expected repeated 404 responses to exhaust retries' >&2
    exit 1
fi
[[ $(<"$MOCK_STATE") == 3 ]]
grep -q 'not visible after 3 attempts' "$tmp_dir/exhausted"

rm -f "$MOCK_STATE"
export MOCK_ERROR_MESSAGE='gh: Internal Server Error (HTTP 500)'
if RELEASE_TAG_MAX_ATTEMPTS=3 RELEASE_TAG_RETRY_DELAY=0 "$verifier" owner/repo v1.0.7 abc 2>"$tmp_dir/non-404"; then
    echo 'Expected a non-404 API error to fail' >&2
    exit 1
fi
[[ $(<"$MOCK_STATE") == 1 ]]
grep -q 'HTTP 500' "$tmp_dir/non-404"

echo 'Release tag verification tests passed'
