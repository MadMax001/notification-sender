-- liquibase formatted sql

-- changeset id:initial_db
-- preconditions onFail:HALT
CREATE SCHEMA IF NOT EXISTS notification;

CREATE SEQUENCE notification.notification_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE notification.notification (
  id BIGINT NOT NULL,
   remote_id VARCHAR(255),
   type VARCHAR(255) NOT NULL,
   updated TIMESTAMP,
   created TIMESTAMP,
   content VARCHAR(255) NOT NULL,
   theme VARCHAR(255),
   person_user VARCHAR(255),
   person_ip VARCHAR(255),
   person_email VARCHAR(255),
   CONSTRAINT pk_notification PRIMARY KEY (id)
);

CREATE SEQUENCE notification.stage_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE notification.notification_stage (
  id BIGINT NOT NULL,
   notification_id BIGINT,
   stage VARCHAR(255) NOT NULL,
   message VARCHAR(255),
   created TIMESTAMP,
   CONSTRAINT pk_notification_stage PRIMARY KEY (id)
);

ALTER TABLE notification.notification_stage
    ADD CONSTRAINT FK_NOTIFICATION_STAGE_ON_NOTIFICATION FOREIGN KEY (notification_id)
    REFERENCES notification.notification (id);

CREATE SEQUENCE notification.attachment_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE notification.notification_attachment (
  id BIGINT NOT NULL,
   notification_id BIGINT,
   name VARCHAR(255) NOT NULL,
   content bytea,
   CONSTRAINT pk_notification_attachment PRIMARY KEY (id)
);

ALTER TABLE notification.notification_attachment
    ADD CONSTRAINT FK_NOTIFICATION_ATTACHMENT_ON_NOTIFICATION
    FOREIGN KEY (notification_id) REFERENCES notification.notification (id);