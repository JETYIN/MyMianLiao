#coding=utf-8
import xml.etree.ElementTree as ET
import sys

print sys.argv[1],'is being processed'

ET.register_namespace("android", "http://schemas.android.com/apk/res/android")
ET.register_namespace("tools", "http://schemas.android.com/tools")
ET.register_namespace("app", "http://schemas.android.com/apk/res-auto")

tree = ET.parse(sys.argv[1])
root = tree.getroot()

print root.tag

if (cmp('LinearLayout',root.tag) == 0):
 root.tag='com.tjut.mianliao.black.BlackLinearLayout'
elif (cmp('RelativeLayout',root.tag) == 0):
 root.tag='com.tjut.mianliao.black.BlackRelativeLayout'
elif (cmp('FrameLayout',root.tag) == 0):
 root.tag='com.tjut.mianliao.black.BlackFrameLayout'

tree.write(sys.argv[1])