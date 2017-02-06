package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.TagView;
import com.tjut.mianliao.component.TagView.TagClickListener;
import com.tjut.mianliao.data.mycollege.TagInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class AddJobTagActivity extends BaseActivity implements TagClickListener,
        OnClickListener {

    public static final String EXT_TAG_INFOS = "ext_tag_infos";
    private static final String FLAG_TAG_MINE = "flag_tag_mine";
    private static final String FLAG_TAG_SERVER = "flag_tag_server";

    private ArrayList<TagInfo> mTagServer;
    private ArrayList<TagInfo> mTagsMine;

    private TagView mTagView;
    private TagView mTagMine;
    private LinearLayout mLlTop;
    private TextView mTvRefresh;
    private boolean mIsEdting;
    private TextView mTvTagCount;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_add_my_tag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.mc_my_tag);
        getTitleBar().showRightText(R.string.prof_save, this);
        mTagsMine = getIntent().getParcelableArrayListExtra(EXT_TAG_INFOS);
        mTagView = (TagView) findViewById(R.id.tg_view);
        mTagMine = (TagView) findViewById(R.id.tg_mine);
        mLlTop = (LinearLayout) findViewById(R.id.ll_add_tag_top);
        mTvRefresh = (TextView) findViewById(R.id.tv_refresh);
        mTvTagCount = (TextView) findViewById(R.id.tv_my_tag_count);
        mTagView.setFlag(FLAG_TAG_SERVER);
        mTagMine.setFlag(FLAG_TAG_MINE);
        mTagMine.setLineMargin(26);
        mTagMine.setItemMargins(20);
        mTagMine.setFirstLineMargin(20);
        mTagMine.setMarginTopWithItemMargin(true);
        mTagView.setLineMargin(36);
        mTagView.setItemMargins(26);
        mTagView.setMarginTopWithItemMargin(true);
        mTagView.registerTagClickListener(this);
        mTagMine.registerTagClickListener(this);
        mTagMine.updateView(mTagsMine);
        updateTagCountInfo(mTagsMine == null ? 0 : mTagsMine.size());
        getTags();
    }

    @Override
    public void onBackPressed() {
        quit(0);
        super.onBackPressed();
    }

    @Override
    public void onTagClick(TagInfo tag, String flag) {
        if (flag.equals(FLAG_TAG_MINE)) {
            mTagView.addTag(tag);
            if (mTagMine.getTags().size() < 6) {
                mTagView.setClickables(true);
            }
        } else if (flag.equals(FLAG_TAG_SERVER)) {
            if (mTagMine.getTags().size() >= 6) {
                toast("您添加的标签过多");
            } else {
                mTagMine.addTag(tag);
                if (mTagMine.getTags().size() == 6) {
                    mTagView.setClickables(false);
                }
            }
        }
        updateTagCountInfo(mTagMine.getChildCount());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                quit(1);
                break;
            case R.id.tv_refresh:
                getTags();
                break;
            default:
                break;
        }
    }

    private String getTagsIndexString() {
        ArrayList<TagInfo> tags = mTagMine.getTags();
        StringBuilder tagStr = new StringBuilder();
        boolean isFirst = true;
        for (TagInfo tag : tags) {
            if (isFirst) {
                isFirst = false;
            } else {
                tagStr.append(Utils.COMMA_DELIMITER);
            }
            tagStr.append(tag.tagIndex);
        }
        return tagStr.toString();
    }

    private String getTagsString() {
        ArrayList<TagInfo> tags = mTagMine.getTags();
        StringBuilder tagStr = new StringBuilder();
        boolean isFirst = true;
        for (TagInfo tag : tags) {
            if (isFirst) {
                isFirst = false;
            } else {
                tagStr.append(Utils.COMMA_DELIMITER);
            }
            tagStr.append(tag.name);
        }
        return tagStr.toString();
    }

    private void getTags() {
        new GetTagsTask().executeLong();
    }

    private void updateTagCountInfo(int count) {
        CharSequence content = getString(R.string.mc_my_tag_count_desc, count);
        content = Utils.getColoredText(content, String.valueOf(count), 0XFF32BBBC, false);
        mTvTagCount.setText(content);
    }

    private void editTag() {
        if (!Utils.isNetworkAvailable(this)) {
            toast(R.string.no_network);
            return;
        }
        if (!mIsEdting) {
            new EditMyTagTask().executeLong();
        } else {
            toast(R.string.handling_last_task);
        }
    }

    private void quit(int i) {
        Intent data = new Intent();
        if (i > 0) {
            data.putExtra(MatchingJobActivity.EXT_TAGS_STR, getTagsString());
            data.putExtra(MatchingJobActivity.EXT_TAGS, getTagsIndexString());
            data.putExtra(IntelMatchActivity.EXT_TAG_INFO, mTagMine.getTags());
        }
        setResult(RESULT_OK, data);
        finish();
    }

    private class GetTagsTask extends MsTask{

        public GetTagsTask() {
            super(AddJobTagActivity.this, MsRequest.LIST_ALL_JOB_TAGS);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                ArrayList<TagInfo> infos = JsonUtil.getArray(
                        response.getJsonArray(), TagInfo.TRANSFORMER);
                if (infos != null) {
                    mTagServer = infos;
                    if (mTagsMine != null) {
                        removeData();
                    }
                    mTagView.updateView(mTagServer);
                }
                mTvRefresh.setVisibility(View.GONE);
                mLlTop.setVisibility(View.VISIBLE);
            } else {
                mTvRefresh.setVisibility(View.VISIBLE);
            }
        }
    }

    private class EditMyTagTask extends MsTask{

        public EditMyTagTask() {
            super(AddJobTagActivity.this, MsRequest.EDIT_JOB_TAGS);
        }

        @Override
        protected void onPreExecute() {
            mIsEdting = true;
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "job_tags=" + Utils.urlEncode(getTagsIndexString());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mIsEdting = false;
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                quit(1);
            }
        }
    }

    public void removeData() {
        ArrayList<TagInfo> tags = new ArrayList<>();
        for (TagInfo tag : mTagServer) {
            for (TagInfo tagInfo : mTagsMine) {
                if (tag.tagIndex == tagInfo.tagIndex) {
                    tags.add(tag);
                }
            }
        }
        mTagServer.removeAll(tags);
    }

}
