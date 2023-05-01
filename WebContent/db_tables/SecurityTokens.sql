CREATE TABLE SecurityTokens (
    SecurityTokenID    bigint(20)           unsigned    NOT NULL    auto_increment,
    DATE_TIME          TIMESTAMP                        NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    TOKEN              char(36)                         NOT NULL    UNIQUE,
    EMAIL              varchar(36),
    WEB_AGENT          varchar(1024),
    IPADDRESS          varchar(32),
    PRIMARY KEY (SecurityTokenID),
    KEY (TOKEN),
    KEY (EMAIL)
) ENGINE=InnoDB CHARACTER SET utf8;

/*
API_DEF
 
{
  "apiEntryName": "SecurityTokens",
  "apiUserName": "user",
  
  "options": {
  	"doNotUseToken":true,
  	"options_fields": [ "TOKEN" ]
  }
}
*/