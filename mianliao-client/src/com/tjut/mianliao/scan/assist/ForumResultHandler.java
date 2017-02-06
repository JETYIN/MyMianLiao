package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.forum.CourseForumActivity;
import com.tjut.mianliao.scan.Scanner;

public class ForumResultHandler extends BaseResultHandler {
    public ForumResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    public void handle() {
        Intent i = new Intent(mActivity, CourseForumActivity.class);
        i.putExtra(Forum.INTENT_EXTRA_GUID,
                mResult.getDisplayResult().replace(MlParser.URI_PREFIX_FORUM, ""));
        mActivity.startActivity(i);
    }
}
