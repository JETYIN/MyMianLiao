#coding=utf-8
import xml.etree.ElementTree as ET
import sys

print sys.argv[1],'is being processed'

ET.register_namespace("android", "http://schemas.android.com/apk/res/android")
ET.register_namespace("tools", "http://schemas.android.com/tools")
ET.register_namespace("app", "http://schemas.android.com/apk/res-auto")

tree = ET.parse(sys.argv[1])
root = tree.getroot()



#process UIImageView

for child in root.iter('TextView'):
    child.tag='com.tjut.mianliao.theme.ThemeTextView'

for child in root.iter('ImageView'):
    child.tag='com.tjut.mianliao.theme.ThemeImageView'



tree.write(sys.argv[1])