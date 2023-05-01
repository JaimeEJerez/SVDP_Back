CREATE TABLE Hauseholdheads (
	HauseholdheadID			INT AUTO_INCREMENT PRIMARY KEY,
	Token					char(36),
	 
    ChapterID				INT,
	CONSTRAINT fk_chapter_hauseholdheads
		FOREIGN KEY (ChapterID) 
			REFERENCES Chapters(ChapterID),
							
	PhisicalAddressID		INT									NOT NULL 	DEFAULT 1,
	CONSTRAINT fk_phisicalAddress_hauseholdheads
		FOREIGN KEY (PhisicalAddressID) 
			REFERENCES PhisicalAddresses(PhisicalAddressID),	

    Email 		 			VARCHAR(128)	 		UNIQUE     	NOT NULL,
	PhoneNumber				VARCHAR(17)	 			         	NOT NULL,
    Password		 		VARCHAR(128)						NOT NULL,
    Name		 			VARCHAR(128)	 					NOT NULL,
    LastName		 		VARCHAR(128)	 					NOT NULL,
    Age						INT 								NOT NULL,

   	NewClient				ENUM( "yes", "no" ),
    
    MaritalStatus			ENUM( "married", "separated", "divorced", "widowed", "never_married" ),
    LiveAlone				ENUM( "yes", "no" ),
    ShareHomeWCouple		ENUM( "yes", "no" ),
    CoupleName				VARCHAR(128),
	CoupleAge				INT,
    OtherAdultsNames 		VARCHAR(1024),
    OtherAdultAges 			VARCHAR(1024),
    
    LiveWDependentSons		ENUM( "yes", "no" ),
    DependentSonsNumbr		INT,
    DependentSonsNames		VARCHAR(1024),
    DependentSonsAges		VARCHAR(1024),
    
    WifeWorks				ENUM( "yes", "no" ),
    WifeSalary				DECIMAL(8,2),
    HusbandWorks			ENUM( "yes", "no" ),
    HusbandSalary			DECIMAL(8,2),
	OtherPersonWorks1		ENUM( "yes", "no" ),
    OtherPersonSalary1		DECIMAL(8,2),
    OtherPersonWorks2		ENUM( "yes", "no" ),
    OtherPersonSalary2		DECIMAL(8,2),
    OtherPersonWorks3		ENUM( "yes", "no" ),
    OtherPersonSalary3		DECIMAL(8,2),
    OtherPersonWorks4		ENUM( "yes", "no" ),
    OtherPersonSalary4		DECIMAL(8,2),

    OtherAdltsHelpWSpends 	ENUM( "yes", "no" ),

    SpentRentMortgage 		DECIMAL(8,2),
	SpentFood				DECIMAL(8,2),
	SpentElectricity		DECIMAL(8,2),
	SpentWater				DECIMAL(8,2),
	SpentPhone				DECIMAL(8,2),
	SpentTransportation		DECIMAL(8,2),
	SpentInternet			DECIMAL(8,2),
	SpentInsurance			DECIMAL(8,2),
	SpentOtherExpenses		DECIMAL(8,2),
        
    HaveVehicle  			ENUM( "yes", "no" ),
    HaveWorkPermition		ENUM( "yes", "no" ),

    ProfessionSkills		VARCHAR(1024),
    
	Need_job				ENUM( "yes", "no" ),
	Need_food				ENUM( "yes", "no" ),
	Need_immigration		ENUM( "yes", "no" ),
	Need_medical			ENUM( "yes", "no" ),
	Need_legal				ENUM( "yes", "no" ),
	Need_others				ENUM( "yes", "no" ),
    Need_others_explain		VARCHAR(1024),
    
    History					VARCHAR(2048),

    Warning1 				ENUM( "yes", "no" ),
    Warning2 				ENUM( "yes", "no" ),

    Validated				char(36),

    AsignedVisitor			VARCHAR(64),
    
    VisitorComment			VARCHAR(2048),
    AdminComment			VARCHAR(2048),
    
    HelpApproveDate			DATE,
    HelpExpireDate			DATE,
    KEY (Email),
    KEY (PhoneNumber),
    KEY (AsignedVisitor)
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "Hauseholdheads",
  "apiUserName": "user",
  
  "hidenForeingTables": [ "Chapters", "HelpGroups" ],
  
  "insert": 
  {
  	"doNotUseToken":true,
  	"generete_uuid":"Token",
    "insert_fields": [ "$ALL", "-HauseholdheadID", "-Validated" ],
    "returns": [ "HauseholdheadID" ]
  },
  
  "update": {
    "update_fields": [ "$ALL", "-Validated", "-Token"  ],
    "update_key": "HauseholdheadID"
  },
  
  "select": {
    "select_fields": [ "$ALL" ],
    "select_keys": [ "Email"  ]
  },
    
  "delete": {
  	"doNotUseToken":true,
    "delete_key":"HauseholdheadID"
  }
}
*/