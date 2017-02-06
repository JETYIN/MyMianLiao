package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.news.NewsSourceDetailsActivity;
import com.tjut.mianliao.scan.Scanner;

public class NewsSourceResultHandler extends BaseResultHandler {
    public NewsSourceResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    public void handle() {
        Intent i = new Intent(mActivity, NewsSourceDetailsActivity.class);
        NewsSource ns = new NewsSource();
        ns.guid = mResult.getDisplayResult().replace(MlParser.URI_PREFIX_NEWS_SOURCE, "");
        i.putExtra(NewsSource.INTENT_EXTRA_NAME, ns);
        mActivity.startActivity(i);
    }
}
