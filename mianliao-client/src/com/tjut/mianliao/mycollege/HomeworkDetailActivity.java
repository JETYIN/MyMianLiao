package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.util.Utils;

public class HomeworkDetailActivity extends BaseActivity {
    public static final int NOTE_EDIT_VIEW = 123;
    public static final String EXT_NOTE_INFO = "ext_note_info";

    private TextView mSubject, mHomeworkContent, mTvCreateTime;
    private NoteInfo mNoteInfo;
    private String mNoteTitle;
    private GridView mGvNotepic;
    private ImageAdapter mAdapter = new ImageAdapter();
    private ArrayList<String> picurls = new ArrayList<>();
    private int mFlagId;
    private RelativeLayout mRlHwdetail;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_homework_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showRightText(R.string.edit, mEditListen);
        mNoteInfo = getIntent().getParcelableExtra(EXT_NOTE_INFO);
        mSubject = (TextView) findViewById(R.id.tv_subject);
        mHomeworkContent = (TextView) findViewById(R.id.tv_homework_content);
        mGvNotepic = (GridView) findViewById(R.id.mgv_gallery);
        mTvCreateTime = (TextView) findViewById(R.id.tv_create_time);
        mAdapter.setImages(mNoteInfo.images);
        mGvNotepic.setAdapter(mAdapter);
        mSubject.setText(mNoteInfo.course);
        mHomeworkContent.setText(mNoteInfo.content);
        if (mNoteInfo.images != null) {
            for (int i = 0; i < mNoteInfo.images.size(); i++) {
                picurls.add(mNoteInfo.images.get(i).image);
            }
        }
        mGvNotepic.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.viewImages(HomeworkDetailActivity.this, picurls, position);
            }
        });
        setTitle();
        if (mNoteInfo.updatedOn <= mNoteInfo.createdOn) {
			mTvCreateTime.setText("创建于" + Utils.getTimeString(6, mNoteInfo.createdOn));
		} else {
			mTvCreateTime.setText("更新于" + Utils.getTimeString(6, mNoteInfo.updatedOn));
		}
    }

    private void setTitle() {
        switch (mNoteInfo.noteType) {
            case NoteInfo.TYPE_HOMEWORK:
                getTitleBar().setTitle(R.string.take_notes_lable_homework);
                mNoteTitle = mNoteInfo.course;
                break;
            case NoteInfo.TYPE_PHOTO:
                getTitleBar().setTitle(R.string.take_notes_lable_notes);
                mNoteTitle = mNoteInfo.course;
                break;

            default:
                break;
        }
    }

    OnClickListener mEditListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
            intent.putExtra(HomeworkNotesActivity.EXT_EDIT_NOTE, mNoteInfo);
            intent.putExtra("Editis", "true");
            intent.putExtra("mNoteTitle", mNoteTitle);
            intent.setClass(HomeworkDetailActivity.this, HomeworkNotesActivity.class);
            startActivityForResult(intent, NOTE_EDIT_VIEW);
        }
    };

    private class ImageAdapter extends BaseAdapter {

        private ArrayList<Image> mImages = new ArrayList<>();

        public void setImages(ArrayList<Image> imgs) {
            mImages = imgs;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (mImages == null) {
                return 0;
            } else {
                return mImages.size();
            }
        }

        @Override
        public String getItem(int position) {
            return mImages.get(position).image;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout view;
            if (convertView != null && convertView instanceof ProImageView) {
                view = (FrameLayout) convertView;
            } else {
                view = (FrameLayout) mInflater.inflate(R.layout.grid_item_photo, parent, false);
            }
           ((ProImageView) view.findViewById(R.id.giv_photo)).setImage(getItem(position), 0);
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                mNoteInfo = (NoteInfo) data.getParcelableExtra (
                        HomeworkNotesActivity.EXT_HOMEWORK_NOTE);
                if (mNoteInfo.images != null && mNoteInfo.images.size() >= 0) {
                    mAdapter.setImages(mNoteInfo.images);
                } else {
                    mGvNotepic.setVisibility(View.GONE);
                }
                mFlagId = mNoteInfo.postId;
                mSubject.setText(mNoteInfo.course);
                mHomeworkContent.setText(mNoteInfo.content);
                setTitle();
                break;

            default:
                break;
        }
    }


    @Override
    protected void onResume() {
    	if (mNoteInfo.updatedOn <= mNoteInfo.createdOn) {
			mTvCreateTime.setText("创建于" + Utils.getTimeString(6, mNoteInfo.createdOn));
		} else {
			mTvCreateTime.setText("更新于" + Utils.getTimeString(6, mNoteInfo.updatedOn));
		}
    	super.onResume();
    }
}
