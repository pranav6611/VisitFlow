# CareSlot · Appointment No-Show Reduction System for Clinics

Helps clinic staff see which appointments are likely to be missed, act on them early
(confirm, remind), and track no-show rates by patient.

| Layer    | Tech                                    |
|----------|-----------------------------------------|
| Frontend | React 18 (Vite), plain CSS, JavaScript  |
| Backend  | Java 17, Spring Boot 3, Spring Data JPA |
| Database | MySQL 8                                 |

## Features

- **Dashboard:** today's total, confirmed, high-risk and no-show rate cards.
- **Risk score (1–95%) for every open appointment,** colour-coded Low / Medium / High.
- **Quick actions:** Confirm, Remind (logs a reminder), Arrived, No-show.
- **Filters:** All, Open, and High risk.
- **Patients page:** no-show rate per patient, plus an add-patient form.
- **Book appointments** from a popup.
- **Separate doctor and patient accounts** with registration, password-protected sign-in, and role-specific pages.

## Accounts and access

Register as a doctor with a name, email, password and specialty, or as a patient with a name, email, password and phone number. Patients see only appointments connected to their own account. Doctors see and manage their own appointment schedule. New accounts are stored in the selected database; H2 accounts reset when the backend stops.

Doctor and patient registration are open in this demo. Set `JWT_SECRET` to a private value before deploying outside a local development environment.

## How the risk score works

`RiskService` is a transparent rule-based model. Each rule adds or subtracts points:

| Factor                              | Effect            |
|-------------------------------------|-------------------|
| Patient history (≥3 visits)         | up to +60 × no-show rate |
| New patient (<3 visits)             | +8                |
| Booked more than 14 days ahead      | +15 (7–14 days: +8) |
| Booked within a day                 | −4                |
| Confirmed by patient                | −12               |
| 2+ reminders with no confirmation   | +6                |
| Slot before 9 AM                    | +5                |
| Monday slot                         | +4                |

Scores are clamped to 1–95. Badges show **High** at 60%+, **Medium** at 30%+, otherwise **Low**.
Replace `RiskService` with a trained model once you have enough history.

## Project structure

```
clinic-noshow/
├── backend/    Spring Boot REST API (port 8080)
├── frontend/   React dashboard (port 5173)
└── database/   schema.sql (optional, tables are auto-created)
```

## 1. Database

For a quick local run, use the included in-memory H2 database. No database server or setup is needed. The database resets whenever the backend stops.

To keep data between runs, start your MySQL 8 **Server** service. MySQL Workbench 8 is the client used to manage that server; the Spring Boot backend connects directly to the same server over JDBC. The backend creates the `clinic_noshow` database and updates its tables on first run. The connection defaults to `localhost:3306`, user `root`, password `root`. Set these environment variables to match your Workbench connection (especially the password):

PowerShell:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "clinic_noshow"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your MySQL password"
```

In Workbench, connect to the local MySQL 8 server using that host, port and user. To install the included schema manually, open `database/schema.sql` in Workbench and execute the script; it creates and selects `clinic_noshow` and defines the tables. This step is optional because Hibernate creates or updates the tables when the backend starts. Keep the backend environment variables and Workbench connection pointed at the same MySQL server and database.

## 2. Backend

Requires JDK 17+ and Maven. For the no-setup demo configuration, activate the H2 profile:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

For MySQL, set the connection variables above in the same terminal, then run `mvn spring-boot:run` without the H2 profile.

On the first run, 2 doctors, 6 patients and 8 appointments for today are inserted as demo data.

### API

| Method | Endpoint                              | Description                              |
|--------|---------------------------------------|------------------------------------------|
| GET    | `/api/appointments?date=YYYY-MM-DD`   | Appointments for a day, with risk scores |
| GET    | `/api/appointments/summary?date=`     | Dashboard counts and no-show rate        |
| POST   | `/api/appointments`                   | Book an appointment                      |
| PUT    | `/api/appointments/{id}/status`       | Set CONFIRMED, COMPLETED, NO_SHOW, etc.  |
| POST   | `/api/appointments/{id}/remind`       | Log a reminder sent                      |
| DELETE | `/api/appointments/{id}`              | Cancel                                   |
| GET    | `/api/patients`                       | Patients, highest no-show rate first     |
| POST   | `/api/patients`                       | Add a patient                            |
| GET    | `/api/doctors`                        | List doctors                             |
| POST   | `/api/auth/register/doctor`            | Register a doctor and sign in            |
| POST   | `/api/auth/register/patient`           | Register a patient and sign in           |
| POST   | `/api/auth/login`                     | Sign in with email, password and role    |
| GET    | `/api/auth/me`                         | Get the current account                  |
| GET    | `/api/appointments/mine`               | Patient's own appointments               |

Marking an appointment COMPLETED or NO_SHOW updates the patient's visit and no-show totals,
which feed the risk score for future bookings.

## 3. Frontend

Requires Node 18+.

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The Vite dev server proxies `/api` requests to the backend at `http://localhost:8080`, avoiding browser CORS errors. If the API is elsewhere, copy `.env.example` to `.env` and set `VITE_API_URL` to its URL (for example, `http://localhost:8081/api`).

## Ideas for next steps

- Send real SMS or WhatsApp reminders (Twilio or a similar provider) from the Remind button
- Let patients confirm by reply or through a link
- Login for receptionist, doctor and admin roles
- Waitlist: offer freed slots to patients who are waiting
