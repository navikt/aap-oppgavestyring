-- give access to IAM users (GCP)
GRANT ALL ON ALL TABLES IN SCHEMA PUBLIC TO cloudsqliamuser;

CREATE TABLE soker
(
    personident TEXT,
    version     INT,
    data        JSON,
    CONSTRAINT unique_personident UNIQUE (personident)
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
    CONSTRAINT fk_personident_soker FOREIGN KEY (personident) REFERENCES soker (personident),
    CONSTRAINT personoppl_unique_personident UNIQUE (personident)
);

CREATE TABLE mottaker
(
    personident TEXT,
    data        JSON,
    CONSTRAINT unique_mottaker_personident UNIQUE (personident)
);
