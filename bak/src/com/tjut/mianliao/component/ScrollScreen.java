package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.AliImgSpec;

public class ScrollScreen {

	private static final int TYPE_TXT = 1;
	private static final int TYPE_POSTS = 2;

	private LayoutInflater mInflater;
	private Context mContext;

	private View layoutView;
	private View contentView;
	private TextView firstTv, secondTv;
	private ArrayList<String> contentArray;
	private ArrayList<CfPost> mCfPosts;
	private boolean isSecondDisplay = false;
	private Animation startAnimation, loopAnimation;
	private MyAnimationListener animationListener;
	private int mTypeScroll;
	private int mDisplayCount;
	private int mCurrentDisplay;
	private int mInflateViewId;

	public ScrollScreen(View layoutView, View contentView, TextView tv1,
			TextView tv2) {
		this(layoutView, contentView, 0);
		this.firstTv = tv1;
		this.secondTv = tv2;
	}

	public ScrollScreen(View layoutView, View contentView, int inflateViewId) {
		this.layoutView = layoutView;
		this.contentView = contentView;
		animationListener = new MyAnimationListener();
		mInflateViewId = inflateViewId;
	}

	public void triggerPageAnim(ArrayList<String> array, long delay) {
		if (startAnimation != null || layoutView.getHeight() == 0) {
			return;
		}
		mTypeScroll = TYPE_TXT;
		this.contentArray = array;
		resetAnimation(delay);
	}

	public void setContext(Context context) {
		mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(context);
	}

	public void triggerPageAnim2(ArrayList<CfPost> array, long delay) {
		if (startAnimation != null || layoutView.getHeight() == 0
				|| array == null || array.size() == 0) {
			return;
		}
		mTypeScroll = TYPE_POSTS;
		this.mCfPosts = array;
		mDisplayCount = (int) Math.ceil((double) (array.size() * 1.0) / 2.0);
		resetAnimation(delay);
	}

	private void resetAnimation(long delay) {
		startAnimation = new TranslateAnimation(0, 0, 0,
				-layoutView.getHeight());
		startAnimation.setStartOffset(delay);
		startAnimation.setFillAfter(true);
		startAnimation.setDuration(4000L);
		startAnimation.setAnimationListener(animationListener);
		contentView.startAnimation(startAnimation);
	}

	private void setTextContent() {
		isSecondDisplay = !isSecondDisplay;
		if (contentArray == null || contentArray.size() == 0) {
			return;
		}

		this.firstTv.setText("");
		this.secondTv.setText("");

		switch (contentArray.size()) {
		case 1:
			this.firstTv.setText(contentArray.get(0));
			break;
		case 2:
			this.firstTv.setText(contentArray.get(0));
			this.secondTv.setText(contentArray.get(1));
			break;
		case 3:
			if (!isSecondDisplay) {
				this.firstTv.setText(contentArray.get(0));
				this.secondTv.setText(contentArray.get(1));
				break;
			}
			this.firstTv.setText(contentArray.get(2));

			break;
		default:
			if (!isSecondDisplay) {
				this.firstTv.setText(contentArray.get(0));
				this.secondTv.setText(contentArray.get(1));
				break;
			}

			this.firstTv.setText(contentArray.get(2));
			this.secondTv.setText(contentArray.get(3));

			break;
		}

	}

	private void setPostContent() {
		LinearLayout mContentView = ((LinearLayout) contentView);
		mContentView.removeAllViews();
		System.out.print("------ scroll screen");
		mContentView.addView(getView(mCurrentDisplay * 2));
		if (mCurrentDisplay * 2 + 1 <= mCfPosts.size()) {
			mContentView.addView(getView(mCurrentDisplay * 2 + 1));
		}
		mCurrentDisplay++;
		if (mCurrentDisplay > mDisplayCount) {
			mCurrentDisplay = 0;
		}
	}

	private View getView(int position) {
		CfPost post = mCfPosts.get(position);
		View view = mInflater.inflate(mInflateViewId, null);
		TextView mTvFlag = (TextView) view.findViewById(R.id.tv_flag);
		TextView mTvTitle = (TextView) view.findViewById(R.id.tv_post_title);
		AvatarView avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
		ImageView image = (ImageView) view.findViewById(R.id.iv_image);
		mTvFlag.setText("NO." + (position + 1));
		mTvTitle.setText(post.content);
		Picasso.with(mContext).load(post.userInfo.getAvatar()).into(avatar);
		if (post.images != null && post.images.size() > 0) {
			Picasso.with(mContext)
					.load(getSmallImageUrl(post.images.get(0).image))
					.into(image);
			image.setVisibility(View.VISIBLE);
		} else {
			image.setVisibility(View.GONE);
		}
		return view;
	}

	private String getSmallImageUrl(String url) {
		return AliImgSpec.POST_PHOTO.makeUrlSingleImg(url);
	}

	private class MyAnimationListener implements AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			if (mTypeScroll == TYPE_POSTS) {
				setPostContent();
			} else {
				setTextContent();
			}
			contentView.setY(0);
			loopAnimation = new TranslateAnimation(0, 0, 0,
					-layoutView.getHeight() * 2);
			loopAnimation.setFillAfter(true);
			loopAnimation.setDuration(4000L);
			loopAnimation.setStartOffset(3000L);
			loopAnimation.setAnimationListener(animationListener);
			contentView.startAnimation(loopAnimation);

		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
		}
	};

}
