import tkinter as tk
from tkinter import ttk
from tkinter import *

import sys

from style import get_default_font, code_frame_style, code_theme
from painter import Painter


ONE_SECOND = 1000


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

        self.painter = Painter(self)

        # create a proxy for the underlying widget
        self._orig = self._w + "_oupdate_highlightrig"
        self.tk.call("rename", self._w, self._orig)
        self.tk.createcommand(self._w, self._proxy)

        self.event_counter = 0
        self.bind("<<TextModified>>", self._on_change)


    def _proxy(self, command, *args):

        # generate an event if something was added or deleted,
        # or the cursor position changed
        if (args[0] in ("insert", "replace", "delete") or
            args[0:3] == ("mark", "set", "insert") or
            args[0:2] == ("xview", "moveto") or
            args[0:2] == ("xview", "scroll") or
            args[0:2] == ("yview", "moveto") or
            args[0:2] == ("yviewHighlighter", "scroll")
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

        self.tag_configure("keyword", foreground=code_theme['keyword'])
        self.tag_configure("main text", foreground=code_theme['main_font_col'])
        self.tag_configure("declaration", foreground=code_theme['declaration'])
        self.tag_configure("operator", foreground=code_theme['operator'])
        self.tag_configure("type", foreground=code_theme['type'])
        self.tag_configure("comment", foreground=code_theme['comment'])
        self.tag_configure("string", foreground=code_theme['string_literal'])
        self.tag_configure("int", foreground=code_theme['int_literal'])
        self.tag_configure("error", foreground=code_theme['error'])
        self.tag_configure("function", foreground=code_theme['function'])

    def update_highlight(self):
        self.painter.paint()

    def handle_events(self, triggerer_count):
        if triggerer_count != self.event_counter:
            return
        self.update_highlight()

    def _on_change(self, event):
        self.event_counter += 1
        print(self.event_counter)
        self.after(ONE_SECOND, self.handle_events, self.event_counter)

        if (self.event_counter >= sys.maxsize):
            self.event_counter = 0


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
