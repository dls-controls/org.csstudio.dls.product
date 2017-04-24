#!/bin/bash
# Wrapper script for CS-Studio.
# Create a directory for personal files and link to it from CSS/ project in CS-Studio.
# Ensure that a maximum of two instances (cs-studio and cs-studio-dev) are running at any time.

function usage() {
    echo "Usage: $0 [args]
General arguments:
    [-w <workspace>]
    [-p <port>] (this option is IGNORED)
    [-d] run the 'dev' instance of CS-Studio.  This allows running on a different
         port or on a different machine via SSH.
Arguments to run an opi file:
    [-o <opifile>] [Eclipse path; links required]
    [-l <links>] in the form path1=eclipse_path1,path2=eclipse_path2,...
    [-x <xmi file>]
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
dev=false
port=5064

while getopts "w:p:do:x:m:sl:" opt; do
    case $opt in
        w)
            workspace=${OPTARG}
            ;;
        d)
            dev=true
            ;;
        p)
            port=${OPTARG}
            echo ">> PORT argument ($port) ignored <<"
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
        x)
            xmifile=${OPTARG}
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

# Product
if [[ "$dev" == false ]]; then
    dev_args="-name cs-studio"
else
    dev_args="-product org.csstudio.dls.product.dev.product -name cs-studio-dev"
    workspace_suffix="-dev"
fi

# Workspace
if [[ -n $workspace ]]; then
    data_args="-data $workspace"
else
    data_args="-data $HOME/cs-studio/workspaces/$(hostname -s)$workspace_suffix"
fi

# Perspective
if [[ -n $xmifile ]]; then
    xmi_args="-workbench_xmi $xmifile"
else
    xmi_args=""
fi

# OPI file and related options.
if [[ -n $macros ]] || [[ -n $links ]]; then
    if [[ -z $opifile ]]; then
        echo "Macros and links arguments require an opi file argument."
        usage
        exit 1
    fi
fi

# If no file specified, open a new databrowser window.
if [[ -z $opifile ]]; then
    opifile="$CSS_DIR/configuration/databrowser.nws"
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

# Create the local location shared between all workspaces.
personal_location=$HOME/cs-studio/$USER
mkdir -p $personal_location
local_links_args="-share_link $personal_location=/CSS/$USER"

# Echo subsequent commands for debugging.
set -x
exec $CSSTUDIO $local_links_args $dev_args $data_args $xmi_args --launcher.openFile "$opifile $macros_escaped $links_escaped"
