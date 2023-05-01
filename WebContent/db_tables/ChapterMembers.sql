CREATE TABLE ChapterMembers (
	ChapterMemberID 		INT AUTO_INCREMENT PRIMARY KEY,
	Token					char(36),
	
    ChurcheID				INT,
	CONSTRAINT fk_churche_ChapterMembers
		FOREIGN KEY (ChurcheID) 
			REFERENCES Churches(ChurcheID),
			
	Email 					VARCHAR(128)			UNIQUE	NOT NULL,
	Password 				VARCHAR(32)						NOT NULL,
	AccessType 				ENUM( "Admin", "SuperAdmin" ) 	NOT NULL,
    Title 					VARCHAR(128)					NOT NULL,
    FirstName  				VARCHAR(128)					NOT NULL,
    LastName  				VARCHAR(128)					NOT NULL,
    Phone					VARCHAR(17)						NOT NULL
) ENGINE=INNODB; 

/*
API_DEF
 
{
  "apiEntryName": "ChapterMembers",
  "apiUserName": "admin",
  "neestDeep":0,

  "insert": 
  {
  	"generete_uuid":"Token",
    "insert_fields": [ "$ALL", "-ChapterMemberID" ],
    "returns": [ "ChapterMemberID" ]
  },
  
  "update": 
  {
    "update_fields": [ "$ALL", "-Token" ],
    "update_key": "ChapterMemberID"
  },
  
  "select": 
  {
    "select_fields": [ "$ALL" ],
    "select_keys": [
      "ChapterMemberID"
    ]
  },
  
  "delete": {
    "delete_key":"ChapterMemberID"
  }
}
*/