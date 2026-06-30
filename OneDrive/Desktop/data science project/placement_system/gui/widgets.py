import tkinter as tk
from tkinter import ttk

# Global Colors for Dark Theme
BG_APP = "#0f172a"
BG_PANEL = "#1e293b"
BG_INPUT = "#334155"
BORDER_COLOR = "#475569"
TEXT_COLOR = "#f8fafc"
TEXT_MUTED = "#94a3b8"
ACCENT_COLOR = "#6366f1"
COLOR_PLACED = "#10b981"
COLOR_PENDING = "#f59e0b"
COLOR_REJECTED = "#ef4444"

FONT_MAIN = ("Outfit", 11)
FONT_BOLD = ("Outfit", 11, "bold")
FONT_LARGE = ("Outfit", 18, "bold")
FONT_SMALL = ("Outfit", 9)

def apply_global_styles():
    style = ttk.Style()
    style.theme_use('default')
    
    # Configure Treeview style
    style.configure("Treeview", 
                    background=BG_PANEL, 
                    foreground=TEXT_COLOR, 
                    fieldbackground=BG_PANEL, 
                    rowheight=28,
                    font=FONT_MAIN,
                    borderwidth=0)
    style.map("Treeview", background=[('selected', ACCENT_COLOR)])
    
    style.configure("Treeview.Heading", 
                    background=BG_INPUT, 
                    foreground=TEXT_COLOR, 
                    font=FONT_BOLD,
                    borderwidth=1,
                    relief="flat")
    
    # Labelframe Style
    style.configure("TLabelframe", background=BG_PANEL, foreground=TEXT_COLOR, font=FONT_BOLD, borderwidth=1, relief="solid")
    style.configure("TLabelframe.Label", background=BG_PANEL, foreground=TEXT_COLOR, font=FONT_BOLD)

class StyledButton(tk.Button):
    def __init__(self, parent, text, command=None, bg=ACCENT_COLOR, fg="#ffffff", **kwargs):
        super().__init__(
            parent, 
            text=text, 
            command=command, 
            bg=bg, 
            fg=fg, 
            activebackground=bg, 
            activeforeground=fg,
            relief="flat", 
            font=FONT_BOLD, 
            padx=16, 
            pady=8,
            cursor="hand2",
            **kwargs
        )
        self.bind("<Enter>", self.on_enter)
        self.bind("<Leave>", self.on_leave)
        self.original_bg = bg

    def on_enter(self, e):
        # Slightly lighten/darken color
        self.config(bg=self.adjust_brightness(self.original_bg, 0.85))

    def on_leave(self, e):
        self.config(bg=self.original_bg)

    def adjust_brightness(self, hex_color, factor):
        hex_color = hex_color.lstrip('#')
        r, g, b = int(hex_color[0:2], 16), int(hex_color[2:4], 16), int(hex_color[4:6], 16)
        r = min(255, max(0, int(r * factor)))
        g = min(255, max(0, int(g * factor)))
        b = min(255, max(0, int(b * factor)))
        return f"#{r:02x}{g:02x}{b:02x}"

class StyledEntry(tk.Entry):
    def __init__(self, parent, placeholder="", **kwargs):
        super().__init__(
            parent,
            bg=BG_INPUT,
            fg=TEXT_COLOR,
            insertbackground=TEXT_COLOR,
            relief="flat",
            font=FONT_MAIN,
            highlightbackground=BORDER_COLOR,
            highlightcolor=ACCENT_COLOR,
            highlightthickness=1,
            **kwargs
        )

class TreeviewWrapper(tk.Frame):
    def __init__(self, parent, columns, headings, **kwargs):
        super().__init__(parent, bg=BG_PANEL, **kwargs)
        
        self.tree = ttk.Treeview(self, columns=columns, show="headings", style="Treeview")
        self.vsb = ttk.Scrollbar(self, orient="vertical", command=self.tree.yview)
        self.hsb = ttk.Scrollbar(self, orient="horizontal", command=self.tree.xview)
        
        self.tree.configure(yscrollcommand=self.vsb.set, xscrollcommand=self.hsb.set)
        
        for col, heading in zip(columns, headings):
            self.tree.heading(col, text=heading, anchor="w")
            self.tree.column(col, anchor="w")
            
        self.tree.grid(row=0, column=0, sticky="nsew")
        self.vsb.grid(row=0, column=1, sticky="ns")
        self.hsb.grid(row=1, column=0, sticky="ew")
        
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)

    def get_selected_item(self):
        selected = self.tree.selection()
        if not selected:
            return None
        return self.tree.item(selected[0])['values']

    def clear(self):
        for item in self.tree.get_children():
            self.tree.delete(item)

    def insert(self, values, **kwargs):
        return self.tree.insert("", "end", values=values, **kwargs)
