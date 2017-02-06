package com.tjut.mianliao.forum;

import com.tjut.mianliao.QrCardActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.scan.assist.MlParser;

public class ForumQrCardActivity extends QrCardActivity {
    private Forum mForum;

    @Override
    protected void initData() {
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
    }

    @Override
    protected String getName() {
        return mForum.name;
    }

    @Override
    protected String getUri() {
        return new StringBuilder(MlParser.URI_PREFIX_FORUM)
                .append(mForum.guid).toString();
    }

    @Override
    protected String getImage() {
        return mForum.icon;
    }

    @Override
    protected int getDefaultImageRes() {
        return R.drawable.ic_avatar_forum;
    }

    @Override
    protected String getDescription() {
        return getString(R.string.qrc_desc_forum);
    }
}
