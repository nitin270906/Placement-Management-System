import sqlite3
import os
from datetime import date
from placement_system.data_structures.queue_ds import Queue
from placement_system.data_structures.priority_queue_ds import PriorityQueue

DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'placement.db')

class InterviewManager:
    # Class-level dictionary to hold active in-memory queues for interview drives
    # Key: company_id (int), Value: custom Queue or PriorityQueue instance
    active_queues = {}

    def __init__(self):
        pass

    def _get_connection(self):
        return sqlite3.connect(DB_PATH)

    def get_all_interviews(self):
        conn = self._get_connection()
        cursor = conn.cursor()
        cursor.execute("""
            SELECT i.interview_id, i.student_id, s.name, s.branch, s.cgpa,
                   i.company_id, c.name, i.scheduled_at, i.round, i.mode, i.status, i.result, i.notes
            FROM interviews i
            JOIN students s ON s.student_id = i.student_id
            JOIN companies c ON c.company_id = i.company_id
            ORDER BY i.scheduled_at ASC
        """)
        rows = cursor.fetchall()
        
        interviews = []
        for r in rows:
            interviews.append({
                'interview_id': r[0],
                'student_id': r[1],
                'student_name': r[2],
                'student_branch': r[3],
                'student_cgpa': r[4],
                'company_id': r[5],
                'company_name': r[6],
                'scheduled_at': r[7],
                'round': r[8],
                'mode': r[9],
                'status': r[10],
                'result': r[11],
                'notes': r[12]
            })
        conn.close()
        return interviews

    def schedule_interview(self, student_id, company_id, scheduled_at, round_num=1, mode="In-Person"):
        # Verify student eligibility first
        conn = self._get_connection()
        cursor = conn.cursor()
        
        cursor.execute("SELECT cgpa, status FROM students WHERE student_id = ?", (student_id,))
        student = cursor.fetchone()
        if not student:
            conn.close()
            return False, "Student not found."
        if student[1] == 'Not Eligible':
            conn.close()
            return False, "Student is marked as Not Eligible."

        cursor.execute("SELECT min_cgpa FROM companies WHERE company_id = ?", (company_id,))
        company = cursor.fetchone()
        if not company:
            conn.close()
            return False, "Company not found."
        
        if student[0] < company[0]:
            conn.close()
            return False, f"Ineligible: Student's CGPA ({student[0]}) is lower than the company's requirement ({company[0]})."

        try:
            cursor.execute("""
            INSERT INTO interviews (student_id, company_id, scheduled_at, round, mode, status, result)
            VALUES (?, ?, ?, ?, ?, 'Scheduled', 'Pending')
            """, (student_id, company_id, scheduled_at, round_num, mode))
            conn.commit()
            return True, "Interview scheduled successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def update_interview_result(self, interview_id, status, result, notes=""):
        conn = self._get_connection()
        cursor = conn.cursor()
        
        try:
            # 1. Fetch interview details
            cursor.execute("SELECT student_id, company_id FROM interviews WHERE interview_id = ?", (interview_id,))
            interview = cursor.fetchone()
            if not interview:
                return False, "Interview not found."
            
            student_id, company_id = interview

            # 2. Update interview record
            cursor.execute("""
            UPDATE interviews 
            SET status=?, result=?, notes=?
            WHERE interview_id=?
            """, (status, result, notes, interview_id))

            # 3. If result is 'Selected' and student is not yet placed, record the placement
            if result == 'Selected':
                # Check student's current placement status
                cursor.execute("SELECT status FROM students WHERE student_id = ?", (student_id,))
                student_status = cursor.fetchone()[0]

                if student_status != 'Placed':
                    # Get company package details
                    cursor.execute("SELECT package_lpa, job_role FROM companies WHERE company_id = ?", (company_id,))
                    company = cursor.fetchone()
                    ctc_lpa = company[0] if company else 0
                    job_role = company[1] if company else "Software Engineer"

                    # Add placement record
                    today = date.today().strftime("%Y-%m-%d")
                    cursor.execute("""
                    INSERT OR REPLACE INTO placements (student_id, company_id, offer_date, ctc_lpa, job_role)
                    VALUES (?, ?, ?, ?, ?)
                    """, (student_id, company_id, today, ctc_lpa, job_role))

                    # Update student status to 'Placed'
                    cursor.execute("UPDATE students SET status='Placed' WHERE student_id=?", (student_id,))

            conn.commit()
            return True, "Interview outcome recorded successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    def delete_interview(self, interview_id):
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("DELETE FROM interviews WHERE interview_id = ?", (interview_id,))
            conn.commit()
            if cursor.rowcount == 0:
                return False, "Interview not found."
            return True, "Interview deleted successfully."
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()

    # ── QUEUE MANAGEMENT Drive Methods ───────────────────────────────────────
    def initialize_drive_queue(self, company_id, sort_by_cgpa=True):
        conn = self._get_connection()
        cursor = conn.cursor()
        
        # Fetch pending (Scheduled) interviews for this company
        cursor.execute("""
            SELECT i.interview_id, i.student_id, s.name, s.cgpa, i.round, i.scheduled_at
            FROM interviews i
            JOIN students s ON s.student_id = i.student_id
            WHERE i.company_id = ? AND i.status = 'Scheduled'
        """, (company_id,))
        rows = cursor.fetchall()
        conn.close()

        if sort_by_cgpa:
            # High CGPA candidates get higher priority
            queue = PriorityQueue()
            for r in rows:
                candidate = {
                    'interview_id': r[0],
                    'student_id': r[1],
                    'student_name': r[2],
                    'student_cgpa': r[3],
                    'round': r[4],
                    'scheduled_at': r[5]
                }
                queue.push(candidate, r[3]) # Priority is CGPA
            self.active_queues[company_id] = queue
        else:
            # FIFO Queue based on scheduling date
            queue = Queue()
            # Sort rows by scheduled_at string first
            sorted_rows = sorted(rows, key=lambda x: x[5])
            for r in sorted_rows:
                candidate = {
                    'interview_id': r[0],
                    'student_id': r[1],
                    'student_name': r[2],
                    'student_cgpa': r[3],
                    'round': r[4],
                    'scheduled_at': r[5]
                }
                queue.enqueue(candidate)
            self.active_queues[company_id] = queue

        return self.get_drive_queue_list(company_id)

    def get_drive_queue_list(self, company_id):
        queue = self.active_queues.get(company_id)
        if not queue:
            return []
        return queue.to_list()

    def dequeue_next_candidate(self, company_id):
        queue = self.active_queues.get(company_id)
        if not queue or queue.is_empty():
            return None
        
        if isinstance(queue, PriorityQueue):
            return queue.pop()
        else:
            return queue.dequeue()
