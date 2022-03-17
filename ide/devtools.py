from tkinter import *
from tkinter import ttk
from tkinter import font as tk_font
from style import devtool_box_style, common_text_style, get_smaller_font
import subprocess
import os

class WOutput(ttk.Frame):
    ''' Read only shell which show compiler and emulator outut '''
    NO_SYNTAX_ERRORS_MSG = "No Syntax Errors have been detected."

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
        stdout_data = self.run_proc.communicate(input=self.entry.get())[0]
        print(stdout_data)
        self.entry.delete(0, END)
        self.write(self.run_proc.stdout)

    def run(self, compile_command, target_dir, file_name):
        if file_name == NONE:
            self.write("Please save the file before running it")
            return

        proc = subprocess.run([compile_command, '-p', file_name], capture_output=True)
        if proc.returncode != 0:
            print([compile_command, '-p', file_name])
            print(f"\n\nreturn code of compile {proc.returncode}\n")
            self.write(proc.stdout)
            return

        output_assemply = os.path.join(target_dir, 'out.s')
        output_binary = os.path.join(target_dir, 'out')

        with open(output_assemply, 'w') as f:
            f.write(proc.stdout)

        subprocess.run(f"arm-linux-gnueabi-gcc -o {output_binary} " +\
            f"-mcpu=arm1176jzf-s -mtune=arm1176jzf-s {output_assemply}")

        self.run_proc = Popen(["qemu-arm", "-L", "/usr/arm-linux-gnueabi/", test_name], stdout=PIPE, stdin=PIPE, stderr=PIPE)
        self.write(self.run_proc.stdout)
        self.run_proc.wait()

        print(f"\nERROR -----> {self.run_proc.errorcode}\n")

        self.run_proc = None

    def write(self, text):
        self.text.configure(state='normal')
        self.text.delete('1.0', 'end')
        self.text.insert('1.0', text)
        self.text.configure(state='disabled')
