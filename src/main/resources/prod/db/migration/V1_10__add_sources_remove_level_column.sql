ALTER TABLE IND_LEVEL_INDICATOR DROP COLUMN COLOR;
ALTER TABLE IND_LEVEL_INDICATOR DROP COLUMN TEMPLATE_VAR;

INSERT INTO `SOURCE` (`ID`, `NAME`) VALUES
(25, 'ILO'),
(26, 'IMF'),
(27, 'HIPSO'),
(28, 'Freedom House'),
(29, 'Sendai Framework'),
(30, 'PIN'),
(31, 'IFRC'),
(32, 'Miehlbradt'),
(33, 'INEE'),
(34, 'Helvetas'),
(35, 'RTI'),
(36, 'Sphere'),
(37, 'FANTA'),
(38, 'FSN'),
(39, 'ENN'),
(40, 'SCH'),
(41, 'CMAM Forum'),
(42, 'ACF'),
(43, 'CaLP'),
(44, 'DCED'),
(45, 'OECD'),
(46, 'NCBI'),
(47, 'UNESCO');