
#include "AvatarScene.h"

#ifdef CC_TARGET_OS_IPHONE

#import "TMLArmatureInitor.h"

#import "TMLAvatarDBUtil.h"

#import "AvatarUpgradeTool.h"

#import "AvatarAnimationCallBack.h"

#else

#include "Java_com_tjut_mianliao_cocos2dx_CocosAvatarView.h"

#endif

USING_NS_CC;

AvatarScene* AvatarScene::s_Instance=nullptr;

const char* AvatarScene::GenderSuitFolderName="suit_male";

Scene* AvatarScene::createScene()
{
    
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    
    // 'layer' is an autorelease object
    auto layer = AvatarScene::getInstance();
    
    // add layer as a child to scene
    scene->addChild(layer);
    
    // return the scene
    return scene;
    
}



AvatarScene* AvatarScene::getInstance()
{
    
    if(s_Instance==nullptr)
    {
        s_Instance=AvatarScene::create();
    }
    
    return s_Instance;
}


// on "init" you need to initialize your instance
bool AvatarScene::init()
{
    //////////////////////////////
    // 1. super init first
    if (!Layer::init() )
    {
        return false;
    }
    
    
#ifdef CC_TARGET_OS_IPHONE
    this->loadArmatureData([AvatarUpgradeTool hasSandboxVersion]);
#else
    this->loadArmatureData(false);//android should set if sandbox version exits here
#endif

    return true;
}


void AvatarScene::addLeftAvatar()
{
    if(leftArmature!=nullptr)
    {
        return;
    }
    
    leftArmature=Armature::create("nv6");
    leftArmature->setAnchorPoint(Vec2(0.5,0));
    leftArmature->setScaleX(-1);
    this->addChild(leftArmature, 1);
    
    
    std::function<void(Armature*, MovementEventType, const std::string&)> armatureFun = [=](Armature* armature, MovementEventType type, const std::string& id)
    {
        if (type == MovementEventType::COMPLETE)
        {
            
#ifdef CC_TARGET_OS_IPHONE
            [[AvatarAnimationCallBack getInstance].delegate playFinished];
#endif
            
        }
    };
    
    leftArmature->getAnimation()->setMovementEventCallFunc(armatureFun);

}

void AvatarScene::removeLeftAvatar()
{
    if(leftArmature!=nullptr)
    {
        leftArmature->removeFromParent();
        leftArmature=nullptr;
    }
    
}

void AvatarScene::addRightAvatar()
{
    

    if(rightArmature!=nullptr)
    {
        return;
    }
    
    rightArmature=Armature::create("nv6");
    rightArmature->setAnchorPoint(Vec2(0.5,0));
    this->addChild(rightArmature, 1);
    
    
    std::function<void(Armature*, MovementEventType, const std::string&)> armatureFun = [=](Armature* armature, MovementEventType type, const std::string& id)
    {
        if (type == MovementEventType::COMPLETE)
        {
            
#ifdef CC_TARGET_OS_IPHONE
            [[AvatarAnimationCallBack getInstance].delegate playFinished];
#endif
            
        }
    };
    
    rightArmature->getAnimation()->setMovementEventCallFunc(armatureFun);
    
}

void AvatarScene::removeRightAvatar()
{
    if(rightArmature!=nullptr)
    {
        rightArmature->removeFromParent();
        rightArmature=nullptr;
    }
}

/*set armature play callback in this method*/
void AvatarScene::addAvatars()
{
    if(rightArmature!=nullptr||leftArmature!=nullptr)
    {
        return;
    }
    
    rightArmature=Armature::create("nv6");
    rightArmature->setAnchorPoint(Vec2(0.5,0));
    this->addChild(rightArmature, 1);
    
    leftArmature=Armature::create("nv6");
    leftArmature->setAnchorPoint(Vec2(0.5,0));
    leftArmature->setScaleX(-1);
    this->addChild(leftArmature, 1);
    
    
    std::function<void(Armature*, MovementEventType, const std::string&)> armatureFun = [=](Armature* armature, MovementEventType type, const std::string& id)
    {
        if (type == MovementEventType::COMPLETE)
        {
    
            #ifdef CC_TARGET_OS_IPHONE
            [[AvatarAnimationCallBack getInstance].delegate playFinished];
            #endif
          
        }
    };
    
    leftArmature->getAnimation()->setMovementEventCallFunc(armatureFun);
    rightArmature->getAnimation()->setMovementEventCallFunc(armatureFun);

}

void AvatarScene::loadArmatureData(bool isSandBoxVersionExists)
{
    
    if(isDataLoaded)
    {
        return;
    }
    
    isDataLoaded=true;
    
    std::string documentPath = CCFileUtils::getInstance()->getWritablePath();
    
    CCString *armatureDataStr;
    
    
    if(isSandBoxVersionExists)
    {
       armatureDataStr =CCString::createWithFormat("%scocos/nv6/nv6.ExportJson",documentPath.c_str());
    }
    else
    {
       
        
          armatureDataStr =CCString::createWithFormat("cocos/nv6/nv6.ExportJson");
    }
    
    ArmatureDataManager *manager=ArmatureDataManager::getInstance();
    
    manager->addArmatureFileInfoAsync(armatureDataStr->getCString(), this, schedule_selector(AvatarScene::dataLoaded));
    
}


