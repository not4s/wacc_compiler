from tkinter import ttk
from tkinter import font as tk_font


DEFAULT_FONT_FAMILY = "Consolas"
DEFAULT_THEME = 'Eye Pain'
DEFAULT_FONT_SIZE = 14
SMALLER_FONT_SIZE = 11


def get_default_font():
    return tk_font.Font(family=DEFAULT_FONT_FAMILY, size=DEFAULT_FONT_SIZE)


def get_smaller_font():
    return tk_font.Font(family=DEFAULT_FONT_FAMILY, size=SMALLER_FONT_SIZE)


def configure_styles(root):
    style = ttk.Style(root)
    style.configure('CodeFrame.TFrame', **common_text_style)
    style.configure('DevToolBox.TFrame', **devtool_box_style)


MAIN_FONT_COLOR = '#dfe2f1'
BACKGROUND_COLOR = '#202746'

# Non Tkinter attributes related to code highlighting
code_theme = {
    'background': BACKGROUND_COLOR,
    'comment': '#ff91fa',
    'keyword': '#DD4A68',
    'operator': '#ee9900',
    'declaration': '#fbff1f',
    'type': '#ee9900',
    'main_font_col': MAIN_FONT_COLOR,
    'function': '#8ebffb',
    'attribute': '#9ded9e',
    'string_literal': '#669900',
    'int_literal': '#c94922',
    'error': '#ff0000',
    'current_line': '#374169',
    'sel_background': '#ffffff',
    'sel_foreground': '#000000',
}

# Tkinter attributes
common_text_style = {
    'background': BACKGROUND_COLOR,
    'padx': 5,
    'pady': 5,
    'font': (DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE),
    'bg': BACKGROUND_COLOR,
    'foreground': MAIN_FONT_COLOR,
    'insertbackground': MAIN_FONT_COLOR,
}

devtool_box_style = {
    'background': BACKGROUND_COLOR,
    'padx': 30,
    'pady': 30,
    'font': (DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE),
    'bg': BACKGROUND_COLOR,
    'foreground': MAIN_FONT_COLOR,
    'insertbackground': MAIN_FONT_COLOR,
}
