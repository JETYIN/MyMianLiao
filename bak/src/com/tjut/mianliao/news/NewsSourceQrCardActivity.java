package com.tjut.mianliao.news;

import com.tjut.mianliao.QrCardActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.scan.assist.MlParser;

public class NewsSourceQrCardActivity extends QrCardActivity {
    private NewsSource mNewsSource;

    @Override
    protected void initData() {
        mNewsSource = getIntent().getParcelableExtra(NewsSource.INTENT_EXTRA_NAME);
    }

    @Override
    protected String getName() {
        return mNewsSource.name;
    }

    @Override
    protected String getUri() {
        return new StringBuilder(MlParser.URI_PREFIX_NEWS_SOURCE)
                .append(mNewsSource.guid).toString();
    }

    @Override
    protected String getImage() {
        return mNewsSource.avatar;
    }

    @Override
    protected int getDefaultImageRes() {
        return R.drawable.ic_news_source_avatar;
    }

    @Override
    protected String getDescription() {
        return getString(R.string.qrc_desc_news_source);
    }
}
