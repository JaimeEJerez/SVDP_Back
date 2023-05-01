CREATE TABLE ClientGroups (
	ClientGroupID 		INT AUTO_INCREMENT 					PRIMARY KEY,
	ClientGroupName			VARCHAR(128)	 	NOT NULL 	UNIQUE 	KEY,
	ClientGroupDescription	VARCHAR(1024)	 	NOT NULL
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "ClientGroups",
  "apiUserName": "admin",
  
  "insert": {
    "insert_fields": [ "$ALL", "-ClientGroupID" ],
    "returns": [ "ClientGroupID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-ClientGroupID" ],
    "update_key": "ClientGroupID"
  },
  
  "select": {
    "select_fields": [ "$ALL"  ],
    "select_keys": [  "ClientGroupName", "ClientGroupID"  ]
  },
  
  "delete": {
    "delete_key":"ClientGroupID"
  }
}
*/