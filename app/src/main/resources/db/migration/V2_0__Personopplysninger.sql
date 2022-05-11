CREATE TABLE personopplysninger
(
    personident TEXT,
    data        JSON,
    CONSTRAINT personoppl_unique_personident UNIQUE (personident)
);
