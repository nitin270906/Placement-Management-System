import tkinter as tk
from tkinter import messagebox, ttk
import os
import sys

# Add project root to python path to avoid import errors when running directly
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))

from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, BG_INPUT, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    StyledButton, StyledEntry, apply_global_styles
)
from placement_system.gui.dashboard_tab import DashboardTab
from placement_system.gui.students_tab import StudentsTab
from placement_system.gui.companies_tab import CompaniesTab
from placement_system.gui.eligibility_tab import EligibilityTab
from placement_system.gui.interviews_tab import InterviewsTab
from placement_system.gui.analytics_tab import AnalyticsTab

class PlacementSystemApp(tk.Tk):
    def __init__(self):
        super().__init__()
        
        self.title("Placement Management System")
        self.geometry("1100x700")
        self.configure(bg=BG_APP)
        self.resizable(True, True)

        apply_global_styles()

        # Application state
        self.logged_in = False
        
        # Setup views
        self.container = tk.Frame(self, bg=BG_APP)
        self.container.pack(fill="both", expand=True)

        self.show_login_screen()

    # ── LOGIN SCREEN ─────────────────────────────────────────────────────────
    def show_login_screen(self):
        # Clear container
        for widget in self.container.winfo_children():
            widget.destroy()

        login_frame = tk.Frame(self.container, bg=BG_PANEL, bd=1, relief="solid")
        login_frame.place(relx=0.5, rely=0.5, anchor="center", width=400, height=360)

        # Header Title
        lbl_brand = tk.Label(login_frame, text="CareerPath", font=("Outfit", 24, "bold"), bg=BG_PANEL, fg=ACCENT_COLOR)
        lbl_brand.pack(pady=(30, 2))
        
        lbl_sub = tk.Label(login_frame, text="PLACEMENT CELL PORTAL", font=FONT_SMALL, bg=BG_PANEL, fg=TEXT_MUTED)
        lbl_sub.pack(pady=(0, 20))

        # Credentials Fields
        fields_frame = tk.Frame(login_frame, bg=BG_PANEL)
        fields_frame.pack(fill="x", padx=40)

        tk.Label(fields_frame, text="Username:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").pack(fill="x", pady=(5, 2))
        self.ent_user = StyledEntry(fields_frame)
        self.ent_user.pack(fill="x", ipady=4, pady=(0, 10))
        self.ent_user.insert(0, "admin")  # Autofill for easy use

        tk.Label(fields_frame, text="Password:", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, anchor="w").pack(fill="x", pady=(5, 2))
        self.ent_pass = StyledEntry(fields_frame, show="*")
        self.ent_pass.pack(fill="x", ipady=4, pady=(0, 20))
        self.ent_pass.insert(0, "admin123")  # Autofill for easy use
        
        self.ent_pass.bind("<Return>", lambda e: self.attempt_login())

        # Sign In Button
        btn_login = StyledButton(login_frame, text="Sign In to Cell", command=self.attempt_login, bg=ACCENT_COLOR)
        btn_login.pack(fill="x", padx=40, pady=10)

    def attempt_login(self):
        username = self.ent_user.get().strip()
        password = self.ent_pass.get().strip()

        if username == "admin" and password == "admin123":
            self.logged_in = True
            self.show_main_app()
        else:
            messagebox.showerror("Authentication Failed", "Invalid credentials. Please enter 'admin' and 'admin123'.")

    # ── MAIN SYSTEM SHELL ────────────────────────────────────────────────────
    def show_main_app(self):
        # Clear container
        for widget in self.container.winfo_children():
            widget.destroy()

        # Top Bar Branding Frame
        top_bar = tk.Frame(self.container, bg=BG_PANEL, height=60)
        top_bar.pack(fill="x", side="top")
        top_bar.pack_propagate(False)

        # Brand Logo
        lbl_logo = tk.Label(top_bar, text="🎓 CareerPath Portal", font=("Outfit", 16, "bold"), bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_logo.pack(side="left", padx=20)

        # User Info & Logout
        btn_logout = StyledButton(top_bar, text="Sign Out", command=self.logout, bg="#ef4444")
        btn_logout.pack(side="right", padx=20, pady=8)

        lbl_user = tk.Label(top_bar, text="Active Session: Administrator", font=FONT_MAIN, bg=BG_PANEL, fg=TEXT_MUTED)
        lbl_user.pack(side="right", padx=10)

        # Notebook tabs panel
        self.notebook = ttk.Notebook(self.container)
        self.notebook.pack(fill="both", expand=True, padx=10, pady=10)
        
        # Instantiate tabs
        self.tab_dashboard = DashboardTab(self.notebook)
        self.tab_students = StudentsTab(self.notebook, on_refresh_callback=self.refresh_all_tabs)
        self.tab_companies = CompaniesTab(self.notebook, on_refresh_callback=self.refresh_all_tabs)
        self.tab_eligibility = EligibilityTab(self.notebook, on_refresh_callback=self.refresh_all_tabs)
        self.tab_interviews = InterviewsTab(self.notebook, on_refresh_callback=self.refresh_all_tabs)
        self.tab_analytics = AnalyticsTab(self.notebook)

        # Add tabs to notebook
        self.notebook.add(self.tab_dashboard, text=" Dashboard ")
        self.notebook.add(self.tab_students, text=" Students Directory ")
        self.notebook.add(self.tab_companies, text=" Recruiter Partners ")
        self.notebook.add(self.tab_eligibility, text=" Eligibility Engine ")
        self.notebook.add(self.tab_interviews, text=" Interview Board ")
        self.notebook.add(self.tab_analytics, text=" Analytics Reports ")

        # Bind tab change event to refresh layouts automatically
        self.notebook.bind("<<NotebookTabChanged>>", self.on_tab_changed)

    def on_tab_changed(self, event):
        selected_index = self.notebook.index(self.notebook.select())
        if selected_index == 0:
            self.tab_dashboard.refresh()
        elif selected_index == 1:
            self.tab_students.load_students()
        elif selected_index == 2:
            self.tab_companies.load_companies()
        elif selected_index == 3:
            self.tab_eligibility.refresh()
        elif selected_index == 4:
            self.tab_interviews.refresh()
        elif selected_index == 5:
            self.tab_analytics.refresh()

    def refresh_all_tabs(self):
        # Refresh current data bindings on other panels
        self.tab_dashboard.refresh()
        self.tab_students.load_students()
        self.tab_companies.load_companies()
        self.tab_eligibility.refresh()
        self.tab_interviews.refresh()
        self.tab_analytics.refresh()

    def logout(self):
        if messagebox.askyesno("Sign Out", "Are you sure you want to end the active session?"):
            self.logged_in = False
            self.show_login_screen()

if __name__ == "__main__":
    app = PlacementSystemApp()
    app.mainloop()
