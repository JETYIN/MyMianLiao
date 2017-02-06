package com.tjut.mianliao.forum;

import android.content.Context;
import android.util.Log;

import com.duanqu.qupai.auth.AuthService;
import com.duanqu.qupai.auth.QupaiAuthListener;
import com.tjut.mianliao.common.Constant;


public class QupaiAuth {

    private static QupaiAuth instance;

    public static QupaiAuth getInstance() {
        if (instance == null) {
            instance = new QupaiAuth();
        }
        return instance;
    }

    private static final String AUTHTAG = "QupaiAuth";

    /**
     * 鉴权 建议只调用一次,在demo里面为了测试调用了多次 得到accessToken，通常一个用户对应一个token
     * @param context
     * @param appKey    appkey
     * @param appsecret appsecret
     * @param space     space
     */
    public void initAuth(Context context , String appKey, String appsecret, String space){
        Log.e("Live","accessToken" + Constant.accessToken);
        Log.e("Live", "space" + Constant.space);

        AuthService service = AuthService.getInstance();
        service.setQupaiAuthListener(new QupaiAuthListener() {
            @Override
            public void onAuthError(int errorCode, String message) {
                Log.e(AUTHTAG, "ErrorCode" + errorCode + "message" + message);
            }

            @Override
            public void onAuthComplte(int responseCode, String responseMessage) {
                Log.e(AUTHTAG, "onAuthComplte" + responseCode + "message" + responseMessage);
                Constant.accessToken = responseMessage;
            }
        });
        service.startAuth(context,appKey, appsecret, space);
    }

}
