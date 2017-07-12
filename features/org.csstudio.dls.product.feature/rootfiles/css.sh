#!/bin/bash
# Wrapper script for CS-Studio.
# Create a directory for personal files and link to it from CSS/ project in CS-Studio.
# Ensure that a maximum of two instances (cs-studio and cs-studio-dev) are running at any time.

function usage() {
    echo "Usage: $0 [args]
General arguments:
    [-w <workspace>]
    [-c] clear cached EPICS configuration. This enables EPICS environment variables
         to be set in an existing workspace
             (see https://github.com/ControlSystemStudio/cs-studio/issues/2196)
    [-d] run the 'dev' instance of CS-Studio.  This allows running on a different
         port or on a different machine via SSH.
Arguments to run an opi file:
    [-o <opifile>] [Eclipse path; links required]
    [-p <plotfile>] databrowser plotfile
    [-n <nwsfile>] DLS-specific nws file
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

declare -A epics_env_vars
DIIRT_PREFS_PLUGIN=org.csstudio.diirt.util.core.preferences
epics_env_vars=( # Map of EPICS env variable to cs-studio preferences
    ["EPICS_CA_ADDR_LIST"]="diirt.ca.addr.list"
    ["EPICS_CA_AUTO_ADDR_LIST"]="diirt.ca.auto.addr.list"
    ["EPICS_CA_BEACON_PERIOD"]="diirt.ca.beacon.period"
    ["EPICS_CA_CONN_TMO"]="diirt.ca.connection.timeout"
    ["EPICS_CA_SERVER_PORT"]="diirt.ca.server.port"
    ["EPICS_CA_REPEATER_PORT"]="diirt.ca.repeater.port"
    ["EPICS_CA_MAX_ARRAY_BYTES"]="diirt.ca.max.array.size")

function set_epics_env() {
    # Extract EPICS environment variables and pass to CS-Studio as a set
    # of custom configuration values
    for key in "${!epics_env_vars[@]}"; do
        # eval to extract named variable
        eval env_value=\"\$"$key"\"
        [ ${env_value:+unset} ] &&
            echo "$DIIRT_PREFS_PLUGIN/${epics_env_vars[$key]}=$env_value" >> $1
    done
}

# This script is intended to be installed alongside the cs-studio binary.
CSS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CSSTUDIO=$CSS_DIR/cs-studio

# Default values.
clear_diirt_config=false
dev=false
opishell=false
port=5064

while getopts "w:do:p:n:x:m:sl:c" opt; do
    case $opt in
        w)
            workspace=${OPTARG}
            ;;
        d)
            dev=true
            ;;
        o)
            runfile=${OPTARG}
            is_opifile=true
            ;;
        p)
            runfile=${OPTARG}
            ;;
        n)
            runfile=${OPTARG}
            is_nwsfile=true
            ;;
        m)
            macros=${OPTARG}
            ;;
        s)
            opishell=true
            ;;
        c)
            clear_diirt_config=true
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
if [[ -z $workspace ]]; then
    workspace="$HOME/cs-studio/workspaces/$(hostname -s)$workspace_suffix"
fi
data_args="-data $workspace"

# EPICS/DIIRT config cleanup
if [[ $clear_diirt_config == true ]]; then
    echo "Deleting cached EPICS preferences"
    rm "$workspace/.metadata/.plugins/org.eclipse.core.runtime/.settings/$DIIRT_PREFS_PLUGIN.prefs"
fi

# Perspective
if [[ -n $xmifile ]]; then
    xmi_args="-workbench_xmi $xmifile"
else
    xmi_args=""
fi

# OPI file and related options.
if [[ -n $macros ]] || [[ -n $links ]]; then
    if [[ -z $runfile ]] || [[ -z $is_opifile ]]; then
        echo "Macros and links arguments require an opi file argument."
        usage
        exit 1
    fi
fi

# EPICS environment variables: if not defined in the calling environment the default args will be used
tmpfile=$(mktemp --tmpdir cs-studio.custom-config.XXXXXX)
trap 'echo "Cleanup custom CA config"; rm -f -- "$tmpfile"' INT TERM HUP EXIT
set_epics_env $tmpfile
plugin_preferences="-pluginCustomization $tmpfile"

# If no file specified, open a new databrowser window.
if [[ -z $runfile ]]; then
    runfile="$CSS_DIR/configuration/databrowser.nws"
    is_nwsfile=true
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

# Make it possible to detect whether this launch was a .nws file.
# This applies only for the initial launch, which is where we may
# need this information.
if [[ $is_nwsfile = true ]]; then
    vm_args="-vmargs -Dnws_file"
fi

# Echo subsequent commands for debugging.
set -x
$CSSTUDIO $plugin_preferences $local_links_args $dev_args $data_args $xmi_args --launcher.openFile "$runfile $macros_escaped $links_escaped" $vm_args
