CREATE TABLE VisitorAssignments (
	VisitorAssignmentsID 		INT 				AUTO_INCREMENT 		PRIMARY KEY,
	
	VoluntierEmail				VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_VisitorAssignments_VoluntierEmail
			FOREIGN KEY (VoluntierEmail) 
				REFERENCES Voluntiers(Email),

	HHoldHeadEmail				VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_VolunteerAssignments_HHoldHeadEmail
			FOREIGN KEY (HHoldHeadEmail) 
				REFERENCES Hauseholdheads(Email),
	
	KEY (HHoldHeadEmail),
    KEY (VoluntierEmail),
	UNIQUE KEY uniq_id (HHoldHeadEmail,VoluntierEmail)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "VisitorAssignments",
  "apiUserName": "user",
  "neestDeep":0,
  
  "insert": 
  {
    "insert_fields": [ "$ALL",  "-VisitorAssignmentsID" ],
    "returns": [ "VisitorAssignmentsID" ]
  },
  
  "update": 
  {
    "update_fields": [ "$ALL", "-VisitorAssignmentsID" ],
    "update_key": "VoluntierEmail"
  },
  
  "select": 
  {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "VoluntierEmail" ]
  },
  
  "delete": 
  {
    "delete_key":"VoluntierEmail"
  }
}
*/