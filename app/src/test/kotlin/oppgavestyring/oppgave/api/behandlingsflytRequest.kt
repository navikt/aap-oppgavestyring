val behandlingsflytRequest = """
{
  "personident": "14098929550",
  "saksnummer": "24352363",
  "referanse": "yolo",
  "behandlingType": "Klage",
  "status": "PÅ_VENT",
  "avklaringsbehov": [
    {
      "definisjon": {
        "type": "5003",
        "behovType": "MANUELT_PÅKREVD",
        "løsesISteg": "BARNETILLEGG"
      },
      "status": "OPPRETTET",
      "endringer": [
        {
          "status": "OPPRETTET",
          "tidsstempel": "2024-05-15T16:27:31.996299178",
          "frist": "2025-05-15",
          "endretAv": "T123456"
        }
      ]
    }
  ],
  "opprettetTidspunkt": "2024-05-15T16:27:31.985882524"
}
"""