import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'placement.db')

class StudentManager:
    def __init__(self):
        pass

    def _get_connection(self):
        return sqlite3.connect(DB_PATH)

    def get_all_students(self):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM students ORDER BY student_id ASC")
        rows = cursor.fetchall()
        
        students = []
        for r in rows:
            students.append({
                'student_id': r[0],
                'name': r[1],
                'branch': r[2],
                'cgpa': r[3],
                'skills': r[4],
                'certifications': r[5],
                'projects': r[6],
                'email': r[7],
                'phone': r[8],
                'status': r[9]
            })
        conn.close()
        return students

    def get_student_by_id(self, student_id):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM students WHERE student_id = ?", (student_id,))
        r = cursor.fetchone()
        conn.close()
        if r:
            return {
                'student_id': r[0],
                'name': r[1],
                'branch': r[2],
                'cgpa': r[3],
                'skills': r[4],
                'certifications': r[5],
                'projects': r[6],
                'email': r[7],
                'phone': r[8],
                'status': r[9]
            }
        return None

    def add_student(self, student_id, name, branch, cgpa, skills, certifications, projects, email, phone, status="Not Placed"):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
            INSERT INTO students (student_id, name, branch, cgpa, skills, certifications, projects, email, phone, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (student_id, name, branch, cgpa, skills, certifications, projects, email, phone, status))
            conn.commit()
            return True, "Student added successfully."
        except sqlite3.IntegrityError:
            return False, "Error: Student ID already exists."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def update_student(self, student_id, name, branch, cgpa, skills, certifications, projects, email, phone, status):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
            UPDATE students 
            SET name=?, branch=?, cgpa=?, skills=?, certifications=?, projects=?, email=?, phone=?, status=?
            WHERE student_id=?
            """, (name, branch, cgpa, skills, certifications, projects, email, phone, status, student_id))
            conn.commit()
            if cursor.rowcount == 0:
                return False, "Student not found."
            return True, "Student updated successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def delete_student(self, student_id):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("DELETE FROM students WHERE student_id = ?", (student_id,))
            conn.commit()
            if cursor.rowcount == 0:
                return False, "Student not found."
            return True, "Student deleted successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def filter_students_by_cgpa(self, min_cgpa):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM students WHERE cgpa >= ? ORDER BY cgpa DESC", (min_cgpa,))
        rows = cursor.fetchall()
        students = []
        for r in rows:
            students.append({
                'student_id': r[0],
                'name': r[1],
                'branch': r[2],
                'cgpa': r[3],
                'skills': r[4],
                'certifications': r[5],
                'projects': r[6],
                'email': r[7],
                'phone': r[8],
                'status': r[9]
            })
        conn.close()
        return students

    def get_eligible_students_for_company(self, company_id):
        # 1. Fetch company criteria
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT name, min_cgpa, required_skills FROM companies WHERE company_id = ?", (company_id,))
        company = cursor.fetchone()
        if not company:
            conn.close()
            return []

        comp_name, min_cgpa, req_skills_str = company
        req_skills = [s.strip().lower() for s in req_skills_str.split(',') if s.strip()]

        # 2. Fetch students meeting min CGPA
        cursor.execute("SELECT * FROM students WHERE cgpa >= ? AND status != 'Not Eligible' ORDER BY cgpa DESC", (min_cgpa,))
        rows = cursor.fetchall()
        conn.close()

        matched_students = []
        for r in rows:
            student_skills_str = r[4] or ''
            student_skills = [s.strip().lower() for s in student_skills_str.split(',') if s.strip()]

            # Compute skill match percentage
            match_count = 0
            if req_skills:
                for skill in req_skills:
                    if skill in student_skills:
                        match_count += 1
                match_percentage = round((match_count / len(req_skills)) * 100, 1)
            else:
                match_percentage = 100.0  # No skills required, 100% match

            matched_students.append({
                'student_id': r[0],
                'name': r[1],
                'branch': r[2],
                'cgpa': r[3],
                'skills': r[4],
                'match_percentage': match_percentage,
                'status': r[9]
            })

        return matched_students
