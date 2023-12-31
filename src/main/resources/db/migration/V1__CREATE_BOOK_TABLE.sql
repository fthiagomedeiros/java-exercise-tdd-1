DROP EXTENSION IF EXISTS "uuid-ossp" CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS Book (
    ID               UUID DEFAULT uuid_generate_v4(),
    ISBN             VARCHAR(256),
    TITLE            VARCHAR(256),
    AUTHOR           VARCHAR(256),
    GENRE            VARCHAR(256)
);

ALTER TABLE Book ADD CONSTRAINT PK_Book PRIMARY KEY (ID);