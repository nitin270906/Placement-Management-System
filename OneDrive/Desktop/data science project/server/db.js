const { DatabaseSync } = require('node:sqlite');
const path = require('path');

const DB_PATH = path.join(__dirname, '..', 'data', 'placement.db');

// Ensure data directory exists
const fs = require('fs');
const dataDir = path.join(__dirname, '..', 'data');
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });

const db = new DatabaseSync(DB_PATH);

// Enable WAL mode for better performance
db.exec('PRAGMA journal_mode = WAL');
db.exec('PRAGMA foreign_keys = ON');

// ── Schema ──────────────────────────────────────────────────────────────────

db.exec(`
  CREATE TABLE IF NOT EXISTS students (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    email       TEXT    NOT NULL UNIQUE,
    phone       TEXT,
    department  TEXT    NOT NULL,
    batch_year  INTEGER NOT NULL,
    cgpa        REAL    DEFAULT 0,
    skills      TEXT    DEFAULT '',   -- comma-separated
    resume_url  TEXT,
    status      TEXT    NOT NULL DEFAULT 'active'
                        CHECK(status IN ('active','placed','not_eligible')),
    created_at  TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS companies (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    industry     TEXT,
    website      TEXT,
    hr_name      TEXT,
    hr_email     TEXT,
    hr_phone     TEXT,
    description  TEXT,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS job_postings (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id      INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title           TEXT    NOT NULL,
    description     TEXT,
    requirements    TEXT,
    salary_min      REAL,
    salary_max      REAL,
    location        TEXT,
    job_type        TEXT    NOT NULL DEFAULT 'full_time'
                            CHECK(job_type IN ('full_time','internship','contract')),
    min_cgpa        REAL    DEFAULT 0,
    eligible_depts  TEXT    DEFAULT '',  -- comma-separated, empty = all
    deadline        TEXT,
    status          TEXT    NOT NULL DEFAULT 'open'
                            CHECK(status IN ('open','closed','cancelled')),
    created_at      TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS applications (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id     INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    job_posting_id INTEGER NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    status         TEXT    NOT NULL DEFAULT 'applied'
                           CHECK(status IN ('applied','shortlisted','interview_scheduled',
                                            'selected','rejected','offer_accepted','offer_declined')),
    applied_at     TEXT    NOT NULL DEFAULT (datetime('now')),
    notes          TEXT,
    UNIQUE(student_id, job_posting_id)
  );

  CREATE TABLE IF NOT EXISTS interviews (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    round          INTEGER NOT NULL DEFAULT 1,
    scheduled_at   TEXT    NOT NULL,
    mode           TEXT    DEFAULT 'in_person'
                           CHECK(mode IN ('in_person','virtual','telephonic')),
    interviewer    TEXT,
    feedback       TEXT,
    result         TEXT    CHECK(result IN ('pass','fail','pending') OR result IS NULL),
    created_at     TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS placements (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id     INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    job_posting_id INTEGER NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    company_id     INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    offer_date     TEXT    NOT NULL DEFAULT (date('now')),
    joining_date   TEXT,
    ctc            REAL,
    offer_letter   TEXT,
    created_at     TEXT    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(student_id)
  );
`);

module.exports = db;
