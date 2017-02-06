#!/usr/bin/python
# android-build.py
# Build android

import sys
import os, os.path
import shutil
from optparse import OptionParser

def do_build(build_mode):

    app_android_root = os.path.dirname(os.path.realpath(__file__))

    if build_mode is None:
        build_mode = 'debug'
    elif build_mode != 'release':
        build_mode = 'debug'

    command = 'cocos compile -p android -s %s --ndk-mode %s' % (app_android_root, build_mode)
    print command

    if os.system(command) != 0:
        raise Exception("Build dynamic library for project [ " + app_android_root + " ] fails!")

# -------------- main --------------
if __name__ == '__main__':

    #parse the params
    usage = """
    %prog [options]
    """

    parser = OptionParser(usage=usage)
    parser.add_option("-n", "--ndk", dest="ndk_build_param",
    help='It is not used anymore, because cocos console does not support it.')
    parser.add_option("-p", "--platform", dest="android_platform",
    help='This parameter is not used any more, just keep compatible.')
    parser.add_option("-b", "--build", dest="build_mode",
    help='The build mode for java project,debug[default] or release. Get more information,please refer to http://developer.android.com/tools/building/building-cmdline.html')
    (opts, args) = parser.parse_args()

    try:
        do_build(opts.build_mode)
    except Exception as e:
        print e
        sys.exit(1)
