CREATE TABLE IF NOT EXISTS `TEMP_INDICATOR`
(
    `ID`                  INT NOT NULL AUTO_INCREMENT,
    `CRS_CODE`            VARCHAR(255),
    `DATA_SOURCE`         VARCHAR(255),
    `DESCRIPTION`         VARCHAR(400),
    `DISAGGREGATION`      BOOLEAN,
    `KEYWORDS`            VARCHAR(350),
    `NAME`                VARCHAR(350),
    `SDG_CODE`            VARCHAR(255),
    `SOURCE`              VARCHAR(255),
    `SOURCE_VERIFICATION` VARCHAR(255),
    `THEMES`              VARCHAR(255),
    `LEVEL`               INT,
    PRIMARY KEY (`ID`),
    KEY `LEVEL` (`LEVEL`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;
