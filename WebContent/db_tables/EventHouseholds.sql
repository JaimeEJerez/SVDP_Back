CREATE TABLE EventHouseholds (
	EventHouseholdID 			INT AUTO_INCREMENT PRIMARY KEY,
	VoluntierID					INT,
	EventID 					INT					NOT NULL,
	
    CONSTRAINT fk_EventID_EventHouseholds
		FOREIGN KEY (EventID) 
			REFERENCES Events(EventID),
			
	HauseholdheadID 			INT					NOT NULL,
	CONSTRAINT fk_HauseholdheadID_EventHouseholds
		FOREIGN KEY (HauseholdheadID) 
			REFERENCES Hauseholdheads(HauseholdheadID),
			
	HelpValue				DECIMAL(5,2)	 	NOT NULL,
    DonationWay				VARCHAR(128)	 	NOT NULL, 			
    DonationValue 			DECIMAL(5,2)	 	NOT NULL
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "EventHouseholds",
  "apiUserName": "admin",

  "insert": {
    "insert_fields": [ "$ALL", "-EventHouseholdID" ],
    "returns": [ "EventHouseholdID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "EventHouseholdID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EventHouseholdID" ]
  },
  
  "delete": {
    "delete_key":"EventHouseholdID"
  }
}
*/