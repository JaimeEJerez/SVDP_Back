CREATE TABLE Chapters (
	ChapterID					INT 			AUTO_INCREMENT PRIMARY KEY,

	ChurcheID					INT,
	CONSTRAINT fk_Churches_Chapters
		FOREIGN KEY (ChurcheID) 
			REFERENCES Churches(ChurcheID),
	
	ChapterName 				VARCHAR(128)	 	NOT NULL	UNIQUE,
	PhoneNumber 				VARCHAR(17)	 		NOT NULL,
	Street 						VARCHAR(128)	 	NOT NULL,
	City 						VARCHAR(128)	 	NOT NULL,
	State 						VARCHAR(128)	 	NOT NULL,
	County						VARCHAR(128)	 	NOT NULL, 
	WebSite						VARCHAR(128),

	BoardMenbersNumber 			INT,

	Status501_3_3				VARCHAR(128),
	EIN_Number 					VARCHAR(128),
	Zip_Code					VARCHAR(1024),
	EMail						VARCHAR(1024),

	PresidentFName				VARCHAR(128), 
	MiddleInitial				char(1), 
	PresidentLName				VARCHAR(128), 
	
	Help_paying_rent			ENUM( "yes", "no" ),
	Help_paying_electricity		ENUM( "yes", "no" ),
	Help_paying_for_food		ENUM( "yes", "no" ),
	Help_paying_medical_care	ENUM( "yes", "no" ),
	Help_paying_legal_care		ENUM( "yes", "no" ),
	Help_finding_job			ENUM( "yes", "no" ),
    Other_helps					VARCHAR(1024),
	LAT							double 				DEFAULT NULL,
  	LON							double 				DEFAULT NULL,
  	Active						TINYINT(1) 			DEFAULT 0
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "Chapters",
  "apiUserName": "admin",
  "neestDeep":1,
  
   "hidenForeingTables": [ "Entities" ],
   
  "insert": {
    "insert_fields": [ "$ALL", "-ChapterID" ],
    "returns": [ "ChapterID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "ChapterID"
  },
  
  "select": { "select_fields": [ "$ALL" ],
    "select_keys": [ "ChapterID" ]
  },
  
  "delete": {
    "delete_key":"ChapterName"
  }
}
*/