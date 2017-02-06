
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

cocos2d::Size getScreenSize()
{
    cocos2d::Size screenSize=Director::getInstance()->getOpenGLView()->getFrameSize();

//    if(screenSize.height==2208&&screenSize.width==1242)
//    {
//        screenSize.height=1920;
//        screenSize.width=1080;
//    }

    return screenSize;
}



cocos2d::Rect getAttachmentRect(const char* jsonFile,const char* attachment_name)
{


    rapidjson::Document readdoc;
    ssize_t size = 0;
    std::string load_str;

    unsigned char* titlech = FileUtils::getInstance()->getFileData(jsonFile, "r", &size);
    load_str = std::string((const char*)titlech,size);

    readdoc.Parse<0>(load_str.c_str());

    cocos2d::Rect rect;

    if(readdoc.HasParseError())
    {
        return rect;
    }

    if(!readdoc.IsObject())
    {
        return rect;
    }

    rapidjson::Value& _json = readdoc["skins"]["default"][attachment_name][attachment_name];

    rect.origin.x=_json["x"].GetDouble();
    rect.origin.y=_json["y"].GetDouble();
    rect.size.width=_json["width"].GetDouble();
    rect.size.height=_json["height"].GetDouble();

    return rect;


}

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


float getAvatarWidth(SkeletonAnimation *anim)
{
    return anim->getState()->data->skeletonData->width;
}

float getAvatarHeight(SkeletonAnimation *anim)
{
    return anim->getState()->data->skeletonData->height;
}


spSkeletonData* getAnimiationData(const char* jsonName,const char* atlasName)
{
    spAtlas* atlas = spAtlas_createFromFile(atlasName, 0);
    spSkeletonJson* json = spSkeletonJson_create(atlas);
    json->scale = 1;
    return  spSkeletonJson_readSkeletonDataFile(json, jsonName);
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



    dayNightAnim=new DayNightAnim(this);

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

    leftArmature=SkeletonAnimation::createWithData(avatarData);
    leftArmature->setAnchorPoint(Vec2(0.5,0));
    leftArmature->setScaleX(-0.3);
    leftArmature->setScaleY(0.3f);
    this->addChild(leftArmature, 1);

    // changeAttachment(leftArmature, "7", "10", "cocos/da/222.atlas");




    std::function<void(int trackIndex, int loopCount)> completeListener=[=](int trackIndex, int loopCount)
    {

#ifdef CC_TARGET_OS_IPHONE
        [[AvatarAnimationCallBack getInstance].delegate playFinished];
#endif

    };

    leftArmature->setCompleteListener(completeListener);

}

void AvatarScene::removeLeftAvatar()
{
    if(leftArmature!=nullptr)
    {
        leftArmature->removeFromParent();
        leftArmature=nullptr;
    }

}

void AvatarScene::removeRightAvatar()
{
    if(rightArmature!=nullptr)
    {
        rightArmature->removeFromParent();
        rightArmature=nullptr;
    }

}

void AvatarScene::addRightAvatar()
{


    if(rightArmature!=nullptr)
    {
        return;
    }

    rightArmature=SkeletonAnimation::createWithData(avatarData);
    rightArmature->setAnchorPoint(Vec2(0.5,0));
    rightArmature->setScale(0.3f);
    rightArmature->setVisible(false);
    this->addChild(rightArmature, 1);


    std::function<void(int trackIndex, int loopCount)> completeListener=[=](int trackIndex, int loopCount)
    {

#ifdef CC_TARGET_OS_IPHONE
        [[AvatarAnimationCallBack getInstance].delegate playFinished];
#endif

    };

    rightArmature->setCompleteListener(completeListener);

}

/*set armature play callback in this method*/
void AvatarScene::addAvatars()
{
    if(rightArmature!=nullptr||leftArmature!=nullptr)
    {
        return;
    }

    rightArmature=SkeletonAnimation::createWithData(avatarData);
    rightArmature->setAnchorPoint(Vec2(0.5,0));
    this->addChild(rightArmature, 1);

    leftArmature=SkeletonAnimation::createWithData(avatarData);
    leftArmature->setAnchorPoint(Vec2(0.5,0));
    leftArmature->setScaleX(-1);
    this->addChild(leftArmature, 1);


    std::function<void(int trackIndex, int loopCount)> completeListener=[=](int trackIndex, int loopCount)
    {

#ifdef CC_TARGET_OS_IPHONE
        [[AvatarAnimationCallBack getInstance].delegate playFinished];
#endif

    };

    rightArmature->setCompleteListener(completeListener);
    leftArmature->setCompleteListener(completeListener);

}


