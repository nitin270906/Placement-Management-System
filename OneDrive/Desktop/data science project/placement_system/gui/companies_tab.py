import tkinter as tk
from tkinter import messagebox, ttk
from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    COLOR_REJECTED, FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    StyledButton, StyledEntry, TreeviewWrapper
)
from placement_system.modules.company_manager import CompanyManager

class CompaniesTab(tk.Frame):
    def __init__(self, parent, on_refresh_callback=None):
        super().__init__(parent, bg=BG_APP)
        self.company_mgr = CompanyManager()
        self.on_refresh_callback = on_refresh_callback
        
        self.setup_ui()
        self.load_companies()

    def setup_ui(self):
        # 1. Header
        header_frame = tk.Frame(self, bg=BG_APP)
        header_frame.pack(fill="x", padx=20, pady=15)
        
        lbl_title = tk.Label(header_frame, text="Recruiters Directory", font=FONT_LARGE, bg=BG_APP, fg=TEXT_COLOR)
        lbl_title.pack(anchor="w")
        
        lbl_sub = tk.Label(header_frame, text="Manage company job criteria, eligibility limits, packages, and drive dates", font=FONT_MAIN, bg=BG_APP, fg=TEXT_MUTED)
        lbl_sub.pack(anchor="w")

        # 2. Control bar
        control_frame = tk.Frame(self, bg=BG_APP)
        control_frame.pack(fill="x", padx=20, pady=10)

        # Search box
        lbl_search = tk.Label(control_frame, text="Search Recruiter:", font=FONT_BOLD, bg=BG_APP, fg=TEXT_COLOR)
        lbl_search.pack(side="left", padx=(0, 10))
        
        self.ent_search = StyledEntry(control_frame, width=30)
        self.ent_search.pack(side="left", padx=(0, 20))
        self.ent_search.bind("<KeyRelease>", self.on_search)

        # Buttons
        btn_add = StyledButton(control_frame, text="Add Recruiter", command=self.add_company_dialog, bg=ACCENT_COLOR)
        btn_add.pack(side="right", padx=5)

        btn_edit = StyledButton(control_frame, text="Edit Details", command=self.edit_company_dialog, bg="#3b82f6")
        btn_edit.pack(side="right", padx=5)

        btn_delete = StyledButton(control_frame, text="Delete Company", command=self.delete_company, bg=COLOR_REJECTED)
        btn_delete.pack(side="right", padx=5)

        # 3. Grid Table view
        grid_frame = tk.Frame(self, bg=BG_PANEL, bd=1, relief="solid")
        grid_frame.pack(fill="both", expand=True, padx=20, pady=10)

        self.table = TreeviewWrapper(
            grid_frame,
            columns=("id", "name", "sector", "min_cgpa", "skills", "package", "role", "location", "drive_date"),
            headings=("ID", "Company Name", "Sector", "Min CGPA", "Required Skills", "Package (LPA)", "Job Role", "Location", "Drive Date")
        )
        self.table.pack(fill="both", expand=True, padx=10, pady=10)

    def load_companies(self, query=None):
        self.table.clear()
        companies = self.company_mgr.get_all_companies()
        
        # Filter if search query exists
        if query:
            q = query.lower()
            companies = [c for c in companies if q in c['name'].lower() or q in c['sector'].lower() or q in c['job_role'].lower()]

        for c in companies:
            self.table.insert((
                c['company_id'],
                c['name'],
                c['sector'] or 'N/A',
                c['min_cgpa'],
                c['required_skills'] or 'None',
                f"{c['package_lpa']} LPA" if c['package_lpa'] else 'N/A',
                c['job_role'] or 'N/A',
                c['location'] or 'N/A',
                c['drive_date'] or 'N/A'
            ))

    def on_search(self, e):
        query = self.ent_search.get()
        self.load_companies(query)

    def delete_company(self):
        selected = self.table.get_selected_item()
        if not selected:
            messagebox.showwarning("Selection Required", "Please select a company from the table to delete.")
            return

        company_id = int(selected[0])
        name = selected[1]
        
        if messagebox.askyesno("Confirm Deletion", f"Are you sure you want to permanently delete {name}? This will remove all their scheduled interviews and placements!"):
            success, msg = self.company_mgr.delete_company(company_id)
            if success:
                messagebox.showinfo("Success", msg)
                self.load_companies()
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Error", msg)

    def add_company_dialog(self):
        self.company_dialog(None)

    def edit_company_dialog(self):
        selected = self.table.get_selected_item()
        if not selected:
            messagebox.showwarning("Selection Required", "Please select a company from the table to edit.")
            return
        self.company_dialog(int(selected[0]))

    def company_dialog(self, company_id=None):
        dialog = tk.Toplevel(self)
        dialog.title("Edit Company Details" if company_id else "Register Corporate Partner")
        dialog.geometry("500x580")
        dialog.configure(bg=BG_PANEL)
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        # Labels & Entries
        lbl_head = tk.Label(dialog, text="Corporate Drive Parameters", font=FONT_LARGE, bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_head.pack(pady=15)

        fields_frame = tk.Frame(dialog, bg=BG_PANEL)
        fields_frame.pack(padx=30, fill="both", expand=True)

        fields = [
            ("Company Name*:", "ent_name"),
            ("Sector Sector:", "ent_sector"),
            ("Min CGPA Required*:", "ent_min_cgpa"),
            ("Required Skills (comma-separated):", "ent_skills"),
            ("CTC Package (LPA)*:", "ent_package"),
            ("Target Job Role*:", "ent_role"),
            ("Job Location:", "ent_location"),
            ("Drive Date (YYYY-MM-DD)*:", "ent_date"),
        ]

        entries = {}
        for idx, (label, name) in enumerate(fields):
            lbl = tk.Label(fields_frame, text=label, font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w")
            lbl.grid(row=idx, column=0, sticky="w", pady=4)
            
            ent = StyledEntry(fields_frame, width=32)
            ent.grid(row=idx, column=1, sticky="w", pady=4, padx=10)
            entries[name] = ent

        # Description text field
        lbl_desc = tk.Label(fields_frame, text="Description:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w")
        lbl_desc.grid(row=8, column=0, sticky="nw", pady=4)
        
        txt_desc = tk.Text(fields_frame, width=32, height=4, font=FONT_MAIN, bg="#334155", fg=TEXT_COLOR, relief="flat")
        txt_desc.grid(row=8, column=1, sticky="w", pady=4, padx=10)

        # Disable ID entry on edits
        if company_id:
            c_data = self.company_mgr.get_company_by_id(company_id)
            if c_data:
                entries["ent_name"].insert(0, c_data["name"])
                entries["ent_sector"].insert(0, c_data["sector"] or "")
                entries["ent_min_cgpa"].insert(0, str(c_data["min_cgpa"]))
                entries["ent_skills"].insert(0, c_data["required_skills"] or "")
                entries["ent_package"].insert(0, str(c_data["package_lpa"]) if c_data["package_lpa"] else "")
                entries["ent_role"].insert(0, c_data["job_role"] or "")
                entries["ent_location"].insert(0, c_data["location"] or "")
                entries["ent_date"].insert(0, c_data["drive_date"] or "")
                txt_desc.insert("1.0", c_data["description"] or "")

        def save():
            name = entries["ent_name"].get().strip()
            sector = entries["ent_sector"].get().strip()
            min_cgpa_str = entries["ent_min_cgpa"].get().strip()
            skills = entries["ent_skills"].get().strip()
            package_str = entries["ent_package"].get().strip()
            role = entries["ent_role"].get().strip()
            location = entries["ent_location"].get().strip()
            date_str = entries["ent_date"].get().strip()
            desc = txt_desc.get("1.0", "end-1c").strip()

            if not name or not min_cgpa_str or not package_str or not role or not date_str:
                messagebox.showerror("Validation Error", "All fields marked with (*) are required.", parent=dialog)
                return

            try:
                min_cgpa = float(min_cgpa_str)
                if min_cgpa < 0 or min_cgpa > 10:
                    raise ValueError
            except ValueError:
                messagebox.showerror("Validation Error", "Min CGPA must be a valid float value between 0.0 and 10.0.", parent=dialog)
                return

            try:
                package = float(package_str)
                if package < 0:
                    raise ValueError
            except ValueError:
                messagebox.showerror("Validation Error", "Package (LPA) must be a positive float value.", parent=dialog)
                return

            if company_id:
                # Update
                success, msg = self.company_mgr.update_company(company_id, name, sector, min_cgpa, skills, package, role, location, desc, date_str)
            else:
                # Add
                success, msg = self.company_mgr.add_company(name, sector, min_cgpa, skills, package, role, location, desc, date_str)

            if success:
                messagebox.showinfo("Success", msg, parent=dialog)
                dialog.destroy()
                self.load_companies()
                if self.on_refresh_callback:
                    self.on_refresh_callback()
            else:
                messagebox.showerror("Database Error", msg, parent=dialog)

        btn_save = StyledButton(dialog, text="Save Recruiter Parameters", command=save, bg=ACCENT_COLOR)
        btn_save.pack(pady=20)
