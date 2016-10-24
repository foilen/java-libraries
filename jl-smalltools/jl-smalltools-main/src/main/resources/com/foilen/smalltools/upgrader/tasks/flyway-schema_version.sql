CREATE TABLE schema_version (
    `version_rank` INT NOT NULL,
    `installed_rank` INT NOT NULL,
    `version` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200) NOT NULL,
    `type` VARCHAR(20) NOT NULL,
    `script` VARCHAR(1000) NOT NULL,
    `checksum` INT,
    `installed_by` VARCHAR(100) NOT NULL,
    `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `execution_time` INT NOT NULL,
    `success` BOOL NOT NULL
) ENGINE=InnoDB;
ALTER TABLE schema_version ADD CONSTRAINT `schema_version_pk` PRIMARY KEY (`version`);