void AvatarScene::loadArmatureData(bool isSandBoxVersionExists)
{

    if(isDataLoaded)
    {
        return;
    }

    isDataLoaded=true;

    std::string documentPath = CCFileUtils::getInstance()->getWritablePath();

    CCString *jsonStr,*atlasStr;


    if(isSandBoxVersionExists)
    {
        jsonStr =CCString::createWithFormat("%scocos/nv6/nv.json",documentPath.c_str());
        atlasStr=CCString::createWithFormat("%scocos/nv6/nv.atlas",documentPath.c_str());
    }
    else
    {

        jsonStr =CCString::createWithFormat("cocos/nv6/nv.json");
        atlasStr=CCString::createWithFormat("cocos/nv6/nv.atlas");
    }

    avatarData=getAnimiationData(jsonStr->getCString(),atlasStr->getCString());
    elephantData=getAnimiationData("cocos/da/222.json","cocos/da/222.atlas");
    sunData=getAnimiationData("cocos/tai/t.json","cocos/tai/t.atlas");
    moonData=getAnimiationData("cocos/yue/y.json","cocos/yue/Y.atlas");



    scheduleOnce(schedule_selector(AvatarScene::dataLoaded), 0.01f);
}



void AvatarScene::dataLoaded(float dt)
{

    this->addRightAvatar();

#ifdef CC_TARGET_OS_IPHONE
    [TMLArmatureInitor loadArmatureInfo];
#else
    onAvatarLoadedJNI();
#endif

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

    cocos2d::Size screenSize=getScreenSize();

    height=(height/screenSize.height)*1334;

    SkeletonAnimation* armature=this->getAvatarWithSide(side);
    avatarScaleY=height/(getAvatarHeight(armature));
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
    if(side==0&&leftArmature!=nullptr)
    {

        leftArmature->setVisible(false);
    }
    else if(rightArmature!=nullptr)
    {
        rightArmature->setVisible(false);
    }
}

