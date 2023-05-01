CREATE TABLE GuestsToEvent (
	GuestsToEventID 	INT 				AUTO_INCREMENT 		PRIMARY KEY,
	DateTime			TIMESTAMP 			NOT NULL DEFAULT CURRENT_TIMESTAMP,

	EventName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_GuestsToEvent_EventName
			FOREIGN KEY (EventName) 
				REFERENCES Events(EventName),
					
	ClientGroupName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_GuestsToEvent_ClientGroupName
			FOREIGN KEY (ClientGroupName) 
				REFERENCES ClientGroups(ClientGroupName),
	
	KEY (EventName),
    KEY (ClientGroupName)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "GuestsToEvent",
  "apiUserName": "admin",
  "neestDeep":0,
  
  "insert": {
    "insert_fields": [ "$ALL", "-GuestsToEventID", "-DateTime" ],
    "returns": [ "GuestsToEventID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-GuestsToEventID", "-DateTime" ],
    "update_key": "EventName"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "EventName" ]
  },
  
  "delete": {
    "delete_key":"GuestsToEventID"
  }
}
*/
 

 

