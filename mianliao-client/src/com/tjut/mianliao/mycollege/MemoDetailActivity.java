package com.tjut.mianliao.mycollege;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.util.Utils;

public class MemoDetailActivity extends BaseActivity {

	public static final String EXT_MEMO_NOTE = "ext_memo_note";
	public static final int WARM_MEMO_EDIT = 134;
	private TitleBar mTitleBar;
	private TextView mMemodetail, mMemotime;
	private NoteInfo mNoteInfo;
	private RelativeLayout mLyMemo;
	private RelativeLayout mRlMemoDetail;
	private TextView mCreateTime;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_memo_detail;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitleBar = getTitleBar();
		mTitleBar.setTitle(R.string.take_notes_lable_memo);
		mTitleBar.showRightText(R.string.edit, mEtmemolisten);

		mRlMemoDetail = (RelativeLayout) findViewById(R.id.rl_memo_detail);
		mMemodetail = (TextView) findViewById(R.id.tv_memo_detail);
		mMemotime = (TextView) findViewById(R.id.tv_memo_time);
		mLyMemo = (RelativeLayout) findViewById(R.id.rl_memo_detail1);
		mCreateTime = (TextView) findViewById(R.id.tv_create_time);
		Intent intent = getIntent();
		mNoteInfo = intent.getParcelableExtra(EXT_MEMO_NOTE);
		if (!(mNoteInfo == null)) {
			mLyMemo.setBackgroundColor(mNoteInfo.color * (-1));
			mMemodetail.setText(mNoteInfo.content);
			long mDateTxt = (long) (mNoteInfo.clock) * 1000;
			if (mDateTxt == 0) {
				mMemotime.setVisibility(View.INVISIBLE);
			} else {
				Date date = new Date(mDateTxt);
				SimpleDateFormat sd = new SimpleDateFormat("MM-dd HH:mm");
				mMemotime.setText(sd.format(date));
			}
		}
		if (mNoteInfo.updatedOn <= mNoteInfo.createdOn) {
			mCreateTime.setText("创建于" + Utils.getTimeString(6, mNoteInfo.createdOn));
		} else {
			mCreateTime.setText("更新于" + Utils.getTimeString(6, mNoteInfo.updatedOn));
		}

	}

	OnClickListener mEtmemolisten = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
			intent.setClass(MemoDetailActivity.this, WarmMemoActivity.class);
			intent.putExtra(WarmMemoActivity.EXT_EDIT_MEMO, mNoteInfo);
			intent.putExtra("Editis", "true");
			startActivityForResult(intent, WARM_MEMO_EDIT);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			mNoteInfo = (NoteInfo) data
					.getParcelableExtra(WarmMemoActivity.EXT_EDIT_MEMO);
			mLyMemo.setBackgroundColor(mNoteInfo.color * (-1));
			mMemodetail.setText(mNoteInfo.content);
			mMemotime.setVisibility(View.VISIBLE);
			long mDateTxt = (long) (mNoteInfo.clock) * 1000;
			if (mDateTxt == 0) {
				mMemotime.setVisibility(View.INVISIBLE);
			} else {
				Date date = new Date(mDateTxt);
				SimpleDateFormat sd = new SimpleDateFormat("MM-dd HH:mm");
				mMemotime.setText(sd.format(date));
			}
			break;
		default:
			break;
		}

	};

	@Override
	protected void onResume() {
		mLyMemo.setBackgroundColor(mNoteInfo.color * (-1));
		mMemodetail.setText(mNoteInfo.content);
		long mDateTxt = (long) (mNoteInfo.clock) * 1000;
		if (mDateTxt == 0) {
			mMemotime.setVisibility(View.INVISIBLE);
		} else {
			Date date = new Date(mDateTxt);
			SimpleDateFormat sd = new SimpleDateFormat("MM-dd HH:mm");
			mMemotime.setText(sd.format(date));
		}
		if (mNoteInfo.updatedOn <= mNoteInfo.createdOn) {
			mCreateTime.setText("创建于" + Utils.getTimeString(6, mNoteInfo.createdOn));
		} else {
			mCreateTime.setText("更新于" + Utils.getTimeString(6, mNoteInfo.updatedOn));
		}
		super.onResume();
	}
}