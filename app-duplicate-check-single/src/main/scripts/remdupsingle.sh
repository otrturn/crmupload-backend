#!/bin/bash

PREFIX=duplicate-check-single

echo "Suche Images mit Prefix: $PREFIX"

IMAGES=$(docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | grep "^$PREFIX" | awk '{print $2}')

if [ -z "$IMAGES" ]; then
	echo "Keine Images gefunden."
	exit 0
fi

echo "Folgende Images werden gelöscht:"
docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | grep "^$PREFIX"

for ID in $IMAGES; do
	echo "Lösche Image $ID"
	docker rmi "$ID"
done

echo "Fertig."
