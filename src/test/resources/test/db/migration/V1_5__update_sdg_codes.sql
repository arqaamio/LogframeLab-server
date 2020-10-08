INSERT INTO `SDG_CODE` (`ID`, `NAME`) VALUES
(1, 'No Poverty'),
(2, 'Zero Hunger'),
(3, 'Good Health And Well-Being'),
(4, 'Quality Education'),
(5, 'Gender Equality'),
(6, 'Clean Water And Sanitation'),
(7, 'Affordable And Clean Energy'),
(8, 'Decente Work And Economic Growth'),
(9, 'Industry, Innovation And Infrastructure'),
(10, 'Reduced Inequalities'),
(11, 'Sustainable Cities And Communities'),
(12, 'Responsible Consumption And Production'),
(13, 'Climate Action'),
(14, 'Life Below Water'),
(15, 'Life On Land'),
(16, 'Peace, Justice and String Institutions'),
(17, 'Partnerships For The Goals')
ON DUPLICATE KEY UPDATE `NAME`=VALUES(`NAME`);