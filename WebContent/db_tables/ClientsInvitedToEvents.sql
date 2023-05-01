CREATE TABLE ClientsInvitedToEvents (
	ClientsInvitedToEventID 	INT 				AUTO_INCREMENT 		PRIMARY KEY,
	DateTime			TIMESTAMP 			NOT NULL DEFAULT CURRENT_TIMESTAMP,

	EventName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientsInvitedToEvents_Events
			FOREIGN KEY (EventName) 
				REFERENCES Events(EventName),
					
	HauseholdheadEmail			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientsInvitedToEvents_Hauseholdheads
			FOREIGN KEY (HauseholdheadEmail) 
				REFERENCES Hauseholdheads(Email),
				
	Status		ENUM( "REGISTERED", "INVITED", "ACCEPTED", "REJECTED",  "REALIZED" )	NOT NULL 	DEFAULT	"REGISTERED",
	
	KEY (EventName),
    KEY (HauseholdheadEmail),
	UNIQUE KEY uniq_id (EventName,HauseholdheadEmail)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "ClientsInvitedToEvents",
  "apiUserName": "admin",
  "neestDeep":0,
  
  "insert": {
    "insert_fields": [ "$ALL", "-ClientsInvitedToEventID", "-Status", "-DateTime" ],
    "returns": [ "ClientsInvitedToEventID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-DateTime", "-HauseholdheadEmail" ],
    "update_key": "ClientsInvitedToEventID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EventName" ]
  },
  
  "delete": {
    "delete_key":"ClientsInvitedToEventID"
  }
}
*/
 

 

