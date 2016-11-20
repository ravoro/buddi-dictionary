# --- !Ups
CREATE TABLE words (
  id         INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  word       VARCHAR(255) NOT NULL,
  definition VARCHAR(255) NOT NULL
);

# --- !Downs
DROP TABLE words