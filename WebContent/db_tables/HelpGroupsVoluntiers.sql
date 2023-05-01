CREATE TABLE HelpGroupsVoluntier (
	HelpGroupsVoluntierID 		INT 				AUTO_INCREMENT 		PRIMARY KEY,
	
	HelpGroupName				VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_HelpGroupsVoluntier_HelpGroupName
			FOREIGN KEY (HelpGroupName) 
				REFERENCES HelpGroups(HelpGroupName),
					
	VoluntierEmail				VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_HelpGroupsVoluntier_VoluntierEmail
			FOREIGN KEY (VoluntierEmail) 
				REFERENCES Voluntiers(Email),
				
	KEY (HelpGroupName),
    KEY (VoluntierEmail),
    UNIQUE KEY uniq_id (HelpGroupName,VoluntierEmail)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "HelpGroupsVoluntier",
  "apiUserName": "admin",
  "neestDeep":0,
  
  "insert": {
    "insert_fields": [ "$ALL", "-HelpGroupsVoluntierID" ],
    "returns": [ "HelpGroupsVoluntierID" ]
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "HelpGroupName" ]
  },
  
  "delete": {
    "delete_key":"HelpGroupName"
  }
}
*/
 

 