void AvatarScene::dataLoaded( float percent )
{
    if(percent==1)
    {
        this->addRightAvatar();
        
        #ifdef CC_TARGET_OS_IPHONE
        [TMLArmatureInitor loadArmatureInfo];
        #else
        onAvatarLoadedJNI();
        #endif
    }
}

bool AvatarScene::checkDataLoaded()
{
    return isDataLoaded;
}

void AvatarScene::setAvatarsHeight(float height)
{
    setAvatarHeight(0, height);
    setAvatarHeight(1, height);
    
}

void AvatarScene::setAvatarHeight(int side,float height)
{
    
    Armature* armature=this->getAvatarWithSide(side);
    avatarScaleY=height/(armature->getContentSize().height);
    armature->setScaleY(avatarScaleY);
    
    float scaleX;
    
    if(side==0)
    {
        scaleX=-avatarScaleY;
    }
    else
    {
        scaleX=avatarScaleY;
    }
    
    armature->setScaleX(scaleX);

}

void AvatarScene::hideArmature(int side)
{
    if(side==0)
    {
        leftArmature->setVisible(false);
    }
    else
    {
         rightArmature->setVisible(false);
    }
}

void AvatarScene::midXArmature(int side)
{
    float x=Director::getInstance()->getVisibleSize().width/2;
    
    if(side==0)
    {
        leftArmature->setPosition(Vec2(x, leftArmature->getPosition().y));
    }
    else
    {
        rightArmature->setPosition(Vec2(x, rightArmature->getPosition().y));
    }
}

void AvatarScene::showMyAvatar(float y,float height)
{
   leftArmature->setVisible(false);
    
    rightArmature->setPosition(Vec2(0,y));
    
    this->midXArmature(1);
    
    this->setAvatarHeight(1, height);
    
}

void AvatarScene::setAvatarsWithPosY(float y)
{
    this->setAvatarWithPosY(0,y);
    this->setAvatarWithPosY(1,y);
    
}

void AvatarScene::setAvatarWithPosY(int side,float y)
{
    Armature *armature=this->getAvatarWithSide(side);
    
    float positionX=armature->getPosition().x;
    
    armature->setPosition(Vec2(positionX, y));
    
}

void AvatarScene::locateAvatars(float y,float height)
{

    leftArmature->setVisible(true);
    rightArmature->setVisible(true);
    
    setAvatarsHeight(height);
    
    float x=Director::getInstance()->getVisibleSize().width/2;
    
    float rightArmatureHeight=rightArmature->getContentSize().width;
    
    float leftArmatureScaleY=leftArmature->getScaleY();
    
    float leftX=x-rightArmatureHeight*leftArmatureScaleY;
    float rightX=x+rightArmatureHeight*leftArmatureScaleY;
    
    leftArmature->setPosition(Vec2(leftX,y));
    rightArmature->setPosition(Vec2(rightX, y));
    
}




void AvatarScene::playArmature(int side ,int armatureIndex)
{
    
    if(side==0)
    {
        
        leftArmature->setVisible(true);
        leftArmature->getAnimation()->playWithIndex(0);
        
    }
    else if(side==1)
    {
        
        rightArmature->setVisible(true);
        rightArmature->getAnimation()->playWithIndex(0);
        
    }
}

void AvatarScene::playArmature(int side ,const char* animationName)
{
    
    if(side==0)
    {
        
        leftArmature->setVisible(true);
        leftArmature->getAnimation()->play(animationName);
        
    }
    else if(side==1)
    {
        
        rightArmature->setVisible(true);
        rightArmature->getAnimation()->play(animationName);
        
    }
}

void AvatarScene::changeSuit(int side,const char* originalLayer,const char* replaceImageName)
{
    Armature* armature=side==0?leftArmature:rightArmature;
    Bone* bone = armature->getBone(originalLayer);
    
    Skin *skin=Skin::createWithSpriteFrameName(replaceImageName);
    
    
    bone->addDisplay(skin, 1);
    bone->changeDisplayWithIndex(1, true);
    
}

void AvatarScene::loadClothesData(const char* suitFolderName,bool isSandBoxVersionExists)
{
    
    CCString *plistFilePath,*pngFilePath;
    
    if (strcmp(AvatarScene::GenderSuitFolderName, suitFolderName)==0&&!isSandBoxVersionExists) {
        
        plistFilePath=CCString::createWithFormat("cocos/clothes/%s/suit.plist",AvatarScene::GenderSuitFolderName);
        pngFilePath=CCString::createWithFormat("cocos/clothes/%s/suit.png",AvatarScene::GenderSuitFolderName);
    }
    else
    {
        std::string documentPath = CCFileUtils::getInstance()->getWritablePath();
        plistFilePath=CCString::createWithFormat("%scocos/clothes/%s/suit.plist",documentPath.c_str(),suitFolderName);
        pngFilePath=CCString::createWithFormat("%scocos/clothes/%s/suit.png",documentPath.c_str(),suitFolderName);
    }
    
    
    ArmatureDataManager::getInstance()->addSpriteFrameFromFile(plistFilePath->getCString(), pngFilePath->getCString());

}



void AvatarScene::loadGenderClothesData(bool isSandBoxVersionExists)
{
    
    this->loadClothesData(AvatarScene::GenderSuitFolderName,isSandBoxVersionExists);
}


void AvatarScene::stopAction(int side)
{
    this->getAvatarWithSide(side)->getAnimation()->stop();

}

Armature* AvatarScene::getAvatarWithSide(int side)
{
    return side==0?leftArmature:rightArmature;
}



