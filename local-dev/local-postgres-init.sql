DROP DATABASE IF EXISTS testdb;
DROP DATABASE IF EXISTS janitordb;
DROP ROLE dbuser;
DROP ROLE janitoruser;
CREATE DATABASE testdb;
CREATE ROLE dbuser WITH LOGIN ENCRYPTED PASSWORD 'dbpwd';
CREATE DATABASE janitordb;
CREATE ROLE janitoruser WITH LOGIN ENCRYPTED PASSWORD 'janitorpwd';