CREATE TABLE HouseholdMembers (
	HouseholdMemberID  			INT AUTO_INCREMENT PRIMARY KEY,
    HauseholdheadID				INT				NOT NULL,
    CONSTRAINT fk_HauseholdheadID_HouseholdMembers
			FOREIGN KEY (HauseholdheadID) 
				REFERENCES Hauseholdheads(HauseholdheadID),
	MenberType			VARCHAR(32)	 			NOT NULL,
    Name  				VARCHAR(128)	 		NOT NULL,
    LastName 			VARCHAR(128)	 		NOT NULL,
    Age					INT					 	NOT NULL,
    Work				ENUM("yes", "no")	 	NOT NULL,
    Vehicule			ENUM("yes", "no")	 	NOT NULL,
    WorkAutorizarion	ENUM("yes", "no")	 	NOT NULL,
    Salary				DECIMAL(5,2)	 		NOT NULL
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "HouseholdMembers",
  "apiUserName": "admin",
  
  "insert": {
    "insert_fields": [ "$ALL",  "-HouseholdMemberID" ],
    "returns": [ "HouseholdMemberID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL" ],
    "update_key": "HouseholdMemberID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "HouseholdMemberID" ]
  },
  
  "delete": {
    "delete_key":"HouseholdMemberID"
  }
}
*/
 

 