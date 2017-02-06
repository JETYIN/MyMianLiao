package com.tjut.mianliao.mycollege;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.mycollege.DayNoteInfo;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.mycollege.HorizontalScrollMemo.OnAddClickListener;
import com.tjut.mianliao.mycollege.HorizontalScrollMemo.OnDelClickListener;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class TakeNoticesActivity extends BaseActivity implements OnClickListener, OnRefreshListener2<ListView> {

    public static final int MEMO_REQUEST = 100;
    public static final int NOTE_REQUEST = 101;
    public static final int HOMEWORK_REQUEST = 102;
    private String mEditis;
    private PullToRefreshListView mListview;
    private String mLastYear;
    private ArrayList<DayNoteInfo> mDayNoteInfos;
    Intent intent = new Intent();
    private LightDialog mDelDialog;
    private int mDelId;
    private boolean isPermitLoad = true;
    private int lastVisibleItem = 0;
    private long lastScrollTime = 0;
    private TextView mDay, mDate, mNoteMemo, mNotePhoto, mNoteHw, mMemoNum, mNoteNum, mHwNum, mTvPadding;
    private ImageView mNewMemo, mNewHomework, mNewNote;
    private TextView mTvYear;
    private ImageView mLineMemo, mLinePhoto, mLineHw, mCircleMemo, mCirclePhoto, mCircleHw;
    private HorizontalScrollMemo mNoteContentMemo;
    private HorizontalScrollPhotonotes mNoteContentPho;
    private HorizontalScrollHomework mNoteContentHw;
    private RelativeLayout mRlMemo, mRlHw, mRlPho;
    private boolean mRefresh;
    private int mOffset = 0;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_take_notices;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDayNoteInfos = new ArrayList<>();
        mOffset = 0;
        new GetPostNoteTask().executeLong();
        getTitleBar().setTitle(R.string.take_notes_title);
        mListview = (PullToRefreshListView) findViewById(R.id.lv_takenotes);
        mDayNoteInfos.add(0, null);
        mListview.setMode(Mode.BOTH);
        mListview.setOnRefreshListener(this);
        mListview.setAdapter(mBaseAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOffset = 0;
        new GetPostNoteTask().executeLong();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_new_memo:
                // showDialog();
                intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                intent.setClass(this, WarmMemoActivity.class);
                mEditis = "false";
                intent.putExtra("mTitle", getString(R.string.take_notes_lable_memo));
                intent.putExtra("Editis", mEditis);
                startActivityForResult(intent, MEMO_REQUEST);
                MobclickAgent.onEvent(this, MStaticInterface.POST);
                break;
            case R.id.tv_new_notes:
                intent.setClass(this, HomeworkNotesActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                mEditis = "false";
                intent.putExtra("Editis", mEditis);
                intent.putExtra("mTitle", getString(R.string.take_notes_lable_notes));
                intent.putExtra("mNoteTitle", "");
                intent.putExtra("NOTE_TYPE", "NOTE");
                startActivityForResult(intent, NOTE_REQUEST);
                MobclickAgent.onEvent(this, MStaticInterface.NOTES);
                break;
            case R.id.tv_new_homework:
                intent.setClass(this, HomeworkNotesActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                intent.putExtra("mTitle", getString(R.string.take_notes_lable_homework));
                mEditis = "false";
                intent.putExtra("Editis", mEditis);
                intent.putExtra("mNoteTitle", "");
                intent.putExtra("NOTE_TYPE", "HOMEWORK");
                startActivityForResult(intent, HOMEWORK_REQUEST);
                MobclickAgent.onEvent(this, MStaticInterface.HOMEWORK);
                break;
            default:
                break;
        }
    }

    private BaseAdapter mBaseAdapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.list_item_notehomeadapter, parent, false);
            DayNoteInfo note = getItem(position);
            return new getViewTask(v, note, position).doInBackground();

            // if (mListview == null) {
            // configListView();
            // }
            // if (!isPermitLoad) {
            // Utils.logE("xxx", "mama " + isPermitLoad);
            // return v;
            // }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public DayNoteInfo getItem(int position) {
            return mDayNoteInfos.get(position);
        }

        @Override
        public int getCount() {
            return mDayNoteInfos.size();
        }

        public void configListView() {
            mListview.setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    switch (scrollState) {
                        case OnScrollListener.SCROLL_STATE_IDLE:// 滑动停止
                            isPermitLoad = true;
                            lastScrollTime = 0;
                            lastVisibleItem = 0;
                            notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (lastVisibleItem != firstVisibleItem) {
                        long nowTime = System.currentTimeMillis();
                        double speed = Math.abs(firstVisibleItem - lastVisibleItem)
                                / ((nowTime - lastScrollTime) / 1000f);
                        lastScrollTime = nowTime;
                        lastVisibleItem = firstVisibleItem;
                        if (speed > 5) {
                            isPermitLoad = false;
                        } else {
                            isPermitLoad = true;
                        }
                    }

                }
            });

        }
    };
    private OnDelClickListener mDelClickListener = new OnDelClickListener() {

        @Override
        public void OnDelClick(int Id) {
            mDelId = Id;
            showDelDialog();
        }
    };
    private OnAddClickListener mAddClickListener = new OnAddClickListener() {

        @Override
        public void onAddClick(int type) {
            switch (type) {
                case 1:
                    intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                    intent.setClass(TakeNoticesActivity.this, WarmMemoActivity.class);
                    mEditis = "false";
                    intent.putExtra("mTitle", getString(R.string.take_notes_lable_memo));
                    intent.putExtra("Editis", mEditis);
                    startActivityForResult(intent, MEMO_REQUEST);
                    break;
                case 2:
                    intent.setClass(TakeNoticesActivity.this, HomeworkNotesActivity.class);
                    intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                    mEditis = "false";
                    intent.putExtra("Editis", mEditis);
                    intent.putExtra("mTitle", getString(R.string.take_notes_lable_notes));
                    intent.putExtra("mNoteTitle", "");
                    intent.putExtra("NOTE_TYPE", "NOTE");
                    startActivityForResult(intent, NOTE_REQUEST);
                    break;
                case 3:
                    intent.setClass(TakeNoticesActivity.this, HomeworkNotesActivity.class);
                    intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                    intent.putExtra("mTitle", getString(R.string.take_notes_lable_homework));
                    mEditis = "false";
                    intent.putExtra("Editis", mEditis);
                    intent.putExtra("mNoteTitle", "");
                    intent.putExtra("NOTE_TYPE", "HOMEWORK");
                    startActivityForResult(intent, HOMEWORK_REQUEST);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NOTE_REQUEST:
            case HOMEWORK_REQUEST:
            case MEMO_REQUEST:
                mOffset = 0;
                new GetPostNoteTask().executeLong();

                break;
            default:
                break;
        }
    }

    private class GetPostNoteTask extends MsTask {
        public GetPostNoteTask() {
            super(TakeNoticesActivity.this, MsRequest.NOTE_LIST_BY_DAY);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mListview.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<DayNoteInfo> mDay = mDayNoteInfos;
                try {
                    if (mOffset <= 0) {
                        mDayNoteInfos.clear();
                    }

                    JSONArray ja = response.getJsonArray();
                    if (ja != null) {
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject json = ja.getJSONObject(i);
                            JSONObject jlist = json.optJSONObject("list");
                            DayNoteInfo dni = new DayNoteInfo();
                            dni.photoNotes = JsonUtil.getArray(jlist.optJSONArray("photo"), NoteInfo.TRANSFORMER);
                            dni.memoNotes = JsonUtil.getArray(jlist.optJSONArray("memo"), NoteInfo.TRANSFORMER);
                            dni.hwNotes = JsonUtil.getArray(jlist.optJSONArray("homework"), NoteInfo.TRANSFORMER);
                            dni.day = json.optInt("day");
                            mDayNoteInfos.add(dni);
                        }
                        if (ja.length() == 0 && mOffset <= 0) {
                            mDayNoteInfos.add(null);
                        } else if (mDayNoteInfos.get(0) != null && !(Utils.isSameDay(mDayNoteInfos.get(0).day * 1000))) {
                            mDayNoteInfos.add(0, null);
                        }
                        mBaseAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showDelDialog() {
        if (mDelDialog == null) {
            mDelDialog = new LightDialog(TakeNoticesActivity.this).setTitleLd(R.string.take_note_isdelate)
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DelNoteTask(mDelId).executeLong();

                        }
                    });
        }
        mDelDialog.show();
    }

    private class DelNoteTask extends MsTask {
        int id;

        public DelNoteTask(int Id) {
            super(TakeNoticesActivity.this, MsRequest.DELETE_NOTE_POST);
            this.id = Id;
        }

        @Override
        protected String buildParams() {
            return "thread_id=" + id;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                try {
                    // for (int i = 0; i < mNoteProcess.size(); i++) {
                    // if (id == mNoteProcess.get(i).postId){
                    // mNoteProcess.remove(i);
                    // }
                    // }
                    // if(mNoteProcess.size()>0){
                    // setData(1, mNoteProcess);
                    // }else {
                    // setData(2, null);
                    // }
                    mOffset = 0;
                    new GetPostNoteTask().executeLong();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class getViewTask extends AsyncTask<Void, Void, View> {
        private View v;
        private DayNoteInfo note;
        private int position;

        public getViewTask(View v, DayNoteInfo note, int position) {
            this.v = v;
            this.note = note;
            this.position = position;
        }

        @Override
        protected View doInBackground(Void... params) {
            mTvPadding = (TextView) v.findViewById(R.id.tv_padding);
            mNewMemo = (ImageView) v.findViewById(R.id.tv_new_memo);
            mNewNote = (ImageView) v.findViewById(R.id.tv_new_notes);
            mNewHomework = (ImageView) v.findViewById(R.id.tv_new_homework);
            mDate = (TextView) v.findViewById(R.id.tv_notes_date);
            mDay = (TextView) v.findViewById(R.id.tv_notes_today);

            mNoteMemo = (TextView) v.findViewById(R.id.tv_notes_classmemo);
            mNotePhoto = (TextView) v.findViewById(R.id.tv_notes_classphoto);
            mNoteHw = (TextView) v.findViewById(R.id.tv_notes_classhw);

            mLineMemo = (ImageView) v.findViewById(R.id.iv_line1);
            mLinePhoto = (ImageView) v.findViewById(R.id.iv_line2);
            mLineHw = (ImageView) v.findViewById(R.id.iv_line3);

            mCircleMemo = (ImageView) v.findViewById(R.id.iv_circle1);
            mCirclePhoto = (ImageView) v.findViewById(R.id.iv_circle2);
            mCircleHw = (ImageView) v.findViewById(R.id.iv_circle3);

            mTvYear = (TextView) v.findViewById(R.id.tv_date_year);

            mNoteContentMemo = (HorizontalScrollMemo) v.findViewById(R.id.v_memo_content);
            mNoteContentPho = (HorizontalScrollPhotonotes) v.findViewById(R.id.v_notes_content);
            mNoteContentHw = (HorizontalScrollHomework) v.findViewById(R.id.v_homework_content);

            mMemoNum = (TextView) v.findViewById(R.id.tv_memo_num);
            mNoteNum = (TextView) v.findViewById(R.id.tv_note_num);
            mHwNum = (TextView) v.findViewById(R.id.tv_homework_num);

            mRlMemo = (RelativeLayout) v.findViewById(R.id.rl_notememo);
            mRlPho = (RelativeLayout) v.findViewById(R.id.rl_notephoto);
            mRlHw = (RelativeLayout) v.findViewById(R.id.rl_notehw);

            mNewMemo.setOnClickListener(TakeNoticesActivity.this);
            mNewNote.setOnClickListener(TakeNoticesActivity.this);
            mNewHomework.setOnClickListener(TakeNoticesActivity.this);

            mNoteContentMemo.registerOnAddClickListener(mAddClickListener);
            mNoteContentPho.registerOnAddClickListener(mAddClickListener);
            mNoteContentHw.registerOnAddClickListener(mAddClickListener);
            mNoteContentMemo.registerOnDelClickListener(mDelClickListener);
            mNoteContentPho.registerOnDelClickListener(mDelClickListener);
            mNoteContentHw.registerOnDelClickListener(mDelClickListener);

            mNoteMemo.setText(R.string.note_unforget_post);
            mNotePhoto.setText(R.string.note_pic);
            mNoteHw.setText(R.string.note_homework);

            boolean isNull1 = false;
            boolean isNull2 = false;
            boolean isNull3 = false;

            if (position == 0) {
                mDay.setVisibility(View.VISIBLE);
                Date curDate = new Date();
                SimpleDateFormat sd = new SimpleDateFormat("MM-dd");
                SimpleDateFormat year = new SimpleDateFormat("yyyy");
                mDate.setText(sd.format(curDate));
                mTvYear.setText(year.format(curDate));
                mLastYear = mTvYear.getText().toString();
                mTvPadding.setVisibility(View.VISIBLE);
                if (note == null) {
                    mNoteContentMemo.setData(2, null);
                    mNoteContentPho.setData(2, null);
                    mNoteContentHw.setData(2, null);

                } else {
                    ArrayList<NoteInfo> mNotesMemo = note.memoNotes;
                    ArrayList<NoteInfo> mNotesPho = note.photoNotes;
                    ArrayList<NoteInfo> mNotesHw = note.hwNotes;

                    long mDateTxt = note.day * 1000;
                    boolean aa = Utils.isSameDay(mDateTxt);
                    mDay.setText(R.string.take_notes_lable_time);
                    if (mNotesMemo.size() == 0) {
                        mNoteContentMemo.setData(2, null);
                    } else {
                        mNoteContentMemo.setData(3, mNotesMemo);
                        mMemoNum.setText("(" + mNotesMemo.size() + ")");
                        mMemoNum.setVisibility(View.VISIBLE);
                    }
                    if (mNotesPho.size() == 0) {
                        mNoteContentPho.setData(2, null);
                    } else {
                        mNoteContentPho.setData(3, mNotesPho);
                        mNoteNum.setText("(" + mNotesPho.size() + ")");
                        mNoteNum.setVisibility(View.VISIBLE);
                    }
                    if (mNotesHw.size() == 0) {
                        mNoteContentHw.setData(2, null);
                    } else {
                        mNoteContentHw.setData(3, mNotesHw);
                        mHwNum.setText("(" + mNotesHw.size() + ")");
                        mHwNum.setVisibility(View.VISIBLE);
                    }
                }

            } else if (note != null) {
                ArrayList<NoteInfo> mNotesMemos = note.memoNotes;
                ArrayList<NoteInfo> mNotesPhotos = note.photoNotes;
                ArrayList<NoteInfo> mNotesHws = note.hwNotes;
                long mDateTxt = (long) (note.day) * 1000;
                Date date = new Date(mDateTxt);
                SimpleDateFormat sd = new SimpleDateFormat("MM-dd");
                SimpleDateFormat year = new SimpleDateFormat("yyyy");
                mTvYear.setText(year.format(date));
                mDate.setText(sd.format(date));
                if (mLastYear.equals(mTvYear.getText().toString())) {
                    mTvYear.setVisibility(View.GONE);
                } else {
                    mLastYear = mTvYear.getText().toString();
                }
                if (mNotesMemos.size() == 0) {

                    isNull1 = true;
                    mRlMemo.setVisibility(View.GONE);
                }
                if (mNotesPhotos.size() == 0) {

                    isNull2 = true;
                    mCirclePhoto.setVisibility(View.GONE);
                    mLinePhoto.setVisibility(View.GONE);
                    mRlPho.setVisibility(View.GONE);
                }
                if (mNotesHws.size() == 0) {

                    isNull3 = true;
                    mCircleHw.setVisibility(View.GONE);
                    mLineHw.setVisibility(View.GONE);
                    mRlHw.setVisibility(View.GONE);
                }
                if (isNull1 && (!isNull2)) {
                    mCircleMemo.setImageResource(R.drawable.pic_note_cir_orange);
                    mLineMemo.setBackgroundColor(0XFFF2BB90);
                    mCirclePhoto.setVisibility(View.GONE);
                    mLinePhoto.setVisibility(View.GONE);
                }
                if (isNull1 && isNull2 && (!isNull3)) {
                    mLineHw.setVisibility(View.GONE);
                    mCircleHw.setVisibility(View.GONE);
                    mCircleMemo.setImageResource(R.drawable.pic_note_cir_pink);
                    mLineMemo.setBackgroundColor(0XFFECBEE5);
                }
                mNoteContentMemo.setData(1, mNotesMemos);
                mMemoNum.setText("(" + mNotesMemos.size() + ")");
                mMemoNum.setVisibility(View.VISIBLE);
                mNoteContentPho.setData(1, mNotesPhotos);
                mNoteNum.setText("(" + mNotesPhotos.size() + ")");
                mNoteNum.setVisibility(View.VISIBLE);
                mNoteContentHw.setData(1, mNotesHws);
                mHwNum.setText("(" + mNotesHws.size() + ")");
                mHwNum.setVisibility(View.VISIBLE);
            }
            return v;

        }

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        mOffset = 0;
        new GetPostNoteTask().executeLong();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mDayNoteInfos.get(0) == null) {
            mOffset = mBaseAdapter.getCount() - 1;
        } else {
            mOffset = mBaseAdapter.getCount();
        }
        new GetPostNoteTask().executeLong();
    }

}
