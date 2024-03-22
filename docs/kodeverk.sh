#!/usr/local/bin/bash

#
# Usage: ./kodeverk.sh <kodeverk>
#
# Example ./kodeverk.sh Tema
# Example ./kodeverk.sh Oppgavetyper
#

curl -X GET \
    https://kodeverk.dev.intern.nav.no/api/v1/kodeverk/$1/koder \
    -H 'Nav-Call-Id: aap' \
    -H 'Nav-Consumer-Id: aap'
