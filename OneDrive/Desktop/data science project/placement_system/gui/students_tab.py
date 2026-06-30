import tkinter as tk
from tkinter import messagebox, ttk
from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    COLOR_REJECTED, FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    StyledButton, StyledEntry, TreeviewWrapper
)
from placement_system.modules.student_manager import StudentManager

class StudentsTab(tk.Frame):
    def __init__(self, parent, on_refresh_callback=None):
        super().__init__(parent, bg=BG_APP)
        self.student_mgr = StudentManager()
        self.on_refresh_callback = on_refresh_callback
        
        self.setup_ui()
        self.load_students()

    def setup_ui(self):
        # 1. Header
        header_frame = tk.Frame(self, bg=BG_APP)
        header_frame.pack(fill="x", padx=20, pady=15)
        
        lbl_title = tk.Label(header_frame, text="Students Directory", font=FONT_LARGE, bg=BG_APP, fg=TEXT_COLOR)
        lbl_title.pack(anchor="w")
        
        lbl_sub = tk.Label(header_frame, text="Register and manage student profiles, branches, CGPA, and credentials", font=FONT_MAIN, bg=BG_APP, fg=TEXT_MUTED)
        lbl_sub.pack(anchor="w")

        # 2. Control bar
        control_frame = tk.Frame(self, bg=BG_APP)
        control_frame.pack(fill="x", padx=20, pady=10)

        # Search box
        lbl_search = tk.Label(control_frame, text="Search Candidates:", font=FONT_BOLD, bg=BG_APP, fg=TEXT_COLOR)
        lbl_search.pack(side="left", padx=(0, 10))
        
        self.ent_search = StyledEntry(control_frame, width=30)
        self.ent_search.pack(side="left", padx=(0, 20))
        self.ent_search.bind("<KeyRelease>", self.on_search)

        # Buttons
        btn_add = StyledButton(control_frame, text="Register Student", command=self.add_student_dialog, bg=ACCENT_COLOR)
        btn_add.pack(side="right", padx=5)

        btn_edit = StyledButton(control_frame, text="Edit Profile", command=self.edit_student_dialog, bg="#3b82f6")
        btn_edit.pack(side="right", padx=5)

        btn_delete = StyledButton(control_frame, text="Delete Profile", command=self.delete_student, bg=COLOR_REJECTED)
        btn_delete.pack(side="right", padx=5)

        # 3. Grid Table view
        grid_frame = tk.Frame(self, bg=BG_PANEL, bd=1, relief="solid")
        grid_frame.pack(fill="both", expand=True, padx=20, pady=10)

        self.table = TreeviewWrapper(
            grid_frame,
            columns=("id", "name", "branch", "cgpa", "skills", "certifications", "projects", "status"),
            headings=("ID", "Full Name", "Branch", "CGPA", "Skills", "Certifications", "Projects", "Status")
        )
        self.table.pack(fill="both", expand=True, padx=10, pady=10)

    def load_students(self, query=None):
        self.table.clear()
        students = self.student_mgr.get_all_students()
        
        # Filter if search query exists
        if query:
            q = query.lower()
            students = [s for s in students if q in s['name'].lower() or q in s['student_id'].lower() or q in s['branch'].lower()]

        for s in students:
            self.table.insert((
                s['student_id'],
                s['name'],
                s['branch'],
                s['cgpa'],
                s['skills'],
                s['certifications'],
                s['projects'],
                s['status']
            ))

    def on_search(self, e):
        query = self.ent_search.get()
        self.load_students(query)

    def delete_student(self):
        selected = self.table.get_selected_item()
        if not selected:
            messagebox.showwarning("Selection Required", "Please select a student record from the table to delete.")
            return

        student_id = selected[0]
        name = selected[1]
        
        if messagebox.askyesno("Confirm Deletion", f"Are you sure you want to permanently remove {name} ({student_id})?"):
            success, msg = self.student_mgr.delete_student(student_id)
            if success:
                messagebox.showinfo("Success", msg)
                self.load_students()
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Error", msg)

    def add_student_dialog(self):
        self.student_dialog(None)

    def edit_student_dialog(self):
        selected = self.table.get_selected_item()
        if not selected:
            messagebox.showwarning("Selection Required", "Please select a student record from the table to edit.")
            return
        self.student_dialog(selected[0])

    def student_dialog(self, student_id=None):
        dialog = tk.Toplevel(self)
        dialog.title("Edit Student Profile" if student_id else "Register Student")
        dialog.geometry("500x550")
        dialog.configure(bg=BG_PANEL)
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        # Labels & Entries
        lbl_head = tk.Label(dialog, text="Student Profile Data", font=FONT_LARGE, bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_head.pack(pady=15)

        fields_frame = tk.Frame(dialog, bg=BG_PANEL)
        fields_frame.pack(padx=30, fill="both", expand=True)

        fields = [
            ("Student ID*:", "ent_id"),
            ("Full Name*:", "ent_name"),
            ("Branch (e.g. CSE)*:", "ent_branch"),
            ("CGPA (0 - 10)*:", "ent_cgpa"),
            ("Skills (comma-separated):", "ent_skills"),
            ("Certifications:", "ent_certs"),
            ("Projects:", "ent_projects"),
            ("Email:", "ent_email"),
            ("Phone:", "ent_phone")
        ]

        entries = {}
        for idx, (label, name) in enumerate(fields):
            lbl = tk.Label(fields_frame, text=label, font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w")
            lbl.grid(row=idx, column=0, sticky="w", pady=4)
            
            ent = StyledEntry(fields_frame, width=32)
            ent.grid(row=idx, column=1, sticky="w", pady=4, padx=10)
            entries[name] = ent

        # Status drop down (for edits)
        lbl_status = tk.Label(fields_frame, text="Placement Status:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w")
        lbl_status.grid(row=9, column=0, sticky="w", pady=4)
        
        status_cb = ttk.Combobox(fields_frame, values=("Not Placed", "Placed", "Not Eligible"), font=FONT_MAIN, state="readonly", width=30)
        status_cb.grid(row=9, column=1, sticky="w", pady=4, padx=10)
        status_cb.set("Not Placed")

        # Disable ID entry on edits
        if student_id:
            s_data = self.student_mgr.get_student_by_id(student_id)
            if s_data:
                entries["ent_id"].insert(0, s_data["student_id"])
                entries["ent_id"].config(state="disabled")
                entries["ent_name"].insert(0, s_data["name"])
                entries["ent_branch"].insert(0, s_data["branch"])
                entries["ent_cgpa"].insert(0, str(s_data["cgpa"]))
                entries["ent_skills"].insert(0, s_data["skills"] or "")
                entries["ent_certs"].insert(0, s_data["certifications"] or "")
                entries["ent_projects"].insert(0, s_data["projects"] or "")
                entries["ent_email"].insert(0, s_data["email"] or "")
                entries["ent_phone"].insert(0, s_data["phone"] or "")
                status_cb.set(s_data["status"])

        def save():
            s_id = entries["ent_id"].get().strip()
            name = entries["ent_name"].get().strip()
            branch = entries["ent_branch"].get().strip()
            cgpa_str = entries["ent_cgpa"].get().strip()
            skills = entries["ent_skills"].get().strip()
            certs = entries["ent_certs"].get().strip()
            projs = entries["ent_projects"].get().strip()
            email = entries["ent_email"].get().strip()
            phone = entries["ent_phone"].get().strip()
            status = status_cb.get()

            if not s_id or not name or not branch or not cgpa_str:
                messagebox.showerror("Validation Error", "All fields marked with (*) are required.", parent=dialog)
                return

            try:
                cgpa = float(cgpa_str)
                if cgpa < 0 or cgpa > 10:
                    raise ValueError
            except ValueError:
                messagebox.showerror("Validation Error", "CGPA must be a valid float value between 0.0 and 10.0.", parent=dialog)
                return

            if student_id:
                # Update
                success, msg = self.student_mgr.update_student(student_id, name, branch, cgpa, skills, certs, projs, email, phone, status)
            else:
                # Add
                success, msg = self.student_mgr.add_student(s_id, name, branch, cgpa, skills, certs, projs, email, phone, status)

            if success:
                messagebox.showinfo("Success", msg, parent=dialog)
                dialog.destroy()
                self.load_students()
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Database Error", msg, parent=dialog)

        btn_save = StyledButton(dialog, text="Save Student Profile", command=save, bg=ACCENT_COLOR)
        btn_save.pack(pady=20)
