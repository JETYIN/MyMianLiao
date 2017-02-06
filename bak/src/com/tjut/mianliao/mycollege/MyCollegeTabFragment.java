package com.tjut.mianliao.mycollege;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.black.MagicFrameLayout;
import com.tjut.mianliao.black.MagicLinearLayout;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.ScrollScreen;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.curriculum.CurriculumActivity;
import com.tjut.mianliao.curriculum.widget.CourseWidgetHelper;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Course.Entry;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.job.RecruitInfo;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.data.mycollege.WeatherInfo;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.news.NewsActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeTextView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class MyCollegeTabFragment extends TabFragment implements OnClickListener {

    private static final String OBJ_STREAM_NEWS_NAME = "news_list";
    private static final String OBJ_STREAM_WEATHER_NAME = "weather_info";
    private static final String OBJ_STREAM_NOTE_NAME = "note_list";
    private static final String OBJ_STREAM_RECRUIT_NAME = "recruit_list";
    public static boolean hasChoose = false;

    private CourseWidgetHelper mCourseHelper;
    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;
    private UserInfoManager mUserInfoManager;

    private TextView mTvWeekNumber;
    private TextView mTvTemper, mTvWetherDes;
    private TextView mTvWeekStr, mTvDate;
    private ProImageView mIvWeatherIcon;
    private TextView mTvCourseTeachar, mTvCourseAddr;
    private TextView mTvCoursePitch, mTvCourseName;
    private TextView mTvTimeRemind;
    private TextView mTvNoteMemo, mTvNoteNote, mTvNoteHw;
    private TextView mTvNewsOne, mTvNewsTwo;
    private TextView mTvMcOne, mTvMcTwo;
    private LinearLayout mTvAddCourse, mTvAddNote;
    private ImageView mClassTitle;
    private MagicFrameLayout mMfClassContent;
    private ThemeTextView mTvNotetitle, mTvNewstitle, mTvJobtitle;
    private MagicLinearLayout mLlNewscontent, mRealLlNewscontent, mLlJobcontent,mRealLlJobcontent;
    private ScrollScreen newsScrollScreen,jobScrollScreen;

    private LinearLayout mLlCourse, mLlNews, mLlRecru, mLlNoteMemo, mLlNoteNote, mLlNoteHw, mLlNote;
    private ImageView mIvMemoLogo, mIvNoteLogo, mIvHwLogo;
    private LinearLayout mLlMemo;
    private LinearLayout mLlWeek, mLlDate, mLlWeather;
    private RelativeLayout mRlUser;
    private LinearLayout mLlMain;
    private FrameLayout mFlMemo;

    private ProImageView mAvatarView;
    private TextView mTvName;
    private boolean mIsNightMode;
    private Settings mSettings;

    private ArrayList<RecruitInfo> mRecruitInfos;
    private ArrayList<NewsInfo> mNews;
    private ArrayList<Notes> mNotes;
    private WeatherInfo mWeatherInfo;
    private List<Course.Entry> mEntries;
    private Notes mNoteMemo;
    private Notes mNoteHw;
    private Notes mNotePhoto;
    private MagicFrameLayout mIvAvatarback;
    private boolean mFetchUserInfoSucc;
    private SharedPreferences mPreferences;
    
    private MsTask mCurrentTask;

    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            refreshData();
        };
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_my_college_homepage;
    }

    @Override
    public int getNaviButtonId() {
        return R.id.nb_tribe;
    }

    @Override
    public String getName() {
        return "MyCollegeTabFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourseHelper = CourseWidgetHelper.getInstance(getActivity());
        mAccountInfo = AccountInfo.getInstance(getActivity());
        mUserInfo = mAccountInfo.getUserInfo();
        mUserInfoManager = UserInfoManager.getInstance(getActivity());
        mRecruitInfos = new ArrayList<>();
        mNews = new ArrayList<>();
        mNotes = new ArrayList<>();
        mWeatherInfo = new WeatherInfo();
        mEntries = new ArrayList<>();
        mSettings = Settings.getInstance(getActivity());
        mIsNightMode = mSettings.isNightMode();
        mPreferences = DataHelper.getSpForData(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mTitleBar.setTitle(getString(R.string.tab_tribe));
        mLlMain = (LinearLayout) view.findViewById(R.id.ll_my_college);
        mLlCourse = (LinearLayout) view.findViewById(R.id.ll_course_remind);
        mLlNote = (LinearLayout) view.findViewById(R.id.ll_note);
        mLlNews = (LinearLayout) view.findViewById(R.id.ll_news);
        mLlRecru = (LinearLayout) view.findViewById(R.id.ll_recru);
        mRlUser = (RelativeLayout) view.findViewById(R.id.rl_user);
        mLlCourse.setOnClickListener(this);
        mLlNote.setOnClickListener(this);
        mLlNews.setOnClickListener(this);
        mLlRecru.setOnClickListener(this);
        mRlUser.setOnClickListener(this);
        initWether(view);
        initCourse(view);
        initNote(view);
        initNews(view);
        initMc(view);
        getDataFromFile();
        getCourseInfo();
        getDatas();
        fillUserInfo(view);
        checkDayNightUI();
        return view;
    }

    private void fillUserInfo(View view) {
        mAvatarView = (ProImageView) view.findViewById(R.id.iv_avatar);
        mTvName = (TextView) view.findViewById(R.id.tv_name);
        mIvAvatarback = (MagicFrameLayout) view.findViewById(R.id.rl_user_info);
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (mAvatarView == null) {
            return;
        }
        mAvatarView.setImage(Utils.getImagePreviewSmall(mUserInfo.avatarFull),
                mUserInfo.getDefaultAvatar(mUserInfo.gender));
        if (getActivity() != null) {
            mTvName.setText(mUserInfo.getDisplayName(getActivity()));
        } else {
            mTvName.setText(mUserInfo.nickname);
        }
        mTvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, mUserInfo.gender == 0 ? R.drawable.pic_bg_woman
                : R.drawable.pic_bg_man, 0);
    }

    @Override
    public void onResume() {
        getCourseInfo();
        if (mCurrentTask == null) {
            new FetchUserTask().executeLong();
        }
        new GetNoteListTask().executeLong();
        refreshUserInfo();
        super.onResume();
    }

    private void refreshUserInfo() {
        if (mUserInfo != null && mAvatarView != null) {
            mUserInfo = mUserInfoManager.getUserInfo(mUserInfo.userId);
            if (mUserInfo != null) {
                updateUserInfo();
            }
        }
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            mLlMain.setBackgroundResource(R.drawable.bg);
        }
        updateBg(mIsNightMode);
    }

    private void updateBg(boolean isNight) {
        mClassTitle
                .setBackgroundResource(isNight ? R.drawable.pic_bg_class_title_black : R.drawable.pic_bg_class_title);
        mMfClassContent.setBackgroundResource(isNight ? R.drawable.pic_bg_cls_black : R.drawable.pic_bg_class_white);
        mFlMemo.setBackgroundResource(isNight ? R.drawable.pic_bg_note_black : R.drawable.pic_bg_memo_white);
        mTvNotetitle.setBackgroundResource(isNight ? R.drawable.pic_bg_memo_title_black : R.drawable.pic_bg_memo_title);
        mTvNewstitle.setBackgroundResource(isNight ? R.drawable.pic_bg_notifi_title_black
                : R.drawable.pic_bg_notifi_title);
        mLlNewscontent
                .setBackgroundResource(isNight ? R.drawable.pic_bg_notifiy_black : R.drawable.pic_bg_notifi_white);
        mTvJobtitle.setBackgroundResource(isNight ? R.drawable.pic_bg_job_title_black : R.drawable.pic_bg_job_title);
        mLlJobcontent.setBackgroundResource(isNight ? R.drawable.pic_bg_jb_black : R.drawable.pic_bg_job_white);
        mIvAvatarback.setBackgroundResource(isNight ? R.drawable.pic_bg_photo_avatar_black
                : R.drawable.pic_bg_photo_avatar);
    }

    @Override
    public void onDestroy() {
        saveDataToFile();
        super.onDestroy();
    }

    @Override
    public void onTabButtonClicked() {
        super.onTabButtonClicked();
        if (!mFetchUserInfoSucc) {
            new FetchUserTask().executeLong();
        }
        refreshUserInfo();
        refreshData();
    }

    private void initWether(View view) {
        mTvWeekNumber = (TextView) view.findViewById(R.id.tv_week_number);
        // mTvWeekNum = (TextView) view.findViewById(R.id.tv_week_num);
        mTvWeekStr = (TextView) view.findViewById(R.id.tv_week_text);
        mTvDate = (TextView) view.findViewById(R.id.tv_date);
        mTvWetherDes = (TextView) view.findViewById(R.id.tv_wether_des);
        mTvTemper = (TextView) view.findViewById(R.id.tv_temper);
//        mIvWeatherIcon = (ProImageView) view.findViewById(R.id.piv_weather_icon);
        mLlWeek = (LinearLayout) view.findViewById(R.id.ll_week_info);
        mLlDate = (LinearLayout) view.findViewById(R.id.ll_date_info);
        mLlWeather = (LinearLayout) view.findViewById(R.id.ll_weather_info);
    }

    private void initCourse(View view) {
        mTvCourseTeachar = (TextView) view.findViewById(R.id.tv_course_teacher);
        mClassTitle = (ImageView) view.findViewById(R.id.iv_bg_title);
        mMfClassContent = (MagicFrameLayout) view.findViewById(R.id.fl_class_content);
        mTvCourseAddr = (TextView) view.findViewById(R.id.tv_course_addr);
        mTvCoursePitch = (TextView) view.findViewById(R.id.tv_course_pitch);
        mTvCourseName = (TextView) view.findViewById(R.id.tv_course_name);
        mTvTimeRemind = (TextView) view.findViewById(R.id.tv_remind_time);
        mTvAddCourse = (LinearLayout) view.findViewById(R.id.ll_add_class);
        mTvAddNote = (LinearLayout) view.findViewById(R.id.ll_add_note);
        mTvAddCourse.setOnClickListener(this);
        mTvAddNote.setOnClickListener(this);
    }

    private void initNote(View view) {
    	mTvNoteMemo = (TextView) view.findViewById(R.id.tv_note_memo);
    	mTvNoteNote = (TextView) view.findViewById(R.id.tv_note_note);
        mTvNoteHw = (TextView) view.findViewById(R.id.tv_note_hw);

        mTvNotetitle = (ThemeTextView) view.findViewById(R.id.tv_note_title);
        mLlMemo = (LinearLayout) view.findViewById(R.id.ll_note_content);
        mLlNoteMemo = (LinearLayout) view.findViewById(R.id.ll_note_memo);
        mLlNoteNote = (LinearLayout) view.findViewById(R.id.ll_note_note);
        mLlNoteHw = (LinearLayout) view.findViewById(R.id.ll_note_hw);

        mFlMemo = (FrameLayout) view.findViewById(R.id.fl_memo);

        mIvMemoLogo = (ImageView) view.findViewById(R.id.iv_memo_logo);
        mIvNoteLogo = (ImageView) view.findViewById(R.id.iv_note_logo);
        mIvHwLogo = (ImageView) view.findViewById(R.id.iv_hw_logo);

    }

    private void initNews(View view) {
        mTvNewsOne = (TextView) view.findViewById(R.id.tv_news_one);
        mTvNewsTwo = (TextView) view.findViewById(R.id.tv_news_two);
        mTvNewstitle = (ThemeTextView) view.findViewById(R.id.tv_new_title);
        mLlNewscontent = (MagicLinearLayout) view.findViewById(R.id.ll_news_content);
        mRealLlNewscontent = (MagicLinearLayout) view.findViewById(R.id.real_ll_news_content);

        newsScrollScreen = new ScrollScreen(mLlNewscontent, mRealLlNewscontent, mTvNewsOne, mTvNewsTwo);
    }

    private void initMc(View view) {
        mTvMcOne = (TextView) view.findViewById(R.id.tv_mic_one);
        mTvMcTwo = (TextView) view.findViewById(R.id.tv_mic_two);
        mTvJobtitle = (ThemeTextView) view.findViewById(R.id.tv_job_title);
        mLlJobcontent = (MagicLinearLayout) view.findViewById(R.id.ll_job_content);
        mRealLlJobcontent=(MagicLinearLayout) view.findViewById(R.id.real_ll_job_content);

        jobScrollScreen = new ScrollScreen(mLlJobcontent, mRealLlJobcontent, mTvMcOne, mTvMcTwo);
    }

    private void updateCourseInfo() {
        if (mEntries == null || mEntries.size() == 0) {
            showCourse(false);
        } else {
            boolean showCourse = false;
            for (Course.Entry entry : mEntries) {
                int startTime = getCourseTimeHour(entry.periodStart);
                if ((startTime > getHourNow()) || (startTime == getHourNow() && getMinNow() < 10)) {
                    showCourse = true;
                    mLlCourse.setVisibility(View.VISIBLE);
                    fillCourse(entry);
                    break;
                }
            }
            showCourse(showCourse);
        }
    }

    private void updateNewsInfo(ArrayList<NewsInfo> mNews) {

        ArrayList<String> list = new ArrayList<String>();
        if (mNews != null) {
            for (NewsInfo info : mNews) {
                list.add(info.title);
            }
        }
        newsScrollScreen.triggerPageAnim(list, 500);

    }

    private void updateNoteInfo() {
        if (mNoteHw == null && mNotePhoto == null && mNoteMemo == null) {
            showNoteInfo(false);
        } else {
            showNoteInfo(true);
            mLlNoteMemo.setVisibility(View.VISIBLE);
            mLlNoteNote.setVisibility(View.VISIBLE);
            mLlNoteHw.setVisibility(View.VISIBLE);
            if (!(mNoteMemo == null)) {
                mTvNoteMemo.setText(mNoteMemo.content);
                setImageCircle(mIvMemoLogo, mNoteMemo.type);
            } else {
            	mLlNoteMemo.setVisibility(View.GONE);
            }
            if (!(mNotePhoto == null)) {
                mTvNoteNote.setText(mNotePhoto.content);
                setImageCircle(mIvNoteLogo, mNotePhoto.type);
            } else {
            	mLlNoteNote.setVisibility(View.GONE);
            }
            if (!(mNoteHw == null)) {
                mTvNoteHw.setText(mNoteHw.content);
                setImageCircle(mIvHwLogo, mNoteHw.type);
            } else {
            	mLlNoteHw.setVisibility(View.GONE);
            }
        }
    }
    private void setImageCircle (ImageView mLogo,int type) {
    	switch (type) {
		case 0:
			mLogo.setImageResource(R.drawable.bg_circle_purple);
			break;
		case 1:
			mLogo.setImageResource(R.drawable.bg_circle_orange);
			break;
		case 2:
			mLogo.setImageResource(R.drawable.bg_circle_pink);
			break;
		default:
			break;
		}
    }

    private void updateRecInfo(ArrayList<RecruitInfo> mRecruitInfos) {

        ArrayList<String> list = new ArrayList<String>();
        if (mRecruitInfos != null) {
            for (RecruitInfo info : mRecruitInfos) {
                list.add(info.title);
            }
        }

        jobScrollScreen.triggerPageAnim(list, 0);

//        if (mRecruitInfos != null && mRecruitInfos.size() > 0) {
//            showJob(true);
//            RecruitInfo info1 = mRecruitInfos.size() > 0 ? mRecruitInfos.get(0) : null;
//            RecruitInfo info2 = mRecruitInfos.size() > 1 ? mRecruitInfos.get(1) : null;
//            mTvMcOne.setText(info1 == null ? "" : info1.title);
//            mTvMcTwo.setText(info2 == null ? "" : info2.title);
//        } else {
//            showJob(false);
//        }
    }

    private void updateWeatherInfo(WeatherInfo info) {
        if (info == null) {
            showWeather(false);
        } else {
            showWeather(true);
            mTvWeekNumber.setText(String.valueOf(info.weekNo));
            mTvTemper.setText(info.temper);
            mTvWetherDes.setText(info.weather);
            mTvWeekStr.setText(info.weekDay);
            mTvDate.setText(info.currentDate);
//            mIvWeatherIcon.setImage(info.weatherIcon, 0);
        }
    }

    private void showCourse(boolean show) {
        mLlCourse.findViewById(R.id.rl_course_show).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mLlCourse.findViewById(R.id.tv_remind_time).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mLlCourse.findViewById(R.id.tv_course_name).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mLlCourse.findViewById(R.id.tv_course_teacher).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mTvAddCourse.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showJob(boolean show) {
        mTvMcOne.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mTvMcTwo.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        // mTvMcThree.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        // mTvMcFour.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showNews(boolean show) {
        mTvNewsOne.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mTvNewsTwo.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        // mTvNewsThree.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showNoteInfo(boolean show) {
        mLlMemo.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        // mLlHw.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        // mLlPic.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mTvAddNote.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showWeather(boolean show) {
        mLlWeek.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mLlDate.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mLlWeather.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void fillCourse(Entry entry) {
        mTvTimeRemind.setText(getCourseShowTime(entry.periodStart));
        mTvCourseAddr.setText(entry.classroom);
        mTvCourseName.setText(entry.getCourse().name);
        mTvCoursePitch.setText(entry.periodStart + "-" + entry.periodEnd);
        mTvCourseTeachar.setText(entry.getCourse().teacher);
    }

    private int getHourNow() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    private int getMinNow() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    private int getCourseTimeHour(int periodStart) {
        switch (periodStart) {
            case 1:
                return 8;
            case 2:
                return 9;
            case 3:
                return 10;
            case 4:
                return 11;
            case 5:
                return 14;
            case 6:
                return 15;
            case 7:
                return 16;
            case 8:
                return 17;
            case 9:
                return 19;
            case 10:
                return 20;
            case 11:
                return 21;
            case 12:
                return 22;
            default:
                return 23;
        }
    }

    private String getCourseShowTime(int periodStart) {
        switch (periodStart) {
            case 1:
                return "08:30";
            case 2:
                return "09:20";
            case 3:
                return "10:20";
            case 4:
                return "11:10";
            case 5:
                return "14:30";
            case 6:
                return "15:20";
            case 7:
                return "16:20";
            case 8:
                return "17:10";
            case 9:
                return "19:30";
            case 10:
                return "20:20";
            case 11:
                return "21:10";
            case 12:
                return "22:00";
            default:
                return "08:30";
        }
    }

    private void getCourseInfo() {
        mCourseHelper.prepareEntries(getActivity(), CourseWidgetHelper.ACTION_RESET);
        mEntries = mCourseHelper.getEntries();
        updateCourseInfo();
    }

    @SuppressWarnings("unchecked")
    private void getDataFromFile() {
        Object weather = Utils.FileToObject(OBJ_STREAM_WEATHER_NAME);
        Object news = Utils.FileToObject(OBJ_STREAM_NEWS_NAME);
        Object recruit = Utils.FileToObject(OBJ_STREAM_RECRUIT_NAME);
        updateWeatherInfo(weather == null ? null : (WeatherInfo) weather);
        updateNoteInfo();
        updateNewsInfo(news == null ? null : (ArrayList<NewsInfo>) news);
        if (recruit != null) {
            updateRecInfo((ArrayList<RecruitInfo>) recruit);
        } else {
            updateRecInfo(null);
        }
    }

    private ArrayList<NewsInfo> getNewsInfos(ArrayList<News> news) {
        ArrayList<NewsInfo> newsInfos = new ArrayList<>();
        for (News ne : news) {
            NewsInfo newsInfo = new NewsInfo();
            newsInfo.title = ne.title;
            newsInfos.add(newsInfo);
        }
        return newsInfos;
    }

    private void saveDataToFile() {
        if (mNotePhoto != null) {
            mNotes.add(mNotePhoto);
        }
        if (mNoteHw != null) {
            mNotes.add(mNoteHw);
        }
        if (mNoteMemo != null) {
            mNotes.add(mNoteMemo);
        }
        if (mNotePhoto != null) {
            mNotes.add(mNotePhoto);
        }
        Utils.ObjectToFile(mWeatherInfo, OBJ_STREAM_WEATHER_NAME);
        Utils.ObjectToFile(mNotes, OBJ_STREAM_NOTE_NAME);
        Utils.ObjectToFile(mNews, OBJ_STREAM_NEWS_NAME);
        Utils.ObjectToFile(mRecruitInfos, OBJ_STREAM_RECRUIT_NAME);
    }

    private void getDatas() {
        new GetMyRecruitTask().executeLong();
        new GetNewsInfoTask().executeLong();
        new GetNoteListTask().executeLong();
        new GetWetherInfoTask().executeLong();
        new FetchUserTask().executeLong();
    }

    private void refreshData() {
        new GetMyRecruitTask().executeLong();
        new GetNewsInfoTask().executeLong();
    }

    private class GetMyRecruitTask extends MsTask {

        public GetMyRecruitTask() {
            super(getActivity(), MsRequest.LIST_MY_RECRUIT_LIST_TODAY);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mRecruitInfos = JsonUtil.getArray(response.getJsonArray(), RecruitInfo.TRANSFORMER);
                updateRecInfo(mRecruitInfos);
            }
        }
    }

    private class GetNewsInfoTask extends MsTask {

        public GetNewsInfoTask() {
            super(getActivity(), MsRequest.NEWS_MY_BROADCAST_TODAY);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<News> news = JsonUtil.getArray(response.getJsonArray(), News.TRANSFORMER);
                mNews = getNewsInfos(news);
                updateNewsInfo(mNews);
            }
        }
    }

    private class GetNoteListTask extends MsTask {

        public GetNoteListTask() {
            super(getActivity(), MsRequest.GETNOTE_POST);
        }

        @Override
        protected String buildParams() {
            return "limit=" + 3;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<NoteInfo> notes = JsonUtil.getArray(ja, NoteInfo.TRANSFORMER);
                if (notes != null) {
                	NoteInfo infoMemo = notes.size() >= 1 ? notes.get(0) : null;
                	NoteInfo infoPho = notes.size() >= 2 ? notes.get(1) : null;
                    NoteInfo infoHw = notes.size() == 3 ? notes.get(2) : null;
                    mNoteMemo = infoMemo == null ? null
                            : new Notes(infoMemo.title, infoMemo.noteType, infoMemo.content);
                    mNoteHw = infoHw == null ? null : new Notes(infoHw.course, infoHw.noteType, infoHw.content);
                    mNotePhoto = infoPho == null ? null : new Notes(infoPho.course, infoPho.noteType, infoPho.content);
                    updateNoteInfo();
                }
//                JSONObject json;
//                try {
//                    if (ja.length() > 0) {
//                    	NoteInfo infoMemo  = Transformer.;
//                        json = ja.getJSONObject(0);
//                        JSONObject jlist = json.optJSONObject("list");
//                        ArrayList<NoteInfo> photos = JsonUtil.getArray(jlist.optJSONArray("photo"),
//                                NoteInfo.TRANSFORMER);
//                        ArrayList<NoteInfo> memos = JsonUtil.getArray(jlist.optJSONArray("memo"), NoteInfo.TRANSFORMER);
//                        ArrayList<NoteInfo> hws = JsonUtil.getArray(jlist.optJSONArray("homework"),
//                                NoteInfo.TRANSFORMER);
//                        NoteInfo infoMemo = memos.size() == 0 ? null : memos.get(0);
//                        NoteInfo infoHw = hws.size() == 0 ? null : hws.get(0);
//                        NoteInfo infoPho = photos.size() == 0 ? null : photos.get(0);
//                        mNoteMemo = infoMemo == null ? null
//                                : new Notes(infoMemo.title, infoMemo.type, infoMemo.content);
//                        mNoteHw = infoHw == null ? null : new Notes(infoHw.course, infoHw.type, infoHw.content);
//                        mNotePhoto = infoPho == null ? null : new Notes(infoPho.course, infoPho.type, infoPho.content);
//                        updateNoteInfo();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    private class GetWetherInfoTask extends MsTask {

        public GetWetherInfoTask() {
            super(getActivity(), MsRequest.WEATHER_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                WeatherInfo weatherInfo = WeatherInfo.fromJson(response.getJsonObject());
                mWeatherInfo = WeatherInfo.copy(weatherInfo);
                updateWeatherInfo(weatherInfo);
            }
        }
    }

    public class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(getActivity(), MsRequest.USER_FULL_INFO);
        }

        @Override
        protected String buildParams() {
            return "user_id=" + mAccountInfo.getUserId();
        }

        @Override
        protected void onPreExecute() {
            mCurrentTask = this;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            mCurrentTask = null;
            if (response.isSuccessful()) {
                mFetchUserInfoSucc = true;
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                if (mUserInfo == null) {
                    System.out.println("null");
                } else {
                    mUserInfoManager.saveUserInfo(mUserInfo);
                    updateUserInfo();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ll_course_remind:
            case R.id.ll_add_class:

                // if (mUserInfo.departmentId == 0){
                // startActivity(ChooseDepartmentActivity.class);
                // } else {
                startActivity(CurriculumActivity.class);
                // }

                MobclickAgent.onEvent(getActivity(), MStaticInterface.COURSE);
                break;
            case R.id.ll_note:
            case R.id.ll_add_note:
                startActivity(TakeNoticesActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.KEEP_NOTES);
                break;
            case R.id.ll_news:
                startActivity(NewsActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.NOTICE);
                break;
            case R.id.ll_recru:
                startActivity(MicroRecHomeActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.RECRUITMENT);
                break;
            case R.id.rl_user:
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                mUserInfo = mUserInfo == null ? mAccountInfo.loadUserInfoFromSp() :
                    mUserInfo.hasUserId() ? mUserInfo : mAccountInfo.loadUserInfoFromSp();
                intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}
