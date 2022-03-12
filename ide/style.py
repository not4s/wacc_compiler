from tkinter import ttk
from tkinter import font as tk_font


DEFAULT_FONT_FAMILY = "Consolas"
DEFAULT_THEME = 'Eye Pain'
DEFAULT_FONT_SIZE = 14


def get_default_font():
    return tk_font.Font(family=DEFAULT_FONT_FAMILY, size=DEFAULT_FONT_SIZE)


def configure_styles(root):
    style = ttk.Style(root)
    style.configure('CodeFrame.TFrame', **code_frame_style)


MAIN_FONT_COLOR = '#dfe2f1'
BACKGROUND_COLOR = '#202746'

code_theme = {
    'background': BACKGROUND_COLOR,
    'highlighted_line': '#293256',
    'comment': '#5e6687',
    'keyword': '#39aca6',
    'main_font_col': MAIN_FONT_COLOR,
    'function': '#c76b29',
    'string_literal': '#22a2c9',
    'int_literal': '#c94922',
    'error': '#9c637a',
}

code_frame_style = {
    'background': BACKGROUND_COLOR,
    'padx': 5,
    'pady': 5,
    'font': (DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE),
    'bg': BACKGROUND_COLOR,
    'foreground': MAIN_FONT_COLOR,
    'insertbackground': MAIN_FONT_COLOR,
}
