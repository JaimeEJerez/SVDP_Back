CREATE TABLE EventAdresses (
	EventAdressID			INT AUTO_INCREMENT PRIMARY KEY,
    RegisterDate            TIMESTAMP     	NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    EventPlace				VARCHAR(64) 	NOT NULL,
    Zipcode					VARCHAR(7) 		NOT NULL,
    Street					VARCHAR(64),
    City					VARCHAR(64),
    State					VARCHAR(64),
    Country					VARCHAR(64)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "EventAdresses",
  "apiUserName": "user",
  
  "insert": {
    "insert_fields": [ "$ALL", "-EventAdressID", "-RegisterDate" ],
    "returns": [ "EventAdressID"  ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-RegisterDate" ],
    "update_key": "EventAdressID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EventAdressID", "EventPlace" ]
  },
  
  "delete": {
    "delete_key":"EventAdressID"
  }
}
*/
 

