import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'placement.db')

def seed_database():
    print(f"Seeding database at {DB_PATH}...")
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Check if students are empty
    cursor.execute("SELECT COUNT(*) FROM students")
    student_count = cursor.fetchone()[0]

    if student_count == 0:
        print("Seeding students...")
        students = [
            ('S001', 'Aryan Sharma', 'CSE', 8.9, 'Python,ML,SQL', 'AWS,TensorFlow', 'E-Commerce App', 'aryan@college.edu', '9000000001', 'Not Placed'),
            ('S002', 'Priya Mehta', 'ECE', 7.5, 'C++,Embedded,VHDL', 'ARM Cortex', 'Smart Home', 'priya@college.edu', '9000000002', 'Not Placed'),
            ('S003', 'Rohan Verma', 'CSE', 9.2, 'Java,Spring,Docker', 'GCP Architect', 'Microservices API', 'rohan@college.edu', '9000000003', 'Not Placed'),
            ('S004', 'Sneha Kapoor', 'IT', 8.1, 'JavaScript,React,Node', 'React Cert', 'Chat App', 'sneha@college.edu', '9000000004', 'Not Placed'),
            ('S005', 'Aditya Singh', 'ME', 7.2, 'SolidWorks,AutoCAD', 'CSWA', 'Baja Buggy Design', 'aditya@college.edu', '9000000005', 'Not Placed'),
            ('S006', 'Kirti Saxena', 'EE', 8.5, 'MATLAB,PLC,Scada', 'Power Systems Cert', 'Grid Simulation', 'kirti@college.edu', '9000000006', 'Not Placed'),
            ('S007', 'Vikram Malhotra', 'CSE', 6.8, 'HTML,CSS,JS,PHP', 'Frontend Spec', 'Portfolio Site', 'vikram@college.edu', '9000000007', 'Not Placed'),
            ('S008', 'Ananya Gupta', 'ECE', 8.8, 'Python,IoT,C', 'Raspberry Pi Cert', 'Weather Station', 'ananya@college.edu', '9000000008', 'Not Placed'),
            ('S009', 'Manish Pandey', 'ME', 6.5, 'CATIA,Ansys', 'FEA Spec', 'Gearbox Simulation', 'manish@college.edu', '9000000009', 'Not Placed'),
            ('S010', 'Tanvi Rao', 'IT', 9.5, 'Python,Django,React,AWS', 'AWS Developer', 'SaaS Platform', 'tanvi@college.edu', '9000000010', 'Not Placed')
        ]
        cursor.executemany("""
        INSERT INTO students (student_id, name, branch, cgpa, skills, certifications, projects, email, phone, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, students)
    else:
        print(f"Students table already has {student_count} records. Skipping.")

    # Check if companies are empty
    cursor.execute("SELECT COUNT(*) FROM companies")
    company_count = cursor.fetchone()[0]

    if company_count == 0:
        print("Seeding companies...")
        companies = [
            (1, 'TCS', 'IT Services', 6.0, 'Python,SQL,Java', 3.5, 'Software Engineer', 'Pan India', 'Leading IT firm', '2026-11-01'),
            (2, 'Infosys', 'IT Services', 6.5, 'Java,Spring,SQL', 4.0, 'Systems Engineer', 'Bengaluru', 'Global IT company', '2026-11-05'),
            (3, 'Google', 'Technology', 8.5, 'Python,Go,DS,Algorithms', 18.0, 'Software Development Engineer', 'Hyderabad', 'Search & Cloud leader', '2026-10-15'),
            (4, 'Stripe', 'Fintech', 8.0, 'Ruby,Go,PostgreSQL', 15.0, 'Backend Engineer', 'Bengaluru (Hybrid)', 'Payment gateway company', '2026-10-20'),
            (5, 'Tesla', 'Automotive', 7.5, 'C++,Embedded,RTOS', 12.0, 'Firmware Engineer', 'Pune', 'EV manufacturer', '2026-11-10'),
            (6, 'JPMorgan', 'Finance', 7.0, 'Java,Python,SQL', 8.5, 'Technology Analyst', 'Mumbai', 'Investment banking leader', '2026-11-15'),
            (7, 'Cognizant', 'IT Services', 6.0, 'Java,SQL,HTML', 4.2, 'Programmer Analyst', 'Chennai', 'Global consulting and tech', '2026-12-01'),
            (8, 'Amazon', 'E-commerce', 8.2, 'Java,C++,AWS', 16.0, 'Software Development Engineer', 'Bengaluru', 'Cloud and retail giant', '2026-10-28'),
            (9, 'Intel', 'Semiconductor', 7.8, 'Verilog,C,Assembly', 10.0, 'Hardware Engineer', 'Bengaluru', 'Leading silicon design firm', '2026-11-20'),
            (10, 'Wipro', 'IT Services', 6.0, 'Java,Python,SQL', 3.6, 'Project Engineer', 'Noida', 'Consulting & IT services', '2026-12-05')
        ]
        cursor.executemany("""
        INSERT INTO companies (company_id, name, sector, min_cgpa, required_skills, package_lpa, job_role, location, description, drive_date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, companies)
    else:
        print(f"Companies table already has {company_count} records. Skipping.")

    conn.commit()
    conn.close()
    print("Database seeding check complete.")

if __name__ == "__main__":
    seed_database()
