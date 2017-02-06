package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;

public class IMResource implements Parcelable {

    public static final int TYPE_BACKGROUND = 1;
    public static final int TYPE_BUBBLE = 2;
    public static final int TYPE_EMOTION_PACKAGE = 3;
    public static final int TYPE_EMOTION = 4;
    public static final int TYPE_CHARACTER_ACTION = 5;
    public static final int TYPE_CHARACTER_ACCESSORY = 6;
    public static final int TYPE_COURSE_BACKGROUD = 7;

    public int id;
    public int type;
    public int ver;
    public String sn;
    public String name;
    public String intro;
    public int price;
    public int credit;
    public String preview;
    public String[][] urls;
    public ArrayList<IMResource> children;
    public boolean vip;
    public boolean add;
    public boolean use;
    public int vipPrice;
    public int vipCredit;

    public static final JsonUtil.ITransformer<IMResource> TRANSFORMER =
            new JsonUtil.ITransformer<IMResource>() {
        @Override
        public IMResource transform(JSONObject json) {
            return fromJson(json);
        }
    };

    private IMResource() {}

    public String getImResPrice() {
        String imPrice = "";
        if (vipPrice > 0) {
            imPrice = vipPrice + "金币";
        } else if (vipCredit > 0) {
            imPrice = vipCredit + "麦粒";
        } else if (price > 0) {
            imPrice = price + "金币";
        } else if (credit > 0) {
            imPrice = credit + "麦粒";
        }
        return imPrice;
    }
    
    public String getImResNormalPrice () {
        String imPrice = "";
        if (price > 0) {
            imPrice = price + "金币";
        } else if (credit > 0) {
            imPrice = credit + "麦粒";
        }
        return imPrice;
    }
    public int getImResPricenum() {
        int imPricenum = 0;
        if (vipPrice > 0) {
            imPricenum = vipPrice;
        } else if (vipCredit > 0) {
            imPricenum = vipCredit;
        } else if (price > 0) {
        	imPricenum = price;
        } else if (credit > 0) {
        	imPricenum = credit;
        }
        return imPricenum;
    }
    public int getImResImg() {
    	int mImgId = R.drawable.bottom_icon_littlekernel;
        if (price > 0) {
        	mImgId = R.drawable.bottom_icon_littlekernel;
        } else if (credit > 0) {
        	mImgId = R.drawable.bottom_icon_littlegolden;
        }
        return mImgId;
    }
    public String getImResVipPrice() {
        String imPrice = "";
        if (vipPrice > 0) {
            imPrice = vipPrice + "金币";
        } else if (vipCredit > 0) {
            imPrice = vipCredit + "麦粒";
        }
        return imPrice;
    }

    public boolean isAllFree() {
        return price == 0 && credit == 0;
    }

    public boolean isFree() {
        return isAllFree() || isVipFree();
    }

    public boolean isVipFree() {
        return vipCredit == 0 && vipPrice == 0;
    }

    public static boolean equals(IMResource resA, IMResource resB) {
        return (resA == resB) ||
                (resA != null && resB != null && resA.equals(resB));
    }

    public static IMResource fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        IMResource res = new IMResource();
        res.id = json.optInt("id");
        res.type = json.optInt("type");
        res.ver = json.optInt("ver");
        res.sn = json.optString("sn");
        res.name = json.optString("name");
        res.intro = json.optString("intro");
        res.price = json.optInt("price");
        res.credit = json.optInt("credit");
        res.preview = json.optString("preview");
        res.urls = new String[][] {
                new String[] {
                        json.optString("url"),
                        json.optString("url2")
                },
                new String[] {
                        json.optString("url3"),
                        json.optString("url4")
                }

        };
        res.children = JsonUtil.getArray(json.optJSONArray("children"), TRANSFORMER);
        res.vip = json.optBoolean("vip");
        res.add = json.optBoolean("add");
        res.use = json.optBoolean("use");
        res.vipPrice = json.optInt("vip_price");
        res.vipCredit = json.optInt("vip_credit");
        return res;
    }

    public static final Parcelable.Creator<IMResource> CREATOR =
            new Parcelable.Creator<IMResource>() {
        @Override
        public IMResource createFromParcel(Parcel source) {
            return new IMResource(source);
        }

        @Override
        public IMResource[] newArray(int size) {
            return new IMResource[size];
        }
    };

    private IMResource(Parcel source) {
        id = source.readInt();
        type = source.readInt();
        ver = source.readInt();
        sn = source.readString();
        name = source.readString();
        intro = source.readString();
        price = source.readInt();
        credit = source.readInt();
        preview = source.readString();
        urls = new String[][] { new String[2], new String[2] };
        source.readStringArray(urls[0]);
        source.readStringArray(urls[1]);

        int size = source.readInt();
        if (size > 0) {
            children = new ArrayList<IMResource>();
            for (int i = 0; i < size; i++) {
                IMResource res = source.readParcelable(IMResource.class.getClassLoader());
                children.add(res);
            }
        }
        vip = source.readInt() == 0 ? false : true;
        add = source.readInt() == 0 ? false : true;
        use = source.readInt() == 0 ? false : true;
        vipPrice = source.readInt();
        vipCredit = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeInt(ver);
        dest.writeString(sn);
        dest.writeString(name);
        dest.writeString(intro);
        dest.writeInt(price);
        dest.writeInt(credit);
        dest.writeString(preview);
        dest.writeStringArray(urls[0]);
        dest.writeStringArray(urls[1]);

        int size = children == null ? 0 : children.size();
        dest.writeInt(size);
        if (size > 0) {
            for (IMResource res : children) {
                dest.writeParcelable(res, flags);
            }
        }
        dest.writeInt(vip ? 1 : 0);
        dest.writeInt(add ? 1 : 0);
        dest.writeInt(use ? 1 : 0);
        dest.writeInt(vipPrice);
        dest.writeInt(vipCredit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof IMResource) {
            IMResource other = (IMResource) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
