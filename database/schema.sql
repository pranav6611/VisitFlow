-- Optional: the backend creates these tables automatically (ddl-auto=update).
-- Run this only if you prefer to manage the schema yourself.

CREATE DATABASE IF NOT EXISTS clinic_noshow
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE clinic_noshow;

CREATE TABLE IF NOT EXISTS doctors (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  specialty  VARCHAR(80)  NOT NULL
);

CREATE TABLE IF NOT EXISTS patients (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  phone        VARCHAR(30)  NOT NULL,
  email        VARCHAR(120),
  total_visits INT NOT NULL DEFAULT 0,
  no_shows     INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS appointments (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id         BIGINT NOT NULL,
  doctor_id          BIGINT NOT NULL,
  appointment_time   DATETIME NOT NULL,
  reason             VARCHAR(200),
  status             VARCHAR(12) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED|CONFIRMED|COMPLETED|NO_SHOW|CANCELLED
  reminder_count     INT NOT NULL DEFAULT 0,
  last_reminder_at   DATETIME NULL,
  risk_score         INT NOT NULL DEFAULT 0,
  created_at         DATETIME NOT NULL,
  CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_appt_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(id),
  INDEX idx_appt_time (appointment_time),
  INDEX idx_appt_status (status)
);

CREATE TABLE IF NOT EXISTS reminders (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id  BIGINT NOT NULL,
  channel         VARCHAR(10) NOT NULL,
  message         VARCHAR(500) NOT NULL,
  sent_at         DATETIME NOT NULL,
  CONSTRAINT fk_reminder_appointment
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE TABLE IF NOT EXISTS user_accounts (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  email          VARCHAR(120) NOT NULL,
  password_hash  VARCHAR(100) NOT NULL,
  role           VARCHAR(12) NOT NULL,
  display_name   VARCHAR(100) NOT NULL,
  doctor_id      BIGINT NULL,
  patient_id     BIGINT NULL,
  CONSTRAINT uq_user_accounts_email UNIQUE (email),
  CONSTRAINT uq_user_accounts_doctor UNIQUE (doctor_id),
  CONSTRAINT uq_user_accounts_patient UNIQUE (patient_id),
  CONSTRAINT fk_user_account_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT fk_user_account_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT chk_user_account_role_link CHECK (
    (role = 'DOCTOR' AND doctor_id IS NOT NULL AND patient_id IS NULL) OR
    (role = 'PATIENT' AND patient_id IS NOT NULL AND doctor_id IS NULL)
  )
);


