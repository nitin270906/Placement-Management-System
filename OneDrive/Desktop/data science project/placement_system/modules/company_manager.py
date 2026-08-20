import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'placement.db')

class CompanyManager:
    def __init__(self):
        pass

    def _get_connection(self):
        return sqlite3.connect(DB_PATH)

    def get_all_companies(self):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM companies ORDER BY name ASC")
        rows = cursor.fetchall()
        
        companies = []
        for r in rows:
            companies.append({
                'company_id': r[0],
                'name': r[1],
                'sector': r[2],
                'min_cgpa': r[3],
                'required_skills': r[4],
                'package_lpa': r[5],
                'job_role': r[6],
                'location': r[7],
                'description': r[8],
                'drive_date': r[9]
            })
        conn.close()
        return companies

    def get_company_by_id(self, company_id):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM companies WHERE company_id = ?", (company_id,))
        r = cursor.fetchone()
        conn.close()
        if r:
            return {
                'company_id': r[0],
                'name': r[1],
                'sector': r[2],
                'min_cgpa': r[3],
                'required_skills': r[4],
                'package_lpa': r[5],
                'job_role': r[6],
                'location': r[7],
                'description': r[8],
                'drive_date': r[9]
            }
        return None

    def add_company(self, name, sector, min_cgpa, required_skills, package_lpa, job_role, location, description, drive_date):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
            INSERT INTO companies (name, sector, min_cgpa, required_skills, package_lpa, job_role, location, description, drive_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (name, sector, min_cgpa, required_skills, package_lpa, job_role, location, description, drive_date))
            conn.commit()
            return True, "Company added successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def update_company(self, company_id, name, sector, min_cgpa, required_skills, package_lpa, job_role, location, description, drive_date):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
            UPDATE companies 
            SET name=?, sector=?, min_cgpa=?, required_skills=?, package_lpa=?, job_role=?, location=?, description=?, drive_date=?
            WHERE company_id=?
            """, (name, sector, min_cgpa, required_skills, package_lpa, job_role, location, description, drive_date, company_id))
            conn.commit()
            if cursor.rowcount == 0:
                return False, "Company not found."
            return True, "Company updated successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def delete_company(self, company_id):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("DELETE FROM companies WHERE company_id = ?", (company_id,))
            conn.commit()
            if cursor.rowcount == 0:
                return False, "Company not found."
            return True, "Company deleted successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()
