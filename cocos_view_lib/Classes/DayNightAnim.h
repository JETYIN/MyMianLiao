//
//  DayNightAnim.h
//  MIMEChat3
//
//  Created by nick on 15/9/17.
//
//

#ifndef __MIMEChat3__DayNightAnim__
#define __MIMEChat3__DayNightAnim__

#include <stdio.h>

#include "cocos2d.h"

#include<vector>

#include "cocos/editor-support/cocostudio/CocoStudio.h"

#include "cocos/editor-support/spine/spine.h"

#ifdef CC_TARGET_OS_IPHONE
#include "CocosGUI.h"
#else
#include "ui/CocosGUI.h"
#endif


#include <spine/SkeletonAnimation.h>

using namespace cocostudio;
using namespace spine;
using namespace ui;

#include <stdio.h>
class AvatarScene;

class DayNightAnim
{
public:
    DayNightAnim(AvatarScene *scene);
    void assignAnin();
    void animFinished();
    void removeElements();
private:
    void doAction();
    void ButtentouchEvent(Ref* ref,Widget::TouchEventType type);


private:
    CallFunc *actionFunc;
    Sprite *day_sprite,*night_sprite=nullptr;
    int anim_phase;
    SkeletonAnimation *elephantArmature,*sunArmature,*moonArmature;
    Button* buttonSkip;
    AvatarScene *scene;
    Label* label;
};


#endif /* defined(__MIMEChat3__DayNightAnim__) */
