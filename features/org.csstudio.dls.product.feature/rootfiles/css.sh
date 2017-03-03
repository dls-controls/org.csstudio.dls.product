#!/bin/bash
# Wrapper script for CS-Studio.

function usage() {
    echo "Usage: $0 [args]
General arguments:
    [-w <workspace>]
    [-p <port>] (only 5064 or 6064 supported)
Arguments to run an opi file:
    [-o <opifile>] [Eclipse path; links required]
    [-l <links>] in the form path1=eclipse_path1,path2=eclipse_path2,...
    [-m <macros>] in the form key1=value1,key2=value2,...
    [-s] launch opi as standalone window

Notes:
 - If you specify an opi file to launch and there is an existing instance of CS-Studio
 running, then the workspace argument is ignored.
 - You can specify simply macros as part of the opifile argument [-o '<opifile> <macros>']
 but these will not work with the -s flag and may cause an error as restricted
 characters will not be escaped correctly.
    "
}

function escape() {
    # CSS cannot accept : or . in LINKS or MACROS when passed on the command
    # line. The affected characters must be replaced using the CSS escape
    # mechanism of [\<ascii-code>].
    # The backslash is double escaped as string is parsed twice before being
    # executed.
    echo $(echo $1 | perl -ne "s|:|[\\\58]|g; print" | perl -ne "s|\.|[\\\46]|g; print;")
}

# This script is intended to be installed alongside the cs-studio binary.
CSS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CSSTUDIO=$CSS_DIR/cs-studio

# Default values.
opishell=false
port=5064

while getopts "w:p:o:m:sl:" opt; do
    case $opt in
        w)
            workspace=${OPTARG}
            ;;
        p)
            port=${OPTARG}
            ;;
        o)
            opifile=${OPTARG}
            ;;
        m)
            macros=${OPTARG}
            ;;
        s)
            opishell=true
            ;;
        l)
            links=${OPTARG}
            ;;
        *)
            echo "Unexpected argument ${OPTARG}"
            usage
            exit 1
            ;;
    esac
done


# Port
if [[ -n $port ]]; then
    if [[ $port = "5064" ]]; then
        # default product; default arguments
        port_args="-name cs-studio"
    elif [[ $port = "6064" ]]; then
        port_args="-product org.csstudio.dls.product.6064.product -name cs-studio-6064"
    else
        echo "Only ports 5064 and 6064 are supported by this script."
        usage
        exit 1
    fi
fi


# Workspace
if [[ -n $workspace ]]; then
    data_args="-data $workspace"
else
    data_args="-data $HOME/.cs-studio-$port"
fi


# OPI file and related options.
if [[ -n $macros ]] || [[ -n $links ]]; then
    if [[ -z $opifile ]]; then
        echo "Macros and links arguments require an opi file argument."
        usage
        exit 1
    fi
fi

if [[ -n $opifile ]]; then
    launch_opi_cmd=--launcher.openFile
fi

# Opening in a standalone window is just a special macro.
if [[ $opishell = true ]]; then
    if [[ -n $macros ]]; then
        macros="$macros,Position=NEW_SHELL"
    else
        macros="Position=NEW_SHELL"
    fi
fi
macros_escaped=$(escape "$macros")

if [[ -n $links ]]; then
    links_escaped="-share_link $(escape "$links")"
fi

# Echo subsequent commands for debugging.
set -x
exec $CSSTUDIO $port_args $data_args $launch_opi_cmd "$opifile $macros_escaped $links_escaped"
