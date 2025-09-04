#!/usr/bin/env bash
set -euo pipefail

# Usage: ./run-tests-and-switch.sh <branch-name>
if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <branch-name>" >&2
  exit 2
fi
TARGET="$1"

if [[ -x "./gradlew" ]]; then
  ./gradlew test
else
  gradle test
fi

git checkout "$TARGET"
echo "Switched to branch: $TARGET"
