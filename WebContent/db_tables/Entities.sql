CREATE TABLE Entities (
	EntityID		INT AUTO_INCREMENT PRIMARY KEY,
	EntityType		VARCHAR(128)	 			NOT NULL,
	EntityGroup		VARCHAR(128)	 			NOT NULL,
	EntityName 		VARCHAR(128) 		UNIQUE 	NOT NULL
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "Entities",
  "apiUserName": "admin",
  
  "insert": {
    "insert_fields": [ "$ALL", "-EntityID" ],
    "returns": [ "EntityID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "EntityID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EntityID" ]
  },
  
  "delete": {
    "delete_key":"EntityID"
  }
}
*/