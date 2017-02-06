package com.tjut.mianliao.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.explore.EmotionsInfo;

public class EmotionManager {

    private static final Pattern CUSTOM_MATCHER = Pattern.compile("\\[\\S{0,15}?\\]"); 

    private static final String EMOJI_NUM_0 = "\u0030\u20e3";
    private static final String EMOJI_NUM_1 = "\u0031\u20e3";
    private static final String EMOJI_NUM_2 = "\u0032\u20e3";
    private static final String EMOJI_NUM_3 = "\u0033\u20e3";
    private static final String EMOJI_NUM_4 = "\u0034\u20e3";
    private static final String EMOJI_NUM_5 = "\u0035\u20e3";
    private static final String EMOJI_NUM_6 = "\u0036\u20e3";
    private static final String EMOJI_NUM_7 = "\u0037\u20e3";
    private static final String EMOJI_NUM_8 = "\u0038\u20e3";
    private static final String EMOJI_NUM_9 = "\u0039\u20e3";

    private static WeakReference<EmotionManager> sInstanceRef;

    private Context mContext;
    private LinkedHashMap<String, Emotion> mDefaultMap;

    private LinkedHashMap<String, Emotion> mEmojiMap;
    private LinkedHashMap<String, Emotion> mCustomEmojiMap;
    private LinkedHashMap<String, Emotion> mCustomEmojiMap2;

    private ArrayList<LinkedHashMap<String, Emotion>> mCustomEmojiMapList = new ArrayList<LinkedHashMap<String, Emotion>>();
    private ArrayList<String> mCustomEmojiPaths = new ArrayList<String>();

    public static synchronized EmotionManager getInstance(Context context) {
        EmotionManager instance = null;
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            instance = sInstanceRef.get();
        }
        instance = new EmotionManager(context);
        sInstanceRef = new WeakReference<EmotionManager>(instance);
        instance.fillCustomEmoji();
        return instance;
    }

    private void fillCustomEmoji() {
        List<EmotionsInfo> emotions = DataHelper.queryEmotionInfo(mContext);
        if (emotions == null || emotions.size() == 0) {
            return;
        }
        
        for (EmotionsInfo info : emotions) {
            if (mCustomEmojiPaths.contains(info.path)) {
                return;
            }

            mCustomEmojiPaths.add(info.path);

            LinkedHashMap<String, Emotion> customEmojiMap = new LinkedHashMap<String, Emotion>();
            initCustomEmoji(info.path, customEmojiMap);
            mCustomEmojiMapList.add(customEmojiMap);
        }

    }

    private EmotionManager(Context context) {
        mContext = context.getApplicationContext();
        initDefault();
        initEmoji();
    }

    public void clear() {
        mDefaultMap.clear();
        mEmojiMap.clear();
        sInstanceRef.clear();
    }

    public CharSequence parseEmotion(CharSequence input, int size) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }

        SpannableString ssb = SpannableString.valueOf(input);
        parseDefault(ssb, size);
