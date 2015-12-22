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
CS-Studio running, then the workspace and port arguments are ignored.
    "
}

# This script is intended to be installed alongside the cs-studio binary.
CSS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CSSTUDIO=$CSS_DIR/cs-studio

# Default values.
opishell=false
workspace=~/.cs-studio

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
            echo "problem with the args"
            usage
            exit 1
            ;;
    esac
done


# Workspace
data_args="-data $workspace"

# Port
if [[ -n $port ]]; then
    if [[ $port = "5064" ]] || [[ $port = "6064" ]]; then
        pcf="org.csstudio.diirt.util.preferences/diirt.home=platform:/config/diirt_$port"
    else
        echo "Only ports 5064 and 6064 are supported by this script."
        usage
        exit 1
    fi
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
    launch_opi_escaped=$(echo $launch_opi | perl -ne "s|\.(?!opi)|[\\\46]|g; print;")
fi


if [[ -n $port ]]; then
    echo exec $CSSTUDIO $data_args -pluginCustomization <( echo $pcf ) "$launch_opi_arg" "$launch_opi_escaped"
    exec $CSSTUDIO $data_args -pluginCustomization <( echo $pcf ) "$launch_opi_arg" "$launch_opi_escaped"
else
    echo exec $CSSTUDIO $data_args "$launch_opi_arg" "$launch_opi_escaped"
    exec $CSSTUDIO $data_args "$launch_opi_arg" "$launch_opi_escaped"
fi

