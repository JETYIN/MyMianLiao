package com.tjut.mianliao.job;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Option;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class OptionManager {

    private static final long POLLING_MILLIS = 60 * 60 * 1000;

    private static WeakReference<OptionManager> sInstanceRef;

    private Context mContext;
    private Handler mHandler;
    private Option mOptionAny;

    private SparseArray<Option> mLocations;
    private ArrayList<Option> mCategories;
    private ArrayList<Option> mTypes;

    public static synchronized OptionManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        OptionManager instance = new OptionManager(context);
        sInstanceRef = new WeakReference<OptionManager>(instance);
        return instance;
    }

    private OptionManager(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mOptionAny = new Option();
        mOptionAny.desc = context.getString(R.string.job_filter_any);
        mOptionAny.addSubOption(mOptionAny.copy());

        mLocations = new SparseArray<Option>();
        mCategories = new ArrayList<Option>();
        mTypes = new ArrayList<Option>();

        mHandler.post(mPollingRunnable);
    }

    public SparseArray<Option> getLocations() {
        return mLocations;
    }

    public ArrayList<Option> getCategories() {
        return mCategories;
    }

    public ArrayList<Option> getTypes() {
        return mTypes;
    }

    public Option getCategory(int index) {
        return index >= 0 && index < mCategories.size()
                ? mCategories.get(index) : null;
    }

    public Option getType(int index) {
        return index >= 0 && index < mTypes.size()
                ? mTypes.get(index) : null;
    }

    private void loadLocations(JSONArray ja) {
        mLocations.clear();
        mLocations.put(mOptionAny.id, mOptionAny);
        int length = ja == null ? 0 : ja.length();
        for (int i = 0; i < length; i++) {
            Option opt = Option.fromJson(ja.optJSONObject(i));
            if (opt != null) {
                if (opt.hasParent()) {
                    Option parentOpt = mLocations.get(opt.parentId, mOptionAny);
                    parentOpt.addSubOption(opt);
                } else {
                    opt.addSubOption(opt.copy());
                    mLocations.put(opt.id, opt);
                }
            }
        }
    }

    private void loadCategories(JSONArray ja) {
        mCategories.clear();
        mCategories.add(mOptionAny);
        mCategories.addAll(JsonUtil.getArray(ja, Option.TRANSFORMER));
    }

    private void loadTypes(JSONArray ja) {
        mTypes.clear();
        mTypes.add(mOptionAny);
        mTypes.addAll(JsonUtil.getArray(ja, Option.TRANSFORMER));
    }

    private Runnable mPollingRunnable = new Runnable() {
        @Override
        public void run() {
            new GetLocationTask().executeLong();
            new GetTypeTask().executeLong();
            mHandler.postDelayed(this, POLLING_MILLIS);
        }
    };

    private class GetLocationTask extends MsTask {

        public GetLocationTask() {
            super(mContext, MsRequest.LIST_LOCATION);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                loadLocations(response.getJsonArray());
            }
        }
    }

    private class GetTypeTask extends MsTask {

        public GetTypeTask() {
            super(mContext, MsRequest.LIST_JOB_TYPE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();

                JSONArray ja = json == null ? null : json.optJSONArray("categories");
                loadCategories(ja);

                ja = json == null ? null : json.optJSONArray("types");
                loadTypes(ja);
            }
        }
    }
}
