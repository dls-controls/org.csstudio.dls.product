#!/bin/bash
# Helper script to run the cs-studio binary as an EDM converter
# application using the DLS colors.list file·

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
EDM_COLORS="${SCRIPT_DIR}/configuration/edm-colors.list"
CS_STUDIO_BINARY="${SCRIPT_DIR}/cs-studio"

"$CS_STUDIO_BINARY" -nosplash -application org.csstudio.opibuilder.converter.edl "$@" -vmargs -Dedm2xml.colorsFile="${EDM_COLORS}"
