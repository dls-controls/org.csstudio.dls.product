#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

TEMPLATE_FILE=splash-template.bmp
OUTPUT_FILE=splash.bmp

FONT_SIZE=14
HEIGHT_OFFSET=14
FONT=Nimbus-Sans-Bold

version=$(cut -d "=" -f 2 <<< cat $DIR/about.mappings)
convert $TEMPLATE_FILE \
    -gravity south \
    -pointsize $FONT_SIZE \
    -fill white -font $FONT \
    -annotate +0+"$HEIGHT_OFFSET" "$version" \
    +gamma 0 +matte \
    splash.bmp

