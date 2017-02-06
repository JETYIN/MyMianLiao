package com.tjut.mianliao.mycollege;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.AlarmHelper;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.forum.TimePicker;
import com.tjut.mianliao.forum.nova.BasePostActivity;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class WarmMemoActivity extends BasePostActivity implements OnClickListener,
        TimePicker.Callback {

    public static final String EXT_WARM_MEMO = "ext_warm_memo";
    public static final String EXT_EDIT_MEMO = "ext_edit_memo";
    private TextView  mSetTime, mTimeShow;
    private NoteInfo edNoteInfo;
    private boolean isEdit;
    private int mColor;
    private TimePicker mTimePicker;
    private long mEndTime = 0;
    private long mUploadTime;
    private ImageView mCancleBt;
    private ImageView mRdioButtonP, mRdioButtonB, mRdioButtonG, mRdioButtonO, mRdioButtonPu;
    private ExpandableGridView mGridView;
    private boolean mShoudUploadTime = true;
    private AlarmHelper mAlarmHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = mInflater.inflate(R.layout.activity_warmmemo, mFlHeader);
        View vfoot = mInflater.inflate(R.layout.activity_warmmemo_foot, mFlFooter);
        mAlarmHelper = AlarmHelper.getInstance(this);
        mRdioButtonP = (ImageView) v.findViewById(R.id.iv_pink);
        mRdioButtonB = (ImageView) v.findViewById(R.id.iv_blue);
        mRdioButtonG = (ImageView) v.findViewById(R.id.iv_grown);
        mRdioButtonO = (ImageView) v.findViewById(R.id.iv_orange);
        mRdioButtonPu = (ImageView) v.findViewById(R.id.iv_purple);
        mGridView = (ExpandableGridView) findViewById(R.id.gv_gallery);
        mSetTime = (TextView) vfoot.findViewById(R.id.tv_memo_stime);
        mTimeShow = (TextView) vfoot.findViewById(R.id.tv_remindtime);
        mCancleBt = (ImageView) vfoot.findViewById(R.id.bt_cancel);
//
//        LinearLayout.LayoutParams mParams = (LayoutParams) mEtContent.getLayoutParams();
//        mParams.height = R.dimen.memo_content_height;
//        mEtContent.setLayoutParams(mParams);
        mEtContent.setTextColor(0XFFFFFFFF);
        mEtContent.setHintTextColor(0XFFFFFFFF);
        mEtContent.setShouldWatcher(false);
        mSetTime.setOnClickListener(this);
        mCancleBt.setOnClickListener(this);
        showTimeShow();
        Intent intent = getIntent();
        String editis = intent.getStringExtra("Editis");
        if (editis.equals("false")) {
            isEdit = false;
            getTitleBar().setTitle(intent.getStringExtra("mTitle"));
            mEtContent.setBackgroundColor(0XFFFE808B);
            mRdioButtonP.setBackgroundResource(R.drawable.pic_memo_cir_pink);
            mRdioButtonP.setFocusable(true);
            mRdioButtonP.setFocusableInTouchMode(true);
            mColor = 0XFFFE808B;

        } else {
            edNoteInfo = intent.getParcelableExtra(EXT_EDIT_MEMO);
            isEdit = true;
            getTitleBar().setTitle(R.string.edit);
            mEtContent.setText(edNoteInfo.content);
            int mColorPri = edNoteInfo.color * (-1);
            long mDateTxt = (long) (edNoteInfo.clock) * 1000;
            mEndTime = (long) (edNoteInfo.clock) * 1000;
            if (mDateTxt == 0) {
                mTimeShow.setVisibility(View.INVISIBLE);
            } else {
                mTimeShow.setVisibility(View.VISIBLE);
                mCancleBt.setVisibility(View.VISIBLE);
                Date date = new Date(mDateTxt);
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                mTimeShow.setText(sd.format(date));
            }
            mColor = mColorPri;
            keepColor(mColorPri);
        }
        getTitleBar().setTitle(intent.getStringExtra("mTitle"));
        getTitleBar().showRightText(R.string.finish, this);
        mTimePicker = new TimePicker(this);
        mTimePicker.setCallback(this);
        mLlOtherOIcon.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                if (!isStateReady() || !hasUpdate()) {
                    return;
                }
                submit();
                break;
            case R.id.tv_memo_stime:
                mTimePicker.pick(mEndTime, 0);
                mTimeShow.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_cancel:
                mTimeShow.setText("");
                mCancleBt.setVisibility(View.GONE);
                mEndTime = 0;
            case R.id.iv_pink:
                mCircleClean();
                mRdioButtonP.setBackgroundResource(R.drawable.pic_memo_cir_pink);
                mEtContent.setBackgroundColor(0XFFFE808B);
                mColor = 0XFFFE808B;
                break;
            case R.id.iv_blue:
                mCircleClean();
                mRdioButtonB.setBackgroundResource(R.drawable.pic_memo_cir_blue);
                mEtContent.setBackgroundColor(0XFF47C3C8);
                mColor = 0XFF47C3C8;
                break;
            case R.id.iv_grown:
                mCircleClean();
                mRdioButtonG.setBackgroundResource(R.drawable.pic_memo_cir_grown);
                mEtContent.setBackgroundColor(0XFFB9CED6);
                mColor = 0XFFB9CED6;
                break;
            case R.id.iv_orange:
                mCircleClean();
                mRdioButtonO.setBackgroundResource(R.drawable.pic_memo_cir_orange);
                mEtContent.setBackgroundColor(0XFFFEC010);
                mColor = 0XFFFEC010;
                break;
            case R.id.iv_purple:
                mCircleClean();
                mRdioButtonPu.setBackgroundResource(R.drawable.pic_memo_cir_purple);
                mEtContent.setBackgroundColor(0XFFF15FA3);
                mColor = 0XFFF15FA3;
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void submit() {
        if (Utils.isNetworkAvailable(this)) {
            mTaskManager.startPostNoteTask(isEdit(), getParams(), getFiles());
        } else {
            toast(R.string.no_network);
        }
    }

    @Override
    protected boolean isEdit() {
        return isEdit;
    }

    @Override
    protected HashMap<String, String> getParams() {
        HashMap<String, String> param = new HashMap<>();
        if (edNoteInfo != null) {
            param.put("id", String.valueOf(edNoteInfo.postId));
        }
        if (mShoudUploadTime) {
        	mUploadTime = mEndTime / 1000;
        	param.put("clock", mUploadTime + "");
        }
        param.put("content", mDesc);
        param.put("color", Math.abs(mColor) + "");
        param.put("note_type", String.valueOf(NoteInfo.TYPE_MEMOR));
        return param;
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_NOTE_POST:
                getTitleBar().showProgress();
                mEtContent.setEnabled(false);
                break;
            case FORUM_EDIT_NOTE:
                getTitleBar().showProgress();
                mEtContent.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_NOTE_POST:
                getTitleBar().hideProgress();
                mEtContent.setEnabled(true);
			if (response.isSuccessful() && response.getJsonObject() != null) {
				NoteInfo notes = NoteInfo.fromJson(response.getJsonObject());
				Intent data = new Intent();
				data.putExtra(EXT_WARM_MEMO, notes);
				setResult(RESULT_OK, data);
				if (mEndTime > System.currentTimeMillis()) {
					updateEndTime();
					mShoudUploadTime = true;
					mAlarmHelper.setMemoAlarm(mEndTime, notes);
				} else {
					if (!(mEndTime == 0)){
						toast("设置时间失败，请选择大于当前时间值的时间");
						mShoudUploadTime = false;
					}
				}
                }
				finish();
                break;
            case FORUM_EDIT_NOTE:
                mEtContent.setEnabled(true);
                getTitleBar().hideProgress();
                if (response.isSuccessful() && response.getJsonObject() != null) {
                    NoteInfo notes = NoteInfo.fromJson(response.getJsonObject());
                    Intent data = new Intent();
                    data.putExtra(EXT_EDIT_MEMO, notes);
                    setResult(RESULT_OK, data);
                    updateEndTime();
                    mShoudUploadTime = true;
                    if (mEndTime > System.currentTimeMillis()) {
                    	mAlarmHelper.setMemoAlarm(mEndTime, notes);
                    } else {
                    	if (!(mEndTime == 0)){
    						toast("设置时间失败，请选择大于当前时间值的时间");
    						mShoudUploadTime = false;
    					}
                    }
                }
                finish();
                break;
            default:
                break;
        }

    }

    private void keepColor(int color) {
        switch (color) {
            case 0XFFFE808B:
                mEtContent.setBackgroundColor(0XFFFE808B);
                mRdioButtonP.setBackgroundResource(R.drawable.pic_memo_cir_pink);
                break;
            case 0XFF47C3C8:
                mEtContent.setBackgroundColor(0XFF47C3C8);
                mRdioButtonB.setBackgroundResource(R.drawable.pic_memo_cir_blue);
                break;
            case 0XFFB9CED6:
                mEtContent.setBackgroundColor(0XFFB9CED6);
                mRdioButtonG.setBackgroundResource(R.drawable.pic_memo_cir_grown);
                break;
            case 0XFFFEC010:
                mEtContent.setBackgroundColor(0XFFFEC010);
                mRdioButtonO.setBackgroundResource(R.drawable.pic_memo_cir_orange);
                break;
            case 0XFFF15FA3:
                mEtContent.setBackgroundColor(0XFFF15FA3);
                mRdioButtonPu.setBackgroundResource(R.drawable.pic_memo_cir_purple);
                break;
            default:
                break;
        }

    }

    @Override
    public void onResult(Calendar time, int requestCode) {
        mEndTime = time.getTimeInMillis();
        if (mEndTime > System.currentTimeMillis()) {
        	updateEndTime();
        	mShoudUploadTime = true;
        } else {
        	toast("设置时间失败，请选择大于当前时间值的时间");
        	mShoudUploadTime = false;
        }
    }

    private void updateEndTime() {
        mTimeShow.setText(Utils.getTimeString(3, mEndTime));
        mCancleBt.setVisibility(View.VISIBLE);
    }

    private void showTimeShow() {
        String mTime = mTimeShow.getText().toString();
        if (mTime.equals("")) {
            mCancleBt.setVisibility(View.GONE);
        } else {
            mCancleBt.setVisibility(View.VISIBLE);
        }
    }

    private void mCircleClean() {
        mRdioButtonP.setBackgroundResource(R.drawable.bg_memo_circle_pink);
        mRdioButtonB.setBackgroundResource(R.drawable.bg_memo_circle_blue);
        mRdioButtonO.setBackgroundResource(R.drawable.bg_memo_circle_orange);
        mRdioButtonG.setBackgroundResource(R.drawable.bg_memo_circle_grown);
        mRdioButtonPu.setBackgroundResource(R.drawable.bg_memo_circle_purple);
    }

    @Override
    protected boolean setCanRefFriend() {
        return false;
    }
}
