package com.tjut.mianliao.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.FeedbackRecord;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2<ListView> {
    private static final long MAX_SESSION_INTERVAL = 15 * 60 * 1000;

    private EditText mContentView;
    private ImageView mIvImage;

    private GetImageHelper mGetImageHelper;
    private String mImageFile;

    private PullToRefreshListView mPtrlvFeedbacks;
    private BaseAdapter mAdapter;
    private ArrayList<FeedbackRecord> mFeedbacks = new ArrayList<FeedbackRecord>();
    private UserInfo mMyInfo;

    private static Comparator<FeedbackRecord> sComparator = new Comparator<FeedbackRecord>() {
        @Override
        public int compare(FeedbackRecord lhs, FeedbackRecord rhs) {
            return (int) Math.signum(lhs.time - rhs.time);
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_feedback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.fbk_title, null);

        mContentView = (EditText) findViewById(R.id.edt_feedback);

        mIvImage = (ImageView) findViewById(R.id.iv_fb_image);

        mGetImageHelper = new GetImageHelper(this, new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    mIvImage.setImageBitmap(bm);
                    mIvImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mImageFile = imageFile;
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                if (success) {
                    Bitmap bm = BitmapFactory.decodeFile(images.get(0));
                    mIvImage.setImageBitmap(bm);
                    mIvImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mImageFile = images.get(0);
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }
        });

        mMyInfo = AccountInfo.getInstance(this).getUserInfo();

        mAdapter = new FeedbackAdapter();
        mPtrlvFeedbacks = (PullToRefreshListView) findViewById(R.id.ptrlv_feedbacks);
        mPtrlvFeedbacks.setAdapter(mAdapter);
        mPtrlvFeedbacks.setOnRefreshListener(this);

        mContentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPtrlvFeedbacks.getRefreshableView().setSelection(mFeedbacks.size() - 1);
                }
            }
        });

        mPtrlvFeedbacks.setRefreshing(Mode.PULL_FROM_START);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_fb_image:
                mGetImageHelper.getImage(true, 1);
                break;
            case R.id.btn_submit:
                submitFeedback(mContentView.getText());
                break;
            case R.id.iv_image:
                if (v.getTag() != null && v.getTag() instanceof String) {
                    Intent i = new Intent(this, ImageActivity.class);
                    i.putExtra(ImageActivity.EXTRA_IMAGE_URL, (String) v.getTag());
                    startActivity(i);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchFeedback(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchFeedback(false);
    }

    private void fetchFeedback(boolean refresh) {
        int offset = refresh ? 0 : mFeedbacks.size();
        new ListFeedBackTask(offset).executeLong();
    }

    private void submitFeedback(CharSequence feedback) {
        if (!TextUtils.isEmpty(feedback)) {
            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> files = null;
            params.putAll(Utils.getProductInfoMap(this));
            params.put("feedback", feedback.toString().trim());
            if (!TextUtils.isEmpty(mImageFile)) {
                files = new HashMap<String, String>();
                files.put("image", mImageFile);
            }
            new SubmitFeedbackTask(params, files).executeLong();
        }
    }

    private class FeedbackAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFeedbacks.size();
        }

        @Override
        public Object getItem(int position) {
            return mFeedbacks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * 2 type of views: 0) Chat from me; 1) Chat from others.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return mFeedbacks.get(position).isReply ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup root) {
            View view;
            FeedbackRecord feedback = mFeedbacks.get(position);
            if (convertView != null) {
                view = convertView;
            } else {
                int layoutId = feedback.isReply ? R.layout.list_item_feedback_sys :
                        R.layout.list_item_feedback;
                view = getLayoutInflater().inflate(layoutId, root, false);
            }

            // set avatar
            ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.iv_avatar);
            if (!feedback.isReply) {
                ivAvatar.setImage(mMyInfo.getAvatar(), mMyInfo.defaultAvatar());
            } else {
                ivAvatar.setImageResource(R.drawable.ic_launcher);
            }

            // set image
            ProImageView ivImage = (ProImageView) view.findViewById(R.id.iv_image);
            if (!TextUtils.isEmpty(feedback.thumb)) {
                ivImage.setVisibility(View.VISIBLE);
                ivImage.setImage(feedback.thumb, R.drawable.bg_img_loading);
                ivImage.setOnClickListener(FeedbackActivity.this);
                ivImage.setTag(feedback.image);
            } else {
                ivImage.setVisibility(View.GONE);
            }

            // set time info
            TextView tvTime = (TextView) view.findViewById(R.id.tv_time);
            if (position == 0 ||
                    (feedback.time - mFeedbacks.get(position - 1).time) > MAX_SESSION_INTERVAL) {
                tvTime.setVisibility(View.VISIBLE);
                tvTime.setText(Utils.getTimeDesc(getApplicationContext(), feedback.time));
            } else {
                tvTime.setVisibility(View.GONE);
            }

            // set content
            TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
            tvMessage.setText(feedback.content);

            return view;
        }
    }

    private class ListFeedBackTask extends MsTask {
        private int mOffset;

        public ListFeedBackTask(int offset) {
            super(getApplicationContext(), MsRequest.FEEDBACK_LIST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrlvFeedbacks.onRefreshComplete();
            if (response.isSuccessful()) {
                if (mOffset == 0) {
                    mFeedbacks.clear();
                }
                mFeedbacks.addAll(JsonUtil.getArray(
                        response.getJsonArray(), FeedbackRecord.TRANSFORMER));
                Collections.sort(mFeedbacks, sComparator);
                mAdapter.notifyDataSetChanged();
            } else {
                response.showFailInfo(getRefContext(), R.string.fbk_list_fail);
            }
        }
    }

    private class SubmitFeedbackTask extends MsMhpTask {
        public SubmitFeedbackTask(HashMap<String, String> parameters,
                HashMap<String, String> files) {
            super(getApplicationContext(), MsRequest.FEEDBACK, parameters, files);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            findViewById(R.id.btn_submit).setEnabled(false);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            findViewById(R.id.btn_submit).setEnabled(true);
            if (response.isSuccessful()) {
                toast(R.string.fbk_success);
                mContentView.setText("");
                mIvImage.setImageResource(R.drawable.ic_camera);

                FeedbackRecord feedback = FeedbackRecord.fromJson(response.getJsonObject());
                if (feedback != null) {
                    mFeedbacks.add(feedback);
                    Collections.sort(mFeedbacks, sComparator);
                    mAdapter.notifyDataSetChanged();
                    mPtrlvFeedbacks.getRefreshableView().setSelection(mFeedbacks.size() - 1);
                }
            } else {
                response.showFailInfo(getRefContext(), R.string.fbk_fail);
            }
        }
    }
}
