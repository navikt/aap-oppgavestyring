

CREATE TABLE OPPGAVE (
    ID BIGSERIAL PRIMARY KEY,
    SAKSNUMMER VARCHAR(50) NOT NULL,
    BEHANDLINGSREFERANSE VARCHAR(50) NOT NULL,
    BEHANDLINGSTYPE VARCHAR(50) NOT NULL,
    STATUS VARCHAR(50) NOT NULL,
    AVKLARINGBEHOVTYPE VARCHAR(50) NOT NULL,
    GJELDERVERDI VARCHAR(50),
    PERSONNUMMER VARCHAR(50) NOT NULL,
    AVKLARINGSBEHOV_OPPRETTET_TIDSPUNKT TIMESTAMP(3) NOT NULL,
    BEHANDLING_OPPRETTET_TIDSPUNKT TIMESTAMP(3) NOT NULL
);

CREATE TABLE UTFORER (
    ID BIGSERIAL PRIMARY KEY,
    OPPGAVE_ID BIGINT REFERENCES OPPGAVE(ID),
    IDENT VARCHAR(50) NOT NULL,
    TIDSSTEMPEL TIMESTAMP(3) NOT NULL
);

CREATE TABLE TILDELT (
    ID BIGSERIAL PRIMARY KEY,
    OPPGAVE_ID BIGINT REFERENCES OPPGAVE(ID) UNIQUE,
    IDENT VARCHAR(50) NOT NULL,
    TIDSSTEMPEL TIMESTAMP(3) NOT NULL
);

CREATE TABLE OPPGAVE_API_REF (
    OPPGAVE_ID BIGINT REFERENCES OPPGAVE(ID),
    OPPGAVE_API_ID BIGINT NOT NULL UNIQUE
)