package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.mycollege.HorizontalScrollMemo.OnAddClickListener;
import com.tjut.mianliao.mycollege.HorizontalScrollMemo.OnDelClickListener;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class HorizontalScrollPhotonotes extends HorizontalScrollView implements OnClickListener {
    private LinearLayout mCotentLayout;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mCurrentId;
    private LightDialog mDelDialog;
    private ArrayList<NoteInfo> mCopyNote;
    private OnAddClickListener mListener;
    private OnDelClickListener mDelListener;

    public HorizontalScrollPhotonotes(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCotentLayout = new LinearLayout(context);
        addView(mCotentLayout);
    }
    public void registerOnDelClickListener(OnDelClickListener listener) {
    	if (listener != null ) {
    		mDelListener = listener;
    	}
    }
    public void setData(int flag, ArrayList<NoteInfo> notes) {
        mCopyNote = notes;
        mCotentLayout.removeAllViews();
        if (flag == 2) {
            for (int i = 0; i < 2; i++) {
                mCotentLayout.addView(mNullView(i));
            }
        } else if (flag == 3){
        	mCotentLayout.addView(mNullView(0));
        	if (notes != null && notes.size() > 0) {
                for (NoteInfo info : notes) {
                    mCotentLayout.addView(getview(info));
                }
            }
        } else {
            if (notes != null && notes.size() > 0) {
                for (NoteInfo info : notes) {
                    mCotentLayout.addView(getview(info));
                }
            }
        }
        requestLayout();
    }

    public View getview(NoteInfo info) {
        View v = inflate(mContext, R.layout.list_item_takenotes_adapter, null);
        TextView mNotesTitle = (TextView) v.findViewById(R.id.tv_notesdetail_title);
        TextView mNotesDetail = (TextView) v.findViewById(R.id.tv_notesdetail_content);
        GridView mGvImg = (GridView) v.findViewById(R.id.mgv_images);
        ImageAdapter adapter = new ImageAdapter();
        adapter.setImages(info.images);
        mGvImg.setAdapter(adapter);
        mNotesTitle.setText(info.course);
        mNotesDetail.setText("\t"+info.content);
        v.setOnClickListener(this);
        v.setOnLongClickListener(mLclickListen);
        v.setTag(info);
        return v;
    }

//    private OnLongClickListener mLclickListen = new OnLongClickListener() {
//
//        @Override
//        public boolean onLongClick(View v) {
//            NoteInfo  mCurrentNote = (NoteInfo) v.getTag();
//            mCurrentId = mCurrentNote.postId;
//            showDelDialog();
//            return false;
//        }
//    };
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_note_detail:
                Intent intent = new Intent(mContext, HomeworkDetailActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                intent.putExtra(HomeworkDetailActivity.EXT_NOTE_INFO, (NoteInfo) v.getTag());
                mContext.startActivity(intent);
                break;
            default:
                break;
        }
    }

    private class ImageAdapter extends BaseAdapter {

        private ArrayList<Image> mImages;

        public void setImages(ArrayList<Image> imgs) {
            mImages = imgs;
        }

        @Override
        public int getCount() {
            if (mImages.size() < 4) {
                return mImages.size();
            } else {
                return 3;
            }
        }

        @Override
        public String getItem(int position) {
            return getImagePreviewSmall(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int viewType = getItemViewType(position);
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.grid_item_photo, parent, false);
                holder = new ViewHolder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
//            Picasso.with(mContext).load(getItem(position)).into(holder.pivPhoto);
            holder.pivPhoto.setImage(getItem(position), 0);
            holder.ivDel.setVisibility(View.GONE);
            return convertView;
        }

        private String getImagePreviewSmall(int index) {
            int count = getImageCount();
            if (count == 0 || index >= count) {
                return null;
            } else {
                return AliImgSpec.TAKE_NOTE_IMAGE.makeUrl(mImages.get(index).image);
            }
        }

        public int getImageCount() {
            return mImages == null ? 0 : mImages.size();
        }
    }

    public View mNullView(int i) {
        View v = inflate(mContext, R.layout.list_item_takenotes_adapter, null);
        RelativeLayout mRlNull = (RelativeLayout) v.findViewById(R.id.rl_note_detail);
        TextView mNotePeompt = (TextView) v.findViewById(R.id.tv_prompt);
        if (i == 0) {
            mRlNull.setBackgroundResource(R.drawable.memo_bottom_bg_border);
            mNotePeompt.setText(R.string.note_saying);
            mNotePeompt.setVisibility(View.VISIBLE);
            mRlNull.setOnClickListener(myAddOnclickListen);
            return v;
        } else {
        	mRlNull.setBackgroundResource(R.drawable.school_pic_bg_border);
            mNotePeompt.setText(R.string.note_prompt);
            mNotePeompt.setVisibility(View.VISIBLE);
            return v;
        }
    }
	private OnClickListener myAddOnclickListen = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onAddClick(2);
			}
		}
	};
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
                   for(int i = 0; i < mCopyNote.size(); i++){
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

    public void registerOnAddClickListener(OnAddClickListener listener) {
    	if (listener != null) {
    		mListener = listener;
    	}
    }
    
    
    private class ViewHolder{
    	@ViewInject(R.id.giv_photo)
    	ProImageView pivPhoto;
    	@ViewInject(R.id.iv_del)
        ImageView ivDel;
    }
}
