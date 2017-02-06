#coding=utf-8
import xml.etree.ElementTree as ET
import sys

print sys.argv[1],'is being processed'

ET.register_namespace("android", "http://schemas.android.com/apk/res/android")
ET.register_namespace("tools", "http://schemas.android.com/tools")
ET.register_namespace("app", "http://schemas.android.com/apk/res-auto")

tree = ET.parse(sys.argv[1])
root = tree.getroot()


if (cmp('LinearLayout',root.tag) == 0):
    root.tag='com.tjut.mianliao.black.MagicLinearLayout'
elif (cmp('RelativeLayout',root.tag) == 0):
    root.tag='com.tjut.mianliao.black.MagicRelativeLayout'
elif (cmp('FrameLayout',root.tag) == 0):
    root.tag='com.tjut.mianliao.black.MagicFrameLayout'


for child in root.iter('LinearLayout'):
    child.tag='com.tjut.mianliao.black.MagicLinearLayout'

for child in root.iter('RelativeLayout'):
    child.tag='com.tjut.mianliao.black.MagicRelativeLayout'

for child in root.iter('FrameLayout'):
    child.tag='com.tjut.mianliao.black.MagicFrameLayout'



tree.write(sys.argv[1])