from tkinter import *
from tkinter import ttk
from tkinter import font as tk_font
from style import get_smaller_font, common_text_style

class EventLog(ttk.Frame):

    def __init__(self, *args, **kwargs):
        kwargs['style'] = "CodeFrame.TFrame"
        ttk.Frame.__init__(self, *args, **kwargs)

        self.label = Label(self, text="Event Log")
        self.label.pack(side="top", fill="both", expand=True)

        self.text = Text(self)
        # self.text = Text(self, width=400, height=400)

        self.scrollbar_v = Scrollbar(self, orient=VERTICAL, command=self.text.yview)
        self.text.configure(yscrollcommand=self.scrollbar_v.set)
        self.text.configure(state='disabled')

        # Text style
        self.text.configure(**common_text_style)
        tab = tk_font.Font(font=self.text['font']).measure('  ')
        self.text.config(tabs=tab)
        self.text['font'] = get_smaller_font()

        self.scrollbar_v.pack(side="right", fill="y")
        self.text.pack(side="top", fill="both", expand=True)

        self.text.configure(state='normal')
        self.text.insert("end", "Lorem Ipsum Lorem Huipsum Assa Massa")
        self.text.configure(state='disabled')
