import tkinter as tk
from tkinter import messagebox, ttk
from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, BG_INPUT, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    COLOR_PLACED, COLOR_REJECTED, FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    StyledButton, StyledEntry, TreeviewWrapper
)
from placement_system.modules.interview_manager import InterviewManager
from placement_system.modules.company_manager import CompanyManager

class InterviewsTab(tk.Frame):
    def __init__(self, parent, on_refresh_callback=None):
        super().__init__(parent, bg=BG_APP)
        self.interview_mgr = InterviewManager()
        self.company_mgr = CompanyManager()
        self.on_refresh_callback = on_refresh_callback
        
        self.active_candidate = None
        self.current_company_id = None
        
        self.setup_ui()
        self.load_all_interviews()
        self.load_companies_cb()

    def setup_ui(self):
        # 1. Header
        header_frame = tk.Frame(self, bg=BG_APP)
        header_frame.pack(fill="x", padx=20, pady=10)
        
        lbl_title = tk.Label(header_frame, text="Interview Board & Queue Scheduler", font=FONT_LARGE, bg=BG_APP, fg=TEXT_COLOR)
        lbl_title.pack(anchor="w")
        
        lbl_sub = tk.Label(header_frame, text="Schedule candidate interviews and run live recruitment drive queues", font=FONT_MAIN, bg=BG_APP, fg=TEXT_MUTED)
        lbl_sub.pack(anchor="w")

        # Main Splitter (Left: Schedule list, Right: Live Queue Drive Panel)
        splitter = tk.Frame(self, bg=BG_APP)
        splitter.pack(fill="both", expand=True, padx=20, pady=5)

        # ── LEFT PANEL: ALL INTERVIEWS SCHEDULE ──────────────────────────────
        left_panel = tk.LabelFrame(splitter, text=" All Scheduled Interviews ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        left_panel.pack(side="left", fill="both", expand=True, padx=(0, 10))
        
        # Sub control bar
        left_ctrl = tk.Frame(left_panel, bg=BG_PANEL)
        left_ctrl.pack(fill="x", padx=10, pady=5)
        
        btn_eval = StyledButton(left_ctrl, text="Record Outcome", command=self.open_eval_dialog, bg="#3b82f6")
        btn_eval.pack(side="left", padx=5)

        btn_cancel = StyledButton(left_ctrl, text="Cancel Schedule", command=self.delete_interview, bg=COLOR_REJECTED)
        btn_cancel.pack(side="left", padx=5)

        self.table = TreeviewWrapper(
            left_panel,
            columns=("id", "student", "company", "date", "round", "mode", "status", "result"),
            headings=("ID", "Candidate Name", "Company", "Time", "Round", "Mode", "Status", "Result")
        )
        self.table.pack(fill="both", expand=True, padx=10, pady=10)

        # ── RIGHT PANEL: LIVE INTERVIEW DRIVE ────────────────────────────────
        right_panel = tk.LabelFrame(splitter, text=" Live Drive Queue Scheduler ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        right_panel.pack(side="right", fill="both", expand=True, padx=(10, 0))

        # Dropdown to select company drive
        drive_ctrl = tk.Frame(right_panel, bg=BG_PANEL, padx=10, pady=10)
        drive_ctrl.pack(fill="x")
        
        tk.Label(drive_ctrl, text="Drive Partner:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR).grid(row=0, column=0, sticky="w", pady=2)
        self.cb_company = ttk.Combobox(drive_ctrl, font=FONT_MAIN, state="readonly", width=18)
        self.cb_company.grid(row=0, column=1, sticky="w", pady=2, padx=5)

        # Queue type select
        self.queue_var = tk.StringVar(value="Priority")
        tk.Label(drive_ctrl, text="Queue Sort:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR).grid(row=1, column=0, sticky="w", pady=2)
        
        rb_frame = tk.Frame(drive_ctrl, bg=BG_PANEL)
        rb_frame.grid(row=1, column=1, sticky="w", pady=2, padx=5)
        
        tk.Radiobutton(rb_frame, text="Priority (CGPA)", variable=self.queue_var, value="Priority", bg=BG_PANEL, fg=TEXT_COLOR, selectcolor=BG_PANEL, activebackground=BG_PANEL, activeforeground=TEXT_COLOR, font=FONT_SMALL).pack(side="left")
        tk.Radiobutton(rb_frame, text="FIFO (Schedule)", variable=self.queue_var, value="FIFO", bg=BG_PANEL, fg=TEXT_COLOR, selectcolor=BG_PANEL, activebackground=BG_PANEL, activeforeground=TEXT_COLOR, font=FONT_SMALL).pack(side="left", padx=5)

        btn_load_drive = StyledButton(drive_ctrl, text="Load Queue", command=self.load_drive_queue, bg=ACCENT_COLOR)
        btn_load_drive.grid(row=0, column=2, rowspan=2, padx=10, sticky="nsew")

        # Queue lists frame
        q_layout_frame = tk.Frame(right_panel, bg=BG_PANEL)
        q_layout_frame.pack(fill="both", expand=True, padx=10, pady=5)

        # Live Queue visual list
        list_frame = tk.Frame(q_layout_frame, bg=BG_PANEL)
        list_frame.pack(side="left", fill="both", expand=True, padx=(0, 5))
        
        tk.Label(list_frame, text="Pending Queue:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_MUTED).pack(anchor="w")
        self.lst_queue = tk.Listbox(list_frame, bg=BG_INPUT, fg=TEXT_COLOR, font=FONT_MAIN, selectbackground=ACCENT_COLOR, bd=0, highlightthickness=1, highlightbackground="#475569")
        self.lst_queue.config(highlightcolor=ACCENT_COLOR)
        self.lst_queue.pack(fill="both", expand=True, pady=4)

        # Active Interview details
        active_frame = tk.Frame(q_layout_frame, bg=BG_PANEL, width=220)
        active_frame.pack(side="right", fill="both", expand=True, padx=(5, 0))
        active_frame.pack_propagate(False)

        tk.Label(active_frame, text="Current Candidate:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_MUTED).pack(anchor="w")
        
        # Display Box for Active Candidate
        self.active_box = tk.Frame(active_frame, bg=BG_INPUT, bd=1, relief="solid", highlightthickness=1, highlightbackground="#475569")
        self.active_box.pack(fill="both", expand=True, pady=4)
        
        self.lbl_active_name = tk.Label(self.active_box, text="[No Active Candidate]", font=FONT_BOLD, bg=BG_INPUT, fg=TEXT_COLOR)
        self.lbl_active_name.pack(pady=(15, 2))
        
        self.lbl_active_meta = tk.Label(self.active_box, text="ID / CGPA details", font=FONT_SMALL, bg=BG_INPUT, fg=TEXT_MUTED)
        self.lbl_active_meta.pack(pady=2)

        # Feedback input
        tk.Label(self.active_box, text="Feedback & Decision Notes:", font=FONT_BOLD, bg=BG_INPUT, fg=TEXT_COLOR).pack(anchor="w", padx=10, pady=(10, 2))
        self.txt_feedback = tk.Text(self.active_box, height=3, bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_SMALL, relief="flat", bd=0)
        self.txt_feedback.pack(fill="x", padx=10, pady=2)

        # Action button row
        act_btn_frame = tk.Frame(self.active_box, bg=BG_INPUT)
        act_btn_frame.pack(fill="x", side="bottom", pady=10, padx=10)

        self.btn_pass = StyledButton(act_btn_frame, text="Select", command=lambda: self.evaluate_active("Selected"), bg=COLOR_PLACED)
        self.btn_pass.pack(side="left", fill="x", expand=True, padx=(0, 2))
        self.btn_pass.config(state="disabled")

        self.btn_fail = StyledButton(act_btn_frame, text="Reject", command=lambda: self.evaluate_active("Rejected"), bg=COLOR_REJECTED)
        self.btn_fail.pack(side="left", fill="x", expand=True, padx=(2, 2))
        self.btn_fail.config(state="disabled")

        self.btn_next = StyledButton(act_btn_frame, text="Next", command=self.dequeue_next, bg="#eab308")
        self.btn_next.pack(side="right", fill="x", expand=True, padx=(2, 0))
        self.btn_next.config(state="disabled")

    def load_all_interviews(self):
        self.table.clear()
        interviews = self.interview_mgr.get_all_interviews()
        for i in interviews:
            self.table.insert((
                i['interview_id'],
                i['student_name'],
                i['company_name'],
                i['scheduled_at'],
                f"Round {i['round']}",
                i['mode'],
                i['status'],
                i['result']
            ))

    def load_companies_cb(self):
        companies = self.company_mgr.get_all_companies()
        self.company_map = {f"{c['name']} (ID: {c['company_id']})": c['company_id'] for c in companies}
        self.cb_company['values'] = list(self.company_map.keys())
        if companies:
            self.cb_company.current(0)

    def delete_interview(self):
        selected = self.table.get_selected_item()
        if not selected:
            messagebox.showwarning("Selection Required", "Please select an interview record to cancel.")
            return
        
        interview_id = int(selected[0])
        student_name = selected[1]
        
        if messagebox.askyesno("Confirm Cancellation", f"Cancel scheduled interview for {student_name}?"):
            success, msg = self.interview_mgr.delete_interview(interview_id)
            if success:
                messagebox.showinfo("Success", msg)
                self.load_all_interviews()
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Error", msg)

    # ── LIVE QUEUE FUNCTIONS ─────────────────────────────────────────────────
    def load_drive_queue(self):
        selected_text = self.cb_company.get()
        if not selected_text:
            return

        company_id = self.company_map[selected_text]
        self.current_company_id = company_id
        
        sort_by_cgpa = (self.queue_var.get() == "Priority")
        
        # Initialize queue in manager
        queue_list = self.interview_mgr.initialize_drive_queue(company_id, sort_by_cgpa=sort_by_cgpa)
        
        self.refresh_queue_listbox(queue_list)
        
        # Reset active candidate
        self.active_candidate = None
        self.lbl_active_name.config(text="[No Active Candidate]")
        self.lbl_active_meta.config(text="ID / CGPA details")
        self.txt_feedback.delete("1.0", "end")
        
        # Enable Dequeue/Next button if items exist
        if queue_list:
            self.btn_next.config(state="normal")
            self.btn_pass.config(state="disabled")
            self.btn_fail.config(state="disabled")
            messagebox.showinfo("Drive Loaded", f"Interview Drive loaded with {len(queue_list)} pending candidates.")
        else:
            self.btn_next.config(state="disabled")
            self.btn_pass.config(state="disabled")
            self.btn_fail.config(state="disabled")
            messagebox.showinfo("Drive Empty", "No pending (Scheduled) interviews found for this company.")

    def refresh_queue_listbox(self, queue_list):
        self.lst_queue.delete(0, tk.END)
        for idx, item in enumerate(queue_list):
            self.lst_queue.insert(tk.END, f"{idx+1}. {item['student_name']} (CGPA: {item['student_cgpa']:.2f}) - R{item['round']}")

    def dequeue_next(self):
        if not self.current_company_id:
            return
        
        candidate = self.interview_mgr.dequeue_next_candidate(self.current_company_id)
        if not candidate:
            self.active_candidate = None
            self.lbl_active_name.config(text="[No Pending Candidates]")
            self.lbl_active_meta.config(text="")
            self.btn_next.config(state="disabled")
            self.btn_pass.config(state="disabled")
            self.btn_fail.config(state="disabled")
            return
        
        self.active_candidate = candidate
        
        # Display candidate
        self.lbl_active_name.config(text=candidate['student_name'])
        self.lbl_active_meta.config(text=f"ID: {candidate['student_id']}  |  CGPA: {candidate['student_cgpa']:.2f}  |  Round {candidate['round']}")
        self.txt_feedback.delete("1.0", "end")
        
        # Update buttons
        self.btn_pass.config(state="normal")
        self.btn_fail.config(state="normal")
        
        # Check if list is empty to disable next button
        rem_queue = self.interview_mgr.get_drive_queue_list(self.current_company_id)
        self.refresh_queue_listbox(rem_queue)
        if not rem_queue:
            self.btn_next.config(state="disabled")

    def evaluate_active(self, result):
        if not self.active_candidate:
            return
        
        notes = self.txt_feedback.get("1.0", "end-1c").strip()
        interview_id = self.active_candidate['interview_id']
        
        success, msg = self.interview_mgr.update_interview_result(
            interview_id=interview_id,
            status="Completed",
            result=result,
            notes=notes
        )
        
        if success:
            res_icon = "🎉" if result == "Selected" else "❌"
            messagebox.showinfo("Result Logged", f"{res_icon} Evaluation recorded for {self.active_candidate['student_name']}.\nResult: {result}")
            
            # Reset active candidate panel
            self.active_candidate = None
            self.lbl_active_name.config(text="[Evaluated - Load Next]")
            self.lbl_active_meta.config(text="")
            self.txt_feedback.delete("1.0", "end")
            self.btn_pass.config(state="disabled")
            self.btn_fail.config(state="disabled")
            
            # Recheck queue
            rem_queue = self.interview_mgr.get_drive_queue_list(self.current_company_id)
            if rem_queue:
                self.btn_next.config(state="normal")
            
            # Refresh layouts
            self.load_all_interviews()
            if self.on_refresh_callback:
                self.on_refresh_callback()
        else:
            messagebox.showerror("Error", msg)

    def open_eval_dialog(self):
        selected = self.table.get_selected_item()
        if not selected:
            messagebox.showwarning("Selection Required", "Please select an interview record from the table to evaluate.")
            return

        interview_id = int(selected[0])
        student_name = selected[1]
        company_name = selected[2]
        round_name = selected[4]
        current_status = selected[6]
        current_result = selected[7]

        # Popup evaluation window
        dialog = tk.Toplevel(self)
        dialog.title("Log Evaluation Results")
        dialog.geometry("400x350")
        dialog.configure(bg=BG_PANEL)
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        lbl_head = tk.Label(dialog, text="Interview Evaluation", font=FONT_LARGE, bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_head.pack(pady=15)

        form = tk.Frame(dialog, bg=BG_PANEL)
        form.pack(padx=30, fill="both", expand=True)

        tk.Label(form, text=f"Candidate: {student_name}", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=0, column=0, columnspan=2, sticky="w", pady=4)
        tk.Label(form, text=f"Company: {company_name} | {round_name}", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=1, column=0, columnspan=2, sticky="w", pady=4)

        # Status drop down
        tk.Label(form, text="Status:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=2, column=0, sticky="w", pady=6)
        status_cb = ttk.Combobox(form, values=("Scheduled", "In Progress", "Completed", "Cancelled"), font=FONT_MAIN, state="readonly", width=18)
        status_cb.grid(row=2, column=1, sticky="w", pady=6, padx=10)
        status_cb.set(current_status)

        # Result drop down
        tk.Label(form, text="Outcome:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=3, column=0, sticky="w", pady=6)
        result_cb = ttk.Combobox(form, values=("Pending", "Selected", "Rejected"), font=FONT_MAIN, state="readonly", width=18)
        result_cb.grid(row=3, column=1, sticky="w", pady=6, padx=10)
        result_cb.set(current_result if current_result else "Pending")

        # Feedback
        tk.Label(form, text="Notes:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").grid(row=4, column=0, sticky="nw", pady=6)
        txt_notes = tk.Text(form, width=20, height=3, font=FONT_MAIN, bg=BG_INPUT, fg=TEXT_COLOR, relief="flat")
        txt_notes.grid(row=4, column=1, sticky="w", pady=6, padx=10)

        # Load existing feedback
        interviews = self.interview_mgr.get_all_interviews()
        match = next((i for i in interviews if i['interview_id'] == interview_id), None)
        if match and match['notes']:
            txt_notes.insert("1.0", match['notes'])

        def save():
            status = status_cb.get()
            result = result_cb.get()
            notes = txt_notes.get("1.0", "end-1c").strip()
            
            success, msg = self.interview_mgr.update_interview_result(interview_id, status, result, notes)
            if success:
                messagebox.showinfo("Success", msg, parent=dialog)
                dialog.destroy()
                self.load_all_interviews()
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Error", msg, parent=dialog)

        btn_save = StyledButton(dialog, text="Submit Evaluation", command=save, bg=ACCENT_COLOR)
        btn_save.pack(pady=20)

    def refresh(self):
        self.load_all_interviews()
        self.load_companies_cb()
