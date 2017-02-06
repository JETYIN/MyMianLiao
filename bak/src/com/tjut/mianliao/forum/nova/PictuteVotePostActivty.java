package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.ChooseTopicActivity;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.image.PreviewImageActivity;
import com.tjut.mianliao.util.Utils;

public class PictuteVotePostActivty extends BasePostActivity {

    private ImageView mImageView1, mImageView2;
    private FrameLayout mFlView1, mFlView2;

    private boolean mSetImg1, mHasImg1, mHasImg2;
    private int mForumId, mThreadType;
    private boolean mIsSpecial;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mThreadType = mChannelInfo.threadType;
        getTitleBar().setTitle(R.string.post_pic_vote_title);
        mIvRefFriend.setVisibility(View.VISIBLE);
        if (!mIsNightMode) {
            mIvTopic.setVisibility(View.VISIBLE);
        }
        mIvTopic.setOnClickListener(this);

        if (mThreadType == CfPost.THREAD_TYPE_PIC_VOTE) {
            mInflater.inflate(R.layout.channel_vote, mFlFooter);
            mGvImages.setVisibility(View.GONE);
            mIsSpecial = true;
        }

        if (mThreadType == CfPost.THREAD_TYPE_TXT) {
            mFlHeader.setVisibility(View.GONE);
            mFlFooter.setVisibility(View.GONE);
        }

        mImageView1 = (ImageView) findViewById(R.id.iv_photo1);
        mImageView2 = (ImageView) findViewById(R.id.iv_photo2);
        mFlView1 = (FrameLayout) findViewById(R.id.fl_vote1);
        mFlView2 = (FrameLayout) findViewById(R.id.fl_vote2);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo1:
            case R.id.fl_vote1:
                mSetImg1 = true;
                chooseImage((Image) mImageView1.getTag());
                break;
            case R.id.iv_photo2:
            case R.id.fl_vote2:
                if (!mHasImg2 && (!mHasImg1 || mIsCancleChooseImage)) {
                    toast("请先选择第一张图片");
                    return;
                }
                mSetImg1 = false;
                chooseImage((Image) mImageView2.getTag());
                break;
            case R.id.iv_topic:
                Intent intent = new Intent(PictuteVotePostActivty.this, ChooseTopicActivity.class);
//                intent.putExtra(ChooseTopicActivity.FORUM_ID, mChannelInfo.forumId);
                startActivityForResult(intent, TxtVotePostActivity.REQUEST_TOPIC);
                break;

            default:
                super.onClick(v);
                break;
        }
    }
    
    @Override
    protected boolean hasUpdate() {
        return (mHasImg1 || mHasImg2) || super.hasUpdate();
    }
    
    @Override
    protected boolean isStateReady() {
    	if (mThreadType == CfPost.THREAD_TYPE_PIC_VOTE) {
    		return super.isStateReady() && isPictureReady();
    	} else {
    		return super.isStateReady();
    	}
    }
    
    private boolean isPictureReady() {
    	if (mFlView1.getVisibility() != View.GONE) {
    		toast("要选择完2张图片才可以哦!");
    		return false;
    	}
    	return true;
    }

    @Override
    protected void showImage(Image image, Bitmap bm) {
        super.showImage(image, bm);
        if (mThreadType == CfPost.THREAD_TYPE_PIC_VOTE) {
            ImageView iv = mSetImg1 ? mImageView1 : mImageView2;
            FrameLayout fl = mSetImg1 ? mFlView1 : mFlView2;
            if (bm == null) {
                iv.setVisibility(View.GONE);
                fl.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.VISIBLE);
                fl.setVisibility(View.GONE);
                Bitmap bitmap = Utils.fileToBitmap(image.file);
                iv.setImageBitmap(bitmap);
            }
            if (mSetImg1) {
                mHasImg1 = true;
            } else {
                mHasImg2 = true;
            }
            iv.setTag(image);
        }
    }

    @Override
    protected HashMap<String, String> getParams() {
        HashMap<String, String> params = super.getParams();
        params.put("forum_id", String.valueOf(mForumId));
        params.put("thread_type", String.valueOf(mThreadType));
        if (mMIH.hasUpdate()) {
            StringBuilder sb = new StringBuilder();
            int size = mMIH.getImages().size();
            for (int i = 0; i < size; i++) {
                Image image = mMIH.getImages().get(i);
                if (sb.length() > 0) {
                    sb.append(",");
                }
                if (image.id > 0) {
                    sb.append(image.id);
                } else {
                    sb.append("new_image_").append(i);
                }
            }
            params.put("images", sb.toString());
        }

        return params;
    }

    @Override
    protected HashMap<String, String> getFiles() {
        HashMap<String, String> files = super.getFiles();
        if (mMIH.hasUpdate()) {
            int size = mMIH.getImages().size();
            for (int i = 0; i < size; i++) {
                Image image = mMIH.getImages().get(i);
                if (image.id == 0) {
                    files.put("new_image_" + i, image.file);
                }
            }
        }
        return files;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PreviewImageActivity.PREVIEW_REQUEST_CODE) {
            ArrayList<String> urls = data.getStringArrayListExtra(
                    PreviewImageActivity.EXTRA_IMAGES_LIST);
            if (mIsSpecial) {
                if (urls == null || urls.size() == 0) {
                    if (mSetImg1) {
                        mImageView1.setVisibility(View.GONE);
                        mFlView1.setVisibility(View.VISIBLE);
                        mMIH.deleteImage(0);
                        mHasImg1 = false;
                        mImageView1.setTag(null);
                    } else {
                        mImageView2.setVisibility(View.GONE);
                        mFlView2.setVisibility(View.VISIBLE);
                        mMIH.deleteImage(mHasImg1 ? 1 : 0);
                        mHasImg2 = false;
                        mImageView2.setTag(null);
                    }
                }
            } else {
                if (urls != null) {
                    mMIH.resetImages(getImagesByUrls(urls));
                }
                mImageAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == TxtVotePostActivity.REQUEST_TOPIC && resultCode == RESULT_OK){
            super.onActivityResult(requestCode, resultCode, data);
            TopicInfo mTpInfo = data.getParcelableExtra(ChooseTopicActivity.TOPIC_INFO);
            String mTopicString = "#" + mTpInfo.name + "#";
            mTopicString = mTopicString.replaceAll("(@|﹫|＠)", "");
            int index = mEtContent.getSelectionStart();
            Editable edit = mEtContent.getEditableText();
            edit.insert(index,mTopicString);
//            Utils.getColoredText(mEtContent.getText(), mTopicString, R.color.btn_blue,false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected boolean setCanRefFriend() {
        return true;
    }
}
