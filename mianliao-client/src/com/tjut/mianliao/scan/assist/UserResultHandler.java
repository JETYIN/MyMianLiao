package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.scan.Scanner;

public class UserResultHandler extends BaseResultHandler {
    public UserResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    public void handle() {
        Intent i = new Intent(mActivity, NewProfileActivity.class);
        i.putExtra(NewProfileActivity.EXTRA_USER_GUID,
                mResult.getDisplayResult().replace(MlParser.URI_PREFIX_USER, ""));
        mActivity.startActivity(i);
    }
}
