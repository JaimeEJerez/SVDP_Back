CREATE TABLE ClientGroupHHHeads (
	ClientGroupHHHeadsID 	INT 	AUTO_INCREMENT 		PRIMARY KEY,
	
	ClientGroupName				VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientGroupHHHeads_ClientGroupName
			FOREIGN KEY (ClientGroupName) 
				REFERENCES ClientGroups(ClientGroupName),
					
	HauseholdheadEmail			VARCHAR(128)		NOT NULL,
		CONSTRAINT fk_ClientGroupHHHeads_HauseholdheadEmail
			FOREIGN KEY (HauseholdheadEmail) 
				REFERENCES Hauseholdheads(Email),
				
	KEY (ClientGroupName),
    KEY (HauseholdheadEmail),
    UNIQUE KEY uniq_id (ClientGroupName,HauseholdheadEmail)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "ClientGroupHHHeads",
  "apiUserName": "admin",
  "neestDeep":0,
  
  "insert": {
    "insert_fields": [ "$ALL", "-ClientGroupHHHeadsID" ],
    "returns": [ "ClientGroupHouseHoldHeadsID" ]
  },
   
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "ClientGroupName" ]
  },
  
  "delete": {
    "delete_key":"ClientGroupName"
  }
}
*/
 
