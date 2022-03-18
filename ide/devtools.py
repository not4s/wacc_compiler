from tkinter import *
from tkinter import ttk
from tkinter import font as tk_font
from style import devtool_box_style, common_text_style, get_smaller_font
import subprocess
import os
import threading


class WOutput(ttk.Frame):

    def __init__(self, *args, **kwargs):
        kwargs['style'] = "CodeFrame.TFrame"
        ttk.Frame.__init__(self, *args, **kwargs)

        self.run_proc = None

        self.label_in = Label(self, text="Program Input")
        self.label_in.pack(side="top", fill="x")

        self.entry = Entry(self)
        self.entry.pack(side='top', fill='x', anchor='s')
        self.entry.insert(0, "Coming soon :)")
        self.entry.configure(state="disabled")

        self.label_out = Label(self, text="Program Output")
        self.label_out.pack(side="top", fill="x")

        self.text = Text(self)

        self.scrollbar_v = Scrollbar(self, orient=VERTICAL, command=self.text.yview)
        self.text.configure(yscrollcommand=self.scrollbar_v.set)
        self.text.insert('1.0', "Run the program to see the output")
        self.text.configure(state='disabled')

        # Text style
        self.text.configure(**common_text_style)
        tab = tk_font.Font(font=self.text['font']).measure('  ')
        self.text.config(tabs=tab)
        self.text['font'] = get_smaller_font()

        self.scrollbar_v.pack(side="right", fill="y")
        self.text.pack(side="top", fill="both", expand=True)

        self.entry.bind('<Return>', self._submit)

    def _submit(self, event):
        if not self.run_proc:
            return
        stdout_data = self.run_proc.stdin.write(self.entry.get().encode('utf-8'))
        self.entry.delete(0, END)
        self.write(self.run_proc.stdout.read().decode('utf-8'))

    def run(self, compile_command, target_dir, file_name):
        self.text.configure(state='normal')
        self.text.delete('1.0', 'end')
        self.text.configure(state='disabled')
        if self.run_proc:
            self.run_proc.kill()
        if file_name == NONE:
            self.write("Please save the file before running it")
            return

        prevDir = os.getcwd()
        os.chdir('..')
        proc = subprocess.run([compile_command, '-p', file_name], text=True, capture_output=True)
        if proc.returncode != 0:
            self.write(proc.stdout)
            os.chdir(prevDir)
            return
        os.chdir(prevDir)

        output_assembly = os.path.join(target_dir, 'out.s')
        output_binary = os.path.join(target_dir, 'out')

        with open(output_assembly, 'w') as f:
            f.write(proc.stdout)

        subprocess.run(f"arm-linux-gnueabi-gcc -o {output_binary} " +\
            f"-mcpu=arm1176jzf-s -mtune=arm1176jzf-s {output_assembly}", shell=True)

        self.provide_stdin(output_binary)

    def provide_stdin(self, output_binary):
        ''' Create window and ask User to provide stdin for the program '''

        def submit_stdin():
            self.run_proc = subprocess.Popen(
                ["qemu-arm", "-L", "/usr/arm-linux-gnueabi/", output_binary],
                stdout=subprocess.PIPE,
                stdin=subprocess.PIPE,
                stderr=subprocess.PIPE,
            )
            submission = text.get('1.0', 'end').encode('utf-8')
            res, _ = self.run_proc.communicate(input=submission)
            self.write(res.decode('utf-8'))
            self.run_proc = None
            win.destroy()
            win.update()

        win = Toplevel(self.master)
        win.title("Please, Provide Standart Input")
        win.geometry("300x300")

        text = Text(win)
        # Text style
        text.configure(**common_text_style)
        tab = tk_font.Font(font=self.text['font']).measure('  ')
        text.config(tabs=tab)
        text['font'] = get_smaller_font()

        button = Button(win, text="Submit", command=submit_stdin)
        button.pack(side="top", fill="x")
        text.pack(side="top", fill="x", expand=True)

    def write(self, text):
        self.text.configure(state='normal')
        self.text.insert('end', text)
        self.text.configure(state='disabled')
