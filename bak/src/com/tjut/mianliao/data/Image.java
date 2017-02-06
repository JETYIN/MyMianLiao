package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class Image implements Parcelable {
    public static final String ID = "id";
    public static final String IMAGE = "image";

    public static final int PREVIEW_SIZE = 120;

    public int id;
    public String image;

    public String file;
    public String fileThumb;

    public Image() {}

    public Image(String file, String fileThumb) {
        this.file = file;
        this.fileThumb = fileThumb;
    }

    public static Image fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Image image = new Image();
        image.id = json.optInt(ID);
        image.image = json.optString(IMAGE);

        return image;
    }

    public static final JsonUtil.ITransformer<Image> TRANSFORMER =
            new JsonUtil.ITransformer<Image>() {
        @Override
        public Image transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<Image> CREATOR =
            new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Image(Parcel in) {
        id = in.readInt();
        image = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(image);
    }
}
