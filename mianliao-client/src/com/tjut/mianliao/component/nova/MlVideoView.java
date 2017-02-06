package com.tjut.mianliao.component.nova;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.image.VideoPlayerActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.videoplay.VideoPlayActivity;

public class MlVideoView extends FrameLayout implements OnClickListener {

    private LayoutInflater mInflater;

    private Context mContext;
    private Activity mActivity;

    private ImageView mVideoThumnnail;
    private ImageView mIvPlay;
    
    private CfPost mCurrentPost;

    public MlVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context.getApplicationContext();
        init(context);
    }

    private void init(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.view_video_layout, this);
        mVideoThumnnail = (ImageView) findViewById(R.id.iv_video);
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mIvPlay.setOnClickListener(this);
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }
    
    public void show(CfPost post) {
        mCurrentPost = post;
        showVideoView();
    }
    
    private void showVideoView() {
        if (TextUtils.isEmpty(mCurrentPost.videoThumbnail)) {
            Picasso.with(mContext)
                .load(R.drawable.bg_default_big_day)
                .into(mVideoThumnnail);
        } else {
            Picasso.with(mContext)
                .load(getSmallImage(mCurrentPost.videoThumbnail))
                .placeholder(R.drawable.bg_img_loading)
                .into(mVideoThumnnail);
        }
    }

    private String getSmallImage(String url) {
        if (url.contains("tjt-post.oss-cn-hangzhou.aliyuncs.com")) {
            return url;
        }
        return AliImgSpec.POST_PHOTO.makeUrlSingleImg(url);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                // start to play in video play activity
                startPlayVideo();
                break;

            default:
                break;
        }
    }

    private void startPlayVideo() {
        if (mActivity == null) {
            return;
        }
        Intent intent = new Intent(mActivity, VideoPlayActivity.class);
        intent.putExtra(VideoPlayerActivity.EXT_FILE_PATH, mCurrentPost.videoUrl);
        mActivity.startActivity(intent);
    }

}
