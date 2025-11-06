CREATE TABLE ClientsNOTInvitedToEvents (
	ClientsNOTInvitedToEventsID INT 		AUTO_INCREMENT 		PRIMARY KEY,
	DateTime					TIMESTAMP 						NOT NULL DEFAULT CURRENT_TIMESTAMP,
	
	ChapterID					INT		NOT NULL DEFAULT 3,
		CONSTRAINT fk_ClientsNOTInvitedToEvents_ChapterID
			FOREIGN KEY (ChapterID) 
				REFERENCES Chapters(ChapterID),
				
	EventName			VARCHAR(128)		NOT NULL,
					
	HauseholdheadEmail			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientsNOTInvitedToEvents_HauseholdheadEmail
			FOREIGN KEY (HauseholdheadEmail) 
				REFERENCES Hauseholdheads(Email),
					
	KEY (EventName),
    KEY (HauseholdheadEmail)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "ClientsNOTInvitedToEvents",
  "apiUserName": "admin",
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "ChapterID", "EventName" ]
  },
  
  "delete": {
    "delete_key":"ClientsNOTInvitedToEventsID"
  },
  
  "defaults": 
    {"ChapterID":"3"}
}
*/