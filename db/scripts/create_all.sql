SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `ad_device`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ad_device` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `serial` VARCHAR(24) NOT NULL,
  `lon` DECIMAL(18,14) NOT NULL,
  `lat` DECIMAL(18,14) NOT NULL,
  `name` VARCHAR(64) NULL,
  `type` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(254) NOT NULL,
  `password` VARCHAR(32) NOT NULL,
  `role` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `campaign` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_campaign_user1_idx` (`user_id` ASC),
  CONSTRAINT `fk_campaign_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ad_medium`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ad_medium` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ad_medium_user1_idx` (`user_id` ASC),
  CONSTRAINT `fk_ad_medium_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_action`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `campaign_action` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `campaign_id` INT NOT NULL,
  `ad_medium_id` INT NOT NULL,
  `time_of_day` SMALLINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_campaign_image_campaign_idx` (`campaign_id` ASC),
  INDEX `fk_campaign_action_ad_medium1_idx` (`ad_medium_id` ASC),
  CONSTRAINT `fk_campaign_image_campaign`
    FOREIGN KEY (`campaign_id`)
    REFERENCES `campaign` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_action_ad_medium1`
    FOREIGN KEY (`ad_medium_id`)
    REFERENCES `ad_medium` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `user_ad_device`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_ad_device` (
  `user_id` INT NOT NULL,
  `ad_device_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `ad_device_id`),
  INDEX `fk_user_has_ad_display_ad_display1_idx` (`ad_device_id` ASC),
  INDEX `fk_user_has_ad_display_user1_idx` (`user_id` ASC),
  CONSTRAINT `fk_user_has_ad_display_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_has_ad_display_ad_display1`
    FOREIGN KEY (`ad_device_id`)
    REFERENCES `ad_device` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ad_device_debug_msg`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ad_device_debug_msg` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `ad_device_id` INT NOT NULL,
  `date` BIGINT NOT NULL,
  `message` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ad_device_debug_ad_device1_idx` (`ad_device_id` ASC),
  CONSTRAINT `fk_ad_device_debug_ad_device1`
    FOREIGN KEY (`ad_device_id`)
    REFERENCES `ad_device` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ad_device_parameter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ad_device_parameter` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `ad_device_id` INT NOT NULL,
  `param` VARCHAR(45) NOT NULL,
  `value` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ad_device_parameter_ad_device1_idx` (`ad_device_id` ASC),
  CONSTRAINT `fk_ad_device_parameter_ad_device1`
    FOREIGN KEY (`ad_device_id`)
    REFERENCES `ad_device` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ad_device_test_image`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ad_device_test_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `ad_device_id` INT NOT NULL,
  `order_index` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `fk_ad_device_test_image_ad_device1_idx` (`ad_device_id` ASC),
  CONSTRAINT `fk_ad_device_test_image_ad_device1`
    FOREIGN KEY (`ad_device_id`)
    REFERENCES `ad_device` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ad_medium_item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ad_medium_item` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `ad_medium_id` INT NOT NULL,
  `type` SMALLINT NOT NULL DEFAULT 0,
  `gentype` SMALLINT NOT NULL DEFAULT 0,
  `geninfo` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ad_medium_item_ad_medium1_idx` (`ad_medium_id` ASC),
  CONSTRAINT `fk_ad_medium_item_ad_medium1`
    FOREIGN KEY (`ad_medium_id`)
    REFERENCES `ad_medium` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `system_parameter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `system_parameter` (
  `param` VARCHAR(45) NOT NULL,
  `value` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`param`))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
