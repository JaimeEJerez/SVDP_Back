CREATE TABLE ClientsGroupsInvitedToEvents (
	ClientsGroupsInvitedToEventID 	INT 	AUTO_INCREMENT 		PRIMARY KEY,
	DateTime			TIMESTAMP 			NOT NULL DEFAULT CURRENT_TIMESTAMP,

	EventName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientsGroupsInvitedToEvents_Events
			FOREIGN KEY (EventName) 
				REFERENCES Events(EventName),
					
	ClientGroupName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientsGroupsInvitedToEvents_ClientGroups
			FOREIGN KEY (ClientGroupName) 
				REFERENCES ClientGroups(ClientGroupName),
					
	KEY (EventName),
    KEY (ClientGroupName),
	UNIQUE KEY uniq_id (EventName,ClientGroupName)
) ENGINE=INNODB;
