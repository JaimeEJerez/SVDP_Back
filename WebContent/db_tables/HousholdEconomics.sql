CREATE TABLE HousholdEconomics (
	HousholdEconomicID 	INT AUTO_INCREMENT PRIMARY KEY,
    HauseholdheadID		INT				NOT NULL,
    CONSTRAINT fk_HauseholdheadID_HousholdEconomics
			FOREIGN KEY (HauseholdheadID) 
				REFERENCES Hauseholdheads(HauseholdheadID),
	ReportDate 		TIMESTAMP     		NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    Income			DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    MortgageRent	DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Food			DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Electricity		DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Water			DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Phone			DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Cable_Internet	DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Transportation	DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    Insurance		DECIMAL(5,2)		NOT NULL	DEFAULT 0,
    OtherExpenses	DECIMAL(5,2)		NOT NULL	DEFAULT 0
) ENGINE=INNODB;

/*
API_DEF
 
{
  "apiEntryName": "HousholdEconomics",
  "apiUserName": "user",
  
  "insert": {
    "insert_fields": [
      "$ALL",
      "-HousholdEconomicID"
    ],
    "returns": [
      "HousholdEconomicID"
    ]
  },
  
  "update": {
    "update_fields": ["$ALL"],
    "update_key":"HousholdEconomicID"
  },
  
  "select": {
    "select_fields": [
      "$ALL"
    ],
    "select_keys": [
      "HousholdEconomicID"
    ]
  },
  
  "delete": {
    "delete_key":"HousholdEconomicID"
  }
}
*/