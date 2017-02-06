package com.tjut.mianliao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.Utils;

public abstract class QrCardActivity extends BaseActivity implements View.OnClickListener {

    private int mBgColor;

    private int mQrCodeSize;
    private int mQrCodeMarkSize;

    private String mPartFileName = "";
    private boolean mQrCodeSaved = false;
    private LightDialog mQrCodeDialog;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_qr_card;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        getTitleBar().showTitleText(R.string.qrc_title, null);

        String name = getName();
        TextView tvName = (TextView) findViewById(R.id.tv_name);
        tvName.setText(name);

        ProImageView avAvatar = (ProImageView) findViewById(R.id.av_avatar);
        avAvatar.setImage(getImage(), getDefaultImageRes());

        mQrCodeSize = getResources().getDimensionPixelSize(R.dimen.qrc_size);
        mQrCodeMarkSize = getResources().getDimensionPixelSize(R.dimen.qrc_mark_size);
        mBgColor = getResources().getColor(R.color.bg_basic);
        mPartFileName = name;

        TextView tvInfo = (TextView) findViewById(R.id.tv_info);
        /**mutate重构Drawable状态，以免多个Imageview重用在内存中引起关联**/
        if (fillQrCode(getUri(), avAvatar.getDrawable().mutate())) {
            tvInfo.setText(getDescription());
        } else {
            tvInfo.setText(R.string.qrc_failed_gen_qrcode);
        }
    }

    protected abstract void initData();

    protected abstract String getName();

    protected abstract String getUri();

    protected abstract String getImage();

    protected abstract int getDefaultImageRes();

    protected abstract String getDescription();

    private boolean fillQrCode(String uri, Drawable drawable) {
        Bitmap bmp = Utils.makeQrCodeBitmap(uri, mQrCodeSize, mQrCodeSize, Color.BLACK, mBgColor);
        if (bmp == null) {
            return false;
        }

        Rect r = new Rect();
        r.left = (mQrCodeSize - mQrCodeMarkSize) / 2;
        r.right = r.left + mQrCodeMarkSize;
        r.top = r.left;
        r.bottom = r.right;
        drawable.setBounds(r.left + 2, r.top + 2, r.right - 2, r.bottom - 2);
        Drawable cover = getResources().getDrawable(R.drawable.bg_round_cover);
        cover.setBounds(r.left, r.top, r.right, r.bottom);

        Canvas canvas = new Canvas(bmp);
        drawable.draw(canvas);
        cover.draw(canvas);

        ImageView ivQrCode = (ImageView) findViewById(R.id.iv_qrcode);

        ivQrCode.setImageBitmap(bmp);
        return true;
    }

    /**当点击二维码时**/
    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.iv_qrcode) {
            if (!Utils.isExtStorageAvailable()) {
                Toast.makeText(this, R.string.storage_not_available, Toast.LENGTH_SHORT).show();
                return;
            } else if (mQrCodeSaved && mQrCodeDialog != null) {
                // No need to save the QR code again. Just show the path.
                mQrCodeDialog.show();
                return;
            }

            v.setEnabled(false);
            getTitleBar().showProgress();
            new AdvAsyncTask<Void, Void, Boolean>() {
                private String mFileName;

                @Override
                protected Boolean doInBackground(Void... params) {
                    ImageView iv = (ImageView) v;
                    BitmapDrawable bd = (BitmapDrawable) iv.getDrawable();
                    Bitmap bm = bd.getBitmap();

                    File dir = Utils.getMianLiaoDir();
                    if (dir == null) {
                        return false;
                    }

                    mFileName = dir.getAbsolutePath() + "/" + getString(R.string.qrc_title)
                            + "_" + mPartFileName + ".png";

                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(mFileName);
                        bm.compress(Bitmap.CompressFormat.PNG, 90, out);

                        // Show the qr code in android default gallery
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(new File(mFileName))));
                        return true;
                    } catch (IOException e) {
                        Utils.logW(getTag(), e.getMessage());
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) { }
                        }
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    v.setEnabled(true);
                    getTitleBar().hideProgress();
                    if (success) {
                        mQrCodeSaved = true;
                        initSaveDialog(mFileName);
                        mQrCodeDialog.show();
                    } else {
                        Toast.makeText(QrCardActivity.this, R.string.qrc_qrcode_save_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }.executeQuick();
        }
    }

    private void initSaveDialog(final String fileName) {
        if (mQrCodeDialog == null) {
            mQrCodeDialog = new LightDialog(this);
            mQrCodeDialog.setTitle(R.string.qrc_qrcode_saved);
            mQrCodeDialog.setMessage(fileName);
            mQrCodeDialog.setPositiveButton(R.string.qrc_view, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.actionView(QrCardActivity.this,
                            Uri.fromFile(new File(fileName)), "image/*", 0);
                }
            });
        }
    }
}