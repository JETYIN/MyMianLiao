package com.tjut.mianliao.contact;

import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.Utils;

public class UserRemarkManager {

    private static final String SP_USER_REMARKS = "user_remarks";

    private static WeakReference<UserRemarkManager> sInstanceRef;

    private Context mContext;
    private SparseArray<UserRemark> mRemarks;

    public static synchronized UserRemarkManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        UserRemarkManager instance = new UserRemarkManager(context);
        sInstanceRef = new WeakReference<UserRemarkManager>(instance);
        return instance;
    }

    private UserRemarkManager(Context context) {
        mContext = context.getApplicationContext();
        mRemarks = new SparseArray<UserRemark>();
        try {
            loadRemarks(new JSONArray(
                    DataHelper.getSpForData(context).getString(SP_USER_REMARKS, "[]")));
        } catch (JSONException e) {
        }
    }

    public void clear() {
        mRemarks.clear();
        sInstanceRef.clear();
    }

    public void update() {
        new GetUserRemarksTask().executeLong();
    }

    public String getRemark(int userId, String defaultValue) {
        UserEntryManager uem = UserEntryManager.getInstance(mContext);
        UserRemark remark = mRemarks.get(userId);
        return remark != null && uem.isFriend(remark.jid) ? remark.remark : defaultValue;
    }

    public String getRemark(UserInfo user) {
        return getRemark(user.userId, user.getNickname());
    }

    public void update(int userId, String account, String remark, MsTaskListener listener) {
        new UpdateRemarkTask(userId, account, remark).setTaskListener(listener).executeLong();
    }

    private void loadRemarks(JSONArray ja) {
        int length = ja == null ? 0 : ja.length();
        mRemarks.clear();
        for (int i = 0; i < length; i++) {
            UserRemark remark = UserRemark.fromJson(ja.optJSONObject(i));
            if (remark != null && remark.isValid()) {
                mRemarks.put(remark.userId, remark);
            }
        }
    }

    private void save() {
        JSONArray ja = new JSONArray();
        int size = mRemarks.size();

        for (int i = 0; i < size; i++) {
            JSONObject json = new JSONObject();
            UserRemark remark = mRemarks.valueAt(i);
            try {
                json.put(UserRemark.USER_ID, remark.userId);
                json.put(UserRemark.ACCOUNT, remark.account);
                json.put(UserRemark.REMARK, remark.remark);
                ja.put(json);
            } catch (JSONException e) {
            }
        }

        save(ja.toString());
    }

    private void save(String data) {
        DataHelper.getSpForData(mContext).edit()
                .putString(SP_USER_REMARKS, data)
                .commit();
    }

    private class UpdateRemarkTask extends MsTask {
        private int mUserId;
        private String mAccount;
        private String mRemark;

        public UpdateRemarkTask(int userId, String account, String remark) {
            super(mContext, MsRequest.USER_UPDATE_REMARK);
            mUserId = userId;
            mAccount = account;
            mRemark = remark;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(mUserId)
                    .append("&name=").append(Utils.urlEncode(mRemark))
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (MsResponse.isSuccessful(response)) {
                if (TextUtils.isEmpty(mRemark)) {
                    mRemarks.remove(mUserId);
                } else {
                    mRemarks.put(mUserId, new UserRemark(mUserId, mAccount, mRemark));
                }
                save();
                ContactUpdateCenter.notifyContactsUpdated(UpdateType.UserInfo);
            }
        }
    }

    private class GetUserRemarksTask extends MsTask {

        public GetUserRemarksTask() {
            super(mContext, MsRequest.USER_LIST_REMARKS);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                loadRemarks(response.json.optJSONArray(MsResponse.PARAM_RESPONSE));
                save(response.json.optString(MsResponse.PARAM_RESPONSE));
                ContactUpdateCenter.notifyContactsUpdated(UpdateType.UserInfo);
            }
        }
    }

    private static class UserRemark {

        private static final String USER_ID = "user_id";
        private static final String ACCOUNT = "account";
        private static final String REMARK = "name";

        private int userId;
        private String account;
        private String jid;
        private String remark;

        private UserRemark() {}

        private UserRemark(int userId, String account, String remark) {
            this.userId = userId;
            this.account = account;
            this.jid = UserInfo.buildJid(account);
            this.remark = remark;
        }

        private static UserRemark fromJson(JSONObject json) {
            if (json == null) {
                return null;
            }
            UserRemark remark = new UserRemark();
            remark.userId = json.optInt(USER_ID);
            remark.account = json.optString(ACCOUNT);
            remark.jid = UserInfo.buildJid(remark.account);
            remark.remark = json.optString(REMARK);
            return remark;
        }

        private boolean isValid() {
            return userId > 0 && !TextUtils.isEmpty(jid) && !TextUtils.isEmpty(remark);
        }
    }
}
