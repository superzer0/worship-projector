#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 REPOSITORY RELEASE_TAG EXPECTED_COMMIT_SHA" >&2
    exit 2
fi

repository=$1
release_tag=$2
expected_sha=$3
max_attempts=${RELEASE_TAG_MAX_ATTEMPTS:-6}
retry_delay=${RELEASE_TAG_RETRY_DELAY:-1}

if [[ ! "$max_attempts" =~ ^[1-9][0-9]*$ || ! "$retry_delay" =~ ^[0-9]+$ ]]; then
    echo 'Retry settings must be non-negative integers and attempts must be positive' >&2
    exit 2
fi

error_file=$(mktemp)
trap 'rm -f "$error_file"' EXIT
tag_ref=''
delay=$retry_delay

for ((attempt = 1; attempt <= max_attempts; attempt++)); do
    if tag_ref=$(gh api "repos/$repository/git/ref/tags/$release_tag" 2>"$error_file"); then
        break
    fi
    if grep -q '(HTTP 404)' "$error_file"; then
        if (( attempt < max_attempts )); then
            echo "Tag $release_tag is not visible yet; retrying verification" >&2
            sleep "$delay"
            (( delay == 0 )) || delay=$((delay * 2))
            continue
        fi
        echo "Tag $release_tag was not visible after $max_attempts attempts" >&2
        cat "$error_file" >&2
        exit 1
    fi
    cat "$error_file" >&2
    exit 1
done

object_type=$(jq -er '.object.type' <<<"$tag_ref")
object_sha=$(jq -er '.object.sha' <<<"$tag_ref")
while [[ "$object_type" == 'tag' ]]; do
    tag_object=$(gh api "repos/$repository/git/tags/$object_sha")
    object_type=$(jq -er '.object.type' <<<"$tag_object")
    object_sha=$(jq -er '.object.sha' <<<"$tag_object")
done

if [[ "$object_type" != 'commit' || "$object_sha" != "$expected_sha" ]]; then
    echo "The remote tag no longer points to the commit that produced these artifacts" >&2
    exit 1
fi
