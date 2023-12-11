DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
	id serial PRIMARY KEY,
	first_name VARCHAR ( 255 ),
	last_name VARCHAR ( 255 )
);

INSERT INTO employees (id,first_name,last_name) VALUES (1, 'Allen Test','Texas');
INSERT INTO employees (id,first_name,last_name) VALUES (2, 'Test', 'data');
