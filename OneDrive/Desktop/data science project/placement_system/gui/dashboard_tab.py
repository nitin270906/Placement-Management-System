import tkinter as tk
from tkinter import ttk
from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    COLOR_PLACED, FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    TreeviewWrapper
)
from placement_system.modules.analytics import AnalyticsEngine
from placement_system.modules.interview_manager import InterviewManager

class DashboardTab(tk.Frame):
    def __init__(self, parent):
        super().__init__(parent, bg=BG_APP)
        self.analytics = AnalyticsEngine()
        self.interview_mgr = InterviewManager()
        
        self.setup_ui()
        self.refresh()

    def setup_ui(self):
        # 1. Header
        header_frame = tk.Frame(self, bg=BG_APP)
        header_frame.pack(fill="x", padx=20, pady=15)
        
        lbl_title = tk.Label(header_frame, text="System Summary Dashboard", font=FONT_LARGE, bg=BG_APP, fg=TEXT_COLOR)
        lbl_title.pack(anchor="w")
        
        lbl_sub = tk.Label(header_frame, text="Real-time recruitment statistics and upcoming drives", font=FONT_MAIN, bg=BG_APP, fg=TEXT_MUTED)
        lbl_sub.pack(anchor="w")

        # 2. Stats Grid Row
        stats_frame = tk.Frame(self, bg=BG_APP)
        stats_frame.pack(fill="x", padx=20, pady=10)
        
        # We will create 4 stat cards
        self.card_placement_rate = self.create_stat_card(stats_frame, "Placement Rate", "0.0%", ACCENT_COLOR)
        self.card_placement_rate.grid(row=0, column=0, padx=(0, 10), sticky="nsew")
        
        self.card_placed = self.create_stat_card(stats_frame, "Placed Candidates", "0", COLOR_PLACED)
        self.card_placed.grid(row=0, column=1, padx=10, sticky="nsew")
        
        self.card_companies = self.create_stat_card(stats_frame, "Recruiter Partners", "0", "#3b82f6")
        self.card_companies.grid(row=0, column=2, padx=10, sticky="nsew")
        
        self.card_avg_package = self.create_stat_card(stats_frame, "Average Package", "0.0 LPA", "#8b5cf6")
        self.card_avg_package.grid(row=0, column=3, padx=(10, 0), sticky="nsew")
        
        stats_frame.grid_columnconfigure(0, weight=1)
        stats_frame.grid_columnconfigure(1, weight=1)
        stats_frame.grid_columnconfigure(2, weight=1)
        stats_frame.grid_columnconfigure(3, weight=1)

        # 3. Feeds Grid (Interviews + Placements)
        feeds_frame = tk.Frame(self, bg=BG_APP)
        feeds_frame.pack(fill="both", expand=True, padx=20, pady=20)
        
        # Left feed: Upcoming interviews
        left_feed = tk.LabelFrame(feeds_frame, text=" Upcoming Drives / Interviews ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        left_feed.pack(side="left", fill="both", expand=True, padx=(0, 10))
        
        self.table_interviews = TreeviewWrapper(
            left_feed,
            columns=("student", "company", "date", "round", "mode", "status"),
            headings=("Candidate", "Company", "Scheduled Date", "Round", "Mode", "Status")
        )
        self.table_interviews.pack(fill="both", expand=True, padx=10, pady=10)
        
        # Right feed: Recent Placements
        right_feed = tk.LabelFrame(feeds_frame, text=" Recent Placements Secured ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        right_feed.pack(side="right", fill="both", expand=True, padx=(10, 0))
        
        self.table_placements = TreeviewWrapper(
            right_feed,
            columns=("student", "company", "ctc", "role", "date"),
            headings=("Placed Candidate", "Company", "CTC (LPA)", "Role", "Offer Date")
        )
        self.table_placements.pack(fill="both", expand=True, padx=10, pady=10)

    def create_stat_card(self, parent, title, initial_value, color):
        card = tk.Frame(parent, bg=BG_PANEL, bd=1, relief="solid", highlightthickness=2, highlightbackground=BG_PANEL)
        
        # Left border coloring bar (accent indicator)
        accent_bar = tk.Frame(card, bg=color, width=6)
        accent_bar.pack(side="left", fill="y")
        
        info_frame = tk.Frame(card, bg=BG_PANEL, padx=15, pady=15)
        info_frame.pack(side="left", fill="both", expand=True)
        
        lbl_title = tk.Label(info_frame, text=title, font=FONT_SMALL, bg=BG_PANEL, fg=TEXT_MUTED)
        lbl_title.pack(anchor="w")
        
        lbl_val = tk.Label(info_frame, text=initial_value, font=FONT_LARGE, bg=BG_PANEL, fg=TEXT_COLOR)
        lbl_val.pack(anchor="w", pady=(4, 0))
        
        # Save reference to label to update it later
        card.lbl_val = lbl_val
        return card

    def refresh(self):
        # 1. Update stats
        stats = self.analytics.get_summary_stats()
        self.card_placement_rate.lbl_val.config(text=f"{stats['placement_rate']}%")
        self.card_placed.lbl_val.config(text=str(stats['placed_students']))
        self.card_companies.lbl_val.config(text=str(stats['total_companies']))
        self.card_avg_package.lbl_val.config(text=f"{stats['avg_ctc_lpa']} LPA")

        # 2. Populate upcoming interviews (top 10)
        self.table_interviews.clear()
        interviews = self.interview_mgr.get_all_interviews()
        # Sort or filter for pending/scheduled
        upcoming = [i for i in interviews if i['status'] in ('Scheduled', 'In Progress')][:10]
        for i in upcoming:
            self.table_interviews.insert((
                i['student_name'],
                i['company_name'],
                i['scheduled_at'],
                f"Round {i['round']}",
                i['mode'],
                i['status']
            ))

        # 3. Populate placements (top 10)
        self.table_placements.clear()
        placements = self.analytics.get_highest_packages()[:10]
        for p in placements:
            self.table_placements.insert((
                p['student_name'],
                p['company_name'],
                f"{p['ctc_lpa']} LPA",
                p['job_role'],
                p['offer_date']
            ))
