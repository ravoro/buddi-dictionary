# --- !Ups
CREATE TABLE words (
  id   INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  word VARCHAR(255) NOT NULL
);

CREATE TABLE definitions (
  wid        INTEGER      NOT NULL,
  definition VARCHAR(255) NOT NULL,
  FOREIGN KEY (wid) REFERENCES words (id)
);

# --- !Downs
DROP TABLE words;
DROP TABLE definitions;