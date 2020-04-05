#!/usr/bin/env sh

./gradlew clean && infer run -a checkers --eradicate -- ./gradlew assemble

command -v less >/dev/null 2>&1 || {
  apt install less
}

if [ -f infer-out/bugs.txt ]; then
  less infer-out/bugs.txt
fi
