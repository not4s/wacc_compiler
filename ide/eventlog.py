from tkinter import *
from tkinter import ttk
from tkinter import font as tk_font
from antlr4.error.ErrorListener import ErrorListener
from style import get_smaller_font, common_text_style


class ErrorData:
    def __init__(self, line, charPositionInLine, msg):
        self.line = line
        self.charPositionInLine = charPositionInLine
        self.msg = msg

    def char_pos(self):
        return f"{self.line}.{self.charPositionInLine}"


class SyntaxErrorListener(ErrorListener):

    def __init__(self, errors):
        super().__init__()
        self.errors = errors

    def syntaxError(self, recognizer, offendingSymbol, line, charPositionInLine, msg, e):
        self.errors.append(ErrorData(line, charPositionInLine, msg))


class EventLog(ttk.Frame):
    NO_SYNTAX_ERRORS_MSG = "No Syntax Errors have been detected."

    def __init__(self, *args, **kwargs):
        kwargs['style'] = "CodeFrame.TFrame"
        ttk.Frame.__init__(self, *args, **kwargs)

        self.label = Label(self, text="Event Log")
        self.label.pack(side="top", fill="x")

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

    def log(self, errors):
        if not errors:
            summary = self.NO_SYNTAX_ERRORS_MSG
        else:
            summary = ""
            for er in errors:
                summary += f"- Line {er.line}:{er.charPositionInLine} {er.msg}\n\n"

        self.text.configure(state='normal')
        self.text.delete('1.0', 'end')
        self.text.insert('1.0', summary)
        self.text.configure(state='disabled')
