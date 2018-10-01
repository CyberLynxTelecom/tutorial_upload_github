#!/bin/sh

set -e

PLUGINS="$1"


for P in "$PLUGINS"/org.eclipse.swt.gtk.*.jar ; do
    if echo "$P" | grep -q ".source_" ; then
	# skip source jars.
	continue
    fi
    zip -d "$P" "*.so" "*.xpt"
done

