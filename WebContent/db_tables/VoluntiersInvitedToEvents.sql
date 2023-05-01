CREATE TABLE VoluntiersInvitedToEvents (
	VoluntiersInvitedToEventID 	INT 				AUTO_INCREMENT 		PRIMARY KEY,
	DateTime			TIMESTAMP 			NOT NULL DEFAULT CURRENT_TIMESTAMP,

	EventName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_VoluntiersInvitedToEvents_Events
			FOREIGN KEY (EventName) 
				REFERENCES Events(EventName),
					
	VoluntierEmail			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_VoluntiersInvitedToEvents_ClientGroups
			FOREIGN KEY (VoluntierEmail) 
				REFERENCES Voluntiers(Email),
				
	Status		ENUM( "REGISTERED", "INVITED", "ACCEPTED", "REJECTED",  "REALIZED" )	NOT NULL 	DEFAULT	"REGISTERED",
	
	KEY (EventName),
    KEY (VoluntierEmail),
	UNIQUE KEY uniq_id (EventName,VoluntierEmail)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "VoluntiersInvitedToEvents",
  "apiUserName": "admin",
  "neestDeep":0,
  
  "insert": {
    "insert_fields": [ "$ALL", "-VoluntiersInvitedToEventID", "-DateTime", "-Status" ],
    "returns": [ "VoluntiersInvitedToEventID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-VoluntiersInvitedToEventID", "-DateTime" ],
    "update_key": "VoluntiersInvitedToEventID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EventName" ]
  },
  
  "delete": {
    "delete_key":"VoluntiersInvitedToEventID"
  }
}
*/
 

 

