LIMIT=70  # This roughly seems to require around 2GB memory

# Check converter root is set
if [ -z "$CONVERTER_ROOT" ]; then
    echo Please set CONVERTER_ROOT
    exit 1
fi

count=0
start_points=$(find $CONVERTER_ROOT -name runcss.sh)
for start_point in $start_points; do
    opis=$(find $(dirname $start_point) -maxdepth 1 -name '*.opi' -printf '%P\n')
    for opi in $opis; do
        # Check if we have reached the limit
        if [ $count -eq $LIMIT ]; then
            break 2
        fi
        count=$(( $count + 1 ))
        # Open the OPI with the launch script
        echo Launching $start_point $opi
        $start_point $opi &
    done
done
