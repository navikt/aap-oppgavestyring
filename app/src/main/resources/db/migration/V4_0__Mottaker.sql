CREATE TABLE mottaker
(
    personident TEXT,
    data        JSON,
    CONSTRAINT unique_mottaker_personident UNIQUE (personident)
);
