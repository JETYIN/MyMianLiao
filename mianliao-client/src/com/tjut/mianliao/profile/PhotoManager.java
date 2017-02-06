package com.tjut.mianliao.profile;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.Photo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.Utils;

public class PhotoManager {

    public static final int TYPE_UNDEFINED = -1;
    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_AVATAR = 1;

    private static final int GALLERY_SIZE = 10;
    private static final Drawable[] COLOR_DRAWABLES = new Drawable[]{
            new ColorDrawable(0XFFFEDFDF),
            new ColorDrawable(0XFFFEEBDC),
            new ColorDrawable(0XFFFFF9E0),
            new ColorDrawable(0XFFE8FEDD),
            new ColorDrawable(0XFFE3F3FE)
    };

    private Context mContext;

    private BitmapLoader mBitmapLoader;

    private UserInfo mUserInfo;
    private Photo mAvatar;

    private int mTodoType;
    private Photo mTodoPhoto;

    private ArrayList<Photo> mPhotos = new ArrayList<Photo>();
    private ArrayList<Photo> mNewPhotos = new ArrayList<Photo>();
    private ArrayList<Photo> mDeletePhotos = new ArrayList<Photo>();
    private PhotoAdapter mAdapter = new PhotoAdapter();

    private ProImageView mIvAvatar;

    private boolean mEdit;
    private boolean mHasUpdate;

    public PhotoManager(Context ctx, UserInfo userInfo, ProImageView avatarView, boolean edit) {
        mBitmapLoader = BitmapLoader.getInstance();

        mContext = ctx;
        mEdit = edit;
        mIvAvatar = avatarView;

        setUserInfo(userInfo);
    }

    public void setUserInfo(UserInfo userInfo) {
        if (mUserInfo == userInfo) {
            return;
        }

        mUserInfo = userInfo;
        mAvatar = null;
        fillPhotos();

        // update views
        showAvatar();
        mAdapter.notifyDataSetChanged();
    }

    public void updatePhotos() {
        fillPhotos();
        mAdapter.notifyDataSetChanged();
    }

    private void fillPhotos() {
        mPhotos.clear();
        if (mUserInfo != null && mUserInfo.photoCount() > 0) {
            for (Photo photo : mUserInfo.getPhotos()) {
                if (photo.isAvatar) {
//                    mAvatar = photo;
                } else {
                    mPhotos.add(photo);
                }
            }
        }
        for (Photo photo : mNewPhotos) {
            mPhotos.add(photo);
        }
    }

    private void showAvatar() {
        if (mIvAvatar != null) {
            if (mAvatar == null) {
                Picasso.with(mContext)
                    .load(Utils.getImagePreviewSmall(mUserInfo.avatarFull))
                    .placeholder(mUserInfo.defaultAvatar())
                    .into(mIvAvatar);
//                mIvAvatar.setImage(Utils.getImagePreviewSmall(mUserInfo.avatarFull), mUserInfo.defaultAvatar());
            } else if (!TextUtils.isEmpty(mAvatar.thumbnail)) {
//                mIvAvatar.setImage(mAvatar.thumbnail, mUserInfo.defaultAvatar());
                Picasso.with(mContext)
                    .load(Utils.getImagePreviewSmall(mAvatar.thumbnail))
                    .placeholder(mUserInfo.defaultAvatar())
                    .into(mIvAvatar);
            } else {
                mBitmapLoader.setBitmap(mIvAvatar, mAvatar.fileThumb, mUserInfo.defaultAvatar());
            }
        }
    }

    public ArrayList<Photo> getNewPhotos() {
        return mNewPhotos;
    }

    /**
     * When editing finished, clear the unused files.
     */
    public void destroy() {
        for (Photo photo : mDeletePhotos) {
            Utils.deleteFile(photo.file);
            Utils.deleteFile(photo.fileThumb);
        }
        for (Photo photo : mNewPhotos) {
            Utils.deleteFile(photo.file);
            Utils.deleteFile(photo.fileThumb);
        }
    }

    public PhotoAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * @param photo
     * @param type  0, photo; 1, avatar
     */
    public void setTodo(Photo photo, int type) {
        mTodoPhoto = photo;
        mTodoType = type;
    }

    public void setTodo(Photo photo) {
        setTodo(photo, photo == null ? TYPE_UNDEFINED : TYPE_PHOTO);
    }

    public boolean hasTodo() {
        return mTodoType != TYPE_UNDEFINED;
    }

    public boolean hasUpdate() {
        return mHasUpdate;
    }

