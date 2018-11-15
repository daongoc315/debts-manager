CREATE TABLE IF NOT EXISTS "user"(
  username VARCHAR(100) UNIQUE NOT NULL PRIMARY KEY,
  "password" CHAR(64) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS userrelationship(
  "from" VARCHAR(100) NOT NULL REFERENCES "user" (username),
  "to" VARCHAR(100) NOT NULL REFERENCES "user" (username)
);

CREATE TABLE IF NOT EXISTS "transaction"(
  id SERIAL UNIQUE NOT NULL PRIMARY KEY,
  description TEXT NOT NULL,
  "from" VARCHAR(100) NOT NULL REFERENCES "user" (username),
  "to" VARCHAR(100) NOT NULL REFERENCES "user" (username),
  at TIMESTAMP NOT NULL,
  value DOUBLE PRECISION
);