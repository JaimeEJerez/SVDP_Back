CREATE TABLE AUTENTICS (
    AUTENTICID         bigint(20)           unsigned    NOT NULL    auto_increment,
    DATE_TIME          TIMESTAMP                        NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    UUID               char(36)                         NOT NULL    UNIQUE,
    VALID_COD          int(6)               unsigned    NOT NULL,
    HauseholdheadID    bigint(20)           unsigned    NOT NULL,
    EMAIL              varchar(36),
    PASSWORD           varchar(36),
    WEB_AGENT          varchar(1024),
    IPADDRESS          varchar(32),
    PRIMARY KEY (AUTENTICID),
    KEY (UUID)
) ENGINE=InnoDB CHARACTER SET utf8;

