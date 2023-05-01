CREATE TABLE PhisicalAddresses (
	PhisicalAddressID		INT AUTO_INCREMENT PRIMARY KEY,
    RegisterDate            TIMESTAMP     	NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    Zipcode					VARCHAR(7) 		NOT NULL,
    Street					VARCHAR(64),
    Apto_Hab				VARCHAR(64),
    City					VARCHAR(64),
    State					VARCHAR(64),
    Country					VARCHAR(64)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "PhisicalAddresses",
  "apiUserName": "user",
  
  "insert": {
    "insert_fields": [ "$ALL", "-PhisicalAddressID", "-RegisterDate" ],
    "returns": [ "PhisicalAddressID"  ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-RegisterDate" ],
    "update_key": "PhisicalAddressID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "PhisicalAddressID" ]
  },
  
  "delete": {
    "delete_key":"PhisicalAddressID"
  }
}
*/
 

