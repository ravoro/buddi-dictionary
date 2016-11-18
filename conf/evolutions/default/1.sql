# --- !Ups
CREATE TABLE words (
  word       VARCHAR(255) NOT NULL PRIMARY KEY,
  definition VARCHAR(255) NOT NULL
);

# --- !Downs
DROP TABLE words