//
//  DayNightAnim.cpp
//  MIMEChat3
//
//  Created by nick on 15/9/17.
//
//

#include "DayNightAnim.h"
#include "AvatarScene.h"

#ifdef CC_TARGET_OS_IPHONE
#include "TMLUtil.h"
#else
#include "Java_com_tjut_mianliao_cocos2dx_CocosAvatarView.h"
#endif

enum AnimPhase {
	AnimPhaseCOUNTDOWN, AnimPhaseSUNRISE, AnimPhaseMOON
};

DayNightAnim::DayNightAnim(AvatarScene *scene) {
	this->scene = scene;
}

void DayNightAnim::removeElements() {
	if (night_sprite != nullptr) {
		night_sprite->removeFromParent();
		night_sprite = nullptr;
	}

}

void DayNightAnim::assignAnin() {
	this->removeElements();
	elephantArmature = nullptr;
	moonArmature = nullptr;
	sunArmature = nullptr;
	scene->hideArmature(0);
	scene->hideArmature(1);

	anim_phase = AnimPhaseSUNRISE;
	actionFunc = CallFunc::create([&] {

		anim_phase++;
		doAction();

	});

	actionFunc->retain();

	night_sprite = Sprite::create("cocos/night_bg.png");
	night_sprite->setAnchorPoint(Vec2(0, 0));
	scene->addChild(night_sprite, 2);

	day_sprite = Sprite::create("cocos/date_bg.png");
	day_sprite->setAnchorPoint(Vec2(0, 0));
	night_sprite->addChild(day_sprite);

#ifdef CC_TARGET_OS_IPHONE
	buttonSkip= Button::create();
	buttonSkip->setTouchEnabled(true);
	buttonSkip->loadTextures("cocos/button_skip.png", "", "");
	buttonSkip->addTouchEventListener(CC_CALLBACK_2(DayNightAnim::ButtentouchEvent,this));
	buttonSkip->setPosition(Vec2(600, 100));
	buttonSkip->cocos2d::Node::setScale(0.5);
	night_sprite->addChild(buttonSkip,3);
#endif

	//    label = Label::createWithSystemFont("Hello World1","Arial", 400);
	//    label->setString("3");
	//    label->setAnchorPoint(Vec2(0.5,0.5));
	//    label->setPosition(Vec2(375, 900));
	//    label->setColor(Color3B(100,100,100));
	//    night_sprite->addChild(label);

	this->doAction();

}

void DayNightAnim::ButtentouchEvent(Ref* ref, Widget::TouchEventType type) {

	switch (type) {

	case Widget::TouchEventType::ENDED: {
		if (elephantArmature != nullptr) {
			elephantArmature->stopAllActions();
		}

		if (sunArmature != nullptr) {
			sunArmature->stopAllActions();
		}

		if (moonArmature != nullptr) {
			moonArmature->stopAllActions();
		}

		animFinished();

	}

		break;

	default:
		break;
	}
}

void DayNightAnim::doAction() {
	switch (anim_phase) {
	case AnimPhaseCOUNTDOWN: {
		//label
		CallFunc *func1 = CallFunc::create([&] {

			label->setString("2");

		});

		CallFunc *func2 = CallFunc::create([&] {
			label->setString("1");

		});

		CallFunc *func3 = CallFunc::create([&] {
			label->removeFromParent();

		});

		Sequence *seqence = Sequence::create(DelayTime::create(1.0f), func1,
				DelayTime::create(1.0f), func2, DelayTime::create(1.0f), func3,
				actionFunc, NULL);
		scene->runAction(seqence);

		//elephant

		elephantArmature = SkeletonAnimation::createWithData(
				scene->elephantData);
		elephantArmature->setAnchorPoint(Vec2(0.5, 1));
		night_sprite->addChild(elephantArmature);

		elephantArmature->setPosition(Vec2(375, 0));

		elephantArmature->setAnimation(0, "animation", false);
		std::function<void(int trackIndex, int loopCount)> completeListener =
				[=](int trackIndex, int loopCount)
				{
					CallFunc *removeCall=CallFunc::create([&] {
								elephantArmature->removeFromParent();

							});
					elephantArmature->runAction(removeCall);

				};

		elephantArmature->setCompleteListener(completeListener);

	}

		break;
	case AnimPhaseSUNRISE: {
		sunArmature = SkeletonAnimation::createWithData(scene->sunData);

		sunArmature->setPosition(Vec2(950, 100));
		sunArmature->setAnchorPoint(Vec2(0.5, 0.5));
		night_sprite->addChild(sunArmature);

		ccBezierConfig beziercofig;
		beziercofig.controlPoint_1 = Vec2(800, -100);
		beziercofig.controlPoint_2 = Vec2(900, 800);
		beziercofig.endPosition = Vec2(375, 500);

		BezierTo *bezier = BezierTo::create(1.0f, beziercofig);

		CallFunc *call = CallFunc::create([&] {
			sunArmature->setAnimation(0, "animation", true);

		});

		sunArmature->runAction(
				Sequence::create(bezier, call, DelayTime::create(1.0f),
						actionFunc, NULL));
	}
		break;
	case AnimPhaseMOON: {
		//moon
		CallFunc *call1 = CallFunc::create([&] {

			day_sprite->removeFromParent();

		});

		CallFunc *call2 = CallFunc::create([&] {
			this->animFinished();

		});

		moonArmature = SkeletonAnimation::createWithData(scene->moonData);
		moonArmature->setAnimation(0, "animation", true);
		moonArmature->setScaleX(-1);
		moonArmature->setAnchorPoint(Vec2(0.5, 0.5));
		moonArmature->setPosition(Vec2(375, 1600));
		night_sprite->addChild(moonArmature);

		moonArmature->runAction(
				Sequence::create(MoveTo::create(1.0f, Vec2(375, 500)), call1,
						DelayTime::create(2.0f), call2, NULL));

		//sun

		ccBezierConfig beziercofig;
		beziercofig.controlPoint_1 = Vec2(100, 300);
		beziercofig.controlPoint_2 = Vec2(200, 700);
		beziercofig.endPosition = Vec2(-380, 100);

		BezierTo *bezier = BezierTo::create(1.0f, beziercofig);

		sunArmature->runAction(Sequence::create(bezier, NULL));
	}
		break;

	default:
		break;
	}
}

void DayNightAnim::animFinished() {

#ifdef CC_TARGET_OS_IPHONE
	[TMLUtil afterChangeDayAnim];
#else
	onNightAnimFinishJNI();
#endif

}

