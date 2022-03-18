CREATE TABLE soker
(
    personident TEXT,
    data        JSON
);

CREATE TABLE sak
(
    personident              TEXT,
    saksid                   UUID UNIQUE,
    diskresjonskode          TEXT,
    egen_ansatt              BOOL,
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
