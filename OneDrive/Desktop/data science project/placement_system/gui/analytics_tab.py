import tkinter as tk
from tkinter import ttk
from placement_system.gui.widgets import (
    BG_APP, BG_PANEL, TEXT_COLOR, TEXT_MUTED, ACCENT_COLOR,
    COLOR_PLACED, FONT_MAIN, FONT_BOLD, FONT_LARGE, FONT_SMALL,
    TreeviewWrapper
)
from placement_system.modules.analytics import AnalyticsEngine

class AnalyticsTab(tk.Frame):
    def __init__(self, parent):
        super().__init__(parent, bg=BG_APP)
        self.analytics = AnalyticsEngine()
        
        self.setup_ui()
        self.refresh()

    def setup_ui(self):
        # 1. Header
        header_frame = tk.Frame(self, bg=BG_APP)
        header_frame.pack(fill="x", padx=20, pady=10)
        
        lbl_title = tk.Label(header_frame, text="Placement Analytics & Reports", font=FONT_LARGE, bg=BG_APP, fg=TEXT_COLOR)
        lbl_title.pack(anchor="w")
        
        lbl_sub = tk.Label(header_frame, text="Generate branch-wise summaries, package evaluations, and custom visuals", font=FONT_MAIN, bg=BG_APP, fg=TEXT_MUTED)
        lbl_sub.pack(anchor="w")

        # Splitter (Left: Branch Table + Highest Packages, Right: Graphical Canvas Chart)
        splitter = tk.Frame(self, bg=BG_APP)
        splitter.pack(fill="both", expand=True, padx=20, pady=10)

        # ── LEFT PANELS: DATA TABLES ─────────────────────────────────────────
        left_panel = tk.Frame(splitter, bg=BG_APP)
        left_panel.pack(side="left", fill="both", expand=True, padx=(0, 10))

        # Branch reports frame
        branch_frame = tk.LabelFrame(left_panel, text=" Branch Placement Reports ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        branch_frame.pack(fill="both", expand=True, pady=(0, 10))
        
        self.table_branches = TreeviewWrapper(
            branch_frame,
            columns=("branch", "total", "placed", "eligible", "rate", "avg_ctc"),
            headings=("Branch", "Total Students", "Placed Count", "Eligible Count", "Placement Rate %", "Avg Package")
        )
        self.table_branches.pack(fill="both", expand=True, padx=10, pady=10)

        # Highest packages frame
        packages_frame = tk.LabelFrame(left_panel, text=" Highest CTC Offers Secured ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        packages_frame.pack(fill="both", expand=True, pady=(10, 0))

        self.table_packages = TreeviewWrapper(
            packages_frame,
            columns=("student", "branch", "company", "ctc", "role", "date"),
            headings=("Student Name", "Branch", "Company Name", "CTC (LPA)", "Job Role", "Offer Date")
        )
        self.table_packages.pack(fill="both", expand=True, padx=10, pady=10)

        # ── RIGHT PANEL: GRAPHICS CANVAS ─────────────────────────────────────
        right_panel = tk.LabelFrame(splitter, text=" Placement Rate Visualisation ", bg=BG_PANEL, fg=TEXT_COLOR, font=FONT_BOLD, bd=1, relief="solid")
        right_panel.pack(side="right", fill="both", expand=False, padx=(10, 0))

        # Label notes
        tk.Label(right_panel, text="Engineering Branch vs Placement Rate (%)", font=FONT_BOLD, bg=BG_PANEL, fg=TEXT_COLOR, pady=10).pack()

        # Canvas drawing component
        self.canvas = tk.Canvas(right_panel, width=380, height=360, bg=BG_PANEL, highlightthickness=0)
        self.canvas.pack(padx=20, pady=10, fill="both", expand=True)

    def draw_chart(self, branch_stats):
        self.canvas.delete("all")
        
        if not branch_stats:
            self.canvas.create_text(190, 180, text="No placement records to chart.", fill=TEXT_MUTED, font=FONT_MAIN)
            return

        # Chart bounding box parameters
        chart_x = 50
        chart_y = 40
        chart_width = 300
        chart_height = 250
        
        # Draw Y-Axis markings & grid lines (0%, 25%, 50%, 75%, 100%)
        for i in range(5):
            pct = i * 25
            # Y coord maps 100% -> chart_y, 0% -> chart_y + chart_height
            y = (chart_y + chart_height) - (pct / 100.0) * chart_height
            # Draw line
            self.canvas.create_line(chart_x, y, chart_x + chart_width, y, fill="#334155", dash=(4, 4))
            # Text label
            self.canvas.create_text(chart_x - 15, y, text=f"{pct}%", fill=TEXT_MUTED, font=FONT_SMALL, anchor="e")

        # Draw axis lines
        self.canvas.create_line(chart_x, chart_y, chart_x, chart_y + chart_height, fill=TEXT_COLOR, width=2) # Y axis
        self.canvas.create_line(chart_x, chart_y + chart_height, chart_x + chart_width, chart_y + chart_height, fill=TEXT_COLOR, width=2) # X axis

        # Draw bars
        num_branches = len(branch_stats)
        bar_gap = 18
        avail_width = chart_width - (bar_gap * (num_branches + 1))
        bar_width = avail_width / num_branches if num_branches > 0 else 40

        for idx, stat in enumerate(branch_stats):
            branch_name = stat['branch']
            rate = stat['placement_rate']

            # Calculate positions
            x1 = chart_x + bar_gap + idx * (bar_width + bar_gap)
            x2 = x1 + bar_width
            y2 = chart_y + chart_height
            
            # Map rate (0-100) to height
            height = (rate / 100.0) * chart_height
            y1 = y2 - height

            # Draw bar rectangle
            # Create a vertical gradient look by filling with accent and drawing outline
            self.canvas.create_rectangle(x1, y1, x2, y2, fill=ACCENT_COLOR, outline="#818cf8", width=1.5)

            # Draw rate percentage text label above bar
            self.canvas.create_text(x1 + (bar_width / 2), y1 - 10, text=f"{rate}%", fill=TEXT_COLOR, font=FONT_SMALL)

            # Draw branch label below X axis
            self.canvas.create_text(x1 + (bar_width / 2), y2 + 15, text=branch_name, fill=TEXT_COLOR, font=FONT_BOLD)

    def refresh(self):
        # 1. Update Branch Report
        self.table_branches.clear()
        branch_stats = self.analytics.get_branch_reports()
        for b in branch_stats:
            self.table_branches.insert((
                b['branch'],
                b['total_students'],
                b['placed_students'],
                b['eligible_students'],
                f"{b['placement_rate']}%",
                f"{b['avg_ctc_lpa']} LPA" if b['placed_students'] > 0 else '0.0 LPA'
            ))

        # 2. Update Highest Packages
        self.table_packages.clear()
        packages = self.analytics.get_highest_packages()
        for p in packages:
            self.table_packages.insert((
                p['student_name'],
                p['student_branch'],
                p['company_name'],
                f"{p['ctc_lpa']} LPA",
                p['job_role'],
                p['offer_date']
            ))

        # 3. Draw visual charts
        self.draw_chart(branch_stats)
