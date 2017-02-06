package com.tjut.mianliao.anim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.util.SysOSAPI;
import com.tjut.mianliao.R;
import com.tjut.mianliao.live.LiveGift;

public class GiftAnimView extends FrameLayout {

	private Context mContext;

	private LayoutInflater mInflater;

	private FrameLayout mFlContainerFirst;

	private FrameLayout mFlContainerSecond;

	private SparseArray<SparseArray<GiftControllerInfo>> mSparseArray;
	private SparseArray<SparseArray<Integer>> mGiftCountArray;

	private List<GiftControllerInfo> mGiftInfos;

	private int mGiftIconLeftMargin;

	private long mLastInvalidateTime;

	private Timer mTimer;
	private TimerTask mTimeTask;

	public GiftAnimView(Context context) {
		this(context, null);
	}

	public GiftAnimView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GiftAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		mSparseArray = new SparseArray<>();
		mGiftCountArray = new SparseArray<>();
		mGiftInfos = new ArrayList<>();
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.layout_gift_anim, this);
		mFlContainerFirst = (FrameLayout) findViewById(R.id.view_container_first);
		mFlContainerSecond = (FrameLayout) findViewById(R.id.view_container_second);
		mGiftIconLeftMargin = context.getResources().getDimensionPixelSize(
				R.dimen.gift_icon_left_margin);
	}

	/**
	 * 添加礼物动画对象
	 *
	 * @param gift
	 */
	public void addGift(GiftControllerInfo gift) {
		if (gift == null) {
			return;
		}
		// 处理数据并处理礼物的先后顺序
		SparseArray<GiftControllerInfo> map = mSparseArray.get(gift.giftId);
		if (map == null) {
			SparseArray<GiftControllerInfo> giftfMap = new SparseArray<>();
			giftfMap.put(gift.userId, gift);
			mSparseArray.put(gift.giftId, giftfMap);
			mGiftInfos.add(gift);
			updateGiftAnimCount(gift);
			startGiftAnimation();
			fresh(100);
		} else {
			GiftControllerInfo giftInfo = map.get(gift.userId);
			if (giftInfo == null) {
				map.put(gift.userId, gift);
				mGiftInfos.add(gift);
				updateGiftAnimCount(gift);
				startGiftAnimation();
			} else {
				giftInfo.giftCount++;
				if (giftInfo.animPlayCount + 1 == giftInfo.giftCount) {
					startCountViewAnim(giftInfo);
				}

			}
			fresh(100);
		}
	}

	private void updateGiftAnimCount(GiftControllerInfo gift) {
		SparseArray<Integer> giftCountMap = mGiftCountArray.get(gift.giftId);
		if (giftCountMap == null) {
			gift.animPlayCount = 0;
		} else {
			Integer count = giftCountMap.get(gift.userId);
			if (count != null && count.intValue() > 0) {
				gift.animPlayCount = count;
			} else {
				gift.animPlayCount = 0;
			}
		}
		((TextView)gift.countView).setText("X" + (gift.animPlayCount + 1));
	}

	private void fresh(long time) {
		if (mLastInvalidateTime > 0 && (System.currentTimeMillis() - mLastInvalidateTime > time)) {
			requestLayout();
		}
		mLastInvalidateTime = System.currentTimeMillis();
	}

	/**
	 * 开始执行礼物动画效果
	 *
	 */
	private void startGiftAnimation() {
		if (mGiftInfos.size() == 0) {
			return;
		}
		GiftControllerInfo info = mGiftInfos.get(0);
		if (info.giftPosition != 0) {
			if (mGiftInfos.size() > 1) {
				info = mGiftInfos.get(1);
			} else {
				return;
			}
		}
		final GiftControllerInfo giftInfo = info;
		if (mFlContainerFirst.getChildCount() == 0) {// 在第一个动画通道执行动画
			giftInfo.allView.setVisibility(View.GONE);
			mFlContainerFirst.removeAllViews();
			mFlContainerFirst.addView(giftInfo.allView);
			giftInfo.giftPosition = 1;
			// first -->allView translate animation
			if (getContext() instanceof Activity) {
				((Activity)getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						giftAnimation(giftInfo);
					}
				});
			}

		} else if (mFlContainerSecond.getChildCount() == 0) {// 在第二个动画通道执行动画
			giftInfo.allView.setVisibility(View.GONE);
			mFlContainerSecond.removeAllViews();
			mFlContainerSecond.addView(giftInfo.allView);
			giftInfo.giftPosition = 2;
			// first -->allView translate animation
			if (getContext() instanceof Activity) {
				((Activity)getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						giftAnimation(giftInfo);
					}
				});
			}
		} else {
			fresh(300);
		}
	}

	private void giftAnimation(final GiftControllerInfo giftInfo) {
		Animation animIn = AnimationUtils.loadAnimation(getContext(),
				R.anim.anim_gift_layout_in);
		animIn.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// giftView translate animation
				TranslateAnimation ta = new TranslateAnimation(-mGiftIconLeftMargin, 0, 0, 0);
				ta.setDuration(500);
				ta.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
//						giftInfo.animPlayCount = 1;
						startCountViewAnim(giftInfo);
					}
				});
				giftInfo.giftView.startAnimation(ta);
				giftInfo.giftView.setVisibility(View.VISIBLE);
			}
		});
		giftInfo.allView.startAnimation(animIn);
		giftInfo.allView.setVisibility(View.VISIBLE);
	}

	private void startCountViewAnim(final GiftControllerInfo giftInfo) {
		if (giftInfo.animPlayCount >= giftInfo.giftCount) {
			giftInfo.lastTime = System.currentTimeMillis();
			giftInfo.allView.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (System.currentTimeMillis() - giftInfo.lastTime >= 3500) {
						SparseArray<GiftControllerInfo> map = mSparseArray.get(giftInfo.giftId);
						map.remove(giftInfo.userId);
						if (map.size() == 0) {
							mSparseArray.remove(giftInfo.giftId);
						}
						mGiftInfos.remove(giftInfo);
						saveGiftCount(giftInfo);
						Animation outAnim = AnimationUtils.loadAnimation(
								getContext(), R.anim.anim_gift_layout_out);
						outAnim.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								giftInfo.allView.setVisibility(View.GONE);
								if (giftInfo.giftPosition == 1) {
									mFlContainerFirst.removeAllViews();
								} else {
									mFlContainerSecond.removeAllViews();
								}
								startGiftAnimation();
							}
						});
						((View) (giftInfo.allView.getParent())).startAnimation(outAnim);
					}
				}
			}, 3500);
		} else {
			playHeartbeatAnimation(giftInfo.countView, new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {}

				@Override
				public void onAnimationRepeat(Animation animation) {}

				@Override
				public void onAnimationEnd(Animation animation) {
					giftInfo.animPlayCount++;
					((TextView)giftInfo.countView).setText("X" + giftInfo.animPlayCount);
					startCountViewAnim(giftInfo);
				}
			});
		}
	}


	public static void playHeartbeatAnimation(final View view,
											  final Animation.AnimationListener listener) {
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new ScaleAnimation(1.0f, 5f, 1.0f, 5f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f));
		animationSet.addAnimation(new AlphaAnimation(1.0f, 0.4f));

		animationSet.setDuration(50);
		animationSet.setInterpolator(new AccelerateInterpolator());
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				AnimationSet animationSet = getAnimationSet(5f, 0.5f, 5f, 0.5f,
						100);
				animationSet.setInterpolator(new DecelerateInterpolator());
				animationSet.addAnimation(new AlphaAnimation(0.4f, 1.0f));
				animationSet.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						AnimationSet animationSet = getAnimationSet(0.5f, 1.3f,
								0.5f, 1.3f, 100);
						animationSet.setInterpolator(new AccelerateInterpolator());
						animationSet.addAnimation(new AlphaAnimation(1.0f, 0.4f));
						animationSet.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(
									Animation animation) {
							}

							@Override
							public void onAnimationRepeat(
									Animation animation) {
							}

							@Override
							public void onAnimationEnd(
									Animation animation) {
								AnimationSet animationSet = getAnimationSet(
										1.3f, 1.0f, 1.3f, 1.0f, 100);
								animationSet.setInterpolator(new DecelerateInterpolator());
								animationSet.addAnimation(new AlphaAnimation(
										0.4f, 1.0f));
								animationSet.setAnimationListener(new AnimationListener() {

									@Override
									public void onAnimationStart(
											Animation animation) {
									}

									@Override
									public void onAnimationRepeat(
											Animation animation) {
									}

									@Override
									public void onAnimationEnd(
											Animation animation) {
										if (listener != null) {
											listener.onAnimationEnd(animation);
										}
									}
								});
								view.startAnimation(animationSet);
							}
						});
						view.startAnimation(animationSet);
					}
				});
				view.startAnimation(animationSet);
			}
		});

		view.startAnimation(animationSet);
		view.setVisibility(View.VISIBLE);
	}

	private static AnimationSet getAnimationSet(float fromX, float toX,
												float fromY, float toY, int duration) {
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new ScaleAnimation(fromX, toX, fromY, toY,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f));

		animationSet.setDuration(duration);
		animationSet.setFillAfter(false);
		return animationSet;
	}

	private void saveGiftCount(GiftControllerInfo gift) {
		SparseArray<Integer> map = mGiftCountArray.get(gift.giftId);
		if (map == null) {
			SparseArray<Integer> giftfMap = new SparseArray<>();
			giftfMap.put(gift.userId, gift.giftCount);
			mGiftCountArray.put(gift.giftId, giftfMap);
		} else {
			map.put(gift.userId, gift.giftCount);
		}
	}

	public void removeGiftCountInfo(LiveGift gift, int userId) {
		SparseArray<Integer> map = mGiftCountArray.get(gift.giftId);
		if (map != null) {
			map.remove(userId);
		}
	}

}
