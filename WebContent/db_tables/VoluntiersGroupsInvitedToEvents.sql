CREATE TABLE VoluntiersGroupsInvitedToEvents (
	VoluntiersGroupsInvitedToEventID 	INT 				AUTO_INCREMENT 		PRIMARY KEY,
	DateTime			TIMESTAMP 			NOT NULL DEFAULT CURRENT_TIMESTAMP,

	EventName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_VoluntiersGroupsInvitedToEvents_Events
			FOREIGN KEY (EventName) 
				REFERENCES Events(EventName),
					
	HelpGroupName			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_VoluntiersGroupsInvitedToEvents_HelpGroups
			FOREIGN KEY (HelpGroupName) 
				REFERENCES HelpGroups(HelpGroupName),
					
	KEY (EventName),
    KEY (HelpGroupName),
	UNIQUE KEY uniq_id (EventName,HelpGroupName)
) ENGINE=INNODB;
