#!/bin/sh
set -eu

case "$(uname -m)" in
    aarch64|arm64)
        export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -XX:UseSVE=0"
        ;;
esac

exec mvn "$@"
