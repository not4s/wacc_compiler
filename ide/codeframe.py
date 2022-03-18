import tkinter as tk
from tkinter import ttk
from tkinter import *
from tkinter import font as tk_font
from PIL import ImageTk

import sys

from style import get_default_font, common_text_style, code_theme
from painter import Painter


ONE_SECOND = 1000  # milliseconds


class Bulb(tk.Label):
    ''' Widget which acts like yellow light bulb in IntelliJ IDEA.
    It is place next to an error region and it displays a relevant
    error message on mouse hover

    The technical name is Bulb, but the appearance design is a broken heart '''

    DISTANCE_FROM_CHAR = 20
    HOVER_LABEL_OFFSET = 36
    PADX_OFFSET = common_text_style['padx']
    PADY_OFFSET = common_text_style['pady']

    def __init__(self, error, *args, **kwargs):
        tk.Label.__init__(self, *args, **kwargs)
        self.error = error
        self.text = args[0]
        self.frame_x = 0
        self.frame_y = 0

        self.image = PhotoImage(file="broken_heart.png")
        self.configure(image=self.image, bg=common_text_style['background'])

        self.hover_label = None
        self.bind("<Enter>", self._hover)
        self.bind("<Leave>", self._hover_leave)

    def redraw(self):
        ''' Change position when user scrolls '''
        char_pos = self.error.char_pos()
        character = self.text.get(char_pos)
        try:
            x, y, width, height = self.text.bbox(char_pos)
        except TypeError:
            self.hide()
            return

        # Coords of char center relative to the top left corner of code text
        self.frame_x = x + self.PADX_OFFSET - width
        self.frame_y = y + self.DISTANCE_FROM_CHAR + self.PADY_OFFSET
        self.place_configure(x=self.frame_x, y=self.frame_y)

    def hide(self):
        self.place_configure(x=-200, y=-200)

    def _hover(self, event):
        self.hover_label = Label(
            self.text, text=self.error.msg, bd=1, relief='sunken',
            anchor='e', justify='left', wraplength=300,
            padx=5, pady=5
        )
        self.hover_label.place(x=self.frame_x + self.HOVER_LABEL_OFFSET, y=self.frame_y)

    def _hover_leave(self, event):
        self.hover_label.destroy()
        self.hover_label = None


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
        self.bulbs = []

        # create a proxy for the underlying widget
        self._orig = self._w + "_orig"
        self.tk.call("rename", self._w, self._orig)
        self.tk.createcommand(self._w, self._proxy)

        self.text_type_counter = 0
        self.event_delete('<<Copy>>', '<Control-c>')
        self.bind("<<TextModified>>", self._on_change)
        self.bind('<Control-c>', self._copy_clipboard)
        self.bind("<<CutLineCommand>>", self._cut_current_line)
        self.bind("<<CursorLineUpdate>>", self._highlight_current_line)

    def _proxy(self, command, *args):

        # generate an event if something was added or deleted or the cursor position changed
        if (args[0] in ("insert", "replace", "delete", "scroll", "moveto") or
            args[0:3] == ("mark", "set", "insert") or
            args[0:2] == ("xview", "moveto") or
            args[0:2] == ("xview", "scroll") or
            args[0:2] == ("yview", "moveto") or
            args[0:2] == ("yviewHighlighter", "scroll")
        ):
            self.event_generate("<<Change>>", when="tail")

        # Triggering update of current line highlight
        if ('set' in args or command == "see") and 'insert' in args:
            self.event_generate("<<CursorLineUpdate>>")

        # Handling copy and cut commands (^C and ^X)
        if args[0] == 'sel.first' and args[1] == 'sel.last':
            if not self.tag_ranges('sel'):
                if command == 'delete':
                    self.event_generate('<<CutLineCommand>>')
                return

        # let the actual widget perform the requested action
        cmd = (self._orig, command) + args
        result = self.tk.call(cmd)

        # Some common updates of a text
        if command in ('insert', 'delete', 'replace'):
            self.event_generate('<<TextModified>>')
            self.event_generate('<<Change>>')

        # return what the actual widget returned
        return result

    def configure_style(self):
        self.configure(**common_text_style)

        # Setting tab size to 2 spaces
        tab = tk_font.Font(font=self['font']).measure('  ')
        self.config(tabs=tab)

        # Current line highlight and selection style
        self.tag_configure("current_line", background=code_theme['current_line'])
        self.tag_raise("sel")

        # Syntax highlight
        self.tag_configure("keyword", foreground=code_theme['keyword'])
        self.tag_configure("attribute", foreground=code_theme['attribute'])
        self.tag_configure("main text", foreground=code_theme['main_font_col'])
        self.tag_configure("declaration", foreground=code_theme['declaration'])
        self.tag_configure("operator", foreground=code_theme['operator'])
        self.tag_configure("type", foreground=code_theme['type'])
        self.tag_configure("comment", foreground=code_theme['comment'])
        self.tag_configure("string", foreground=code_theme['string_literal'])
        self.tag_configure("int", foreground=code_theme['int_literal'])
        self.tag_configure("error", background=code_theme['error'], foreground='#ffffff')
        self.tag_configure("function", foreground=code_theme['function'])

    def update_highlight(self):
        self.painter.paint()

    def clear_error_bulbs(self):
        for bulb in self.bulbs:
            bulb.destroy()
        self.bulbs = []

    def add_error_bulb(self, error):
        bulb = Bulb(error, self)
        bulb.place(x=0, y=0)
        bulb.redraw()
        self.bulbs.append(bulb)

    def handle_events(self, triggerer_count):
        ''' Calls syntax painting only if user didn't edit text for more than one second '''
        if triggerer_count != self.text_type_counter:
            return
        self.update_highlight()

    def _on_change(self, event):
        self.clear_error_bulbs()

        self.text_type_counter += 1
        self.after(ONE_SECOND, self.handle_events, self.text_type_counter)

        if (self.text_type_counter >= sys.maxsize):
            self.text_type_counter = 0

    def _highlight_current_line(self, event):
        self.tag_remove("current_line", '1.0', "end")
        self.tag_add("current_line", "insert linestart", "insert lineend+1c")

    def _copy_clipboard(self, event):
        try:
            copied_text = self.selection_get()
        except:
            # Copy entire line if selection is empty
            copied_text = self.get("insert linestart", "insert lineend+1c")

        self.clipboard_clear()
        self.clipboard_append(copied_text)

    def _cut_current_line(self, event):
        self.clipboard_clear()
        line = self.get("insert linestart", "insert lineend+1c")
        self.clipboard_append(line)

        self.delete("insert linestart", "insert lineend+1c")

        self.event_generate("<<CursorLineUpdate>>")


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

        self.text.configure_style()
        self.text['font'] = get_default_font()

        self.linenumbers = TextLineNumbers(self)
        self.linenumbers.attach(self.text)

        self.scrollbar_v.pack(side="right", fill="y")
        self.linenumbers.pack(side="left", fill="y")
        self.text.pack(side="right", fill="both", expand=True)

        self.text.bind("<<Change>>", self._on_change)
        self.text.bind("<Configure>", self._on_change)

    def link_event_log(self, event_log):
        self.text.painter.attach_event_log(event_log)

    def _on_change(self, event):
        self.linenumbers.redraw()
        for bulb in self.text.bulbs:
            bulb.redraw()