void AvatarScene::showArmature(int side)
{
    if(side==0&&leftArmature!=nullptr)
    {
        leftArmature->setVisible(true);
    }
    else if(rightArmature!=nullptr)
    {
        rightArmature->setVisible(true);
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
    dayNightAnim->removeElements();
    cocos2d::Size screenSize=getScreenSize();

    y=(y/screenSize.height)*1334;

    if(leftArmature==nullptr)
    {
        this->addLeftAvatar();
    }

    leftArmature->setVisible(false);

    rightArmature->setVisible(true);

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
    cocos2d::Size screenSize=getScreenSize();;
    y=(y/screenSize.height)*1334;
    SkeletonAnimation *armature=this->getAvatarWithSide(side);

    float positionX=armature->getPosition().x;

    armature->setPosition(Vec2(positionX, y));

}


void AvatarScene::showDayNightAnim()
{
    dayNightAnim->assignAnin();
}

void AvatarScene::finishDayNightAnim()
{
    dayNightAnim->animFinished();
}

void AvatarScene::locateAvatars(float y,float height)
{
    dayNightAnim->removeElements();
    cocos2d::Size screenSize=getScreenSize();
    y=(y/screenSize.height)*1334*(2208.0f/1920);

    leftArmature->setVisible(true);
    rightArmature->setVisible(true);

    setAvatarsHeight(height);

    float x=Director::getInstance()->getVisibleSize().width/2;

    float rightArmatureHeight=getAvatarWidth(rightArmature);

    float leftArmatureScaleY=leftArmature->getScaleY();

    float leftX=x-rightArmatureHeight*leftArmatureScaleY;
    float rightX=x+rightArmatureHeight*leftArmatureScaleY;

    leftArmature->setPosition(Vec2(leftX,y));
    rightArmature->setPosition(Vec2(rightX, y));

    int avatarWidth=getAvatarWidth(leftArmature)*leftArmature->getScaleY();

    avatarRecRect.origin.x=(int)(leftX-avatarWidth/2.0f)-avatarWidth;
    avatarRecRect.origin.y=(int)y;
    avatarRecRect.size.width=(int)(rightX-leftX+avatarWidth)+2*avatarWidth;
    avatarRecRect.size.height=(int)height;

}


cocos2d::Rect AvatarScene::getAvatarRecRect()
{
    return avatarRecRect;
}

void AvatarScene::playArmature(int side ,int armatureIndex)
{

    if(side==0)
    {

        leftArmature->setVisible(true);

    }
    else if(side==1)
    {

        rightArmature->setVisible(true);

    }
}

void AvatarScene::playArmature(int side ,const char* animationName)
{

	   if(side==0)
	    {
	        leftArmature->setVisible(true);
	        leftArmature->setAnimation(0, animationName, false);

	    }
	    else if(side==1)
	    {
	        rightArmature->setVisible(true);
	        rightArmature->setAnimation(0, animationName, false);

	    }
}

void AvatarScene::changeSuit(int side,const char* originalLayer,const char* replaceImageName,const char* atlasPath)
{
    SkeletonAnimation* anim=side==0?leftArmature:rightArmature;
    
    changeAttachment(anim,originalLayer,replaceImageName,atlasPath);

//    Armature* armature=side==0?leftArmature:rightArmature;
//    Bone* bone = armature->getBone(originalLayer);
//
//    Skin *skin=Skin::createWithSpriteFrameName(replaceImageName);
//
//
//    bone->addDisplay(skin, 1);
//    bone->changeDisplayWithIndex(1, true);

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
    //this->getAvatarWithSide(side)->getAnimation()->stop();

    this->playArmature(side, "Animation11");
}

SkeletonAnimation* AvatarScene::getAvatarWithSide(int side)
{
    return side==0?leftArmature:rightArmature;
}


bool AvatarScene::changeAttachment(SkeletonAnimation *anim, const std::string& slotName, const std::string& attachmentName, const std::string& atlasFile)
{



    spSkeleton *_skeleton= anim->getSkeleton();

    spSlot* slot=NULL;

    std::string jsonFile (atlasFile);

    jsonFile.replace( jsonFile.find("atlas"), 5, "json");

    cocos2d::Rect attachmentRect=getAttachmentRect(jsonFile.c_str(),attachmentName.c_str());


    for (int i = 0; i < _skeleton->slotsCount; ++i)
    {
        spSlot* i_slot = _skeleton->slots[i];

        if (strcmp(i_slot->data->name, slotName.c_str()) == 0)
        {
            slot=i_slot;
            break;
        }
    }


    if (NULL == slot)
    {
        return false;
    }

    int nType = slot->attachment->type;
    spAtlas* atlas = spAtlas_createFromFile(atlasFile.c_str(), 0);



    CCASSERT(atlas, "SkeletonRenderer::changeAttachment Error reading atlas file");

    spAtlasAttachmentLoader* atlasAttachMentLoader = spAtlasAttachmentLoader_create(atlas);
    spAttachmentLoader *attachmentLoader = &(atlasAttachMentLoader->super);

    spSkin *skin= spSkin_create("default");
    switch (nType)
    {
        case SP_ATTACHMENT_REGION:
        {
            spRegionAttachment* regionAttachmentSrc = (spRegionAttachment*)(slot->attachment);
            spAttachment* attachment = spAttachmentLoader_newAttachment(
                    attachmentLoader, NULL, SP_ATTACHMENT_REGION, slotName.c_str(), attachmentName.c_str());
            //spAttachment* attachment = spAttachmentLoader_newAttachment(attachmentLoader, NULL, SP_ATTACHMENT_REGION, "role_weapon", "weapon_104005");
            spRegionAttachment* regionAttachment = (spRegionAttachment*)attachment;
            regionAttachment->width = regionAttachmentSrc->width;
            regionAttachment->height = regionAttachmentSrc->height;
            regionAttachment->rotation = regionAttachmentSrc->rotation;
            regionAttachment->x = regionAttachmentSrc->x;
            regionAttachment->y = regionAttachmentSrc->y;
            regionAttachment->scaleX = regionAttachmentSrc->scaleX;
            regionAttachment->scaleY = regionAttachmentSrc->scaleY;
            regionAttachment->a = regionAttachmentSrc->a;
            regionAttachment->b = regionAttachmentSrc->b;
            regionAttachment->r = regionAttachmentSrc->r;
            regionAttachment->g = regionAttachmentSrc->g;


            regionAttachment->width = attachmentRect.size.width;
            regionAttachment->height = attachmentRect.size.height;


            float height=getAvatarHeight(anim);


            if (strcmp(slotName.c_str(), "toufa") == 0)
            {
                regionAttachment->x-= attachmentRect.origin.x;

            }

            spRegionAttachment_updateOffset(regionAttachment);
            spSlot_setAttachment(slot,(spAttachment*)regionAttachment);
        }
            break;
        case SP_ATTACHMENT_BOUNDING_BOX:
        {
            spBoundingBoxAttachment* boxAttachmentSrc = (spBoundingBoxAttachment*)(slot->attachment);
            spAttachment* attachment = spAttachmentLoader_newAttachment(
                    attachmentLoader, skin, SP_ATTACHMENT_BOUNDING_BOX, slotName.c_str(), attachmentName.c_str());
            spBoundingBoxAttachment* boxAttachment = (spBoundingBoxAttachment*)attachment;
            boxAttachment->verticesCount = boxAttachmentSrc->verticesCount;
            boxAttachment->vertices = boxAttachmentSrc->vertices;
            for (int i = 0; i < boxAttachmentSrc->verticesCount; i++)
                boxAttachment->vertices =boxAttachmentSrc->vertices;

           spSlot_setAttachment(slot,(spAttachment*)boxAttachment);
        }
            break;
        case SP_ATTACHMENT_MESH:
        {
            spMeshAttachment* meshAttachmentSrc = (spMeshAttachment*)(slot->attachment);
            spAttachment* attachment = spAttachmentLoader_newAttachment(
                    attachmentLoader, skin, SP_ATTACHMENT_MESH, slotName.c_str(), attachmentName.c_str());
            spMeshAttachment* meshAttachment = (spMeshAttachment*)attachment;
            meshAttachment->rendererObject = meshAttachmentSrc->rendererObject;
            meshAttachment->regionU = meshAttachmentSrc->regionU;
            meshAttachment->regionV = meshAttachmentSrc->regionV;
            meshAttachment->regionU2 = meshAttachmentSrc->regionU2;
            meshAttachment->regionV2 = meshAttachmentSrc->regionV2;
            meshAttachment->regionRotate = meshAttachmentSrc->regionRotate;
            meshAttachment->regionOffsetX = meshAttachmentSrc->regionOffsetX;
            meshAttachment->regionOffsetY = meshAttachmentSrc->regionOffsetY;
            meshAttachment->regionWidth = meshAttachmentSrc->regionWidth;
            meshAttachment->regionHeight = meshAttachmentSrc->regionHeight;
            meshAttachment->regionOriginalWidth = meshAttachmentSrc->regionOriginalWidth;
            meshAttachment->regionOriginalHeight = meshAttachmentSrc->regionOriginalHeight;

            spMeshAttachment_updateUVs(meshAttachment);

           spSlot_setAttachment(slot,(spAttachment*)meshAttachment);
        }
            break;
        case SP_ATTACHMENT_SKINNED_MESH:
        {
            spSkinnedMeshAttachment* skinnedAttachmentSrc = (spSkinnedMeshAttachment*)(slot->attachment);
            spAttachment* attachment = spAttachmentLoader_newAttachment(
                    attachmentLoader, skin, SP_ATTACHMENT_SKINNED_MESH, slotName.c_str(), attachmentName.c_str());
            spSkinnedMeshAttachment* skinnedAttachment = (spSkinnedMeshAttachment*)attachment;
            skinnedAttachment->rendererObject = skinnedAttachmentSrc->rendererObject;
            skinnedAttachment->regionU = skinnedAttachmentSrc->regionU;
            skinnedAttachment->regionV = skinnedAttachmentSrc->regionV;
            skinnedAttachment->regionU2 = skinnedAttachmentSrc->regionU2;
            skinnedAttachment->regionV2 = skinnedAttachmentSrc->regionV2;
            skinnedAttachment->regionRotate = skinnedAttachmentSrc->regionRotate;
            skinnedAttachment->regionOffsetX = skinnedAttachmentSrc->regionOffsetX;
            skinnedAttachment->regionOffsetY = skinnedAttachmentSrc->regionOffsetY;
            skinnedAttachment->regionWidth = skinnedAttachmentSrc->regionWidth;
            skinnedAttachment->regionHeight = skinnedAttachmentSrc->regionHeight;
            skinnedAttachment->regionOriginalWidth = skinnedAttachmentSrc->regionOriginalWidth;
            skinnedAttachment->regionOriginalHeight = skinnedAttachmentSrc->regionOriginalHeight;

            spSkinnedMeshAttachment_updateUVs(skinnedAttachment);

            spSlot_setAttachment(slot,(spAttachment*)skinnedAttachment);
        }
            break;
        default:
            break;
    }
    return true;
}

