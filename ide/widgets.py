import tkinter as tk
from tkinter import *
from settings import get_default_font


class TextLineNumbers(tk.Canvas):
    def __init__(self, *args, **kwargs):
        tk.Canvas.__init__(self, *args, **kwargs)
        self.textwidget = None

    def attach(self, text_widget):
        self.textwidget = text_widget

    def redraw(self, *args):
        '''redraw line numbers'''
        self.delete("all")

        index = self.textwidget.index("@0,0")
        while True :
            dline = self.textwidget.dlineinfo(index)
            if dline is None: break
            y = dline[1]
            linenum = str(index).split(".")[0]
            self.create_text(2, y, anchor="nw", text=linenum, font=get_default_font())
            index = self.textwidget.index("%s+1line" % index)


class CodeText(tk.Text):
    def __init__(self, *args, **kwargs):
        tk.Text.__init__(self, *args, **kwargs)

        # create a proxy for the underlying widget
        self._orig = self._w + "_orig"
        self.tk.call("rename", self._w, self._orig)
        self.tk.createcommand(self._w, self._proxy)

    def _proxy(self, *args):
        # let the actual widget perform the requested action
        cmd = (self._orig,) + args
        result = self.tk.call(cmd)

        # generate an event if something was added or deleted,
        # or the cursor position changed
        if (args[0] in ("insert", "replace", "delete") or
            args[0:3] == ("mark", "set", "insert") or
            args[0:2] == ("xview", "moveto") or
            args[0:2] == ("xview", "scroll") or
            args[0:2] == ("yview", "moveto") or
            args[0:2] == ("yview", "scroll")
        ):
            self.event_generate("<<Change>>", when="tail")

        # return what the actual widget returned
        return result


class CodeFrame(tk.Frame):
    def __init__(self, *args, **kwargs):
        tk.Frame.__init__(self, *args, **kwargs)
        self.text = CodeText(self, width=400, height=400, wrap=NONE)

        self.text.tag_configure("bigfont", font=("Helvetica", "24", "bold"))
        self.scrollbar_v = Scrollbar(self, orient=VERTICAL, command=self.text.yview)
        self.scrollbar_h = Scrollbar(self, orient=HORIZONTAL, command=self.text.xview)
        self.scrollbar_h.pack(side="bottom", fill="x")
        self.text.configure(yscrollcommand=self.scrollbar_v.set)
        self.text.configure(xscrollcommand=self.scrollbar_h.set)

        self.text['font'] = get_default_font()

        self.linenumbers = TextLineNumbers(self, width=30)
        self.linenumbers.attach(self.text)

        self.scrollbar_v.pack(side="right", fill="y")
        self.linenumbers.pack(side="left", fill="y")
        self.text.pack(side="right", fill="both", expand=True)

        self.text.bind("<<Change>>", self._on_change)
        self.text.bind("<Configure>", self._on_change)

        # self.linenumbers['font'] = default_font

    def _on_change(self, event):
        self.linenumbers.redraw()
