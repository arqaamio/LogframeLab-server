CREATE TABLE IF NOT EXISTS `SOURCE` (
	`ID` INT NOT NULL AUTO_INCREMENT,
	`NAME` VARCHAR(255) NOT NULL UNIQUE,
	PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `SOURCE` (`ID`, `NAME`) VALUES
(1, 'UN Sustainable Development Goals'),
(2, 'World Bank'),
(3, 'European Union'),
(4, 'Capacity4Dev'),
(5, 'UN OHCHR'),
(6, 'WHO'),
(7, 'UNICEF'),
(8, 'WFP'),
(9, 'FAO'),
(10, 'UNHCR'),
(11, 'IOM'),
(12, 'Global Clusters'),
(13, 'ECHO'),
(14, 'IASC Indicator Registry'),
(99, 'Other');

CREATE TABLE IF NOT EXISTS `INDICATOR_SOURCE` (
	`INDICATOR_ID` INT NOT NULL,
	`SOURCE_ID` INT NOT NULL,
	PRIMARY KEY (`INDICATOR_ID`, `SOURCE_ID`),
    CONSTRAINT `FK_INDICATOR_SOURCE_INDICATOR` FOREIGN KEY (`INDICATOR_ID`) REFERENCES `IND_INDICATOR` (`ID`),
    CONSTRAINT `FK_INDICATOR_SOURCE_SOURCE` FOREIGN KEY (`SOURCE_ID`) REFERENCES `SOURCE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `INDICATOR_SOURCE` (`INDICATOR_ID`, `SOURCE_ID`)
SELECT
    ind1.ID AS INDICATOR_ID,
    sou.ID AS SOURCE_ID
FROM
    IND_INDICATOR ind1
JOIN SOURCE sou
WHERE
    ind1.SOURCE<>'' AND
    ind1.SOURCE LIKE sou.NAME;

INSERT INTO `INDICATOR_SOURCE` (`INDICATOR_ID`, `SOURCE_ID`) VALUES
(41,14),
(41,6),
(1422,13),
(1422,8),
(1425,13),
(1425,6),
(1439,9),
(1439,6),
(1448,9),
(1448,6),
(1452,9),
(1452,6),
(1455,9),
(1455,6),
(1458,9),
(1458,6),
(1461,9),
(1461,6);

-- --------------------------------------------------------

--
-- Table structure for table `SDG_CODE`
--

CREATE TABLE IF NOT EXISTS `SDG_CODE` (
	`ID` INT NOT NULL AUTO_INCREMENT,
	`NAME` VARCHAR(255) NOT NULL UNIQUE,
	PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `SDG_CODE` (`ID`, `NAME`) VALUES
(1, 'End poverty in all its forms everywhere'),
(2, 'End hunger, achieve food security and improved nutrition and promote sustainable agriculture'),
(3, 'Ensure healthy lives and promote well-being for all at all ages'),
(4, 'Ensure inclusive and equitable quality education and promote lifelong learning opportunities for all'),
(5, 'Achieve gender equality and empower all women and girls'),
(6, 'Ensure availability and sustainable management of water and sanitation for all'),
(7, 'Ensure access to affordable, reliable, sustainable and modern energy for all'),
(8, 'Promote sustained, inclusive and sustainable economic growth, full and productive employment and decent work for all'),
(9, 'Build resilient infrastructure, promote inclusive and sustainable industrialization and foster innovation'),
(10, 'Reduce inequality within and among countries'),
(11, 'Make cities and human settlements inclusive, safe, resilient and sustainable'),
(12, 'Ensure sustainable consumption and production patterns'),
(13, 'Take urgent action to combat climate change and its impacts'),
(14, 'Conserve and sustainably use the oceans, seas and marine resources for sustainable development'),
(15, 'Protect, restore and promote sustainable use of terrestrial ecosystems, sustainably manage forests, combat desertification, and halt and reverse land degradation and halt biodiversity loss'),
(16, 'Promote peaceful and inclusive societies for sustainable development, provide access to justice for all and build effective, accountable and inclusive institutions at all levels'),
(17, 'Strengthen the means of implementation and revitalize the global partnership for sustainable development');

CREATE TABLE IF NOT EXISTS `INDICATOR_SDG_CODE` (
	`INDICATOR_ID` INT NOT NULL,
	`SDG_CODE_ID` INT NOT NULL,
	PRIMARY KEY (`INDICATOR_ID`, `SDG_CODE_ID`),
    CONSTRAINT `FK_INDICATOR_SDG_CODE_INDICATOR` FOREIGN KEY (`INDICATOR_ID`) REFERENCES `IND_INDICATOR` (`ID`),
    CONSTRAINT `FK_INDICATOR_SDG_CODE_SDG_CODE` FOREIGN KEY (`SDG_CODE_ID`) REFERENCES `SDG_CODE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `INDICATOR_SDG_CODE` (`INDICATOR_ID`, `SDG_CODE_ID`)
SELECT
    ind1.ID AS INDICATOR_ID,
    sdg.ID AS SDG_CODE_ID
FROM
    IND_INDICATOR ind1
JOIN SDG_CODE sdg
WHERE
    ind1.SDG_CODE <>'' AND ind1.SDG_CODE NOT LIKE '%,%' AND
    sdg.ID LIKE SUBSTRING(ind1.SDG_CODE, 1, LOCATE('.', ind1.SDG_CODE)-1);

-- --------------------------------------------------------

--
-- Table structure for table `CRS_CODE`
--

CREATE TABLE IF NOT EXISTS `CRS_CODE` (
	`ID` INT NOT NULL AUTO_INCREMENT,
	`NAME` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `CRS_CODE` (`ID`, `NAME`) VALUES
(110, 'Education'),
(111, 'Education, Level Unspecified'),
(112, 'Basic Education'),
(113, 'Secondary Education'),
(114, 'Post-Secondary Education'),
(120, 'Health'),
(121, 'Health, General'),
(122, 'Basic Health'),
(123, 'Non-communicable diseases (NCDs)'),
(130, 'Population Policies/Programmes & Reproductive Health'),
(140, 'Water Supply & Sanitation'),
(150, 'Government & Civil Society'),
(151, 'Government & Civil Society-general'),
(152, 'Conflict, Peace & Security'),
(160, 'Other Social Infrastructure & Services'),
(210, 'Transport & Storage'),
(220, 'Communications'),
(230, 'Energy'),
(231, 'Energy Policy'),
(232, 'Energy generation, renewable sources'),
(233, 'Energy generation, non-renewable sources'),
(234, 'Hybrid energy plants'),
(235, 'Nuclear energy plants'),
(236, 'Energy distribution'),
(240, 'Banking & Financial Services'),
(250, 'Business & Other Services'),
(310, 'Agriculture, Forestry, Fishing'),
(311, 'Agriculture'),
(312, 'Forestry'),
(313, 'Fishing'),
(320, 'Industry, Mining, Construction'),
(321, 'Industry'),
(322, 'Mineral Resources & Mining'),
(323, 'Construction'),
(330, 'Trade Policies & Regulations'),
(331, 'Trade Policies & Regulations'),
(332, 'Tourism'),
(410, 'General Environment Protection'),
(430, 'Other Multisector'),
(510, 'General Budget Support'),
(520, 'Development Food Assistance'),
(530, 'Other Commodity Assistance'),
(600, 'Action Relating to Debt'),
(720, 'Emergency Response'),
(730, 'Reconstruction Relief & Rehabilitation'),
(740, 'Disaster Prevention & Preparedness'),
(910, 'Administrative Costs of Donors'),
(930, 'Refugees in Donor Countries'),
(998, 'Unallocated / Unspecified');

CREATE TABLE IF NOT EXISTS `INDICATOR_CRS_CODE` (
	`INDICATOR_ID` INT NOT NULL,
	`CRS_CODE_ID` INT NOT NULL,
	PRIMARY KEY (`INDICATOR_ID`, `CRS_CODE_ID`),
    CONSTRAINT `FK_INDICATOR_CRS_CODE_INDICATOR` FOREIGN KEY (`INDICATOR_ID`) REFERENCES `IND_INDICATOR` (`ID`),
    CONSTRAINT `FK_INDICATOR_CRS_CODE_CRS_CODE` FOREIGN KEY (`CRS_CODE_ID`) REFERENCES `CRS_CODE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `INDICATOR_CRS_CODE` (`INDICATOR_ID`, `CRS_CODE_ID`)
SELECT
    ind1.ID AS INDICATOR_ID,
    crs.ID AS CRS_CODE_ID
FROM
    IND_INDICATOR ind1
JOIN CRS_CODE crs
WHERE
    ind1.CRS_CODE<>'' AND ind1.CRS_CODE NOT LIKE '%,%' AND
    crs.ID LIKE SUBSTRING(ind1.CRS_CODE, 1, 3);

INSERT INTO `INDICATOR_CRS_CODE` (`INDICATOR_ID`, `CRS_CODE_ID`) VALUES
(1516,122),
(1516,430),
(1557,112),
(1557,113),
(1558,112),
(1558,113),
(1561,112),
(1561,113),
(1563,112),
(1563,113),
(1566,112),
(1566,113),
(1567,112),
(1567,113),
(1568,112),
(1568,113);

ALTER TABLE `IND_INDICATOR`
    DROP COLUMN `SOURCE`,
    DROP COLUMN `SDG_CODE`,
    DROP COLUMN `CRS_CODE`;
