from tkinter import *
from tkinter import ttk
from tkinter import font as tk_font
from style import devtool_box_style, common_text_style, get_smaller_font
import subprocess
import os
import threading


def God_forgive_me(writing, woutput):

    def wrapper():
        writing()
        woutput.forgive()

    return wrapper


def run_emulator(woutput, binary_file):


    woutput.run_proc = subprocess.Popen(
        ["qemu-arm", "-L", "/usr/arm-linux-gnueabi/", binary_file],
        stdout=subprocess.PIPE,
        stdin=subprocess.PIPE,
        # stdin=woutput.stdin.fileno(),
        stderr=subprocess.PIPE,
    )

    woutput.run_proc.stdout.write = God_forgive_me(woutput.run_proc.stdout.write, woutput)
    print("jopa")
    # stdout_text, _ = woutput.run_proc.communicate(input=woutput.heresy_magic())

    # woutput.heresy_magic()

    # stdout_text, _ = woutput.run_proc.communicate()

    text = woutput.run_proc.stdout.read().decode('utf-8')
    print("stddout>:", text, ":<stdout")
    woutput.write(text)
    print("finito")
    woutput.run_proc = None


class WOutput(ttk.Frame):

    def __init__(self, *args, **kwargs):
        kwargs['style'] = "CodeFrame.TFrame"
        ttk.Frame.__init__(self, *args, **kwargs)

        self.run_proc = None

        self.label_in = Label(self, text="Program Input")
        self.label_in.pack(side="top", fill="x")

        self.entry = Entry(self)
        self.entry.pack(side='top', fill='x', anchor='s')

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
            return
        os.chdir(prevDir)

        output_assembly = os.path.join(target_dir, 'out.s')
        output_binary = os.path.join(target_dir, 'out')

        with open(output_assembly, 'w') as f:
            f.write(proc.stdout)

        subprocess.run(f"arm-linux-gnueabi-gcc -o {output_binary} " +\
            f"-mcpu=arm1176jzf-s -mtune=arm1176jzf-s {output_assembly}", shell=True)

        trd = threading.Thread(target=run_emulator, args=(self, output_binary))
        trd.start()
        print("thread started")

    def write(self, text):
        self.text.configure(state='normal')
        self.text.insert('end', text)
        self.text.configure(state='disabled')

    def forgive(self):
        self.

    def heresy_magic(self):
        while True:
            pass
            # input =
