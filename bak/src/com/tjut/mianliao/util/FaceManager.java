package com.tjut.mianliao.util;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.FaceInfo;
import com.tjut.mianliao.data.Faceset;

public class FaceManager {
    private static final String TAG = "FaceManager";

    private static final String FACEPP_API_KEY    = "bd0639da2eeb47f3b36eb69f3399a3c1";
    private static final String FACEPP_API_SECRET = "LqEPwxTS6DkeyIdLBs_IRNkV1M1bB7X4";

    private static final int SEARCH_FACE_DELTA_COUNT = 10;
    private static final int SEARCH_FACE_MAX_COUNT = 10000;

    private static WeakReference<FaceManager> sInstanceRef;

    private Context mContext;
    private HttpRequests mFaceppRequests;
    private List<FaceListener> mListeners;
    private Map<String, List<Faceset>> mFacesetMap;
    private Map<Faceset, List<FaceInfo>> mCandidateMap;

    private FaceInfo mCurrentFace;
    private String mCurrentGender;
    private int mCurrentCount;
    private List<Faceset> mCurrentFacesets;
    private List<FaceInfo> mCurrentCandidates;

    public static synchronized FaceManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        FaceManager instance = new FaceManager(context);
        sInstanceRef = new WeakReference<FaceManager>(instance);
        return instance;
    }

    private FaceManager(Context context) {
        mContext = context;
        mFaceppRequests = new HttpRequests(FACEPP_API_KEY, FACEPP_API_SECRET);
        mListeners = new ArrayList<FaceListener>();
        mFacesetMap = new HashMap<String, List<Faceset>>();
        mCandidateMap = new HashMap<Faceset, List<FaceInfo>>();
        mCurrentFacesets = new ArrayList<Faceset>();
        mCurrentCandidates = new ArrayList<FaceInfo>();
    }

    public void registerListener(FaceListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterListener(FaceListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void clear() {
        mCurrentFace = null;
        mCurrentGender = null;
        mCurrentCount = 0;
        mCurrentFacesets.clear();
        mCurrentCandidates.clear();
        mCandidateMap.clear();
        mFacesetMap.clear();
        mListeners.clear();
        sInstanceRef.clear();
    }

    public void obtainMyFace(Bitmap bitmap, String faceId) {
        new ObtainMyFaceTask(bitmap, faceId).executeLong();
    }

    public void obtainSimilarFace(FaceInfo face, String gender) {
        if (mListeners.isEmpty()) {
            return;
        }

        if (!face.equals(mCurrentFace) || !gender.equals(mCurrentGender)) {
            mCurrentFace = face;
            mCurrentGender = gender;
            mCurrentCount = 0;
            mCurrentFacesets.clear();
            mCurrentCandidates.clear();
            mCandidateMap.clear();
        }

        if (mCurrentCandidates.isEmpty()) {
            if (mFacesetMap.isEmpty()) {
                new ObtainFacesetsTask().executeLong();
            } else {
                if (mCurrentFacesets.isEmpty()) {
                    List<Faceset> facesets = mFacesetMap.get(getFacesetTag(gender));
                    if (facesets != null) {
                        mCurrentFacesets.addAll(facesets);
                        mCurrentCount = Math.min(SEARCH_FACE_MAX_COUNT,
                                mCurrentCount + SEARCH_FACE_DELTA_COUNT);
                    }
                }
                if (mCurrentFacesets.isEmpty()) {
                    notifyError(mContext.getString(R.string.face_error_get_facesets));
                } else {
                    int index = (int) (Math.random() * mCurrentFacesets.size());
                    new ObtainCandidatesTask(
                            face, mCurrentFacesets.get(index), mCurrentCount).executeLong();
                }
            }
        } else {
            int index = (int) (Math.random() * mCurrentCandidates.size());
            new ObtainSimilarFaceTask(mCurrentCandidates.get(index)).executeLong();
        }
    }

    public MsResponse addUserFaceBG(String url) {
        MsResponse mr = fppDetectionDetect(url);
        if (!MsResponse.isSuccessful(mr)) {
            return mr;
        }

        FaceInfo face = mrToFace(mr);
        if (face == null) {
            return getErrorResponse(R.string.face_error_detect_face);
        }

        mr = fppInfoGetFacesetList();
        if (!MsResponse.isSuccessful(mr)) {
            return mr;
        }

        List<Faceset> facesets = mrToFacesets(mr, getFacesetTag(face.gender));
        if (facesets == null) {
            return getErrorResponse(R.string.face_error_get_facesets);
        }

        if (!addUserFace(face, facesets)) {
            mr = getErrorResponse(R.string.face_error_add_face);
        }

        mr.value = face;
        return mr;
    }

    public void removeUserFace(String faceId) {
        new RemoveFaceTask(faceId).executeLong();
    }

    private MsResponse fppDetectionDetect(String url) {
        return fppDetectionDetect(new PostParameters().setUrl(url));
    }

    private MsResponse fppDetectionDetect(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        return fppDetectionDetect(new PostParameters().setImg(data));
    }

    private MsResponse fppDetectionDetect(PostParameters params) {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.detectionDetect(params.setMode("oneface"));
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse fppRecognitionSearch(String faceId, String facesetId, int count) {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.recognitionSearch(new PostParameters()
                    .setKeyFaceId(faceId).setFacesetId(facesetId).setCount(count));
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse fppInfoGetFace(String faceId) {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.infoGetFace(new PostParameters().setFaceId(faceId));
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse fppInfoGetFacesetList() {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.infoGetFacesetList();
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse fppFacesetCreate(String faceId, String tag) {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.facesetCreate(
                    new PostParameters().setFaceId(faceId).setTag(tag));
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse fppFacesetAddFace(String faceId, String facesetId) {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.facesetAddFace(new PostParameters()
                    .setFaceId(faceId).setFacesetId(facesetId));
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse fppFacesetRemoveFace(String faceId, String facesetId) {
        MsResponse mr = new MsResponse();
        try {
            mr.json = mFaceppRequests.facesetRemoveFace(new PostParameters()
                    .setFaceId(faceId).setFacesetId(facesetId));
            mr.code = MsResponse.MS_SUCCESS;
        } catch (FaceppParseException e) {
            mr.code = MsResponse.MS_FAILED;
            mr.response = MsResponse.getFailureDesc(
                    mContext, R.string.face_error_parse, mr.code);
        }
        return mr;
    }

    private MsResponse getErrorResponse(int descResId) {
        MsResponse mr = new MsResponse();
        mr.code = MsResponse.MS_FAILED;
        mr.response = mContext.getString(descResId);
        return mr;
    }

    private String getFacesetTag(String gender) {
        if (Utils.getUseDevServer()) {
            return new StringBuilder("dev_").append(gender).toString();
        } else {
            return gender;
        }
    }

    private FaceInfo mrToFace(MsResponse mr) {
        if (mr.json == null) {
            return null;
        }
        JSONArray array = mr.json.optJSONArray("face");
        if (array == null) {
            array = mr.json.optJSONArray("face_info");
        }
        if (array != null && array.length() > 0) {
            return FaceInfo.fromJson(array.optJSONObject(0));
        }
        return null;
    }

    private List<FaceInfo> mrToFaces(MsResponse mr) {
        if (mr.json == null) {
            return null;
        }
        JSONArray array = mr.json.optJSONArray("candidate");
        if (array != null) {
            List<FaceInfo> faces = new ArrayList<FaceInfo>();
            for (int i = 0; i < array.length(); i++) {
                FaceInfo face = FaceInfo.fromJson(array.optJSONObject(i));
                if (face != null) {
                    faces.add(face);
                }
            }
            return faces;
        }
        return null;
    }

    private List<Faceset> mrToFacesets(MsResponse mr, String tag) {
        if (mr.json == null) {
            return null;
        }
        JSONArray array = mr.json.optJSONArray("faceset");
        if (array != null) {
            List<Faceset> facesets = new ArrayList<Faceset>();
            for (int i = 0; i < array.length(); i++) {
                Faceset fs = Faceset.fromJson(array.optJSONObject(i));
                if (fs != null && (tag == null || tag.equals(fs.tag))) {
                    facesets.add(fs);
                }
            }
            return facesets;
        }
        return null;
    }

    private boolean addUserFace(FaceInfo face, List<Faceset> facesets) {
        while (!facesets.isEmpty()) {
            int index = (int) (Math.random() * facesets.size());
            String facesetId = facesets.remove(index).id;
            MsResponse mr = fppFacesetAddFace(face.id, facesetId);
            if (MsResponse.isSuccessful(mr)) {
                if (mr.json.optBoolean("success")) {
                    try {
                        mFaceppRequests.trainSearch(
                                new PostParameters().setFacesetId(facesetId));
                    } catch (FaceppParseException e) {
                        Utils.logE(TAG, mContext.getString(R.string.face_error_parse)
                                + ": " + e.getErrorMessage());
                    }
                    return true;
                }
            } else {
                Utils.logE(TAG, mr.response);
            }
        }

        MsResponse mr = fppFacesetCreate(face.id, getFacesetTag(face.gender));
        if (MsResponse.isSuccessful(mr)) {
            String facesetId = mr.json.optString("faceset_id");
            if (!TextUtils.isEmpty(facesetId)) {
                try {
                    mFaceppRequests.trainSearch(
                            new PostParameters().setFacesetId(facesetId));
                } catch (FaceppParseException e) {
                    Utils.logE(TAG, mContext.getString(R.string.face_error_parse)
                            + ": " + e.getErrorMessage());
                }
                return true;
            }
        } else {
            Utils.logE(TAG, mr.response);
        }

        return false;
    }

    private void notifyError(String errMsg) {
        for (FaceListener listener : mListeners) {
            listener.onFaceErrorOccured(errMsg);
        }
    }

    public interface FaceListener {
        public void onFaceErrorOccured(String errMsg);
        public void onMyFaceObtained(FaceInfo face);
        public void onSimilarFaceObtained(FaceInfo face);
    }

    private class ObtainMyFaceTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private Bitmap mBitmap;
        private String mFaceId;

        public ObtainMyFaceTask(Bitmap bitmap, String faceId) {
            mBitmap = bitmap;
            mFaceId = faceId;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            if (mBitmap != null && !mBitmap.isRecycled()) {
                return fppDetectionDetect(mBitmap);
            } else {
                return fppInfoGetFace(mFaceId);
            }
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            if (!MsResponse.isSuccessful(mr)) {
                notifyError(mr.response);
                return;
            }

            FaceInfo face = mrToFace(mr);
            if (face == null) {
                notifyError(mContext.getString(R.string.face_error_obtain_my_face));
                return;
            }

            for (FaceListener listener : mListeners) {
                listener.onMyFaceObtained(face);
            }
        }
    }

    private class ObtainFacesetsTask extends AdvAsyncTask<Void, Void, MsResponse> {

        @Override
        protected MsResponse doInBackground(Void... params) {
            return fppInfoGetFacesetList();
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            if (!MsResponse.isSuccessful(mr)) {
                notifyError(mr.response);
                return;
            }

            List<Faceset> facesets = mrToFacesets(mr, null);
            if (facesets == null || facesets.isEmpty()) {
                notifyError(mContext.getString(R.string.face_error_get_facesets));
                return;
            }

            for (Faceset fs : facesets) {
                List<Faceset> list = mFacesetMap.get(fs.tag);
                if (list == null) {
                    list = new ArrayList<Faceset>();
                    mFacesetMap.put(fs.tag, list);
                }
                list.add(fs);
            }

            obtainSimilarFace(mCurrentFace, mCurrentGender);
        }
    }

    private class ObtainCandidatesTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private FaceInfo mFace;
        private Faceset mFaceset;
        private int mCount;

        public ObtainCandidatesTask(FaceInfo face, Faceset faceset, int count) {
            mFace = face;
            mFaceset = faceset;
            mCount = count;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            return fppRecognitionSearch(mFace.id, mFaceset.id, mCount);
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            if (!MsResponse.isSuccessful(mr)) {
                notifyError(mr.response);
                return;
            }

            List<FaceInfo> faces = mrToFaces(mr);
            if (faces == null) {
                notifyError(mContext.getString(R.string.face_error_search_face));
                return;
            }

            if (!faces.isEmpty() && faces.get(0).equals(mFace)) {
                faces.remove(0);
            }

            if (!faces.isEmpty()) {
                mCurrentCandidates.addAll(faces);
                List<FaceInfo> old = mCandidateMap.put(mFaceset, faces);
                if (old != null && old.size() < faces.size()) {
                    mCurrentCandidates.removeAll(old);
                }
            }
            mCurrentFacesets.remove(mFaceset);

            obtainSimilarFace(mCurrentFace, mCurrentGender);
        }
    }

    private class ObtainSimilarFaceTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private FaceInfo mCandidate;

        public ObtainSimilarFaceTask(FaceInfo candidate) {
            mCandidate = candidate;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            return fppInfoGetFace(mCandidate.id);
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            if (!MsResponse.isSuccessful(mr)) {
                notifyError(mr.response);
                return;
            }

            FaceInfo face = mrToFace(mr);
            if (face == null) {
                notifyError(mContext.getString(R.string.face_error_obtain_similar_face));
                return;
            }

            face.similarity = mCandidate.similarity;
            mCurrentCandidates.remove(mCandidate);

            for (FaceListener listener : mListeners) {
                listener.onSimilarFaceObtained(face);
            }
        }
    }

    private class RemoveFaceTask extends AdvAsyncTask<Void, Void, Void> {
        private String mFaceId;

        public RemoveFaceTask(String faceId) {
            mFaceId = faceId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (TextUtils.isEmpty(mFaceId)) {
                return null;
            }

            MsResponse mr = fppInfoGetFace(mFaceId);
            if (!MsResponse.isSuccessful(mr)) {
                return null;
            }

            JSONArray array = mr.json.optJSONArray("face_info");
            if (array.length() == 0) {
                return null;
            }

            mr.json = array.optJSONObject(0);
            List<Faceset> facesets = mrToFacesets(mr, null);
            if (facesets != null) {
                while (!facesets.isEmpty()) {
                    String facesetId = facesets.remove(0).id;
                    mr = fppFacesetRemoveFace(mFaceId, facesetId);
                    if (MsResponse.isSuccessful(mr)) {
                        if (mr.json.optBoolean("success")) {
                            try {
                                mFaceppRequests.trainSearch(
                                        new PostParameters().setFacesetId(facesetId));
                            } catch (FaceppParseException e) {
                                Utils.logE(TAG, mContext.getString(R.string.face_error_parse)
                                        + ": " + e.getErrorMessage());
                            }
                        }
                    } else {
                        Utils.logE(TAG, mr.response);
                    }
                }
            }
            return null;
        }
    }
}
