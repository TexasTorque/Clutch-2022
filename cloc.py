#!/usr/bin/env python3
# Designed for use in a Bash shell
# Requires CLOC (http://cloc.sourceforge.net/)
# to be installed and added to the PATH
print(sum([int(i) for i in [i for i in [l for l in __import__('os').popen('cloc .').read().split('\n') if l.startswith('Java')][0].split(' ') if i][2:]])) if __name__ == '__main__' else None