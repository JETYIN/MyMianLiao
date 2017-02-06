package com.tjut.mianliao.forum;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.Utils;

public class MultiImageHelper {
    private Context mContext;

    private ArrayList<Image> mImages = new ArrayList<Image>();
    private ArrayList<Image> mNewImages = new ArrayList<Image>();
    private ArrayList<Image> mDeleteImages = new ArrayList<Image>();

    private Image mPending;

    public MultiImageHelper(Context ctx) {
        mContext = ctx;
    }

    public void addImages(List<Image> images) {
        if (images != null) {
            mImages.addAll(images);
        }
    }

    public void resetImages(List<Image> images) {
        if (images != null) {
            mImages.clear();
            mNewImages.clear();
            mDeleteImages.clear();
            mImages.addAll(images);
            mNewImages.addAll(images);
        }

    }

    public void setDeletedImages(List<Image> images) {
        if (images != null) {
            mDeleteImages.addAll(images);
        }
    }

    public Image addImage(String fileName) {
        String file = GetImageHelper.saveAsTodo(mContext, fileName);
        Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
        int index = deletePending();
        mImages.add(index, image);
        mNewImages.add(image);
        return image;
    }

    public boolean delImage(String fileName) {
        if (fileName == null || "".equals(fileName)) {
            return false;
        }
        Image delImage = null;
        for (Image image : mImages) {
            if (fileName.equals(image.image) || fileName.equals(image.fileThumb)) {
                delImage = image;
            }
        }
        if (delImage != null) {
            deleteImage(delImage);
            return true;
        }
        return false;

    }

    public void replaceImage(int index, String fileName) {
        if (fileName == null || "".equals(fileName)) {
            return;
        }
        String file = GetImageHelper.saveAsTodo(mContext, fileName);
        Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
        mImages.remove(index);
        mImages.add(index, image);
        mNewImages.remove(index);
        mNewImages.add(index, image);
    }

    public ArrayList<Image> addImage(ArrayList<String> fileNames) {
        ArrayList<Image> images = new ArrayList<>();
        for (String fileName : fileNames) {
            images.add(addImage(fileName));
        }
        return images;
    }

    public boolean hasNewImages() {
        return mNewImages.size() > 0;
    }

    public boolean hasDeletedImages() {
        if (mDeleteImages.size() > 0) {
            for (Image photo : mDeleteImages) {
                if (photo.id > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasUpdate() {
        return hasNewImages() || hasDeletedImages();
    }

    public void setPending(Image image) {
        mPending = image;
    }

    public int deletePending() {
        int index = deleteImage(mPending);
        mPending = null;
        return index;
    }

    public ArrayList<Image> getImages() {
        return mImages;
    }

    public ArrayList<Image> getNewImages() {
        return mNewImages;
    }

    public ArrayList<Image> getDelImages() {
        return mDeleteImages;
    }

    /**
     * When editing finished, clear the unused files.
     */
    public void destroy() {
        for (Image photo : mDeleteImages) {
            Utils.deleteFile(photo.file);
            Utils.deleteFile(photo.fileThumb);
        }
        for (Image photo : mNewImages) {
            Utils.deleteFile(photo.file);
            Utils.deleteFile(photo.fileThumb);
        }
    }

    public void deleteImage(int index) {
        if (index > mImages.size() - 1) {
            return;
        }
        mImages.remove(index);
    }

    public int deleteImage(Image image) {
        int index = mImages.size();
        if (image == null) {
            return index;
        }
        int size = mImages.size();
        for (int i = 0; i < size; i++) {
            if (mPending == mImages.get(i)) {
                index = i;
                break;
            }
        }
        mImages.remove(image);
        mNewImages.remove(image);
        mDeleteImages.add(image);
        return index;
    }

}