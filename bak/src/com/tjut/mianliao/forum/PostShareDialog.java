package com.tjut.mianliao.forum;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.util.SnsHelper;

public class PostShareDialog {

    private PostShareDialog() {}

    public static void show(final Activity activity, final CfPost post) {
        new LightDialog(activity).setTitleLd(R.string.please_choose)
                .setItems(R.array.cf_share_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                activity.startActivity(new Intent()
                                        .setClass(activity, ForumPostShareActivity.class)
                                        .putExtra(CfPost.INTENT_EXTRA_NAME, post));
                                break;

                            case 1:
                                SnsHelper.getInstance().openShareBoard(activity, post);
                                break;

                            default:
                                break;
                        }
                    }
                })
                .show();
    }
}
