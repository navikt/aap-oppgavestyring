-- give access to IAM users (GCP)
GRANT ALL ON ALL TABLES IN SCHEMA PUBLIC TO cloudsqliamuser;

CREATE TABLE soker
(
    personident TEXT,
    data        JSON,
    CONSTRAINT unique_personident UNIQUE (personident)
);

CREATE TABLE sak
(
    personident              TEXT,
    saksid                   UUID UNIQUE,
    diskresjonskode          TEXT,
    skjermet                 BOOL,
    lokalkontor_enhetsnummer TEXT
);

CREATE TABLE oppgave
(
    saksid           UUID,
    oppgaveid        UUID UNIQUE,
    nay_eller_kontor TEXT,
    status           TEXT,
    CONSTRAINT fk_sak FOREIGN KEY (saksid) REFERENCES Sak (saksid)
);

CREATE TABLE rolle
(
    oppgaveid UUID,
    rolle     TEXT,
    CONSTRAINT fk_oppgave FOREIGN KEY (oppgaveid) REFERENCES Oppgave (oppgaveid),
    CONSTRAINT unique_rolle UNIQUE (oppgaveid, rolle)
);

CREATE TABLE tildeling
(
    saksid    UUID,
    ident     TEXT,
    rolle     TEXT,
    opprettet TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_tildeling UNIQUE (saksid, ident, rolle)
);

CREATE TABLE personopplysninger
(
    personident            TEXT,
    norg_enhet_id          TEXT,
    adressebeskyttelse     TEXT,
    geografisk_tilknytning TEXT,
    er_skjermet            BOOL,
    er_skjermet_fom        DATE NULL,
    er_skjermet_tom        DATE NULL,
    CONSTRAINT personoppl_unique_personident UNIQUE (personident)
);

CREATE TABLE mottaker
(
    personident TEXT,
    data        JSON,
    CONSTRAINT unique_mottaker_personident UNIQUE (personident)
);
