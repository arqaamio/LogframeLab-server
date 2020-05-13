INSERT INTO `IND_LEVEL_INDICATOR` (`ID`, `NAME`, `DESCRIPTION`, `TEMPLATE_VAR`, `COLOR`) VALUES
(1, 'OUTPUT', 'OUTPUT', '{output}', 'green'),
(2, 'OUTCOME', 'OUTCOME', '{outcomes}', 'red'),
(3, 'OTHER_OUTCOMES', 'OTHER OUTCOMES', '{otheroutcomes}', 'orange'),
(4, 'IMPACT', 'IMPACT', '{impact}', 'purple');


INSERT INTO `IND_INDICATOR` (`ID`, `NAME`, `DESCRIPTION`, `KEYWORDS`, `LEVEL`) VALUES
(1, 'Number of food insecure people receiving EU assistance', 'Food & Agriculture', 'food insecurity,nutrition,farming,agriculture', 2),
(2, 'Number of women of reproductive age, adolescent girls and children under 5 reached by nutrition related interventions supported by the EU', 'Food & Agriculture', 'IYCF,nutrition,Pregnant,lactating,SAM,IMAM,CMAM,malnurished,severe acute malnutrition,MAM,moderate acute malnutrition,dietary diversity,food intake,calories', 2),
(3, 'Number of smallholders reached with EU supported interventions aimed to increase their sustainable production, access to markets and/or security of land', 'Food & Agriculture', 'farmers,agriculture,smallholders,sustainable production,land rights,property,land tenure', 2),
(4, 'Agricultural and pastoral ecosystems where sustainable management practices have been introduced with EU support (ha)', 'Food & Agriculture', 'sustainable agriculture,sustainable agricultural practices,pastoral ecosystems', 2),
(5, 'Number of 1-year old fully immunised with EU support', 'Health', 'vaccinations,immunisation,measles', 2),
(6, 'Number of women of reproductive age using modern contraception methods with EU support', 'Health', 'contraception,contraceptive methods,reproductive health,reproductive rights', 2),
(7, 'Number of students enrolled in education with EU support (primary education)', 'Education', 'education,students,primary education,pupils', 2),
(8, 'Number of students enrolled in education with EU support (secondary education)', 'Education', 'education,students,secondary education,pupils', 2),
(9, 'Number  of individuals with access to improved drinking water source with EU support', 'WASH', 'borehole,water tanker,imrpoved water source,drinking water', 2),
(10, 'Number of individuals access to sanitation facility with EU support', 'WASH', 'sanitation,latrine,pit-latrine', 2);