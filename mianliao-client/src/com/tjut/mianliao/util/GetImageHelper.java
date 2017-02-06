package com.tjut.mianliao.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.nereo.imagechoose.MultiImageSelectorActivity;

import org.lasque.tusdk.modules.components.TuSdkHelperComponent;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.common.TuSdkEditImageHelper;
import com.tjut.mianliao.common.TuSdkEditImageHelper.EditImageListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.contact.UserInfo;

/**
 * In most case when we need a image, we don't need the whole source file. We
 * need to crop/zoom it based on requirement. Here's a generic process: 1. Get
 * source image from Gallery/Camera. (startActivityForResult) 2. Crop the image
 * to desired size. (startActivityForResult) 3. Zoom out the image if it's too
 * large. 4. Save the image to disk. ACTIVITY_REQUEST_CODE used: 91,92,93
 */
public class GetImageHelper {
    private static final String TAG = "GetImageHelper";

    private static final int DEFAULT_ZOOM_SIZE = 1024; // max Width/Height in pixels.
    private static final Paint SCALE_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG);

    public static final ImageSpec DEFAULT_SPEC = new ImageSpec(DEFAULT_ZOOM_SIZE, 0, 0);
    public static final ImageSpec SQUARE_SPEC = new ImageSpec(DEFAULT_ZOOM_SIZE, 1, 1);
    public static final ImageSpec AVATAR_SPEC = new ImageSpec(UserInfo.AVATAR_MAX_SIZE, 1, 1);
    public static final ImageSpec SCENE_SPEC = new ImageSpec(600, 2, 1);

    public static final int IMAGE_REQUEST_CODE = 91;
    public static final int CAMERA_REQUEST_CODE = 92;
    public static final int RESULT_REQUEST_CODE = 93;
    public static final int REQUEST_IMAGE_CODE = 94;

    private final String mDefaultTitle;
    private final String mCropImageTitle;

    private static Activity mActivity;
    private String mTitle;
    private ImageSpec mDefaultSpec;
    private ImageSpec mSpec;

    private ImageResultListener mListener;
    private TuSdkEditImageHelper mEditImageHelper;

    private Uri mTmpImgUri;
    private Uri mCroppedImgUri;
    private String mResultImgFile;

    private LightDialog mGetImageDialog;
    public TuSdkHelperComponent mComponentHelper;
    private Bitmap mBitmap;

    public GetImageHelper(Activity activity, ImageResultListener listener) {
        this(activity, null, DEFAULT_SPEC, listener);
    }

    /**
     * @param activity
     * @param title
     *            null or empty to use default title
     * @param spec
     *            null to use default spec(1024, 0, 0)
     * @param listener
     */
    public GetImageHelper(Activity activity, String title, ImageSpec spec, ImageResultListener listener) {
        mActivity = activity;

        mDefaultTitle = mActivity.getString(R.string.please_choose);
        mCropImageTitle = mActivity.getString(R.string.crop_image);

        mTitle = TextUtils.isEmpty(title) ? mDefaultTitle : title;
        mDefaultSpec = spec != null ? spec : DEFAULT_SPEC;
        mSpec = mDefaultSpec;
        mListener = listener;
        
        mEditImageHelper = TuSdkEditImageHelper.getInstance();
    }

    private boolean initUris() {
        if (mActivity.getExternalCacheDir() == null) {
            return false;
        } else if (mTmpImgUri == null) {
            String basePath = mActivity.getExternalCacheDir().getAbsolutePath();
            mTmpImgUri = Uri.fromFile(new File(basePath + "/get_img_tmp.jpg"));
            mCroppedImgUri = Uri.fromFile(new File(basePath + "/get_img_cropped.jpg"));
            mResultImgFile = basePath + "/get_img_scaled.jpg";
        }
        return true;
    }

    public void getImage() {
        getImage(null, null);
    }

    public void getImage(boolean showCamara, int maxSelect) {
        Intent intent = new Intent(mActivity, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, showCamara);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_TEXT, false);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, maxSelect);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
                MultiImageSelectorActivity.MODE_MULTI);
        mActivity.startActivityForResult(intent, REQUEST_IMAGE_CODE);
    }
    
    public void getImageGallery() {
        if (checkStorage()) {
            mActivity.startActivityForResult(getFromGallery(), IMAGE_REQUEST_CODE);
        }
    }

    public void getImageCamera() {
        if (checkStorage()) {
            mActivity.startActivityForResult(getFromCamera(mTmpImgUri), CAMERA_REQUEST_CODE);
        }
    }

    public void getImage(int title, ImageSpec spec) {
        getImage(mActivity.getString(title), spec);
    }

    private boolean checkStorage() {
        if (!Utils.isExtStorageAvailable() || !initUris()) {
            Toast.makeText(mActivity, R.string.storage_not_available, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void getImage(String title, ImageSpec spec) {
        if (!checkStorage()) {
            return;
        }
        if (mGetImageDialog == null) {
            initGetImageDialog();
        }
        mSpec = spec == null ? mDefaultSpec : spec;
        mGetImageDialog.setTitle(TextUtils.isEmpty(title) ? mTitle : title);
        mGetImageDialog.show();
    }

    private void initGetImageDialog() {
        mGetImageDialog = new LightDialog(mActivity);
        mGetImageDialog.setItems(R.array.get_image_choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        getImageGallery();
                        break;
                    case 1:
                        getImageCamera();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * @return true if the result is handled, false not.
     */
    public boolean handleResult(int requestCode, Intent data) {
        if (!checkStorage() || data == null) {
            return false;
        }
        switch (requestCode) {
            case REQUEST_IMAGE_CODE:
                ArrayList<String> images = data.getStringArrayListExtra(
                        MultiImageSelectorActivity.EXTRA_RESULT);
                boolean hasImage = images != null && images.size() > 0;
                if (hasImage && images.size() == 1) {
                    // Use TuSDK to edit image
                    String imageFile = images.get(0);
                    Utils.logD("LogFile", "edit image file --> " + imageFile);
                    mEditImageHelper.editImageByTuSdk(mActivity, imageFile,
                            new EditImageListener() {
                        
                        @Override
                        public void onEditImageResult(boolean succ, String imagePath) {
                            if (succ && mListener != null) {
                                Utils.logD("LogFile", "after edit image file --> " + imagePath);
                                ArrayList<String> images = new ArrayList<>();
                                images.add(imagePath);
                                mListener.onImageResult(true, images);
                            }
                        }
                    });
                } else {
                    if (mListener != null) {
                        mListener.onImageResult(hasImage, images);
                        return hasImage;
                    }
                }
                return true;
                /**
                 * @Deprecated
                 */
            case IMAGE_REQUEST_CODE:
            case CAMERA_REQUEST_CODE:
                if (data != null && data.getData() != null) {

                    // Intent crop = getCropIntent(data.getData(),
                    // mCroppedImgUri, mSpec.aspectX, mSpec.aspectY);
                    // mActivity.startActivityForResult(Intent.createChooser(crop,
                    // mCropImageTitle), RESULT_REQUEST_CODE);

                    mBitmap = BitmapZoomOutImage(mActivity, data.getData(), mSpec.maxSize);
                } else {
                    mBitmap = BitmapZoomOutImage(mActivity, mTmpImgUri, mSpec.maxSize);
                }
                if (mBitmap == null) {
                    return false;
                }
                if (saveBitmap(mBitmap, mResultImgFile)) {
                    if (mListener != null) {
                        mListener.onImageResult(mBitmap != null, mResultImgFile, mBitmap);
                    }
                }
                return true;
            case RESULT_REQUEST_CODE:
                mBitmap = zoomOutImage(mCroppedImgUri.getPath(), mResultImgFile, mSpec.maxSize);
                if (mListener != null) {
                    mListener.onImageResult(mBitmap != null, mResultImgFile, mBitmap);
                }
                return true;
            default:
                return false;
        }
    }

    
    public static Intent getFromGallery() {
        String action = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? Intent.ACTION_GET_CONTENT
                : Intent.ACTION_PICK;
        Intent intent = new Intent(action);
        intent.setType("image/*");
        return intent;
    }
    
    public static Intent getFromCamera(Uri tmpFileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFileUri);
        return intent;
    }

    public static Intent getCropIntent(Uri srcUri, Uri destUri, int aspectX, int aspectY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(srcUri, "image/*");
        intent.putExtra("crop", "true");
        if (aspectX > 0 || aspectY > 0) {
            intent.putExtra("aspectX", aspectX);
            intent.putExtra("aspectY", aspectY);
        }
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, destUri);
        return intent;
    }

    /**
     * @param src
     *            Src file name.
     * @param dest
     *            Please use JPG file ext for the dest
     * @param size
     *            0 to use DEFAULT_ZOOM_SIZE
     * @return The result Bitmap.
     */

    public static Bitmap BitmapZoomOutImage(Activity activity, Uri uri, int size) {
        ContentResolver cr = activity.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);

            options.inJustDecodeBounds = false;

            int targetSize = size == 0 ? DEFAULT_ZOOM_SIZE : size;
            int srcHeight = options.outHeight;
            int srcWidth = options.outWidth;

            if (Math.max(srcHeight, srcWidth) > targetSize) {
                int destWidth, destHeight;
                if (srcWidth > srcHeight) {
                    destWidth = targetSize;
                    options.inSampleSize = srcWidth / destWidth;
                } else {
                    destHeight = targetSize;
                    options.inSampleSize = srcHeight / destHeight;
                }
            } else {
                options.inSampleSize = 1;
            }

            return BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);

        } catch (FileNotFoundException e) {

            return null;
        }
    }

    public static Bitmap zoomOutImage(String src, String dest, int size) {
        File destFile = new File(dest);
        if (destFile.isFile() && !destFile.delete()) {
            return null;
        }

        int targetSize = size == 0 ? DEFAULT_ZOOM_SIZE : size;
        if (!Utils.isImageSizeExceeded(src, targetSize)) {
            if (Utils.renameFile(src, dest)) {
                return Utils.fileToBitmap(dest);
            } else {
                return null;
            }
        }

        Bitmap srcBmp = Utils.fileToBitmap(src);
        int srcWidth = srcBmp.getWidth();
        int srcHeight = srcBmp.getHeight();
        int destWidth, destHeight;
        if (srcWidth > srcHeight) {
            destWidth = targetSize;
            destHeight = srcHeight * destWidth / srcWidth;
        } else {
            destHeight = targetSize;
            destWidth = srcWidth * destHeight / srcHeight;
        }
        Bitmap destBmp = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBmp);
        canvas.drawBitmap(srcBmp, new Rect(0, 0, srcWidth, srcHeight), new Rect(0, 0, destWidth, destHeight),
                SCALE_PAINT);
        srcBmp.recycle();

        if (saveBitmap(destBmp, dest)) {
            File srcFile = new File(src);
            if (!srcFile.delete()) {
                Utils.logD(TAG, "delete src file failed.");
            }
            return destBmp;
        }

        return null;
    }

    /**
     * Since GetImageHelper will use the same temp files when getting a image,
     * so if you want to keep the result image, you need to save it in a
     * different name, which is the purpose of this method.
     *
     * @param ctx
     * @param file
     *            the IMAGE file which will be used later.
     * @return If the action succeeded, the path of the new file is returned,
     *         null otherwise.
     */
    public static String saveAsTodo(Context ctx, String file) {
        File ecd = ctx.getExternalCacheDir();
        if (Utils.isExtStorageAvailable() && ecd != null) {
            String newFile = ecd.getAbsolutePath() + "/todo_img_" + System.currentTimeMillis() + ".jpg";
            if (Utils.copy(file, newFile)) {
                return newFile;
            }
        }
        return null;
    }

    /**
     * @param src
     * @param maxSize
     * @return null if the resize failed, src if zoom out is not required, the
     *         new file path if the src is zoomed out.
     */
    public static String zoomOutImage(String src, int maxSize) {
        if (TextUtils.isEmpty(src) || !(new File(src).isFile())) {
            return null;
        }

        int targetSize = maxSize == 0 ? DEFAULT_ZOOM_SIZE : maxSize;
        if (!Utils.isImageSizeExceeded(src, targetSize)) {
            return src;
        }

        Bitmap srcBmp = Utils.fileToBitmap(src);
        int srcWidth = srcBmp.getWidth();
        int srcHeight = srcBmp.getHeight();
        int destWidth, destHeight;
        if (srcWidth > srcHeight) {
            destWidth = targetSize;
            destHeight = srcHeight * destWidth / srcWidth;
        } else {
            destHeight = targetSize;
            destWidth = srcWidth * destHeight / srcHeight;
        }
        Bitmap destBmp = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBmp);
        canvas.drawBitmap(srcBmp, new Rect(0, 0, srcWidth, srcHeight), new Rect(0, 0, destWidth, destHeight),
                SCALE_PAINT);
        srcBmp.recycle();

        String dest = src + ".resized.jpg";
        if (saveBitmap(destBmp, dest)) {
            destBmp.recycle();
            return dest;
        }
        return null;
    }

    public static boolean saveBitmap(Bitmap bitmap, String fileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            return true;
        } catch (IOException e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    public static class ImageSpec {
        public int maxSize;
        public int aspectX;
        public int aspectY;

        public ImageSpec(int maxSize, int aspectX, int aspectY) {
            this.maxSize = maxSize;
            this.aspectX = aspectX;
            this.aspectY = aspectY;
        }
    }

    public interface ImageResultListener {
        /**
         * @deprecated
         * @param success
         * @param imageFile
         * @param bm
         */
        public void onImageResult(boolean success, String imageFile, Bitmap bm);
        public void onImageResult(boolean success, ArrayList<String> images);
    }

}
