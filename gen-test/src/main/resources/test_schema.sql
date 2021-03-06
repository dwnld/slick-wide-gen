CREATE TABLE thin(
       col_1 INT auto_increment PRIMARY KEY,
       col_2 VARCHAR(255),
       col_3 TIMESTAMP);

CREATE TABLE wide(
       col_0 TINYINT auto_increment PRIMARY KEY,
       col_1 INT ,
       col_2 VARCHAR(255),
       col_3 TIMESTAMP,
       col_4 DATE,
       col_5 INT,
       col_7 TIME,
       col_8 DOUBLE,
       col_9 LONG,
       col_1_0 TINYINT,
       col_1_1 INT,
       col_1_2 VARCHAR(255),
       col_1_3 TIMESTAMP,
       col_1_4 DATE,
       col_1_5 INT,
       col_1_7 TIME,
       col_1_8 DOUBLE,
       col_1_9 LONG,
       col_2_0 TINYINT,
       col_2_1 INT,
       col_2_2 VARCHAR(255),
       col_2_3 TIMESTAMP,
       col_2_4 DATE,
       col_2_5 INT,
       col_2_7 TIME,
       col_2_8 DOUBLE,
       col_2_9 LONG);

CREATE INDEX test_index_name ON wide (col_0, col_1_2, col_2_9)
