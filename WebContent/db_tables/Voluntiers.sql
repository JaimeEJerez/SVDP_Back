CREATE TABLE Voluntiers (
	VoluntierID 		INT AUTO_INCREMENT PRIMARY KEY,
	Token				char(36),
	
	ChapterID			INT,
		CONSTRAINT fk_Chapters_Voluntiers
			FOREIGN KEY (ChapterID) 
				REFERENCES Chapters(ChapterID),
				
	FirstName 			VARCHAR(64) 			NOT NULL,
	LastName 			VARCHAR(64) 			NOT NULL,
	PhoneNumber 		VARCHAR(17) 			NOT NULL,
	Email 		 		VARCHAR(128)	UNIQUE  NOT NULL,
	Password 			VARCHAR(32) 			NOT NULL,
	
	PhisicalAddressID		INT					NOT NULL 	DEFAULT 1,
		CONSTRAINT fk_PhisicalAddresses_Voluntiers
			FOREIGN KEY (PhisicalAddressID) 
				REFERENCES PhisicalAddresses(PhisicalAddressID),	
		
	Talents				VARCHAR(1024),
	ComunityHours		ENUM( "yes", "no" ),
	VerifCode 			CHAR(6),
	Visitor				ENUM( "yes", "no" ),
	KEY (Email),
    KEY (PhoneNumber)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "Voluntiers",
  "apiUserName": "user",
  
  "hidenForeingTables": [ "Chapters" ],
   
  "insert": 
  {
  	"doNotUseToken":true,
  	"generete_uuid":"Token",
    "insert_fields": [ "$ALL",  "-VoluntierID", "-VerifCode" ],
    "returns": [ "VoluntierID" ]
  },
  
  "update": 
  {
    "update_fields": [ "$ALL", "-Token" ],
    "update_key": "VoluntierID"
  },
  
  "select": 
  {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "Email" ]
  },
  
  "options": 
  {
  	"doNotUseToken":true,
  	"options_fields": [ "VoluntierID", "VerifCode" ]
  },
  
  "delete": 
  {
    "delete_key":"VoluntierID"
  }
}
*/