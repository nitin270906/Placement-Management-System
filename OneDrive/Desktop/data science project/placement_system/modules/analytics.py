import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'placement.db')

class AnalyticsEngine:
    def __init__(self):
        pass

    def _get_connection(self):
        return sqlite3.connect(DB_PATH)

    def get_summary_stats(self):
        conn = self._get_connection()
        cursor = conn.cursor()

        # Counts
        cursor.execute("SELECT COUNT(*) FROM students")
        total_students = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM students WHERE status = 'Placed'")
        placed_students = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM students WHERE status = 'Not Eligible'")
        not_eligible = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM companies")
        total_companies = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM interviews")
        total_interviews = cursor.fetchone()[0]

        # Calculate Placement Rate
        eligible_students = total_students - not_eligible
        placement_rate = 0.0
        if eligible_students > 0:
            placement_rate = round((placed_students / eligible_students) * 100, 1)

        # Average Salary
        cursor.execute("SELECT AVG(ctc_lpa) FROM placements")
        avg_ctc = cursor.fetchone()[0] or 0.0
        avg_ctc = round(avg_ctc, 2)

        conn.close()

        return {
            'total_students': total_students,
            'placed_students': placed_students,
            'not_eligible': not_eligible,
            'eligible_students': eligible_students,
            'placement_rate': placement_rate,
            'total_companies': total_companies,
            'total_interviews': total_interviews,
            'avg_ctc_lpa': avg_ctc
        }

    def get_branch_reports(self):
        conn = self._get_connection()
        cursor = conn.cursor()

        # Aggregate branch wise
        cursor.execute("""
            SELECT 
                branch,
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'Placed' THEN 1 ELSE 0 END) AS placed,
                SUM(CASE WHEN status = 'Not Eligible' THEN 1 ELSE 0 END) AS not_eligible
            FROM students
            GROUP BY branch
        """)
        rows = cursor.fetchall()

        branch_stats = []
        for r in rows:
            branch = r[0]
            total = r[1]
            placed = r[2]
            not_eligible = r[3]
            eligible = total - not_eligible
            
            rate = 0.0
            if eligible > 0:
                rate = round((placed / eligible) * 100, 1)

            # Get average package for this branch
            cursor.execute("""
                SELECT AVG(p.ctc_lpa) 
                FROM placements p
                JOIN students s ON s.student_id = p.student_id
                WHERE s.branch = ?
            """, (branch,))
            avg_ctc = cursor.fetchone()[0] or 0.0
            avg_ctc = round(avg_ctc, 2)

            branch_stats.append({
                'branch': branch,
                'total_students': total,
                'placed_students': placed,
                'eligible_students': eligible,
                'placement_rate': rate,
                'avg_ctc_lpa': avg_ctc
            })

        conn.close()
        # Sort by placement rate descending
        branch_stats.sort(key=lambda x: x['placement_rate'], reverse=True)
        return branch_stats

    def get_highest_packages(self):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("""
            SELECT s.name, s.branch, c.name, p.ctc_lpa, p.job_role, p.offer_date
            FROM placements p
            JOIN students s ON s.student_id = p.student_id
            JOIN companies c ON c.company_id = p.company_id
            ORDER BY p.ctc_lpa DESC
        """)
        rows = cursor.fetchall()
        conn.close()

        records = []
        for r in rows:
            records.append({
                'student_name': r[0],
                'student_branch': r[1],
                'company_name': r[2],
                'ctc_lpa': r[3],
                'job_role': r[4],
                'offer_date': r[5]
            })
        return records
