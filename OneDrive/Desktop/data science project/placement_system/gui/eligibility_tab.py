import tkinter as tk
from tkinter import messagebox, ttk
from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    StyledButton, TreeviewWrapper
)
from placement_system.modules.student_manager import StudentManager
from placement_system.modules.company_manager import CompanyManager
from placement_system.modules.interview_manager import InterviewManager

class EligibilityTab(tk.Frame):
    def __init__(self, parent, on_refresh_callback=None):
        super().__init__(parent, bg=BG_APP)
        self.student_mgr = StudentManager()
        self.company_mgr = CompanyManager()
        self.interview_mgr = InterviewManager()
        self.on_refresh_callback = on_refresh_callback
        
        self.setup_ui()
        self.load_companies()

    def setup_ui(self):
        # 1. Header
        header_frame = tk.Frame(self, bg=BG_APP)
        header_frame.pack(fill="x", padx=20, pady=15)
        
        lbl_title = tk.Label(header_frame, text="Eligibility Engine", font=FONT_LARGE, bg=BG_APP, fg=TEXT_COLOR)
        lbl_title.pack(anchor="w")
        
        lbl_sub = tk.Label(header_frame, text="Identify qualified candidates matching corporate recruitment profiles", font=FONT_MAIN, bg=BG_APP, fg=TEXT_MUTED)
        lbl_sub.pack(anchor="w")

        # 2. Match Criteria Select Area
        criteria_frame = tk.LabelFrame(self, text=" Select Recruiting Partner & Criteria ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        criteria_frame.pack(fill="x", padx=20, pady=10)
        
        inner_frame = tk.Frame(criteria_frame, bg=BG_PANEL, padx=15, pady=15)
        inner_frame.pack(fill="x")

        lbl_select = tk.Label(inner_frame, text="Target Recruiter:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_select.grid(row=0, column=0, sticky="w", pady=5)
        
        self.cb_company = ttk.Combobox(inner_frame, font=FONT_MAIN, state="readonly", width=35)
        self.cb_company.grid(row=0, column=1, sticky="w", pady=5, padx=10)
        self.cb_company.bind("<<ComboboxSelected>>", self.on_company_selected)

        # Criteria text displays
        self.lbl_cgpa_val = tk.Label(inner_frame, text="Min CGPA Requirement: N/A", font=FONT_MAIN, bg=BG_PANEL, fg=TEXT_MUTED)
        self.lbl_cgpa_val.grid(row=0, column=2, sticky="w", pady=5, padx=20)

        self.lbl_skills_val = tk.Label(inner_frame, text="Required Core Skills: N/A", font=FONT_MAIN, bg=BG_PANEL, fg=TEXT_MUTED)
        self.lbl_skills_val.grid(row=1, column=2, columnspan=2, sticky="w", pady=5, padx=20)

        # 3. Eligible matches list
        matches_frame = tk.LabelFrame(self, text=" Eligible Matching Candidates ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        matches_frame.pack(fill="both", expand=True, padx=20, pady=10)
        
        control_sub_frame = tk.Frame(matches_frame, bg=BG_PANEL)
        control_sub_frame.pack(fill="x", padx=10, pady=5)
        
        lbl_note = tk.Label(control_sub_frame, text="Students who meet or exceed the minimum CGPA are listed along with their skill overlap match percentage.", font=FONT_SMALL, bg=BG_PANEL, fg=TEXT_MUTED)
        lbl_note.pack(side="left", pady=5)

        btn_queue = StyledButton(control_sub_frame, text="Schedule Selected Candidate", command=self.schedule_selected, bg=ACCENT_COLOR)
        btn_queue.pack(side="right", padx=5)

        self.table = TreeviewWrapper(
            matches_frame,
            columns=("id", "name", "branch", "cgpa", "skills", "match_pct", "status"),
            headings=("Student ID", "Candidate Name", "Branch", "CGPA", "Skills Matrix", "Skill Match %", "Placement Status")
        )
        self.table.pack(fill="both", expand=True, padx=10, pady=10)

    def load_companies(self):
        companies = self.company_mgr.get_all_companies()
        self.companies_map = {f"{c['name']} (ID: {c['company_id']})": c['company_id'] for c in companies}
        
        self.cb_company['values'] = list(self.companies_map.keys())
        if companies:
            self.cb_company.current(0)
            self.on_company_selected(None)

    def on_company_selected(self, e):
        selected_text = self.cb_company.get()
        if not selected_text:
            return
        
        company_id = self.companies_map[selected_text]
        company = self.company_mgr.get_company_by_id(company_id)
        
        if company:
            self.lbl_cgpa_val.config(text=f"Min CGPA Requirement: {company['min_cgpa']:.2f}")
            self.lbl_skills_val.config(text=f"Required Core Skills: {company['required_skills'] or 'None'}")
            
            # Fetch and render eligible matches
            self.load_matches(company_id)

    def load_matches(self, company_id):
        self.table.clear()
        matches = self.student_mgr.get_eligible_students_for_company(company_id)
        
        for m in matches:
            self.table.insert((
                m['student_id'],
                m['name'],
                m['branch'],
                m['cgpa'],
                m['skills'] or 'None',
                f"{m['match_percentage']}%",
                m['status']
            ))

    def schedule_selected(self):
        selected_text = self.cb_company.get()
        if not selected_text:
            messagebox.showwarning("Select Company", "Please select a target company first.")
            return

        selected_row = self.table.get_selected_item()
        if not selected_row:
            messagebox.showwarning("Selection Required", "Please select a candidate from the match table to schedule.")
            return

        student_id = selected_row[0]
        student_name = selected_row[1]
        company_id = self.companies_map[selected_text]
        company_name = selected_text.split(" (ID:")[0]

        # Open short scheduling dialog
        dialog = tk.Toplevel(self)
        dialog.title("Schedule Interview Drive")
        dialog.geometry("400x300")
        dialog.configure(bg=BG_PANEL)
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        lbl_head = tk.Label(dialog, text="Drive Details", font=FONT_LARGE, bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_head.pack(pady=15)

        form_frame = tk.Frame(dialog, bg=BG_PANEL)
        form_frame.pack(padx=20, fill="both", expand=True)

        # Labels
        tk.Label(form_frame, text=f"Candidate: {student_name}", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=0, column=0, columnspan=2, sticky="w", pady=5)
        tk.Label(form_frame, text=f"Company: {company_name}", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=1, column=0, columnspan=2, sticky="w", pady=5)

        tk.Label(form_frame, text="Date (YYYY-MM-DD)*:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=2, column=0, sticky="w", pady=5)
        ent_date = StyledEntry(form_frame, width=18)
        ent_date.grid(row=2, column=1, sticky="w", pady=5, padx=10)
        # Prefill default drive date
        company = self.company_mgr.get_company_by_id(company_id)
        if company and company['drive_date']:
            ent_date.insert(0, company['drive_date'])

        tk.Label(form_frame, text="Interview Mode:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=3, column=0, sticky="w", pady=5)
        cb_mode = ttk.Combobox(form_frame, values=("In-Person", "Virtual", "Telephonic"), font=FONT_MAIN, state="readonly", width=16)
        cb_mode.grid(row=3, column=1, sticky="w", pady=5, padx=10)
        cb_mode.set("In-Person")

        tk.Label(form_frame, text="Round (Number):", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=4, column=0, sticky="w", pady=5)
        ent_round = StyledEntry(form_frame, width=18)
        ent_round.grid(row=4, column=1, sticky="w", pady=5, padx=10)
        ent_round.insert(0, "1")

        def submit():
            dt = ent_date.get().strip()
            mode = cb_mode.get()
            rnd_str = ent_round.get().strip()

            if not dt or not rnd_str:
                messagebox.showerror("Validation Error", "Please specify the date and round number.", parent=dialog)
                return

            try:
                rnd = int(rnd_str)
                if rnd <= 0:
                    raise ValueError
            except ValueError:
                messagebox.showerror("Validation Error", "Round must be a positive integer.", parent=dialog)
                return

            success, msg = self.interview_mgr.schedule_interview(student_id, company_id, dt, rnd, mode)
            if success:
                messagebox.showinfo("Success", msg, parent=dialog)
                dialog.destroy()
                self.load_matches(company_id)
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Error", msg, parent=dialog)

        btn_submit = StyledButton(dialog, text="Confirm Schedule", command=submit, bg=ACCENT_COLOR)
        btn_submit.pack(pady=20)

    def refresh(self):
        self.load_companies()
