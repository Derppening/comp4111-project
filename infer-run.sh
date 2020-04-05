#!/usr/bin/env sh

# Important Note: "--eradicate" is not enabled by default, because it causes too many false positives.
# The worst offender is assuming that all non-annotated parameters, return values and fields are by default NotNull.
# Some false positives can be fixed by introducing helper methods that enact the pattern it wants, but for now I think
# it is too much effort to adjust to a tool like this.
#
# Suggested workflow is to run this script once without "--eradicate", then after all those bugs are fixed, run with
# "--eradicate". Review each item and ensure that they are **NOT FALSE POSITIVES** before actually fixing it.

help() {
  echo "Usage: $(basename "$0") [INFER_ARGS]..."
  echo
}

if [ "$1" = "--help" ]; then
  help
  exit 1
fi

./gradlew clean && infer run -a checkers "$@" -- ./gradlew assemble

command -v less >/dev/null 2>&1 || {
  apt install less
}

if [ -f infer-out/bugs.txt ]; then
  less infer-out/bugs.txt
fi
