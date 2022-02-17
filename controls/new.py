#!/usr/bin/env python3
import sys, os
path = "rev-" + str(1 + max(map(int, list(filter(None, [x[0][6:] for x in os.walk('.')])))))
os.system(f"mkdir {path}; cp -r base.xcf {path}/driver.xcf; cp -r base.xcf {path}/operator.xcf")