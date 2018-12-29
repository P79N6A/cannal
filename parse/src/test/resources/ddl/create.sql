 CREATE TABLE `test` (
  `id`  bigint(20)  zerofill unsigNed   NOT NULL AUTO_INCREMENT COMMENT 'id',
  `c_tinyint` tinyint(4) DEFAULT '1' COMMENT 'tinyint',
  `c_smallint` smallint(6) DEFAULT 0 COMMENT 'smallint',
  `c_mediumint` mediumint(9) DEFAULT NULL COMMENT 'mediumint',
  `c_int` int(11) DEFAULT NULL COMMENT 'int',
  `c_bigint` bigint(20) DEFAULT NULL COMMENT 'bigint',
  `c_decimal` decimal(10,3) DEFAULT NULL COMMENT 'decimal',
  `c_date` date DEFAULT '0000-00-00' COMMENT 'date',
  `c_datetime` datetime DEFAULT '0000-00-00 00:00:00' COMMENT 'datetime',
  `c_timestamp` timestamp NULL DEFAULT NULL COMMENT 'timestamp',
  `c_time` time DEFAULT NULL COMMENT 'time',
  `c_char` char(10) DEFAULT NULL COMMENT 'char',
  `c_varchar` varchar(10) DEFAULT 'hello' COMMENT 'varchar',
  `c_blob` blob COMMENT 'blob',
  `c_text` text COMMENT 'text',
  `c_mediumtext` mediumtext COMMENT 'mediumtext',
  `c_longblob` longblob COMMENT 'longblob',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_a` (`c_tinyint`),
  KEY `k_b` (`c_smallint`),
  KEY `k_c` (`c_mediumint`,`c_int`)
) ENGINE=InnoDB AUTO_INCREMENT=1769503 DEFAULT CHARSET=utf8mb4 COMMENT='10000000';