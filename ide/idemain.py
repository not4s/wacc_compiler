import tkinter
from tkinter import *
from tkinter.filedialog import asksaveasfile, askopenfile
from tkinter.messagebox import showerror
from tkinter import messagebox
from tkinter import ttk
import os

from codeframe import CodeFrame
from style import configure_styles
from eventlog import EventLog
from devtools import WOutput


FILE_NAME = tkinter.NONE
DIR_NAME = tkinter.NONE
CODE_SIDE_MINSIZE = 300
TOOLS_MINSIZE = CODE_SIDE_MINSIZE
WINDOW_MINSIZE = CODE_SIDE_MINSIZE + TOOLS_MINSIZE
HORISONTAL_SASH_WIDTH = 1

COMPILE_SCRIPT_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'compile')


def set_FILE_NAME(value):
    global FILE_NAME
    FILE_NAME = value
    dirname_update()


def new_file():
    set_FILE_NAME(tkinter.NONE)
    code_frame.text.delete('1.0', tkinter.END)


def save_file(event=None):
    data = code_frame.text.get('1.0', tkinter.END)
    out = open(FILE_NAME, 'w')
    out.write(data)
    out.close()


def save_as():
    out = asksaveasfile(mode='w', defaultextension='.txt')
    set_FILE_NAME(out.name)
    data = code_frame.text.get('1.0', tkinter.END)
    try:
        out.write(data.rstrip())
    except Exception:
        showerror(title="Error", message="Saving file error....")


def quick_save(event):
    if FILE_NAME == tkinter.NONE:
        save_as()
    else:
        save_file()


def open_file():
    inp = askopenfile(mode="r")
    if inp is None:
        return
    set_FILE_NAME(inp.name)

    data = inp.read()
    code_frame.text.delete('1.0', tkinter.END)
    code_frame.text.insert('1.0', data)


def about():
    messagebox.showinfo("About WACCCode", "WACC Programming Language IDE")


def dirname_update(event=None):
    global DIR_NAME
    DIR_NAME = os.path.dirname(os.path.abspath(FILE_NAME))


def get_dirname(self=None, event=None):
    return DIR_NAME


def run_prog():
    woutput.run(COMPILE_SCRIPT_PATH, DIR_NAME, FILE_NAME)


root = tkinter.Tk()

configure_styles(root)

root.title("WACCCode")
root.minsize(width=WINDOW_MINSIZE, height=WINDOW_MINSIZE)
root.resizable(height=True, width=True)

# Menu
menu_bar = tkinter.Menu(root)

file_menu = tkinter.Menu(menu_bar)
file_menu.add_command(label="New", command=new_file)
file_menu.add_command(label="Open", command=open_file)
file_menu.add_command(label="Save", command=save_file)
root.bind('<Control-Key-s>', quick_save)
file_menu.add_command(label="Save as", command=save_as)

menu_bar.add_cascade(label="File", menu=file_menu)
menu_bar.add_command(label="Run", command=run_prog)
menu_bar.add_command(label="About", command=about)
menu_bar.add_command(label="Exit", command=root.quit)

root.config(menu=menu_bar)

main_pane = PanedWindow(root, orient=HORIZONTAL)
main_pane.pack(fill="both", expand=True)

code_frame = CodeFrame(main_pane)
main_pane.add(code_frame, minsize=CODE_SIDE_MINSIZE)

devtool_pane = PanedWindow(main_pane, orient=VERTICAL, sashwidth=HORISONTAL_SASH_WIDTH)
main_pane.add(devtool_pane, minsize=TOOLS_MINSIZE)

woutput = WOutput(devtool_pane)
devtool_pane.add(woutput, minsize=TOOLS_MINSIZE)

event_log = EventLog(devtool_pane)
code_frame.link_event_log(event_log)
devtool_pane.add(event_log, minsize=TOOLS_MINSIZE)

root.mainloop()
