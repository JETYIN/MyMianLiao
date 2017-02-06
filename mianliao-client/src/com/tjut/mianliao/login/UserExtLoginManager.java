package com.tjut.mianliao.login;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.tjut.mianliao.LoginStateHelper;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.UserExtInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.register.ChooseSchoolActivity;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class UserExtLoginManager {

    private static WeakReference<UserExtLoginManager> sInstanceRef;
    private List<UserExtLoginListener> mUserExtLoginListeners;
    private List<UserExtRegisterListener> mUserExtRegisterListeners;
    private List<UserExtBindListener> mUserExtBindListeners;
    private List<UserBindListListener> mUserBindListListeners;
    private Context mContext;
    private UserExtInfo mUserExtInfo;
    private ArrayList<UserExtInfo> mUserExtInfos;
    // check status about what to do after get user_info and uid
    private boolean mIsLoginByExt = true, mIsCancleLoginByExt = true;
    private boolean mIsTaskRunning;

    public static synchronized UserExtLoginManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        UserExtLoginManager instance = new UserExtLoginManager(context);
        sInstanceRef = new WeakReference<UserExtLoginManager>(instance);
        return instance;
    }

    private UserExtLoginManager(Context context) {
        mContext = context.getApplicationContext();
        mUserExtLoginListeners = new CopyOnWriteArrayList<UserExtLoginListener>();
        mUserExtRegisterListeners = new CopyOnWriteArrayList<UserExtRegisterListener>();
        mUserExtBindListeners = new CopyOnWriteArrayList<UserExtBindListener>();
        mUserBindListListeners = new CopyOnWriteArrayList<UserBindListListener>();
    }

    public void registerUserExtLoginListener(UserExtLoginListener listener) {
        if (listener != null && !mUserExtLoginListeners.contains(listener)) {
            mUserExtLoginListeners.add(listener);
        }
    }

    public void registerUserExtRegisterListener(UserExtRegisterListener listener) {
        if (listener != null && !mUserExtRegisterListeners.contains(listener)) {
            mUserExtRegisterListeners.add(listener);
        }
    }

    public void registerUserExtBindListener(UserExtBindListener listener) {
        if (listener != null && !mUserExtBindListeners.contains(listener)) {
            mUserExtBindListeners.add(listener);
        }
    }

    public void registerUserBindListListener(UserBindListListener listener) {
        if (listener != null && !mUserBindListListeners.contains(listener)) {
            mUserBindListListeners.add(listener);
        }
    }

    public void unregisterUserExtLoginListener(UserExtLoginListener listener) {
        mUserExtLoginListeners.remove(listener);
    }

    public void unregisterUserExtRegisterListener(UserExtRegisterListener listener) {
        mUserExtRegisterListeners.remove(listener);
    }

    public void unregisterUserExtBindListener(UserExtBindListener listener) {
        mUserExtBindListeners.remove(listener);
    }

    public void unregisterUserBindListListener(UserBindListListener listener) {
        mUserBindListListeners.remove(listener);
    }

    public void setUserExtInfo(UserExtInfo info) {
        mUserExtInfo = info;
    }

    public void setIsTaskRunning(boolean isTaskRunning) {
        mIsTaskRunning = isTaskRunning;
    }

    public UserExtInfo getUserExtInfo() {
        return mUserExtInfo;
    }

    public void setIsLoginByExt(boolean isLoginByExt) {
        mIsLoginByExt = isLoginByExt;
    }

    public void setIsCancleLoginByExt(boolean isCancleLoginByExt) {
        mIsCancleLoginByExt = isCancleLoginByExt;
    }

    public boolean isLoginByExt() {
        return mIsLoginByExt;
    }

    public boolean isCancleLoginByExt() {
        return mIsCancleLoginByExt;
    }

    public boolean isTaskRunning() {
        return mIsTaskRunning;
    }

    public void userExtLogin() {
        mIsLoginByExt = true;
        if (mIsTaskRunning) {
            return;
        }
        new ExtLoginTask().executeLong();
    }

    public void userExtRegister() {
        new ExtRegisterTask().executeLong();
    }

    public void userExtBind() {
        new BindTask().executeLong();
    }

    public void userExtUnbind() {
        new UnbindTask().executeLong();
    }

    public void getBindList() {
        new GetBindListTask().executeLong();
    }

    private boolean getInfoFromResponse(MsResponse response) {
        JSONObject json = response.getJsonObject();
        if (json == null) {
            response.showFailInfo(mContext, R.string.reg_failed);
            return false;
        } else {
            UserInfo me = UserInfo.fromJson(json);
            String token = json.optString("token");
            if (TextUtils.isEmpty(token) || me.userId == 0) {
                response.showFailInfo(mContext, R.string.reg_failed);
                return false;
            } else {
                LoginStateHelper.accountLogin(mContext, me.account, token, me);
//                try {
//                    EMClient.getInstance().createAccount(me.account, mRegInfo.password);
//                } catch (HyphenateException e) {
//                    e.printStackTrace();
//                }
                return true;
            }
        }
    }

    private class ExtLoginTask extends MsTask {

        public ExtLoginTask() {
            super(mContext, MsRequest.USEREXT_LOGIN);
            mIsTaskRunning = true;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mUserExtInfo.type)
                    .append("&ext_id=").append(mUserExtInfo.extId)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                if (getInfoFromResponse(response)) {
                    for (UserExtLoginListener listener : mUserExtLoginListeners) {
                        listener.onExtLoginSuccess(mUserExtInfo.type);
                    }
                }
            } else {
                for (UserExtLoginListener listener : mUserExtLoginListeners) {
                    listener.onExtLoginFailed(mUserExtInfo.type);
                }
            }
        }
    }

    private class ExtRegisterTask extends MsMhpTask {

        public ExtRegisterTask() {
            super(mContext, MsRequest.USEREXT_REGISTER,
                    mUserExtInfo.getParameters(), mUserExtInfo.getFiles());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                if (getInfoFromResponse(response)) {
                    for (UserExtRegisterListener listener : mUserExtRegisterListeners) {
                        listener.onRegisterSuccess(mUserExtInfo.type);
                    }
                }
            } else {
                mContext.startActivity(new Intent(mContext, ChooseSchoolActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                for (UserExtRegisterListener listener : mUserExtRegisterListeners) {
                    listener.onRegisterFailed();
                }
            }
        }
    }

    protected class BindTask extends MsTask {

        public BindTask() {
            super(mContext, MsRequest.USEREXT_BIND);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mUserExtInfo.type)
                    .append("&ext_id=").append(mUserExtInfo.extId)
                    .append("&nick=").append(Utils.urlEncode(mUserExtInfo.nickName))
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for(UserExtBindListener listener : mUserExtBindListeners){
                    listener.onBindSuccess(mUserExtInfo.type);
                }
            } else {
                for(UserExtBindListener listener : mUserExtBindListeners){
                    listener.onBindFailed(response.code);
                }
            }
        }
    }

    protected class UnbindTask extends MsTask{

        public UnbindTask() {
            super(mContext, MsRequest.USEREXT_UNBIND);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mUserExtInfo.type).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for(UserExtBindListener listener : mUserExtBindListeners){
                    listener.onUnbindSuccess(mUserExtInfo.type);
                }
            } else {
                for(UserExtBindListener listener : mUserExtBindListeners){
                    listener.onUnbindFailed();
                }
            }
        }
    }

    protected class GetBindListTask extends MsTask{

        public GetBindListTask() {
            super(mContext, MsRequest.USEREXT_LIST_MY_BIND);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                mUserExtInfos = new ArrayList<UserExtInfo>();
                UserExtInfo info;
                for (int i = 0; i < ja.length(); i++) {
                    try {
                        info = new UserExtInfo();
                        JSONObject jo = (JSONObject) ja.get(i);
                        info.type = jo.optInt("type");
                        info.extId = jo.optString("ext_id");
                        info.nickName = jo.optString("nick");
                        mUserExtInfos.add(info);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for(UserBindListListener listener : mUserBindListListeners){
                    listener.onGetBindListSuccess(mUserExtInfos);
                }
            } else {
                for(UserBindListListener listener : mUserBindListListeners){
                    listener.onGetBindListFailed();
                }
            }
        }

    }

    public interface UserExtLoginListener {
        void onExtLoginSuccess(int type);
        void onExtLoginFailed(int type);
    }

    public interface UserExtRegisterListener {
        void onRegisterSuccess(int type);
        void onRegisterFailed();
    }

    public interface UserExtBindListener {
        void onBindSuccess(int type);
        void onUnbindSuccess(int type);
        void onBindFailed(int code);
        void onUnbindFailed();
    }

    public interface UserBindListListener{
        void onGetBindListSuccess(List<UserExtInfo> userExtInfos);
        void onGetBindListFailed();
    }

}
