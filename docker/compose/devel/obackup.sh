#!/usr/bin/env sh

/orientdb/bin/console.sh "connect remote:/smevente root rododendron
; export database /orientdb/backup/smevente-$(date +\%F_\%R);" &>/tmp/smevente-$(date +\%F_\%R).log
