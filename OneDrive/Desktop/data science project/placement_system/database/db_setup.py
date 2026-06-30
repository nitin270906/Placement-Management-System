import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'placement.db')

def setup_database():
    print(f"Setting up database at {DB_PATH}...")
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # 1. Students Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS students (
        student_id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        branch TEXT NOT NULL,
        cgpa REAL NOT NULL,
        skills TEXT DEFAULT '',
        certifications TEXT DEFAULT '',
        projects TEXT DEFAULT '',
        email TEXT,
        phone TEXT,
        status TEXT NOT NULL DEFAULT 'Not Placed'
    )
    """)

    # 2. Companies Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS companies (
        company_id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        sector TEXT,
        min_cgpa REAL NOT NULL DEFAULT 0,
        required_skills TEXT DEFAULT '',
        package_lpa REAL,
        job_role TEXT,
        location TEXT,
        description TEXT,
        drive_date TEXT
    )
    """)

    # 3. Interviews Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS interviews (
        interview_id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id TEXT NOT NULL,
        company_id INTEGER NOT NULL,
        scheduled_at TEXT NOT NULL,
        round INTEGER NOT NULL DEFAULT 1,
        mode TEXT DEFAULT 'In-Person',
        status TEXT NOT NULL DEFAULT 'Scheduled',
        result TEXT DEFAULT 'Pending',
        notes TEXT,
        FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
        FOREIGN KEY (company_id) REFERENCES companies (company_id) ON DELETE CASCADE
    )
    """)

    # 4. Placements Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS placements (
        placement_id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id TEXT NOT NULL UNIQUE,
        company_id INTEGER NOT NULL,
        offer_date TEXT NOT NULL,
        ctc_lpa REAL,
        job_role TEXT,
        FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
        FOREIGN KEY (company_id) REFERENCES companies (company_id) ON DELETE CASCADE
    )
    """)

    conn.commit()
    conn.close()
    print("Database setup complete.")

if __name__ == "__main__":
    setup_database()
