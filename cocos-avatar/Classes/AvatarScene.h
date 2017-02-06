#ifndef __HELLOWORLD_SCENE_H__
#define __HELLOWORLD_SCENE_H__

#include "cocos2d.h"

#include<vector>

#include "cocos/editor-support/cocostudio/CocoStudio.h"

using namespace cocostudio;

using std::vector;

class AvatarScene : public cocos2d::Layer
{
private:
    
      void menuCloseCallback(cocos2d::Ref* pSender);
      void dataLoaded( float percent);
      Armature* getAvatarWithSide(int side);
    
public:
    // there's no 'id' in cpp, so we recommend returning the class instance pointer
    static cocos2d::Scene* createScene();
    
    static AvatarScene* getInstance();

    // Here's a difference. Method 'init' in cocos2d-x returns bool, instead of returning 'id' in cocos2d-iphone
    virtual bool init();
    
    void addAvatars();
    
    // a selector callback
    
    
  
    
    //-----------------------lib method---------------------------
    
    void setAvatarsHeight(float height); //set all avtars height
    
    void setAvatarHeight(int side,float height);//set one avatar's height with side
    
    
    void playArmature(int side ,int armatureIndex);
    
    void playArmature(int side ,const char* animationName);
    
    void loadArmatureData(bool isSandBoxVersionExists);
    
 
    void changeSuit(int side,const char* originalLayer,const char* replaceImageName);
    
    void hideArmature(int side);
    
    void midXArmature(int side);//put avatar to center x of screen
    
    void showMyAvatar(float y,float height);//hide right avatar and put left avatar to the center x of screen,setting pos y and height at last.
    
    void locateAvatars(float y,float height); //put the two avatars to the specific location with pos y and height,others value are calculated.
    
    void setAvatarsWithPosY(float y); //set all avatars's pos y;
    
    void setAvatarWithPosY(int side,float y);//set one avatar's posy with side.
    
    void loadClothesData(const char* pngFile,const char* plistFile);
    
    void stopAction(int side);
    
    void loadGenderClothesData(bool isSandBoxVersionExists);
    
    void loadClothesData(const char* suitFolderName,bool isSandBoxVersionExists);
    
    bool checkDataLoaded();
    
    void addLeftAvatar();
    void addRightAvatar();
    void removeLeftAvatar();
    void removeRightAvatar();

     //--------------------------------------------------

    
    
    
    // implement the "static create()" method manually
    CREATE_FUNC(AvatarScene);
    
public:
    
    static AvatarScene* s_Instance;
    static const char* GenderSuitFolderName;
    
private:
    
    Armature* leftArmature=nullptr,*rightArmature=nullptr;
    
    bool isDataLoaded=false;
    
    float avatarScaleY;
};

#endif // __HELLOWORLD_SCENE_H__
