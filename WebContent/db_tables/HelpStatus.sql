CREATE TABLE HelpStatus (
	HelpStatusID				INT AUTO_INCREMENT PRIMARY KEY,
	
    HauseholdheadID				INT					NOT NULL,
    CONSTRAINT fk_Hauseholdheads_HelpStatus
			FOREIGN KEY (HauseholdheadID) 
				REFERENCES Hauseholdheads(HauseholdheadID),
				
	Work						ENUM( "yes", "no" ),
    Food						ENUM( "yes", "no" ),
    Inmigration					ENUM( "yes", "no" ),
    Medical						ENUM( "yes", "no" ),
    Legal						ENUM( "yes", "no" ),
    Other						ENUM( "yes", "no" ),
    OtherDescription 			VARCHAR(128),
    Reason						VARCHAR(128),
    
	Approved_application		ENUM( "yes", "no" ),
	
	KEY (HauseholdheadID)
	
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "HelpStatus",
  "apiUserName": "admin",
  "neestDeep":0,
  
  "insert": {
    "insert_fields": [
      "$ALL",
      "-HelpStatusID"
    ],
    "returns": [
      "HelpStatusID"
    ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "HelpStatusID"
  },
  
  "select": {
    "select_fields": [
      "$ALL"
    ],
    "select_keys": [
      "HauseholdheadID"
    ]
  },
  
  "delete": {
    "delete_key":"HelpStatusID"
  }
}
*/
 

 