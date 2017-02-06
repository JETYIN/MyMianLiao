package com.tjut.mianliao.component;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.Utils;

public class FlexibleImageView extends FrameLayout {

    private static final String TAG = "FlexibleImageView";
    
    private Context mContext;
    
    private ProImageView mIvImage;
    private GridView mGvImage;
    private GridView mGvImages;
    private TextView mTvCount;

    private int mDimenSingleImageHeight;
    private int mDimenDoubleImageHeight;
    private int mDimenMuiltImageHeight;
    
    private LayoutInflater mInflater;
    private ImageAdapter mAdapter;
    private ArrayList<Image> mImages;
    private int mMaxCount = 9;
    private boolean mClickble = true;
    private OnImageClickListener mListener;
    
    private HashMap<View, String> mViewMaps;

    private boolean isPermitLoad = true;
    
    private boolean mIsNightMode;

    public void setPermitLoad(boolean flag) {  
        isPermitLoad = flag;
    }

    public FlexibleImageView(Context context) {
        super(context, null);
    }

    public void setClickble(boolean clickble) {
        mClickble = clickble;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        mListener = listener;
    }

    public FlexibleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context.getApplicationContext();
        mIsNightMode = Settings.getInstance(context).isNightMode();
        Resources res = context.getResources();
        mDimenSingleImageHeight = res.getDimensionPixelSize(R.dimen.fi_single_image_height);
        mDimenDoubleImageHeight = res.getDimensionPixelSize(R.dimen.fi_double_image_height);
        mDimenMuiltImageHeight = res.getDimensionPixelSize(R.dimen.fi_muilt_image_height);
        mAdapter = new ImageAdapter();
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.comp_flexible_image_view, this);
        mViewMaps = new HashMap<>();

        mIvImage = (ProImageView) findViewById(R.id.iv_image);
        mIvImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickble) {
                    Utils.viewImages(getContext(), getImageUrls(), 0);
                }
                if (mListener != null) {
                    mListener.onImageClicked();
                }
            }
        });

        mGvImage = (GridView) findViewById(R.id.gv_image);
        mGvImage.setAdapter(mAdapter);
        mGvImage.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.viewImages(getContext(), getImageUrls(), position);
            }
        });

        mGvImages = (GridView) findViewById(R.id.gv_images);
        mGvImages.setAdapter(mAdapter);
        mGvImages.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.viewImages(getContext(), getImageUrls(), position);
            }
        });

        mTvCount = (TextView) findViewById(R.id.tv_count);
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
    }

    public void setImages(ArrayList<Image> images) {
        int count = images == null ? 0 : images.size();
        setVisibility(count == 0 ? GONE : VISIBLE);
        mImages = images;

        if (count == 1) {
            mIvImage.setVisibility(VISIBLE);
            mGvImages.setVisibility(GONE);
            mGvImage.setVisibility(GONE);
            String url = getImagePreviewSmall(0);
            if (isPermitLoad) {
            	Picasso.with(mContext)
                	.load(url)
                        .placeholder(mIsNightMode ? R.drawable.bg_default_big_night :
                            R.drawable.bg_default_big_day)
                	.into(mIvImage);
            } else {
            	Picasso.with(mContext)
                	.load(R.drawable.bg_img_loading)
                	.into(mIvImage);
            }

            return;
        } else {
            mIvImage.setVisibility(GONE);
        }

        if (count == 2) {
            mGvImages.setVisibility(GONE);
            mGvImage.setVisibility(VISIBLE);
            mIvImage.setVisibility(GONE);
            mAdapter.notifyDataSetChanged();
        } else {
            mGvImages.setVisibility(VISIBLE);
            mGvImage.setVisibility(GONE);
            mIvImage.setVisibility(GONE);
            mAdapter.notifyDataSetChanged();
        }

        if (count > mMaxCount) {
            mTvCount.setVisibility(VISIBLE);
            mTvCount.setText(getContext().getString(R.string.cf_image_count, count));
        } else {
            mTvCount.setVisibility(GONE);
        }
    }

    public int getImageCount() {
        return mImages == null ? 0 : mImages.size();
    }

    private String getImagePreviewSmall(int index) {
        int count = getImageCount();
        if (count == 0 || index >= count) {
            return null;
        } else if (count == 1) {
            return AliImgSpec.POST_PHOTO.makeUrlSingleImg(mImages.get(0).image);
        } else {
            return AliImgSpec.POST_THUMB_SQUARE.makeUrl(mImages.get(index).image);
        }
    }

    private ArrayList<String> getImageUrls() {
        ArrayList<String> urls = new ArrayList<String>();
        if (mImages != null) {
            for (Image image : mImages) {
                urls.add(image.image);
            }
        }
        return urls;
    }

    private class ImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Math.min(mMaxCount, getImageCount());
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
            String url = getItem(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.grid_item_photo_normal, parent, false);
                holder.image = (ProImageView) convertView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                String cacheUrl = mViewMaps.get(convertView);
                if (!isPermitLoad && cacheUrl != null && !"".equals(cacheUrl) && cacheUrl.equals(url)) {
                    Utils.logD(TAG, "Read from cache : " + url);
                    return convertView;
                }
            }

            if (isPermitLoad) {
            	Picasso.with(mContext)
            		.load(url)
                        .placeholder(mIsNightMode ? R.drawable.bg_default_small_night :
                            R.drawable.bg_default_small_day)
            		.into(holder.image);
                mViewMaps.put(convertView, url);
            } else {
            	Picasso.with(mContext).load(R.drawable.bg_img_loading).into(holder.image);
            }

            return convertView;
        }
    }

    class ViewHolder {
        ProImageView image;
    }

    public interface OnImageClickListener {
        void onImageClicked();
    }
}
