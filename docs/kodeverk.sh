#!/usr/local/bin/bash

# USAGE:
#   GET ONE
#
#     ./kodeverk.sh <kodeverk>
#
#   LIST ALL
#
#     ./kodeverk.sh
#
# Example ./kodeverk.sh Tema
# Example ./kodeverk.sh Oppgavetyper
# Example ./kodeverk.sh Behandlingstema
# Example ./kodeverk.sh Behandlingstyper

lookup() {
  curl -X GET \
      https://kodeverk.dev.intern.nav.no/api/v1/kodeverk/$1/koder \
      -H 'Nav-Call-Id: aap' \
      -H 'Nav-Consumer-Id: aap'
}


all() {
  curl -X 'GET' \
    'https://kodeverk.dev.intern.nav.no/api/v1/kodeverk' \
    -H 'accept: application/json' \
    -H 'Nav-Consumer-Id: aap' \
    -H 'Nav-Call-Id: aap'
}

if [ -z "$1" ]; then
  all
else
  lookup $1
fi
