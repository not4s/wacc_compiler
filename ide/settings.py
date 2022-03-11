from tkinter import font as tk_font


DEFAULT_FONT_FAMILY = "Consolas"
DEFAULT_FONT_SIZE = 14


def get_default_font():
    return tk_font.Font(family=DEFAULT_FONT_FAMILY, size=DEFAULT_FONT_SIZE)
