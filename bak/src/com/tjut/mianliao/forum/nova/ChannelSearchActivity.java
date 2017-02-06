package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

@SuppressLint("NewApi")
public class ChannelSearchActivity extends BaseActivity implements OnClickListener, TextWatcher,
        OnEditorActionListener, Runnable, OnFocusChangeListener, OnTouchListener {

    private static final long DELAY_MILLS = 500;

    public static final int SEARCH_ATTR_VOICE = CfPost.THREAD_TYPE_PIC_VOICE;
    public static final int SEARCH_ATTR_PIC = CfPost.THREAD_TYPE_PIC_TXT;
    public static final int SEARCH_ATTR_TEXT = CfPost.THREAD_TYPE_TXT;

    private TextView mTvVoice, mTvPic, mTvText;
    private View mSearchView;
    private EditText mTvSearch;
    private ImageView mIvSearchClear;
    private InputMethodManager mIMManager;
    private Handler mHandler;
    private String mSearchKey;
    private ListView mLvSearchChl;
    private SearchChannelAdapter mAdapter;
    private LinearLayout mLlSearch;
    private ArrayList<ChannelInfo> mChannelInfos;
    private ArrayList<ChannelTagInfo> mChannelType;
    private LinearLayout mLlSearchbyOther;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_channel_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TitleBar titleBar = getTitleBar();
        mChannelInfos = new ArrayList<>();
        mChannelType = new ArrayList<>();
        mTvSearch = (EditText) titleBar.findViewById(R.id.et_search_content);
        mIvSearchClear = (ImageView) titleBar.findViewById(R.id.iv_search_clear);
        mTvSearch.setHintTextColor(Color.GRAY);
        mTvSearch.setTextColor(0XFFA9A9A9);
        mTvSearch.addTextChangedListener(this);
        mTvSearch.setOnEditorActionListener(this);
        mTvSearch.setOnFocusChangeListener(this);
        mIvSearchClear.setOnClickListener(this);

        mTvVoice = (TextView) findViewById(R.id.tv_voice_search);
        mTvPic = (TextView) findViewById(R.id.tv_pic_search);
        mTvText = (TextView) findViewById(R.id.tv_text_search);
        mLvSearchChl = (ListView) findViewById(R.id.lv_search_channel);
        mLlSearch = (LinearLayout) findViewById(R.id.ll_search_by_other);
        mLlSearchbyOther = (LinearLayout) findViewById(R.id.ll_search_by_other);
        findViewById(R.id.sv_channel_search).setOnTouchListener(this);
        mLvSearchChl.setOnTouchListener(this);
        
        mHandler = new Handler();
        mAdapter = new SearchChannelAdapter();
        mLvSearchChl.setAdapter(mAdapter);

        mTvVoice.setOnClickListener(this);
        mTvPic.setOnClickListener(this);
        mTvText.setOnClickListener(this);
        mIMManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        new ChannelTypeTask().executeLong();
    }

    @Override
    protected TitleBar getTitleBar() {
        TitleBar titleBar = super.getTitleBar();
        mSearchView = mInflater.inflate(R.layout.channel_search_bar, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        lp.setMargins(100, 0, 100, 0);
        mSearchView.setLayoutParams(lp);
        titleBar.addView(mSearchView);
        return titleBar;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_voice_search:
                startSearch(String.valueOf(SEARCH_ATTR_VOICE), null);
                break;
            case R.id.tv_pic_search:
                startSearch(String.valueOf(SEARCH_ATTR_PIC), null);
                break;
            case R.id.tv_text_search:
                startSearch(String.valueOf(SEARCH_ATTR_TEXT), null);
                break;
            case R.id.iv_search_clear:
                doClear();
                break;
            case R.id.ll_channel_neight:
                mTvSearch.setText("");
                hideInput();
                showChannelInfoDetail((ChannelInfo) v.getTag());
                break;
            case R.id.ll_channel_type:
                ChannelTagInfo mChlTag = (ChannelTagInfo)v.getTag();
                startSearch(null, mChlTag);
                break;
            default:
                break;
        }
    }

    private void doClear() {
        mTvSearch.setText("");
        hideInput();
    }

    private void showChannelInfoDetail(ChannelInfo channel) {
        Intent channelIntent = new Intent(this, ForumChannelDetailActivity.class);
        channelIntent.putExtra(ForumChannelDetailActivity.EXT_DATA, channel);
        startActivity(channelIntent);
    }

    private Runnable mHideImeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIMManager != null) {
                mIMManager.hideSoftInputFromWindow(mTvSearch.getWindowToken(), 0);
            }
        }
    };

    public void hideInput() {
        mTvSearch.clearFocus();
        mTvSearch.post(mHideImeRunnable);
    }


    private void startSearch(String type, ChannelTagInfo mTypeInfo) {
        Intent intent = new Intent(this, ChannelPostsListActivity.class);
        if (type != null &&(!type.equals(""))){
            intent.putExtra(ChannelPostsListActivity.SEARCH_CHANNEL_WAY, type);
        } else if (mTypeInfo != null) {
            intent.putExtra(ChannelPostsListActivity.SEARCH_CHANNEL_INFO, mTypeInfo);
        }
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        mIvSearchClear.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        mLvSearchChl.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        mLlSearch.setVisibility(TextUtils.isEmpty(text) ? View.VISIBLE : View.GONE);
        mSearchKey = text.toString();
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        hideInput();
        return true;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mSearchKey)) {
            mChannelInfos.clear();
            return;
        }
        new SearchChannelTask().executeLong();
    }

    private class SearchChannelTask extends MsTask{

        public SearchChannelTask() {
            super(ChannelSearchActivity.this, MsRequest.SEARCH);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("name=")
                    .append(Utils.urlEncode(mSearchKey))
                    .toString();
        }
        

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                ArrayList<ChannelInfo> channels = JsonUtil.getArray(
                        response.getJsonArray(), ChannelInfo.TRANSFORMER);
                mChannelInfos.clear();
                mChannelInfos.addAll(channels);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class SearchChannelAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mChannelInfos.size();
        }

        @Override
        public ChannelInfo getItem(int position) {
            return mChannelInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_channel_night, parent, false);
            }
            ChannelInfo  channel = getItem(position);
            view.setOnClickListener(ChannelSearchActivity.this);
            view.setTag(channel);
            ProImageView piv_avatar = (ProImageView) view.findViewById(R.id.piv_channel_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_channel_title);
            TextView tv_intro = (TextView) view.findViewById(R.id.tv_channel_intro);
            piv_avatar.setImage(channel.icon, R.drawable.ic_ntc_forum);
            tv_title.setText(channel.name);
            tv_intro.setText(channel.intro);
            return view;
        }
        
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            mLvSearchChl.setVisibility(View.GONE);
            mLlSearch.setVisibility(View.VISIBLE);
        } else {
            mLvSearchChl.setVisibility(View.VISIBLE);
            mLlSearch.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Utils.hideInput(mTvSearch);
        return false;
    }
    
    private View getView (ChannelTagInfo channelType) {
        View view = mInflater.inflate(R.layout.list_item_channel_search, null);
        view.setOnClickListener(ChannelSearchActivity.this);
        ProImageView mChannelIcon = (ProImageView) view.findViewById(R.id.iv_channel_icon);
        TextView mChannelName = (TextView) view.findViewById(R.id.tv_channel_name);
        TextView mChannelNameen = (TextView) view.findViewById(R.id.tv_channel_name_en);
        mChannelIcon.setImage(channelType.icon, R.drawable.image_tree_hole);
        mChannelName.setText(channelType.name);
        mChannelNameen.setText(channelType.nameEn);
        view.setTag(channelType);
        return view;
    }       
    
    private void addView () {
       for (int i = 0; i < mChannelType.size(); i++) {
           mLlSearchbyOther.addView(getView(mChannelType.get(i)));
       } 
    }
    
    private class ChannelTypeTask extends MsTask{

        public ChannelTypeTask() {
            super(ChannelSearchActivity.this, MsRequest.CHLTYPE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                ArrayList<ChannelTagInfo> channels = JsonUtil.getArray(
                        response.getJsonArray(), ChannelTagInfo.TRANSFORMER);
                mChannelType.clear();
                mChannelType.addAll(channels);
                addView();
            }
        }
    }
}
