#!/usr/bin/env python3
#
# Count Lines Of Code .py
# A wrapper fo the CLOC program (http://cloc.sourceforge.net/)
#
# CLOC must be installed and added to the PATH
# Designed for UNIX, but feel free to adapt to Windows
#
# (c) Justus Languell 2022

import os

class LineCounter:
    def __init__(self):
        self.lines = os.popen('cloc .').read().split('\n')
    def lines_for_language(self, lang):
        try:
            return sum([int(i) for i in [i for i in [l for l in self.lines if l.startswith(lang)][0].split(' ') if i][2:]])
        except:
            return -1;

if __name__ == '__main__':
    c = LineCounter()
    for lang in ['Java', 'C++', 'Python']:
        lines = c.lines_for_language(lang)
        print(f'{lang}: {lines} lines = ~{max(lines // 46, 1)} pages') if lines > 0 else None
