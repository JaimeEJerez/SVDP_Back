CREATE TABLE HelpGroups (
	HelpGroupID 		INT AUTO_INCREMENT 					PRIMARY KEY,
	HelpGroupName			VARCHAR(128)	 	NOT NULL 	UNIQUE 	KEY,
	HelpGroupDescription	VARCHAR(1024)	 	NOT NULL
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "HelpGroups",
  "apiUserName": "admin",
  
  "insert": {
    "insert_fields": [ "$ALL", "-HelpGroupID" ],
    "returns": [ "HelpGroupID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-HelpGroupID" ],
    "update_key": "HelpGroupID"
  },
  
  "select": {
    "select_fields": [ "$ALL"  ],
    "select_keys": [  "HelpGroupID", "HelpGroupName"  ]
  },
  
  "delete": {
    "delete_key":"HelpGroupID"
  }
}
*/