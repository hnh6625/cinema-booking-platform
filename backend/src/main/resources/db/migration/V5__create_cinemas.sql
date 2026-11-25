CREATE TABLE cinemas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    address VARCHAR(500) NOT NULL ,
    version BIGINT NOT NULL ,
    CONSTRAINT ck_cinemas_name_not_blank CHECK ( length(trim(name)) > 0 ),
    CONSTRAINT ck_cinemas_address_not_blank CHECK ( length(trim(address)) > 0 )
);