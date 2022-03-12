import tkinter as tk
from tkinter import ttk
from tkinter import *
from style import get_default_font, code_frame_style, code_theme


class TextLineNumbers(tk.Canvas):

    H_PADDING = 2

    def __init__(self, *args, **kwargs):
        tk.Canvas.__init__(self, *args, **kwargs)
        self.textwidget = None
        self.no_digits = 2
        self.adjust_width()

    def adjust_width(self):
        width = 2 * self.H_PADDING + self.no_digits * get_default_font()['size']
        self.config(width=width)

    def attach(self, text_widget):
        self.textwidget = text_widget

    def redraw(self, *args):
        '''redraw line numbers'''
        self.delete("all")
        index = self.textwidget.index("@0,0")
        while True:
            dline = self.textwidget.dlineinfo(index)
            if dline is None:
                break
            y = dline[1]
            linenum = str(index).split(".")[0]

            # Adjusting the width of the linenumbers pane
            if self.no_digits != len(linenum):
                self.no_digits = len(linenum)
                self.adjust_width()

            self.create_text(self.H_PADDING, y, anchor="nw", justify='left',
                             text=linenum, font=get_default_font())
            index = self.textwidget.index("%s+1line" % index)


class CodeText(tk.Text):
    def __init__(self, *args, **kwargs):
        tk.Text.__init__(self, *args, **kwargs)

        # create a proxy for the underlying widget
        self._orig = self._w + "_orig"
        self.tk.call("rename", self._w, self._orig)
        self.tk.createcommand(self._w, self._proxy)

    def _proxy(self, command, *args):

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

        # avoid error when copying
        if (command == 'get' and
            (args[0] == 'sel.first' and args[1] == 'sel.last') and
            not self.tag_ranges('sel')
        ):
            return

        # avoid error when deleting
        if (command == 'delete' and
            (args[0] == 'sel.first' and args[1] == 'sel.last') and
            not self.tag_ranges('sel')
        ):
            return

        # let the actual widget perform the requested action
        cmd = (self._orig, command) + args
        result = self.tk.call(cmd)

        if command in ('insert', 'delete', 'replace'):
            self.event_generate('<<TextModified>>')

        # return what the actual widget returned
        return result

    def configure_syntax_highlight(self):
        self.configure(**code_frame_style)

        text.tag_add("keyword")
        text.tag_add("main text")
        text.tag_add("comment")
        text.tag_add("string")
        text.tag_add("int")
        text.tag_add("error")
        text.tag_add("function")

        text.tag_config("keyword", foreground=code_theme['keyword'])
        text.tag_config("main text", foreground=code_theme['main_font_col'])
        text.tag_config("comment", foreground=code_theme['comment'])
        text.tag_config("string", foreground=code_theme['string_literal'])
        text.tag_config("int", foreground=code_theme['int_literal'])
        text.tag_config("error", foreground=code_theme['error'])
        text.tag_config("function", foreground=code_theme['function'])


class CodeFrame(ttk.Frame):

    def __init__(self, *args, **kwargs):
        kwargs['style'] = "CodeFrame.TFrame"
        ttk.Frame.__init__(self, *args, **kwargs)
        self.text = CodeText(self, width=400, height=400, wrap=NONE)

        self.scrollbar_v = Scrollbar(self, orient=VERTICAL, command=self.text.yview)
        self.scrollbar_h = Scrollbar(self, orient=HORIZONTAL, command=self.text.xview)
        self.scrollbar_h.pack(side="bottom", fill="x")
        self.text.configure(yscrollcommand=self.scrollbar_v.set)
        self.text.configure(xscrollcommand=self.scrollbar_h.set)

        self.text.configure_syntax_highlight()

        self.text['font'] = get_default_font()

        self.linenumbers = TextLineNumbers(self)
        self.linenumbers.attach(self.text)

        self.scrollbar_v.pack(side="right", fill="y")
        self.linenumbers.pack(side="left", fill="y")
        self.text.pack(side="right", fill="both", expand=True)

        self.text.bind("<<Change>>", self._on_change)
        self.text.bind("<Configure>", self._on_change)

    def _on_change(self, event):
        self.linenumbers.redraw()
