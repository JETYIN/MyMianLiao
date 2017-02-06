package com.tjut.mianliao.util;

import android.content.Context;
import android.widget.Toast;

import com.tjut.mianliao.R;

public class PoliceHelper {

    private static final int TYPE_NEWS_COMMENT = 1;
    private static final int TYPE_FORUM_POST = 2;
    private static final int TYPE_FORUM_REPLY = 3;
    private static final int TYPE_QA_QUESTION = 4;
    private static final int TYPE_QA_ANSWER = 5;

    private PoliceHelper() {
    }

    public static void reportNewsComment(Context context, int id) {
        new ReportTask(context, TYPE_NEWS_COMMENT, id).executeLong();
    }

    public static void reportForumPost(Context context, int id) {
        new ReportTask(context, TYPE_FORUM_POST, id).executeLong();
    }

    public static void reportForumReply(Context context, int id) {
        new ReportTask(context, TYPE_FORUM_REPLY, id).executeLong();
    }

    public static void reportQaQuestion(Context context, int id) {
        new ReportTask(context, TYPE_QA_QUESTION, id).executeLong();
    }

    public static void reportQaAnswer(Context context, int id) {
        new ReportTask(context, TYPE_QA_ANSWER, id).executeLong();
    }

    private static class ReportTask extends MsTask {
        private int mType;
        private int mId;

        public ReportTask(Context context, int type, int id) {
            super(context, MsRequest.POLICE_REPORT);
            mType = type;
            mId = id;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mType)
                    .append("&object_id=").append(mId)
                    .append("&description=").toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Context context = getRefContext();
            String toast = MsResponse.isSuccessful(response)
                    ? context.getString(R.string.pr_success)
                    : MsResponse.getFailureDesc(context, R.string.pr_failed, response.code);
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        }
    }
}
