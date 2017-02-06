package com.tjut.mianliao.mycollege;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class HorizontalScrollMemo extends HorizontalScrollView implements OnClickListener {

	private LinearLayout mCotentLayout;
	private Context mContext;
	private TextView mTime, mContent;
	private int mCurrentId;
    private LightDialog mDelDialog;
    private ArrayList<NoteInfo> mCopyNote;
    private OnAddClickListener mListener;
    private OnDelClickListener mDelListener;

	public HorizontalScrollMemo(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mCotentLayout = new LinearLayout(context);
		addView(mCotentLayout);
	}

    public void setData(int flag, ArrayList<NoteInfo> notes) {
	    mCopyNote =notes;
		mCotentLayout.removeAllViews();
		if (flag == 2) {
            for (int i = 0; i < 2; i++) {
                mCotentLayout.addView(mNullView(i));
            }
        } else if (flag == 3){
        	mCotentLayout.addView(mNullView(0));
        	if (notes != null && notes.size() > 0) {
                for (NoteInfo info : notes) {
                    mCotentLayout.addView(getView(info));
                }
            }
        } else {
            if (notes != null && notes.size() > 0) {
                for (NoteInfo info : notes) {
                    mCotentLayout.addView(getView(info));
                }
            }
        }
		requestLayout();
	}
    
    public void registerOnAddClickListener(OnAddClickListener listener) {
    	if (listener != null) {
    		mListener = listener;
    	}
    }
    public void registerOnDelClickListener(OnDelClickListener listener) {
    	if (listener != null ) {
    		mDelListener = listener;
    	}
    }
    
	public View getView(NoteInfo info){
			View v = inflate(mContext, R.layout.list_item_memo_adapter,
					null);
			mTime = (TextView) v.findViewById(R.id.tv_memo_time);
			mContent = (TextView) v.findViewById(R.id.tv_memo_content);

			long mDateTxt = (long) (info.clock) * 1000;
			if(mDateTxt == 0){
				mTime.setVisibility(View.INVISIBLE);
			}else {
				Date date = new Date(mDateTxt);
				SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
				mTime.setText(sd.format(date));
			}

			mContent.setText(info.content);
			v.setOnClickListener(this);
			v.setOnLongClickListener(mLclickListen);
			v.setTag(info);
			return v;
	}
	private OnLongClickListener mLclickListen = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
        	if (mDelListener != null) {
        		NoteInfo  mCurrentNote = (NoteInfo) v.getTag();
        		mCurrentId = mCurrentNote.postId;
				mDelListener.OnDelClick(mCurrentId);
			}
        	return false;
//            showDelDialog();
//            return false;
        }
    };
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()){
			case R.id.rl_note_detail:
				intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
				NoteInfo m = (NoteInfo) v.getTag();
				intent.putExtra(MemoDetailActivity.EXT_MEMO_NOTE,(NoteInfo) v.getTag());
				intent.setClass(mContext, MemoDetailActivity.class);
				mContext.startActivity(intent);
				break;
			default:
	            break;
		}
	}

	public View mNullView(int i){
		View v = inflate(mContext, R.layout.list_item_takenotes_adapter, null);
		RelativeLayout mRlNull = (RelativeLayout) v.findViewById(R.id.rl_note_detail);
		TextView mNotePeompt = (TextView) v.findViewById(R.id.tv_prompt);
		if (i == 0) {
            mRlNull.setBackgroundResource(R.drawable.memo_bottom_bg_border);
            mNotePeompt.setText(R.string.never_put_tomorrow);
            mNotePeompt.setVisibility(View.VISIBLE);
            mRlNull.setOnClickListener(myAddOnclickListen);
		} else {
		    mRlNull = (RelativeLayout) v.findViewById(R.id.rl_note_detail);
            mRlNull.setBackgroundResource(R.drawable.school_pic_bg_border);
            mNotePeompt.setText(R.string.memo_prompt);
            mNotePeompt.setVisibility(View.VISIBLE);
		}
		return v;
	}
	private OnClickListener myAddOnclickListen = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onAddClick(1);
			}
		}
	};
	private void showDelDialog(){
        if(mDelDialog == null){
            mDelDialog = new LightDialog(mContext)
            .setTitleLd(R.string.take_note_isdelate)
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new DelNoteTask().executeLong();

                }
            });
        }
        mDelDialog.show();
    }

    private class DelNoteTask extends MsTask {

        public DelNoteTask() {
            super(mContext, MsRequest.DELETE_NOTE_POST);
        }
        @Override
        protected String buildParams() {
            return "thread_id=" + mCurrentId;
        }
        @Override
        protected void onPostExecute(MsResponse response) {
           if(response.isSuccessful()){
               try {
                    for (int i = 0; i < mCopyNote.size(); i++) {
                      if (mCurrentId == mCopyNote.get(i).postId){
                          mCopyNote.remove(i);
                      }
                   }
                   if(mCopyNote.size()>0){
                       setData(1, mCopyNote);
                   }else {
                       setData(2, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
           }
        }
    }

    public interface OnAddClickListener{
    	void onAddClick(int type);
    }
    public interface OnDelClickListener{
    	void OnDelClick(int Id);
    }  
}

