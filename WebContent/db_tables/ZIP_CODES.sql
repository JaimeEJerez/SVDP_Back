CREATE TABLE zip_codes 
(
  ZIP_CODE	int(11) DEFAULT NULL,
  STT		text,
  LAT		double DEFAULT NULL,
  LON		double DEFAULT NULL,
  CITY		text,
  STATE		text,
  ACTIVE	TINYINT(1)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;