//        boolean parseDefault = parseDefault(ssb, size);
//        if (parseDefault) {
//            return ssb;
//        }
        parseEmoji(ssb, size);
        
        for (LinkedHashMap<String, Emotion> map : mCustomEmojiMapList) {
            parseCustomEmo(ssb, size, map);
        }

        // parseCustomEmo(ssb, size, mCustomEmojiMap2);
        return ssb;
    }

    public ArrayList<Emotion> getDefaultList() {
        return new ArrayList<Emotion>(mDefaultMap.values());
    }

    public ArrayList<Emotion> getEmojiList() {
        return new ArrayList<Emotion>(mEmojiMap.values());
    }

    public ArrayList<Emotion> getCustomEmojiList(int index) {

        if (index < 0 || index >= mCustomEmojiMapList.size()) {
            return null;
        }

        return new ArrayList<Emotion>(mCustomEmojiMapList.get(index).values());
    }

    private void initDefault() {
        mDefaultMap = new LinkedHashMap<String, Emotion>();
        TypedArray ta = mContext.getResources().obtainTypedArray(R.array.emotions_default);
        for (int i = 0; i < ta.length(); i += 2) {
            Emotion emotion = Emotion.fromString(ta.getString(i), ta.getResourceId(i + 1, 0));
            mDefaultMap.put(emotion.value, emotion);
        }
        ta.recycle();
    }

    public void initCustomEmoji1(String path) {
        mCustomEmojiMap = new LinkedHashMap<String, Emotion>();
        initCustomEmoji(path, mCustomEmojiMap);
    }

    public void initCustomEmoji2(String path) {
        mCustomEmojiMap2 = new LinkedHashMap<String, Emotion>();
        initCustomEmoji(path, mCustomEmojiMap2);
    }

    private void initCustomEmoji(String path, LinkedHashMap<String, Emotion> map) {
        try {

            InputStream inputStream = new FileInputStream(path + "/emojList.plist"); // mContext.getAssets().open(path+"/emojList.plist");
            List<Emotion> emotions = PullParseService.getEmoInfo(inputStream);

            for (Emotion emotion : emotions) {
                emotion.setImageName(path + "/" + emotion.getImageName() + ".png");
                map.put(emotion.getParseText(), emotion);
                emotion.isBig = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEmoji() {
        mEmojiMap = new LinkedHashMap<String, Emotion>();
        TypedArray ta = mContext.getResources().obtainTypedArray(R.array.emotions_emoji);
        for (int i = 0; i < ta.length(); i += 2) {
            Emotion emotion = Emotion.fromCodePoint(ta.getInt(i, 0), ta.getResourceId(i + 1, 0));
            mEmojiMap.put(emotion.value, emotion);
        }
        ta.recycle();

        mEmojiMap.put(EMOJI_NUM_0, Emotion.fromString(EMOJI_NUM_0, R.drawable.emoji_0030));
        mEmojiMap.put(EMOJI_NUM_1, Emotion.fromString(EMOJI_NUM_1, R.drawable.emoji_0031));
        mEmojiMap.put(EMOJI_NUM_2, Emotion.fromString(EMOJI_NUM_2, R.drawable.emoji_0032));
        mEmojiMap.put(EMOJI_NUM_3, Emotion.fromString(EMOJI_NUM_3, R.drawable.emoji_0033));
        mEmojiMap.put(EMOJI_NUM_4, Emotion.fromString(EMOJI_NUM_4, R.drawable.emoji_0034));
        mEmojiMap.put(EMOJI_NUM_5, Emotion.fromString(EMOJI_NUM_5, R.drawable.emoji_0035));
        mEmojiMap.put(EMOJI_NUM_6, Emotion.fromString(EMOJI_NUM_6, R.drawable.emoji_0036));
        mEmojiMap.put(EMOJI_NUM_7, Emotion.fromString(EMOJI_NUM_7, R.drawable.emoji_0037));
        mEmojiMap.put(EMOJI_NUM_8, Emotion.fromString(EMOJI_NUM_8, R.drawable.emoji_0038));
        mEmojiMap.put(EMOJI_NUM_9, Emotion.fromString(EMOJI_NUM_9, R.drawable.emoji_0039));
    }

    private boolean parseDefault(Spannable ssb, int size) {
        Matcher matcher = CUSTOM_MATCHER.matcher(ssb);
        int start = 0;
        int end = 0;
        boolean isMatched = false;
        while (matcher.find(end)) {
            isMatched = true;
            start = matcher.start();
            end = matcher.end();
            Emotion emotion = mDefaultMap.get(matcher.group());
            if (emotion != null) {
                Object span = emotion.getSpan(mContext, size);
                if (span != null) {
                    ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return isMatched;
    }

    private void parseCustomEmo(Spannable ssb, int size, LinkedHashMap<String, Emotion> map) {

        if (map == null) {
            return;
        }

        Matcher matcher = CUSTOM_MATCHER.matcher(ssb);
        int start = 0;
        int end = 0;
        while (matcher.find(end)) {
            start = matcher.start();
            end = matcher.end();
            Emotion emotion = map.get(matcher.group());
            if (emotion != null) {
                Object span = emotion.getSpan(mContext, size);
                if (span != null) {
                    ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    private void parseEmoji(Spannable ssb, int size) {
        int length = ssb.length();
        int skip = 0;
        for (int i = 0; i < length; i += skip) {
            Emotion emotion = null;
            int codePoint = Character.codePointAt(ssb, i);
            skip = Character.charCount(codePoint);

            if (codePoint > 0xff) {
                emotion = mEmojiMap.get(Emotion.getValue(codePoint));
            }

            if (emotion == null && i + skip < length) {
                int followCodePoint = Character.codePointAt(ssb, i + skip);
                if (followCodePoint == 0x20e3) {
                    int followSkip = Character.charCount(followCodePoint);
                    switch (codePoint) {
                        case 0x0030:
                            emotion = mEmojiMap.get(EMOJI_NUM_0);
                            break;
                        case 0x0031:
                            emotion = mEmojiMap.get(EMOJI_NUM_1);
                            break;
                        case 0x0032:
                            emotion = mEmojiMap.get(EMOJI_NUM_2);
                            break;
                        case 0x0033:
                            emotion = mEmojiMap.get(EMOJI_NUM_3);
                            break;
                        case 0x0034:
                            emotion = mEmojiMap.get(EMOJI_NUM_4);
                            break;
                        case 0x0035:
                            emotion = mEmojiMap.get(EMOJI_NUM_5);
                            break;
                        case 0x0036:
                            emotion = mEmojiMap.get(EMOJI_NUM_6);
                            break;
                        case 0x0037:
                            emotion = mEmojiMap.get(EMOJI_NUM_7);
                            break;
                        case 0x0038:
                            emotion = mEmojiMap.get(EMOJI_NUM_8);
                            break;
                        case 0x0039:
                            emotion = mEmojiMap.get(EMOJI_NUM_9);
                            break;
                        default:
                            followSkip = 0;
                            break;
                    }
                    skip += followSkip;
                }
            }

            if (emotion != null) {
                Object span = emotion.getSpan(mContext, size);
                if (span != null) {
                    ssb.setSpan(span, i, i + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }
}
