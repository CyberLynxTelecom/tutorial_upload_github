'''Apport package hook for Eclipse

Copyright 2010 Benjamin Drung <bdrung@ubuntu.com>
License: EPL-1.0 | GPL-2+
'''

import apport.hookutils
import glob
import os

def add_info(report):
    apport.hookutils.attach_conffiles(report, "eclipse-platform")

    # Guess workspace if we fail to detect it
    workspace = "~/workspace"

    # try to detect the workspace directory
    preffile_pattern = "~/.eclipse/org.eclipse.platform_*/configuration/" + \
                       ".settings/org.eclipse.ui.ide.prefs"
    for preffile in glob.glob(os.path.expanduser(preffile_pattern)):
        lines = open(preffile).readlines()
        lines = filter(lambda l: l.startswith("RECENT_WORKSPACES="), lines)
        if len(lines) > 0:
            workspaces = lines[0].split("=")[1]
            # workspaces are separated by "\n" - we take the first item
            workspace = workspaces.split("\\n")[0]

    # workspace/.metadata/.log
    logfile = os.path.join(workspace, ".metadata", ".log")
    apport.hookutils.attach_file_if_exists(report, logfile,
                                           "workspace.metadata.log")
