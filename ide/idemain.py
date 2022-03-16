import tkinter
from tkinter import *
from tkinter.filedialog import asksaveasfile, askopenfile
from tkinter.messagebox import showerror
from tkinter import messagebox
from tkinter import ttk

from codeframe import CodeFrame
from style import configure_styles
from eventlog import EventLog


FILE_NAME = tkinter.NONE
CODE_SIDE_MINSIZE = 300
SHELL_MINSIZE = CODE_SIDE_MINSIZE
WINDOW_MINSIZE = CODE_SIDE_MINSIZE + SHELL_MINSIZE
HORISONTAL_SASH_WIDTH = 1


def new_file():
    global FILE_NAME
    FILE_NAME = tkinter.NONE
    code_frame.text.delete('1.0', tkinter.END)


def save_file(event=None):
    data = code_frame.text.get('1.0', tkinter.END)
    out = open(FILE_NAME, 'w')
    out.write(data)
    out.close()


def save_as():
    global FILE_NAME
    out = asksaveasfile(mode='w', defaultextension='.txt')
    FILE_NAME = out.name
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
    global FILE_NAME
    inp = askopenfile(mode="r")
    if inp is None:
        return
    FILE_NAME = inp.name

    data = inp.read()
    code_frame.text.delete('1.0', tkinter.END)
    code_frame.text.insert('1.0', data)


def about():
    messagebox.showinfo("About WACCCode", "WACC Programming Language IDE")


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
menu_bar.add_command(label="About", command=about)
menu_bar.add_command(label="Exit", command=root.quit)

root.config(menu=menu_bar)


# Resizable Panes with sashes design:
#  +----------+---------+
#  |          |  Shell  |
#  |   Code   +---------+
#  |   edit   |  Event  |
#  |          |   Log   |
#  +----------+---------+

main_pane = PanedWindow(root, orient=HORIZONTAL)
main_pane.pack(fill="both", expand=True)

code_frame = CodeFrame(main_pane)
main_pane.add(code_frame, minsize=CODE_SIDE_MINSIZE)

devtool_pane = PanedWindow(main_pane, orient=VERTICAL, sashwidth=HORISONTAL_SASH_WIDTH)
main_pane.add(devtool_pane, minsize=SHELL_MINSIZE)

wacc_shell_frame = Frame(devtool_pane, bg="#00ff00")
devtool_pane.add(wacc_shell_frame, minsize=SHELL_MINSIZE)

event_log = EventLog(devtool_pane)
code_frame.link_event_log(event_log)
devtool_pane.add(event_log, minsize=SHELL_MINSIZE)

root.mainloop()
