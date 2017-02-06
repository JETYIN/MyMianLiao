package com.tjut.mianliao.image;

import java.util.List;

import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter;
import ru.truba.touchgallery.TouchView.TouchImageView;
import ru.truba.touchgallery.TouchView.TouchImageView.onTouchImageClickListener;
import android.content.Context;
import android.view.ViewGroup;

import com.tjut.mianliao.component.GalleryViewPagerW;

public class GalleryImageAdapter extends BasePagerAdapter implements onTouchImageClickListener {

    private TouchImageView mImageView;
    private OnImageItemClickListener mListener;
    
    public GalleryImageAdapter(Context context, List<String> resources) {
        super(context, resources);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mImageView = ((GalleryImageView) object).getImageView();
        mImageView.setOnTouchImageClickListener(this);
        ((GalleryViewPagerW) container).mCurrentView = mImageView;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        GalleryImageView iv = new GalleryImageView(mContext);
        iv.setImage(mResources.get(position));
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        collection.addView(iv, 0);
        return iv;
    }

    @Override
    public void onClick(boolean isDoubleClick) {
        if (mListener != null) {
            mListener.onImageClick(isDoubleClick);
        }
    }
    
    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        mListener = listener;
    }
    
    public interface OnImageItemClickListener{
        void onImageClick(boolean isDoubleClick);
    }
}
