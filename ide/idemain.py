import tkinter
from tkinter import *
from tkinter.filedialog import asksaveasfile, askopenfile
from tkinter.messagebox import showerror
from tkinter import messagebox

from widgets import *


FILE_NAME = tkinter.NONE


def new_file():
    global FILE_NAME
    FILE_NAME = "Untitled"
    code_frame.text.delete('1.0', tkinter.END)


def save_file():
    data = code_frame.text.get('1.0', tkinter.END)
    out = open(FILE_NAME, 'w')
    out.write(data)
    out.close()


def save_as():
    out = asksaveasfile(mode='w', defaultextension='.txt')
    data = code_frame.text.get('1.0', tkinter.END)
    try:
        out.write(data.rstrip())
    except Exception:
        showerror(title="Error", message="Saving file error....")


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
root.title("WACCCode")

root.minsize(width=800, height=800)
root.maxsize(width=800, height=800)

# Menu
menu_bar = tkinter.Menu(root)

file_menu = tkinter.Menu(menu_bar)
file_menu.add_command(label="New", command=new_file)
file_menu.add_command(label="Open", command=open_file)
file_menu.add_command(label="Save", command=save_file)
file_menu.add_command(label="Save as", command=save_as)

menu_bar.add_cascade(label="File", menu=file_menu)
menu_bar.add_command(label="About", command=about)
menu_bar.add_command(label="Exit", command=root.quit)

root.config(menu=menu_bar)

code_frame = CodeFrame(root)
code_frame.pack(side="top", fill="both", expand=True)

root.mainloop()