    public void addPhoto(String fileName) {
        // save it as a to-do item,
        String file = GetImageHelper.saveAsTodo(mContext, fileName);
        if (file == null) {
            return;
        }
        Photo photo = new Photo();
        photo.file = file;
        photo.fileThumb = GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE);

        if (mTodoType == TYPE_AVATAR) {
            mAvatar = photo;
            mAvatar.isAvatar = true;
            showAvatar();
        } else {
            mPhotos.add(photo);
        }

        if (mTodoPhoto != null) {
            // the to-do photo is replace, so delete it.
            deleteTodo(false);
        }

        mNewPhotos.add(photo);
        mAdapter.notifyDataSetChanged();
        mHasUpdate = true;
    }

    public Photo getAvatar() {
        return mAvatar;
    }

    public void setAvatar() {
        if (mTodoPhoto != null && mAvatar != mTodoPhoto) {
            if (mAvatar != null) {
                mPhotos.add(mAvatar);
                mAvatar.isAvatar = false;
            }
            mPhotos.remove(mTodoPhoto);
            mTodoPhoto.isAvatar = true;
            mAvatar = mTodoPhoto;
            showAvatar();
            mAdapter.notifyDataSetChanged();
            mHasUpdate = true;
        }
        setTodo(null);
    }

    public void removeAddedPhoto(ArrayList<String> urls) {
        int[] urlIndex = new int[urls.size()];
        for (int i = 0; i < urls.size(); i++) {
            for (Photo photo : mNewPhotos) {
                if (urls.get(i).equals(photo.file)) {
                    urlIndex[i] = mNewPhotos.indexOf(photo);
                }
            }
        }
        removePhoto(urlIndex);
    }

    private void removePhoto(int[] index) {
        ArrayList<Photo> photos = getPhotosByIndexs(index);
        for (Photo photo : photos) {
            mNewPhotos.remove(photo);
        }
    }

    private ArrayList<Photo> getPhotosByIndexs(int[] index) {
        ArrayList<Photo> photos = new ArrayList<>();
        for (int pos : index) {
            photos.add(mNewPhotos.get(pos));
        }
        return photos;
    }

    public String getDeletePhotos() {
        if (mDeletePhotos.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Photo photo : mDeletePhotos) {
                if (photo.id > 0) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(String.valueOf(photo.id));
                }
            }
            return sb.toString();
        }
        return "";
    }
    
    public int getPhotoSize() {
        return mPhotos.size();
    }

    public void deleteTodo(boolean notifyUpdate) {
        if (mTodoPhoto != null) {
            mTodoPhoto.isAvatar = false;
            mNewPhotos.remove(mTodoPhoto);
            mDeletePhotos.add(mTodoPhoto);
            if (mPhotos.remove(mTodoPhoto) && notifyUpdate) {
                mAdapter.notifyDataSetChanged();
            }
        }
        setTodo(null);
        mHasUpdate = true;
    }

    public class PhotoAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mUserInfo.isMine(mContext)) {
                return mPhotos.size() < GALLERY_SIZE ? mPhotos.size() + 1 : GALLERY_SIZE;
            } else {
                return mPhotos.size();
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return mEdit ? position < mPhotos.size() + 1 : position < mPhotos.size();
        }

        @Override
        public Photo getItem(int position) {
            return position < mPhotos.size() ? mPhotos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position < mPhotos.size() ? mPhotos.get(position).id : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return mEdit && position == mPhotos.size() ? 1 : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AvatarView view;
            int viewType = getItemViewType(position);
            if (convertView != null && convertView instanceof ProImageView) {
                view = (AvatarView) convertView;
            } else {
                view = (AvatarView) LayoutInflater.from(mContext).inflate(
                        R.layout.grid_item_photo_profile, parent, false);
                if (viewType == 1) {
                    view.setScaleType(ImageView.ScaleType.CENTER);
                    view.setBackgroundResource(R.drawable.bg_add_pic);
                    view.setImageResource(R.drawable.btn_add_photo);
                }
            }

            if (viewType == 0) {
                Photo photo = getItem(position);
                if (photo != null) {
                    if (photo.fileThumb == null) {
                        view.setImage(getImagePreviewSmall(photo.thumbnail), R.drawable.bg_img_loading);
                    } else {
                        mBitmapLoader.setBitmap(view, photo.fileThumb, R.drawable.bg_img_loading);
                    }
                }
            }

            return view;
        }
    }


    private String getImagePreviewSmall(String url) {
        return AliImgSpec.POST_THUMB.makeUrl(url);
    }
}
