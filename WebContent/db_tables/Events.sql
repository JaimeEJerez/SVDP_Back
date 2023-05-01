CREATE TABLE Events (
	EventID				INT 			AUTO_INCREMENT PRIMARY KEY,
	EventName			VARCHAR(128)				NOT NULL 	UNIQUE 	KEY,
	
	EventAdressID		INT							NOT NULL,
	CONSTRAINT fk_EventAdresses_Events
		FOREIGN KEY (EventAdressID) 
			REFERENCES EventAdresses(EventAdressID),	

	EvenDate				DATE					NOT NULL,
	StartTime				TIME					NOT NULL,
    EndTime					TIME					NOT NULL,
    AllNight 				ENUM( "yes", "no" ),
    
	SudgestedDonation 		DECIMAL(5,2),

	EventType		 		VARCHAR(128),
    NumberOfUnits			INT,
    UnitValue				DECIMAL(5,2),
    
    KEY (EvenDate)
    
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "Events",
  "apiUserName": "admin",
  "neestDeep":0,

  "insert": {
    "insert_fields": [ "$ALL", "-EventID" ],
    "returns": [ "EventID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "EventID"
  },
  
  "select": {
  	"comparator":">=",
  	"neestDeep":1,
  	"order":"ORDER BY EvenDate",
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EvenDate" ]
  },
  
  "delete": {
    "delete_key":"EventID"
  }
}
*/