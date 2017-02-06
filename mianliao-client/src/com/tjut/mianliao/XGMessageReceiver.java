package com.tjut.mianliao;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;
import com.tjut.mianliao.chat.OfficialAccountInfo;
import com.tjut.mianliao.component.LiveDialog;
import com.tjut.mianliao.component.LiveXGDialog;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.InboxMessage;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.push.PushMessage;
import com.tjut.mianliao.data.push.PushTaskMessage;
import com.tjut.mianliao.forum.nova.MessageRemindManager;
import com.tjut.mianliao.task.PopTaskActivity;

public class XGMessageReceiver extends XGPushBaseReceiver {

    private static final int CAT_NOTICE = 1;
    private static OnTaskFinishPushListener mListener;
    private static ArrayList<OnPublicNumPushListener> mPublisteners;

    @Override
    public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult result) {
    }

    @Override
    public void onUnregisterResult(Context context, int errorCode) {
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult result) {
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult result) {
    }

    @Override
    public void onSetTagResult(Context context, int errorCode, String tagName) {
    }

    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        if (context == null || message == null) {
            return;
        }

        String customContent = message.getCustomContent();
        String content = message.getContent();
        String title = message.getTitle();
        try {
            JSONObject obj = new JSONObject(customContent);
            PushMessage pushMessage = PushMessage.fromJson(obj);

            switch (pushMessage.getCategory()) {

                case PushMessage.SINGLE_TASK_FINISHED:
                    pushMessage.getPushTaskMessage().setPop(false);
                    showNoticeMessage(context, pushMessage);
                    break;
                case PushMessage.ALL_TASKS_FINISHED:
                    if (mListener != null) {
                        mListener.onTaskFinished(pushMessage);
                    }
                    if (pushMessage != null) {
                        if (pushMessage.getPushTaskMessage().isPop()) {
                            showNoticeMessage(context, pushMessage);
                        }
                    }
                    break;
                case PushMessage.FROM_DAY_TO_NIGHT:
                case PushMessage.FROM_NIGHT_TO_DAY:
                    break;
                case PushMessage.MESSAGE_REMIND:
                    int msgType = obj.optInt("msg_type");
                    MessageRemindManager.hasNewMessage(msgType);
                    break;
                case PushMessage.PUBLIC_NUMBER:
                    OfficialAccountInfo info = OfficialAccountInfo.fromJson(obj.optJSONObject("data"));
                    if (mPublisteners != null && mPublisteners.size() > 0) {
                        for (OnPublicNumPushListener listener : mPublisteners) {
                            listener.OnPublicNumPush(info);
                        }
                    }
                    break;
                case PushMessage.MESSAGE_REMIND_NO_INBOX:
                    int objId = obj.optInt("arg");
                    msgType = obj.optInt("msg_type");
                    sendNotification(context, msgType, objId, content, title);
                    break;
                case PushMessage.SYSTEM_NOTICE_INFO:
                    Log.e("target", content);
                    msgType = obj.optInt("msg_type");
                    if (msgType == PushMessage.MSG_TYPE_LIVE_CONNECTION_REQUEST) {
                        MessageRemindManager.hasLiveConnectionRequest(pushMessage);
                        return;
                    }
                    UserInfo userinfo = AccountInfo.getInstance(context).getUserInfo();
                    /**服务器推送用户更新数据，使用观察者模式**/
                    UserInfoManager.getInstance(context).updateUserInfo(userinfo.jid);
                    LiveXGDialog mLiveDialog = new LiveXGDialog(context);
                    mLiveDialog.setText(content);
                    mLiveDialog.setPositiveButton("", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                    MessageRemindManager.hasNewMessage(InboxMessage.POST_STICKLVL_DEL_BANNED);
                    break;
                default:
                    NotificationHelper nh = NotificationHelper.getInstance(context);
                    nh.sendDefaultNotification(title, content);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(Context context, int msgType, int objId,
                                  String content, String title) {
        NotificationHelper nh = NotificationHelper.getInstance(context);
        switch (msgType) {
            case PushMessage.MSG_TYPE_POST_RECOMMEND:
                nh.sendPostRecommendNotification(title, content, objId);
                break;
            case PushMessage.MSG_TYPE_WEB_ADV:
                nh.sendWebAdvNotification(title, content, objId);
                break;
            case PushMessage.MSG_TYPE_TRIBE_RECOMMEND:
                nh.sendTirbeRecommendNotification(title, content, objId);
                break;
            case PushMessage.MSG_TYPE_TOPIC_RECOMMEND:
                nh.sendTopicRecommendNotification(title, content, objId);
                break;
            default:
                break;
        }
    }

    private void showNoticeMessage(Context context, PushMessage pushMessage) {
        Intent intent = new Intent(context, PopTaskActivity.class);
        PushTaskMessage ptm = pushMessage.getPushTaskMessage();
        if (ptm != null) {
            intent.putExtra(PopTaskActivity.EXT_TASK_NAME, ptm.getName());
            intent.putExtra(PopTaskActivity.EXT_PROCESS, ptm.getProcess());
            intent.putExtra(PopTaskActivity.EXT_CREDIT, ptm.getCredit());
            intent.putExtra(PopTaskActivity.EXT_POP, ptm.isPop());
            intent.putExtra(PopTaskActivity.EXT_SCHOOL_NUM, ptm.getUnlockSchoolNum());
            intent.putExtra(PopTaskActivity.EXT_TASK_CONTENT, ptm.getContent());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.activity_slide_left_in, R.anim.activity_slide_right_out);
    }

    public static void registerOnTaskFinishPushListener(OnTaskFinishPushListener listener) {
        if (listener != null) {
            mListener = listener;
        }
    }

    public static void registerOnPublicNumListener(OnPublicNumPushListener listener) {
        if (mPublisteners == null) {
            mPublisteners = new ArrayList<>();
        }
        if (listener != null && !mPublisteners.contains(listener)) {
            mPublisteners.add(listener);
        }
    }

    public interface OnTaskFinishPushListener {
        /**
         * Call this method while a/all task is finished
         *
         * @param mesage
         */
        void onTaskFinished(PushMessage mesage);
    }

    public interface OnPublicNumPushListener {
        /**
         * It's used to notice while a public number has push message
         *
         * @param info
         */
        void OnPublicNumPush(OfficialAccountInfo info);
    }
}
