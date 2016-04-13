#!/bin/bash
# Wrapper script for CS-Studio.

function usage() {
    echo "Usage: $0 [args]
General arguments:
    [-w <workspace>]
    [-p <port>] (only 5064 or 6064 supported)
Arguments to run an opi file:
    [-o <opifile>] [Eclipse path; links required]
    [-l <links>]
    [-m <macros>] in the form a=b,c=d
    [-s] launch opi as standalone window

Note: If you specify an opi file to launch and there is an existing instance of
CS-Studio running, then the workspace argument is ignored.
    "
}

# This script is intended to be installed alongside the cs-studio binary.
CSS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CSSTUDIO=$CSS_DIR/cs-studio

# Default values.
opishell=false
port=5064

while getopts "w:p:o:m:sl:" o; do
    case ${o} in
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
    # Opening in a standalone window is just a special macro.
    if [[ $opishell = true ]]; then
        if [[ -n $macros ]]; then
            macros="${macros},Position=NEW_SHELL"
        else
            macros="Position=NEW_SHELL"
        fi
    fi

    launch_opi_arg=--launcher.openFile
    launch_opi="$opifile $macros"
    echo $links
    if [[ -n "${links}" ]]; then
        launch_opi="$launch_opi -share_link $links"
    fi
    # CSS cannot accept . in a command-line argument (other than in the filename).
    # This replaces with the CSS escape mechanism [\46].
    # Accepted extensions: opi, nws
    launch_opi_escaped=$(echo $launch_opi | perl -ne "s|\.(?!opi\|nws)|[\\\46]|g; print;")
fi


# Echo subsequent commands for debugging.
set -x
exec $CSSTUDIO $port_args $data_args "$launch_opi_arg" "$launch_opi_escaped"
