# -*- coding: utf-8 -*-
from pathlib import Path
import runpy

root_script = Path(__file__).resolve().parent.parent / "take_screenshots.py"
runpy.run_path(str(root_script), run_name="__main__")
