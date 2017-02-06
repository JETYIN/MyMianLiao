package com.tjut.mianliao.bounty;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MapActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.component.CardItemConf;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.StaticMapView;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.forum.MultiImageHelper;
import com.tjut.mianliao.forum.TimePicker;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class BountyPostActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        TimePicker.Callback, DialogInterface.OnClickListener,
        OnGetGeoCoderResultListener, EmotionPicker.EmotionListener {

    private static final String TAG = "BountyPostActivity";

    private static final int REQUEST_REF = 101;
    private static final int REQUEST_MAP = 102;

    private EditText mEtDesc;
    private CheckBox mCbEmotion;
    private EmotionPicker mEmotionPicker;

    private TimePicker mTimePicker;
    private long mEndTime;

    private GetImageHelper mGetImageHelper;
    private GridView mGvImages;
    private MultiImageHelper mMIH;
    private ImageAdapter mImageAdapter;

    private LightDialog mChooseDialog;
    private LightDialog mDiscardDialog;
    private LightDialog mUploadingDialog;

    private BtyPostTask mLastPostTask;
    private BountyTask mBounty = new BountyTask();

    private LatLng mLatLng;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bounty_post;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.bty_new_task, null);

        mMIH = new MultiImageHelper(this);
        mImageAdapter = new ImageAdapter();
        mGvImages = (GridView) findViewById(R.id.gv_gallery);
        mGvImages.setAdapter(mImageAdapter);
        mGvImages.setOnItemClickListener(this);

        mGetImageHelper = new GetImageHelper(this, new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    mMIH.addImage(imageFile);
                    showImages();
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                
            }
        });

        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mEmotionPicker.setEmotionListener(this);
        mCbEmotion = (CheckBox) findViewById(R.id.cb_input_emotion);
        findViewById(R.id.iv_input_attach).setVisibility(View.GONE);

        mEtDesc = (EditText) findViewById(R.id.et_desc);
        mEtDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideEmoPicker();
                }
                mEtDesc.setText(Utils.getRefFriendText(
                        mEtDesc.getText(), getApplicationContext()));
            }
        });

        mTimePicker = new TimePicker(this);
        mTimePicker.setCallback(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                collectInfo();
                if (!isStateReady() || !hasUpdate()) {
                    return;
                }
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(BountyTask.REQ_DEADLINE, String.valueOf(mBounty.reqDeadline));
                params.put(BountyTask.DESC, mBounty.desc);
                params.put(BountyTask.PLACE, mBounty.place);
                params.put(BountyTask.REWARD, mBounty.reward);
                params.put(BountyTask.CONTACT, mBounty.contact);
                params.put(BountyTask.QUOTA, String.valueOf(mBounty.quota));
                if (mLatLng != null) {
                    params.put(BountyTask.LOCATION, mLatLng.longitude + "," + mLatLng.latitude);
                }

                HashMap<String, String> files = null;
                if (mMIH.hasNewImages()) {
                    ArrayList<Image> images = mMIH.getImages();
                    StringBuilder sb = new StringBuilder();
                    files = new HashMap<String, String>();
                    int size = images.size();
                    for (int i = 0; i < size; i++) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        String name = "image" + i;
                        sb.append(name);
                        files.put(name, images.get(i).file);
                    }
                    params.put("images", sb.toString());
                }

                new BtyPostTask(params, files).executeLong();
                break;
            case R.id.et_desc:
                hideEmoPicker();
                break;
            case R.id.iv_input_ref:
                startActivityForResult(new Intent(this, RefFriendActivity.class), REQUEST_REF);
                hideEmoPicker();
                break;
            case R.id.iv_input_image:
                mGetImageHelper.getImage();
                hideEmoPicker();
                break;
            case R.id.cb_input_emotion:
                Utils.toggleInput(mEtDesc, mEmotionPicker);
                break;
            case R.id.cif_deadline:
                mTimePicker.pick(mEndTime, 0);
                break;
            case R.id.tv_map:
                if (mLatLng == null) {
                    startActivityForResult(new Intent(this, MapActivity.class), REQUEST_MAP);
                } else {
                    findViewById(R.id.smv_location).setVisibility(View.GONE);
                    ((EditText) findViewById(R.id.et_location)).setText("");
                    mLatLng = null;
                    ((TextView) v).setText(R.string.map);
                }
                break;
            case R.id.smv_location:
                Intent iMap = new Intent(this, MapActivity.class);
                iMap.putExtra(MapActivity.EXTRA_LOCATION, new LatLngWrapper(mLatLng));
                startActivityForResult(iMap, REQUEST_MAP);
                break;
            case R.id.tv_switch_task_type:
                View vQuota = findViewById(R.id.et_quota);
                if (v instanceof TextView) {
                    TextView tvTaskType = (TextView) v;
                    if (vQuota.getVisibility() == View.VISIBLE) {
                        vQuota.setVisibility(View.INVISIBLE);
                        tvTaskType.setText(R.string.bty_task_type_single);
                    } else {
                        vQuota.setVisibility(View.VISIBLE);
                        tvTaskType.setText(R.string.bty_task_type_multiple);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Image image = (Image) parent.getItemAtPosition(position);
        mMIH.setPending(image);
        if (image != null) {
            showChooseDialog();
        } else {
            mGetImageHelper.getImage();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMIH != null) {
            mMIH.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REF) {
            if (resultCode == RESULT_OK) {
                String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
                int ss = mEtDesc.getSelectionStart();
                int color = getResources().getColor(R.color.ref_friend);
                mEtDesc.getText().replace(ss, ss,
                        Utils.getColoredText(refs, refs, color));
            }
        } else if (requestCode == REQUEST_MAP) {
            if (resultCode == RESULT_OK) {
                ((TextView) findViewById(R.id.tv_map)).setText(R.string.bty_clear_map);
                LatLngWrapper wrapper = data.getParcelableExtra(MapActivity.EXTRA_LOCATION);
                mLatLng = wrapper.latLng;

                GeoCoder geoCoder = GeoCoder.newInstance();
                geoCoder.setOnGetGeoCodeResultListener(this);
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLatLng));
                StaticMapView smv = (StaticMapView) findViewById(R.id.smv_location);
                smv.showMap(mLatLng);
            }
        } else if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
    }

    private void showImages() {
        findViewById(R.id.iv_input_image).setVisibility(
                mImageAdapter.reachMaxCount() ? View.GONE : View.VISIBLE);
        if (mMIH.getImages().isEmpty()) {
            mGvImages.setVisibility(View.GONE);
        } else {
            mGvImages.setVisibility(View.VISIBLE);
            mImageAdapter.notifyDataSetChanged();
        }
    }

    private void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this)
                    .setTitleLd(R.string.qa_discard_title)
                    .setMessage(R.string.qa_discard_message)
                    .setNegativeButton(R.string.qa_discard_continue, null)
                    .setPositiveButton(R.string.qa_discard_quit, this)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                            R.drawable.selector_btn_red);
        }
        mDiscardDialog.show();
    }

    private void showChooseDialog() {
        if (mChooseDialog == null) {
            mChooseDialog = new LightDialog(this)
                    .setTitleLd(R.string.please_choose)
                    .setItems(R.array.fp_image_choices, this);
        }
        mChooseDialog.show();
    }

    private boolean isStateReady() {
        return check(mLastPostTask == null, R.string.handling_last_task)
                && check(!TextUtils.isEmpty(mBounty.desc), R.string.bty_tst_desc_empty)
                && check(mBounty.reqDeadline > 0, R.string.bty_tst_please_set_deadline)
                && check(!TextUtils.isEmpty(mBounty.reward), R.string.bty_tst_reward_empty)
                && check(mBounty.quota > 0, R.string.bty_tst_quota_invalid);
    }

    private boolean check(boolean qualified, int failMsg) {
        if (!qualified) {
            toast(failMsg);
        }
        return qualified;
    }

    private boolean hasUpdate() {
        return !(TextUtils.isEmpty(mBounty.desc) && TextUtils.isEmpty(mBounty.reward)
                && TextUtils.isEmpty(mBounty.contact) && TextUtils.isEmpty(mBounty.place)
                && mEndTime == 0) || mMIH.hasUpdate();
    }

    /**
     * @return Always return true. So it can be used as a combination with some other functions.
     */
    private boolean collectInfo() {
        mBounty.desc = ((EditText) findViewById(R.id.et_desc)).getText().toString();
        mBounty.reward = ((EditText) findViewById(R.id.et_reward)).getText().toString();
        mBounty.contact = ((EditText) findViewById(R.id.et_contact)).getText().toString();
        mBounty.place = ((EditText) findViewById(R.id.et_location)).getText().toString();
        mBounty.reqDeadline = mEndTime / 1000;
        EditText etQuota = (EditText) findViewById(R.id.et_quota);
        if (etQuota.getVisibility() == View.VISIBLE) {
            try {
                mBounty.quota = Integer.parseInt(etQuota.getText().toString());
            } catch (NumberFormatException e) {
                Utils.logD(TAG, e.getMessage());
            }
        } else {
            mBounty.quota = 1;
        }
        return true;
    }

    private void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this)
                    .setTitleLd(R.string.qa_upload_title)
                    .setMessage(R.string.qa_upload_message)
                    .setNegativeButton(R.string.qa_upload_wait, null)
                    .setPositiveButton(R.string.qa_upload_quit, this)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                            R.drawable.selector_btn_red);
        }
        mUploadingDialog.show();
    }

    private void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
        mCbEmotion.setChecked(false);
    }

    @Override
    public void onBackPressed() {
        if (mEmotionPicker.isVisible()) {
            hideEmoPicker();
            return;
        }

        if (mLastPostTask != null) {
            showUploadingDialog();
        } else if (collectInfo() && hasUpdate()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResult(Calendar time, int requestCode) {
        mEndTime = time.getTimeInMillis();
        updateEndTime();
    }

    private void updateEndTime() {
        ((CardItemConf) findViewById(R.id.cif_deadline))
                .setContent(DateFormat.getDateTimeInstance().format(new Date(mEndTime)));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mChooseDialog) {
            switch (which) {
                case 0:
                    mGetImageHelper.getImageGallery();
                    break;
                case 1:
                    mGetImageHelper.getImageCamera();
                    break;
                case 2:
                    mMIH.deletePending();
                    showImages();
                    break;
                default:
                    break;
            }
        } else if (dialog == mDiscardDialog || dialog == mUploadingDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                finish();
            }
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        mBounty.place = reverseGeoCodeResult.getAddress();
        setText(R.id.et_location, mBounty.place);
    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        mEtDesc.getText().insert(
                mEtDesc.getSelectionStart(), emotion.getSpannable(this));
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mEtDesc);
    }

    private class ImageAdapter extends BaseAdapter {

        private static final int MAX_SIZE = 9;
        private ArrayList<Image> mImages;

        private ImageAdapter() {
            mImages = mMIH.getImages();
        }

        public boolean reachMaxCount() {
            return mImages.size() == MAX_SIZE;
        }

        @Override
        public int getCount() {
            return mImages.size() < MAX_SIZE ? mImages.size() + 1 : mImages.size();
        }

        @Override
        public Object getItem(int position) {
            return position == mImages.size() ? null : mImages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position == mImages.size() ? 1 : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProImageView view;
            int viewType = getItemViewType(position);
            if (convertView != null && convertView instanceof ProImageView) {
                view = (ProImageView) convertView;
            } else {
                view = (ProImageView) getLayoutInflater().inflate(R.layout.grid_item_photo,
                        parent, false);
                if (viewType == 1) {
                    view.setScaleType(ImageView.ScaleType.CENTER);
                    view.setImageResource(R.drawable.btn_add_photo);
                    view.setBackgroundResource(R.drawable.selector_btn_add_photo);
                }
            }
            if (viewType == 1) {
                return view;
            }

            String url = mImages.get(position).image;
            if (!TextUtils.isEmpty(url)) {
                view.setImage(AliImgSpec.POST_THUMB_SQUARE.makeUrl(url), R.drawable.bg_img_loading);
            } else {
                BitmapLoader.getInstance().setBitmap(view, mImages.get(position).fileThumb, 0);
            }
            return view;
        }
    }

    private class BtyPostTask extends MsMhpTask {

        public BtyPostTask(HashMap<String, String> parameters, HashMap<String, String> files) {
            super(getApplicationContext(), MsRequest.BTY_POST, parameters, files);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLastPostTask = this;
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse msResponse) {
            super.onPostExecute(msResponse);
            mLastPostTask = null;
            getTitleBar().hideProgress();
            if (msResponse.isSuccessful()) {
                setResult(RESULT_OK);
                finish();
            } else {
                msResponse.showFailInfo(getApplicationContext(), R.string.bty_tst_post_failed);
            }
        }
    }
}
