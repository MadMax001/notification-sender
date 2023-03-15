-- liquibase formatted sql

-- changeset id:initial_db
-- preconditions onFail:HALT
CREATE SCHEMA IF NOT EXISTS notification;

CREATE SEQUENCE notification.notification_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE notification.notification (
  id BIGINT NOT NULL,
   remote_id VARCHAR(255),
   type VARCHAR(255) not null,
   updated TIMESTAMP,
   created TIMESTAMP,
   content TEXT,
   theme VARCHAR(255),
   person_user VARCHAR(255),
   person_ip VARCHAR(255),
   person_email VARCHAR(255),
   CONSTRAINT pk_notification PRIMARY KEY (id)
);

CREATE TABLE notification.notification_stage (
    id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    stage VARCHAR(255) not null,
    message VARCHAR(255),
    created TIMESTAMP,
    CONSTRAINT pk_notification_stage PRIMARY KEY (id),
    CONSTRAINT fk_notification_stage FOREIGN KEY (notification_id)
        REFERENCES notification.notification (id) ON DELETE CASCADE
)

