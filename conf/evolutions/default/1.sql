# --- !Ups
CREATE TABLE languages (
  code VARCHAR(2) NOT NULL PRIMARY KEY
);
INSERT INTO languages VALUES ("en");
INSERT INTO languages VALUES ("ru");

CREATE TABLE words (
  id   INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  word VARCHAR(255) NOT NULL,
  lang VARCHAR(2)   NOT NULL,
  FOREIGN KEY (lang) REFERENCES languages (code)
);

CREATE TABLE definitions (
  wid        INTEGER      NOT NULL,
  definition VARCHAR(255) NOT NULL,
  lang       VARCHAR(2)   NOT NULL,
  FOREIGN KEY (wid) REFERENCES words (id),
  FOREIGN KEY (lang) REFERENCES languages (code)
);

# --- !Downs
DROP TABLE languages;
DROP TABLE words;
DROP TABLE definitions;