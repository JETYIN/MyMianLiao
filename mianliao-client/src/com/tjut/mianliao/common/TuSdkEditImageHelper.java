package com.tjut.mianliao.common;

import java.io.File;
import java.lang.ref.WeakReference;

import org.lasque.tusdk.TuSdkGeeV1;
import org.lasque.tusdk.core.TuSdkResult;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.image.BitmapHelper;
import org.lasque.tusdk.impl.activity.TuFragment;
import org.lasque.tusdk.impl.components.TuEditMultipleComponent;
import org.lasque.tusdk.modules.components.TuSdkComponent.TuSdkComponentDelegate;

import android.app.Activity;
import android.graphics.Bitmap;

public class TuSdkEditImageHelper {

    private static WeakReference<TuSdkEditImageHelper> mInstanceRef;
    
    private Activity mActivity;
    
    public static synchronized TuSdkEditImageHelper getInstance() {
        if (mInstanceRef != null && mInstanceRef.get() != null) {
             return mInstanceRef.get();
        }
        TuSdkEditImageHelper instance = new TuSdkEditImageHelper();
        mInstanceRef = new WeakReference<TuSdkEditImageHelper>(instance);
        return instance;
    }
    
    public void clear() {
        mActivity = null;
        mInstanceRef.clear();
    }
    
    public void editImageByTuSdk(Activity activity, final String imagePath, EditImageListener listener) {
        if (activity == null)
            return;
        mActivity = activity;
        Bitmap bitmap = BitmapHelper.getBitmap(new File(imagePath));
        TuSdkResult result = new TuSdkResult();
        result.image = bitmap;
        openEditMultiple(result, listener);
    }

    private void openEditMultiple(TuSdkResult result, final EditImageListener listener) {
        if (result == null || listener == null)
            return;

        TuSdkComponentDelegate delegate = new TuSdkComponentDelegate() {
            @Override
            public void onComponentFinished(TuSdkResult result, Error error, TuFragment lastFragment) {
                TLog.d("onEditMultipleComponentReaded: %s | %s", result, error);
                if (result != null && result.imageSqlInfo != null) {
                    listener.onEditImageResult(true, result.imageSqlInfo.path);
                } else {
                    listener.onEditImageResult(false, "");
                }
            }
        };

        // 组件选项配置
        TuEditMultipleComponent component = TuSdkGeeV1.editMultipleCommponent(mActivity, delegate);

        // 设置图片
        component.setImage(result.image)
            // 设置系统照片
            .setImageSqlInfo(result.imageSqlInfo)
            // 设置临时文件
            .setTempFilePath(result.imageFile)
            // 在组件执行完成后自动关闭组件
            .setAutoDismissWhenCompleted(true)
            // 开启组件
            .showComponent();
    }
 
    public interface EditImageListener{
        void onEditImageResult(boolean succ, String imagePath);
    }
}
