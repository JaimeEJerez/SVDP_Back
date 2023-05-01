CREATE TABLE Churches 
(
	ChurcheID		INT AUTO_INCREMENT PRIMARY KEY,
	Name 			VARCHAR(128)	 	NOT NULL,
	Street 			VARCHAR(128)	 	NOT NULL,
	City 			VARCHAR(128)	 	NOT NULL,
	State 			VARCHAR(128)	 	NOT NULL,
	ZipCode			VARCHAR(7)	 		NOT NULL, 
	County	 		VARCHAR(128)	 	NOT NULL,
	PhoneNumber 	VARCHAR(15)	 		NOT NULL,
	WebSite 		VARCHAR(128),
	LAT				double 				DEFAULT NULL,
  	LON				double 				DEFAULT NULL,
  	Active			TINYINT(1)			DEFAULT 0
) ENGINE=INNODB;


/*
API_DEF
 
{
  "apiEntryName": "Churches",
  "apiUserName": "admin",
  
  "hidenForeingTables": [ "Chapters" ],
  
  "insert": {
    "insert_fields": [ "$ALL", "-ChurcheID" ],
    "returns": [ "ChurcheID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "ChurcheID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "ChurcheID" ]
  },
  
  "delete": {
    "delete_key":"ChurcheID"
  }
}
*/