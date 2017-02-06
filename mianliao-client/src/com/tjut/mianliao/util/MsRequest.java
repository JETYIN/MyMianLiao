package com.tjut.mianliao.util;

public final class MsRequest {

    public static final int GET = 1;
    public static final int POST = 2;
    public static final int MULTI_PART_POST = 2;

    private String mApi;
    private String mName;
    private int mType;
    private boolean mRequireAuth;

    private String mUrl;
    private String mDesc;

    public MsRequest(String api, String name, int type, boolean requireAuth) {
        mApi = api;
        mName = name;
        mType = type;
        mRequireAuth = requireAuth;
    }

    public String getUrl() {
        if (mUrl == null) {
            mUrl = new StringBuilder(Utils.getServerAddress()).
                    append(mApi).append("/").append(getName()).toString();
        }
        return mUrl;
    }

    public String getToLocalUrl() {
        return new StringBuilder(Utils.TO_LOCAL_ADDRESS)
                .append(mApi).append("/").append(getName())
                .toString();
    }

    public String getDesc() {
        if (mDesc == null) {
            mDesc = mApi + ":" + mName;
        }
        return mDesc;
    }

    public String getApi() {
        return mApi;
    }

    public String getName() {
        return mName;
    }

    public int getType() {
        return mType;
    }

    public boolean requireAuth() {
        return mRequireAuth;
    }

    private static final String API_LOGIN = "api/login";
    public static final MsRequest LOGIN = new MsRequest(API_LOGIN, "", POST, false);

    private static final String API_FRIEND = "api/userFriend";
    public static final MsRequest FRIEND_LIST_BY_NAMES = new MsRequest(API_FRIEND, "list_user_by_names", GET, true);
    public static final MsRequest FRIEND_LIST_BY_IDS = new MsRequest(API_FRIEND, "list_updated_user_by_ids", GET, true);
    public static final MsRequest FRIEND_LIST_MY_FRIEND = new MsRequest(API_FRIEND, "list_my_friend", GET, true);
    public static final MsRequest FRIEND_FOLLOW_USER = new MsRequest(API_FRIEND, "follow_user", POST, true);
    public static final MsRequest FRIEND_CANCEL_FOLLOW_USER = new MsRequest(API_FRIEND, "cancelFollow", POST, true);
    public static final MsRequest FRIEND_MY_FOLLOW_LIST = new MsRequest(API_FRIEND, "list_follows", GET, true);
    public static final MsRequest FRIEND_LIST_FANS = new MsRequest(API_FRIEND, "list_fans", GET, true);
    public static final MsRequest FRIEND_LIST_FRIENDS = new MsRequest(API_FRIEND, "list_friends", GET, true);

    private static final String API_USER = "api/user";
    public static final MsRequest USER_FULL_INFO = new MsRequest(API_USER, "full_info", GET, true);
    public static final MsRequest VERIFY_REQUEST = new MsRequest(API_USER, "verify_request", MULTI_PART_POST, true);
    public static final MsRequest USER_UPDATE_REMARK = new MsRequest(API_USER, "update_remark_name", POST, true);
    public static final MsRequest USER_LIST_REMARKS = new MsRequest(API_USER, "list_remark_name", GET, true);
    public static final MsRequest USER_UPDATE_PROFILE = new MsRequest(API_USER, "update_profile", POST, true);
    public static final MsRequest USER_GET_VISITORS = new MsRequest(API_USER, "get_visitors", GET, true);
    public static final MsRequest USER_VISITOR_INFO = new MsRequest(API_USER, "visitor_info", GET, true);
    public static final MsRequest USER_VISIT = new MsRequest(API_USER, "visit", POST, true);
    public static final MsRequest USER_PRAISE_TIMES = new MsRequest(API_USER, "praise_times", GET, true);
    public static final MsRequest USER_GET_PRICERS = new MsRequest(API_USER, "get_pricers", GET, true);
    public static final MsRequest USER_PRAISE_INFO = new MsRequest(API_USER, "praise_info", GET, true);
    public static final MsRequest USER_RED_MEN = new MsRequest(API_USER, "List_red_men", GET, true);
    public static final MsRequest USER_FOLLOW_IDS = new MsRequest(API_USER, "followUids", GET, true);

    public static final MsRequest FIND_BY_SUGGEST = new MsRequest(API_USER, "find_by_suggestion", GET, true);
    public static final MsRequest FIND_BY_NICK = new MsRequest(API_USER, "find_by_nick", GET, true);
    public static final MsRequest FIND_BY_FACE_ID = new MsRequest(API_USER, "find_by_face_id", GET, true);
    public static final MsRequest FIND_BY_LOCATION = new MsRequest(API_USER, "find_by_location", GET, true);
    public static final MsRequest UPDATE_PROFILE = new MsRequest(API_USER, "update_profile", MULTI_PART_POST, true);
    public static final MsRequest UPDATE_INFO = new MsRequest(API_USER, "update_info", POST, true);
    public static final MsRequest CHECK_IN = new MsRequest(API_USER, "checkin", POST, true);
    public static final MsRequest CHECK_IN_V2 = new MsRequest(API_USER, "checkinv2", POST, true);
    public static final MsRequest LIST_SCORE_TASK = new MsRequest(API_USER, "list_score_task", GET, true);
    public static final MsRequest PING = new MsRequest(API_USER, "ping", GET, true);
    public static final MsRequest VERIFY_EMAIL_REQUEST = new MsRequest(API_USER, "verify_email_request", POST, true);
    public static final MsRequest VERIFY_EMAIL = new MsRequest(API_USER, "verify_email", POST, true);
    public static final MsRequest UPDATE_PASSWORD = new MsRequest(API_USER, "update_password", POST, true);
    public static final MsRequest CHECK_IN_LIST = new MsRequest(API_USER, "checkin_list", GET, true);
    public static final MsRequest CHECK_IN_RECORD = new MsRequest(API_USER, "checkin_record", GET, true);
    public static final MsRequest WEEK_CHECK_IN_INFO = new MsRequest(API_USER, "week_reward_list", GET, true);
    public static final MsRequest LIST_USER_BEHAVIOR = new MsRequest(API_USER, "list_user_behavior", GET, true);
    public static final MsRequest LIST_RECOMMEND_USER_TRIBE = new MsRequest(API_USER, "Recommend_tribe_and_user", GET, true);
    public static final MsRequest LIST_ADD_BLACK = new MsRequest(API_USER, "add_black_list", POST, true);
    public static final MsRequest LIST_REMOVE_BLACK = new MsRequest(API_USER, "remove_black_list", POST, true);
    public static final MsRequest LIST_BLACK_LIST = new MsRequest(API_USER, "query_black_list", GET, true);

    private static final String API_COURSE = "api/course";
    public static final MsRequest CURRENT_SEMESTER = new MsRequest(API_COURSE, "current_semester", GET, true);
    public static final MsRequest GET_MY_COURSES = new MsRequest(API_COURSE, "get_my_courses", GET, true);

    private static final String API_CF_CIRCLE = "api/cforumCircle";
    public static final MsRequest CFC_LIST_TIMELINE = new MsRequest(API_CF_CIRCLE, "list_timeline", GET, true);
    public static final MsRequest CFC_LIST_HOT = new MsRequest(API_CF_CIRCLE, "list_hot", GET, true);
    public static final MsRequest ADD_FORUM_LISTEN = new MsRequest(API_CF_CIRCLE, "add_forum_listen", POST, true);
    public static final MsRequest SETTING = new MsRequest(API_CF_CIRCLE, "setting", POST, true);
    public static final MsRequest FIND_MY_SETTING = new MsRequest(API_CF_CIRCLE, "find_my_setting", GET, true);
    public static final MsRequest LIST_TIMELINE_TRADE = new MsRequest(API_CF_CIRCLE, "list_timeline_trade", GET, true);
    public static final MsRequest LIST_SEARCH_POST = new MsRequest(API_CF_CIRCLE, "search_threads", GET, true);


    private static final String API_CF = "api/cforum";
    public static final MsRequest LIST_SQUARE_FORUMS = new MsRequest(API_CF, "list_square_forum", GET, true);
    public static final MsRequest LIST_POSTS = new MsRequest(API_CF, "list_thread_by_forum", GET, true);
    public static final MsRequest FIND_FORUM_BY_ID = new MsRequest(API_CF, "find_forum_by_id", GET, true);
    public static final MsRequest LIST_MY_REPLY = new MsRequest(API_CF, "list_my_reply", GET, true);
    public static final MsRequest LIST_REPLY_TO_ME = new MsRequest(API_CF, "list_reply_to_me", GET, true);
    public static final MsRequest LIST_REPLY_BY_THREAD = new MsRequest(API_CF, "list_reply_by_thread", GET, true);
    public static final MsRequest CF_LIST_MY_POSTS = new MsRequest(API_CF, "list_my_thread", GET, true);
    public static final MsRequest CF_LIST_USER_POSTS = new MsRequest(API_CF, "list_user_thread", GET, true);
    public static final MsRequest CF_DELETE = new MsRequest(API_CF, "delete", POST, true);
    public static final MsRequest CF_REPLY = new MsRequest(API_CF, "reply", POST, true);
    public static final MsRequest CF_POST = new MsRequest(API_CF, "post", MULTI_PART_POST, true);
    public static final MsRequest CF_EDIT_THREAD = new MsRequest(API_CF, "edit_thread", MULTI_PART_POST, true);
    public static final MsRequest CF_THUMB_LIKE = new MsRequest(API_CF, "thumb_like", POST, true);
    public static final MsRequest CF_THUMB_HATE = new MsRequest(API_CF, "thumb_hate", POST, true);
    public static final MsRequest CF_TOPIC_SUGGESTED = new MsRequest(API_CF, "topic_suggested", GET, true);
    public static final MsRequest CF_TOPIC_SEARCH = new MsRequest(API_CF, "topic_search", GET, true);
    public static final MsRequest CF_TOPIC_THREAD = new MsRequest(API_CF, "topic_thread", GET, true);
    public static final MsRequest CF_COLLECT_THREAD = new MsRequest(API_CF, "thread_collect", POST, true);
    public static final MsRequest CF_COLLECT_THREAD_LISTS = new MsRequest(API_CF, "thread_collect_list", GET, true);
    public static final MsRequest CF_LIST_THREADS_BY_FORUM = new MsRequest(API_CF, "list_threads_by_forum", GET, true);
    public static final MsRequest CF_LIST_HOT_THREADS_BY_FORUM = new MsRequest(API_CF, "list_hot_threads_by_forum", GET, true);
    public static final MsRequest CF_BANNED = new MsRequest(API_CF, "banned", POST, true);
    public static final MsRequest CF_DELETE_V4 = new MsRequest(API_CF, "delete_v4", POST, true);
    public static final MsRequest CF_STICK_THREAD_V4 = new MsRequest(API_CF, "stick_thread_v4", POST, true);
    public static final MsRequest CF_REFRESH_COUNT = new MsRequest(API_CF, "fresh", GET, true);
    public static final MsRequest CF_TOPIC_BY_ID = new MsRequest(API_CF, "topic_by_id", POST, true);

    public static final MsRequest CREATE_FORUM = new MsRequest(API_CF, "create_forum", MULTI_PART_POST, true);
    public static final MsRequest EDIT_FORUM = new MsRequest(API_CF, "edit_forum", MULTI_PART_POST, true);
    public static final MsRequest DISBAND_FORUM = new MsRequest(API_CF, "disband_forum", POST, true);

    public static final MsRequest CF_REQUEST_MEMBER = new MsRequest(API_CF, "request_member", POST, true);
    public static final MsRequest CF_QUIT_FORUM = new MsRequest(API_CF, "quit_forum", POST, true);
    public static final MsRequest CF_STICK_THREAD = new MsRequest(API_CF, "stick_thread", POST, true);
    public static final MsRequest CF_SUGGEST_THREAD = new MsRequest(API_CF, "suggest_thread", POST, true);
    public static final MsRequest SEARCH_FORUM = new MsRequest(API_CF, "search_forum", GET, true);
    public static final MsRequest RECOMMENDED_FORUM = new MsRequest(API_CF, "list_more_forum", GET, true);
    public static final MsRequest LIST_MEMBER = new MsRequest(API_CF, "list_member", GET, true);
    public static final MsRequest LIST_MEMBER_REQUEST = new MsRequest(API_CF, "list_member_request", GET, true);
    public static final MsRequest ACCEPT_MEMBER = new MsRequest(API_CF, "accept_member", POST, true);
    public static final MsRequest REMOVE_MEMBER = new MsRequest(API_CF, "remove_member", POST, true);
    public static final MsRequest FORUM_INVITE = new MsRequest(API_CF, "invite", POST, true);
    public static final MsRequest FORUM_POST_SHARE = new MsRequest(API_CF, "share_thread", POST, true);
    public static final MsRequest LIST_SUGGESTED_THREAD = new MsRequest(API_CF, "list_suggested_thread", GET, true);
    public static final MsRequest THREAD_INFO = new MsRequest(API_CF, "thread_info", GET, true);

    public static final MsRequest POST_EXTRA_ACTION = new MsRequest(API_CF, "additional_action", POST, true);
    public static final MsRequest LIST_EXTRA_ACTION_MEMBERS = new MsRequest(
            API_CF, "list_additional_userdata", GET, true);

    public static final MsRequest LIST_COMMENT_WITH_REPLY_BY_THREAD = new MsRequest(
            API_CF, "list_comment_with_reply_by_thread", GET, true);
    public static final MsRequest LIST_COMMENT_WITH_REPLY_BY_THREAD_V2 = new MsRequest(
            API_CF, "list_comment_with_reply_by_thread_v2", GET, true);

    public static final MsRequest THREAD_HOT_TOPIC = new MsRequest(API_CF, "topic_hottest_list", GET, true);
    public static final MsRequest THREAD_HOT_POST = new MsRequest(API_CF, "thread_choice_hot_list", GET, true);
    public static final MsRequest THREAD_MY_REPLY = new MsRequest(API_CF, "List_my_reply", GET, true);
    public static final MsRequest THREAD_HOT_POST_LIST = new MsRequest(API_CF, "thread_choice_hot_list_detail", GET, true);

    private static final String API_CFE = "api/cforumEvent";
    public static final MsRequest CFE_LIST_LATEST = new MsRequest(API_CFE, "list_event", GET, true);
    public static final MsRequest CFE_LIST_ATTENDED = new MsRequest(API_CFE, "list_my_reg_event", GET, true);
    public static final MsRequest CFE_LIST_OWNED = new MsRequest(API_CFE, "list_my_event", GET, true);
    public static final MsRequest CFE_EVENT_SUMMARY = new MsRequest(API_CFE, "summary", GET, true);

    private static final String API_OPEN = "api/open";
    public static final MsRequest SYSTEM_INFO = new MsRequest(API_OPEN, "system_info", GET, true);
    public static final MsRequest SEARCH_SCHOOL = new MsRequest(API_OPEN, "search_school", GET, false);
    public static final MsRequest LIST_DEPARTMENT = new MsRequest(API_OPEN, "list_department", GET, false);
    public static final MsRequest CHECK_USERNAME = new MsRequest(API_OPEN, "check_username", GET, false);
    public static final MsRequest WEATHER_INFO = new MsRequest(API_OPEN, "weather_info", GET, false);
    public static final MsRequest REGISTER = new MsRequest(API_OPEN, "register", MULTI_PART_POST, false);
    public static final MsRequest RESET_PASSWORD = new MsRequest(API_OPEN, "reset_password", POST, false);
    public static final MsRequest SPLASH = new MsRequest(API_OPEN, "splash", GET, false);
    public static final MsRequest REFRESH_RATE = new MsRequest(API_OPEN, "refresh_rate", GET, false);

    private static final String API_FEEDBACK = "api/feedback";
    public static final MsRequest FEEDBACK = new MsRequest(API_FEEDBACK, "feedback", MULTI_PART_POST, true);
    public static final MsRequest FEEDBACK_LIST = new MsRequest(API_FEEDBACK, "list", GET, true);

    private static final String API_PROMOTION = "api/promotion";
    public static final MsRequest PROM_TOUCH = new MsRequest(API_PROMOTION, "touch", GET, true);
    public static final MsRequest PROM_INFO = new MsRequest(API_PROMOTION, "info", GET, true);

    private static final String API_USA = "api/usa"; // User score activity =.=
    public static final MsRequest USA_PREGRAB = new MsRequest(API_USA, "pregrab", GET, true);
    public static final MsRequest USA_GRAB = new MsRequest(API_USA, "grab", POST, true);

    private static final String API_JOB = "api/job";
    public static final MsRequest SEARCH_JOB = new MsRequest(API_JOB, "search_job", GET, true);
    public static final MsRequest LIST_JOB_TYPE = new MsRequest(API_JOB, "list_job_type", GET, true);
    public static final MsRequest JOB_MY_RESUME = new MsRequest(API_JOB, "my_resume", GET, true);
    public static final MsRequest JOB_MY_RESUME_ALT = new MsRequest(API_JOB, "my_resume_alt", GET, true);
    public static final MsRequest JOB_CREATE_RESUME = new MsRequest(API_JOB, "create_resume", MULTI_PART_POST, true);
    public static final MsRequest JOB_CREATE_RESUME_ALT = new MsRequest(
            API_JOB, "create_resume_alt", MULTI_PART_POST, true);
    public static final MsRequest JOB_DELETE_RESUME_ALT = new MsRequest(
            API_JOB, "delete_resume_alt", MULTI_PART_POST, true);
    public static final MsRequest JOB_EDIT_RESUME = new MsRequest(API_JOB, "edit_resume", MULTI_PART_POST, true);
    public static final MsRequest JOB_LIST_OFFER = new MsRequest(API_JOB, "list_req_by_candidate", GET, true);
    public static final MsRequest CANDIDATE_REQUEST = new MsRequest(API_JOB, "candidate_request", POST, true);
    public static final MsRequest CANDIDATE_ACCEPT = new MsRequest(API_JOB, "candidate_accept", POST, true);
    public static final MsRequest CORP_INFO = new MsRequest(API_JOB, "corp_info", GET, true);
    public static final MsRequest RECRUITS_BY_WEEK = new MsRequest(API_JOB, "list_my_recruit_list_by_week", GET, true);
    public static final MsRequest EDIT_JOB_TAGS = new MsRequest(API_JOB, "edit_job_tags", POST, true);
    public static final MsRequest LIST_MY_JOB_TAGS = new MsRequest(API_JOB, "list_my_job_tags", GET, true);
    public static final MsRequest LIST_ALL_JOB_TAGS = new MsRequest(API_JOB, "list_all_job_tags", GET, true);
    public static final MsRequest LIST_JOBS_BY_MY_TAG = new MsRequest(API_JOB, "list_jobs_by_my_tag", GET, true);
    public static final MsRequest LIST_JOB_BY_TAGS = new MsRequest(API_JOB, "list_job_by_tags", GET, true);
    public static final MsRequest JOB_INFO = new MsRequest(API_JOB, "job_info", GET, true);
    public static final MsRequest LIST_MY_RECRUIT_LIST_TODAY = new MsRequest(API_JOB, "list_my_recruit_list_today", GET, true);
    public static final MsRequest LIST_MY_RECRUIT_LIST = new MsRequest(API_JOB, "list_my_recruit_list", GET, true);
    public static final MsRequest JOB_INSERT_RECRUIT = new MsRequest(API_JOB, "insert_recruit", POST, true);

    private static final String API_NEWS = "api/broadcast";
    public static final MsRequest NEWS_SUGGESTED_COUNT = new MsRequest(API_NEWS, "count_new_suggested", GET, true);
    public static final MsRequest NEWS_LIST_LATEST = new MsRequest(API_NEWS, "broadcast_latest", GET, true);
    public static final MsRequest NEWS_LIST_SUGGESTED = new MsRequest(API_NEWS, "broadcast_suggested", GET, true);
    public static final MsRequest NEWS_LIST_COMMENT = new MsRequest(API_NEWS, "list_comment", GET, true);
    public static final MsRequest NEWS_DETAILS = new MsRequest(API_NEWS, "get_broadcast", GET, true);
    public static final MsRequest NEWS_LIKE = new MsRequest(API_NEWS, "like", POST, true);
    public static final MsRequest NEWS_COMMENT = new MsRequest(API_NEWS, "comment", POST, true);
    public static final MsRequest NEWS_DELETE_COMMENT = new MsRequest(API_NEWS, "delete_comment", POST, true);
    public static final MsRequest NEWS_SOURCES_FOLLOWED = new MsRequest(API_NEWS, "broadcaster_followed", GET, true);
    public static final MsRequest NEWS_SOURCES_SEARCH = new MsRequest(API_NEWS, "broadcaster_search", GET, true);
    public static final MsRequest NEWS_SOURCE_DETAILS = new MsRequest(API_NEWS, "broadcaster_profile", GET, true);
    public static final MsRequest NEWS_SOURCE_NEWS = new MsRequest(API_NEWS, "list_by_broadcaster", GET, true);
    public static final MsRequest NEWS_SOURCE_FOLLOW = new MsRequest(API_NEWS, "follow_broadcaster", POST, true);
    public static final MsRequest NEWS_MY_BROADCAST_TODAY = new MsRequest(API_NEWS, "my_broadcast_today", GET, true);
    public static final MsRequest NEWS_BY_ID = new MsRequest(API_NEWS, "find_broadcast_by_id", GET, true);
    public static final MsRequest OFFICIAL_ACCOUNT = new MsRequest(API_NEWS, "list_official_account", GET, true);
    public static final MsRequest NEWS_BROADCAST = new MsRequest(API_NEWS, "list_by_broadcaster", GET, true);
    public static final MsRequest NEWS_BROADCAST_DETAIL = new MsRequest(API_NEWS, "find_broadcast_by_id", GET, true);

    private static final String API_QA = "api/qa";
    public static final MsRequest QA_LIST_QUESTION = new MsRequest(API_QA, "list_question", GET, true);
    public static final MsRequest QA_LIST_MY_QUESTION = new MsRequest(API_QA, "list_my_question", GET, true);
    public static final MsRequest QA_LIST_MY_ANSWERED = new MsRequest(API_QA, "list_my_answered", GET, true);
    public static final MsRequest QA_LIST_ANSWER = new MsRequest(API_QA, "list_answer", GET, true);
    public static final MsRequest QA_CHOOSE_ANSWER = new MsRequest(API_QA, "choose_answer", POST, true);
    public static final MsRequest QA_ASK = new MsRequest(API_QA, "ask", MULTI_PART_POST, true);
    public static final MsRequest QA_ANSWER = new MsRequest(API_QA, "answer", MULTI_PART_POST, true);

    private static final String API_POLICE = "api/police";
    public static final MsRequest POLICE_REPORT = new MsRequest(API_POLICE, "report", POST, true);

    private static final String API_NOTICE = "api/inbox";
    public static final MsRequest NOTICE_TOUCH = new MsRequest(API_NOTICE, "touch_v2", GET, true);
    public static final MsRequest NOTICE_SUMMARY = new MsRequest(API_NOTICE, "summary_v2", GET, true);
    public static final MsRequest NOTICE_LIST = new MsRequest(API_NOTICE, "list_v2", GET, true);
    public static final MsRequest INBOX_LIST = new MsRequest(API_NOTICE, "message", GET, true);
    public static final MsRequest PUSH_LIST = new MsRequest(API_NOTICE, "push_list", GET, true);

    private static final String API_UTIL = "api/util";
    public static final MsRequest LIST_LOCATION = new MsRequest(API_UTIL, "list_location", GET, true);
    public static final MsRequest INC_SHARE = new MsRequest(API_UTIL, "inc_share", POST, true);
    public static final MsRequest OSS_TOKEN = new MsRequest(API_UTIL, "oss_token", GET, true);
    public static final MsRequest WEEK_INFO = new MsRequest(API_UTIL, "week_info", GET, true);

    private static final String API_BOUNTY = "api/bounty";
    public static final MsRequest BTY_LIST_MY_TASK = new MsRequest(API_BOUNTY, "list_my_task", GET, true);
    public static final MsRequest BTY_LIST_MY_SIGNED_CONTRACT = new MsRequest(
            API_BOUNTY, "list_my_signed_contract", GET, true);
    public static final MsRequest BTY_LIST_MY_SIGNED_TASK = new MsRequest(API_BOUNTY, "list_my_signed_task", GET, true);
    public static final MsRequest BTY_LIST_MY_FAV_TASK = new MsRequest(API_BOUNTY, "list_my_fav_task", GET, true);
    public static final MsRequest BTY_LIST_TASK = new MsRequest(API_BOUNTY, "list_task", GET, true);
    public static final MsRequest BTY_LIST_TASK_BY_LOCATION = new MsRequest(
            API_BOUNTY, "list_task_by_location", GET, true);
    public static final MsRequest BTY_LIST_SUGGESTED_TASK = new MsRequest(API_BOUNTY, "list_suggested_task", GET, true);
    public static final MsRequest BTY_POST = new MsRequest(API_BOUNTY, "post", MULTI_PART_POST, true);
    public static final MsRequest BTY_CANCEL = new MsRequest(API_BOUNTY, "delete_task", POST, true);
    public static final MsRequest BTY_FAV = new MsRequest(API_BOUNTY, "fav_task", POST, true);
    public static final MsRequest BTY_APPLY = new MsRequest(API_BOUNTY, "request_contract", POST, true);
    public static final MsRequest BTY_LIST_CONTRACT_BY_TASK = new MsRequest(
            API_BOUNTY, "list_contract_by_task", GET, true);
    public static final MsRequest BTY_RATING = new MsRequest(API_BOUNTY, "rating", POST, true);
    public static final MsRequest BTY_GET_CREDIT = new MsRequest(API_BOUNTY, "get_credit", GET, true);
    public static final MsRequest BTY_LIST_RATING = new MsRequest(API_BOUNTY, "list_rating", GET, true);

    private static final String API_MEDAL = "api/badge";
    public static final MsRequest MEDAL_SET_PRIMARY_BADGES = new MsRequest(API_MEDAL, "set_primary_badges", POST, true);
    public static final MsRequest MEDAL_FETCH_ALL = new MsRequest(API_MEDAL, "fetch_all", GET, true);

    private static final String API_IM = "api/im";
    public static final MsRequest IM_PREPARE_UPLOAD = new MsRequest(API_IM, "prepare_upload", GET, true);

    private static final String API_IM_USER_RES = "api/imUserRes";
    public static final MsRequest IMUR_FIND_USER_USING = new MsRequest(API_IM_USER_RES, "find_user_using", GET, true);
    public static final MsRequest IMUR_LIST_MY = new MsRequest(API_IM_USER_RES, "list_my", GET, true);
    public static final MsRequest IMUR_NEWEST = new MsRequest(API_IM_USER_RES, "newest", GET, true);
    public static final MsRequest IMUR_ADD = new MsRequest(API_IM_USER_RES, "add", POST, true);
    public static final MsRequest IMUR_USE = new MsRequest(API_IM_USER_RES, "use", POST, true);
    public static final MsRequest IMUR_UNUSE = new MsRequest(API_IM_USER_RES, "unuse", POST, true);
    public static final MsRequest IMUR_LIST = new MsRequest(API_IM_USER_RES, "list", GET, true);
    public static final MsRequest IMUR_FIND_BY_ID = new MsRequest(API_IM_USER_RES, "find_by_id", GET, true);
    public static final MsRequest IMUR_VIP_CENTER_LIST = new MsRequest(API_IM_USER_RES, "vip_center_list", GET, true);

    private static final String API_IM_RES_WEB = "api/imUserResWeb";
    public static final MsRequest IMRW_LIST = new MsRequest(API_IM_RES_WEB, "list", GET, true);
    public static final MsRequest IMRW_CHOOSE = new MsRequest(API_IM_RES_WEB, "choose", GET, true);
    public static final MsRequest IMRW_CLICK = new MsRequest(API_IM_RES_WEB, "click", GET, true);
    public static final MsRequest IMRW_USE = new MsRequest(API_IM_RES_WEB, "use", GET, true);
    public static final MsRequest IMRW_UNUSE = new MsRequest(API_IM_RES_WEB, "unuse", GET, true);
    public static final MsRequest IMRW_ADD = new MsRequest(API_IM_RES_WEB, "add", GET, true);
    public static final MsRequest IMRW_RESOURCE_LIST = new MsRequest(API_IM_RES_WEB, "resource_list", GET, true);
    public static final MsRequest IMRW_ACTIVITY = new MsRequest(API_IM_RES_WEB, "activity", GET, true);
    public static final MsRequest IMRW_VIP_CENTER = new MsRequest(API_IM_RES_WEB, "vip_center", GET, true);
    public static final MsRequest IMRW_VIP_CLICK = new MsRequest(API_IM_RES_WEB, "vip_click", GET, true);
    public static final MsRequest IMRW_GAME = new MsRequest(API_IM_RES_WEB, "game", GET, true);
    public static final MsRequest IMRW_GAME_CLICK = new MsRequest(API_IM_RES_WEB, "qa_game_click", GET, true);

    private static final String API_USERGROUP = "api/userGroup";
    public static final MsRequest LIST_MY_GROUP = new MsRequest(API_USERGROUP, "list_my_group", GET, true);
    public static final MsRequest FIND_GROUP_BY_ID = new MsRequest(API_USERGROUP, "find_group_by_id", GET, true);
    public static final MsRequest GROUP_LIST_MEMBER = new MsRequest(API_USERGROUP, "list_member", GET, true);
    public static final MsRequest CREATE_GROUP = new MsRequest(API_USERGROUP, "create_group", POST, true);
    public static final MsRequest EDIT_GROUP = new MsRequest(API_USERGROUP, "edit_group", POST, true);
    public static final MsRequest DELETE_GROUP = new MsRequest(API_USERGROUP, "delete_group", POST, true);
    public static final MsRequest ADD_USER = new MsRequest(API_USERGROUP, "add_users", POST, true);
    public static final MsRequest EDIT_MEMBER = new MsRequest(API_USERGROUP, "edit_member", POST, true);
    public static final MsRequest REMOVE_USER = new MsRequest(API_USERGROUP, "remove_user", POST, true);
    public static final MsRequest QUIT = new MsRequest(API_USERGROUP, "quit", POST, true);
    public static final MsRequest THEMELIST = new MsRequest(API_USERGROUP, "theme_list", GET, true);
    public static final MsRequest ENTER_TOPIC = new MsRequest(API_USERGROUP, "my_room", POST, true);

    private static final String API_USEREXT = "api/userExt";
    public static final MsRequest USEREXT_LOGIN = new MsRequest(API_USEREXT, "login", GET, true);
    public static final MsRequest USEREXT_LIST_MY_BIND = new MsRequest(API_USEREXT, "list_my_bind", GET, true);
    public static final MsRequest USEREXT_REGISTER = new MsRequest(API_USEREXT, "register", POST, true);
    public static final MsRequest USEREXT_BIND = new MsRequest(API_USEREXT, "bind", POST, true);
    public static final MsRequest USEREXT_UNBIND = new MsRequest(API_USEREXT, "unbind", POST, true);

    private static final String API_CHANNEL = "api/channel";
    public static final MsRequest CHANNEL_LIST = new MsRequest(API_CHANNEL, "list", GET, true);
    public static final MsRequest CHECK_CREATE = new MsRequest(API_CHANNEL, "check_create", GET, true);
    public static final MsRequest CREATE = new MsRequest(API_CHANNEL, "create", GET, true);
    public static final MsRequest SEARCH = new MsRequest(API_CHANNEL, "search", GET, true);
    public static final MsRequest CHLTYPE = new MsRequest(API_CHANNEL, "tag_list", GET, true);
    public static final MsRequest NEW_LIST = new MsRequest(API_CHANNEL, "new_list", GET, true);
    public static final MsRequest MY_CHANNEL = new MsRequest(API_CHANNEL, "my_channel", GET, true);
    public static final MsRequest CHANNEL_COLLECT = new MsRequest(API_CHANNEL, "collect", GET, true);
    public static final MsRequest COLLECTION_LIST = new MsRequest(API_CHANNEL, "collection_list", GET, true);
    public static final MsRequest FIND_CHANNEL_BY_ID = new MsRequest(API_CHANNEL, "find_channel_by_id", GET, true);

    private static final String API_SCHOOL = "api/school";
    public static final MsRequest SCHOOL_SEARCH_AREA = new MsRequest(API_SCHOOL, "school_of_area", GET, true);
    public static final MsRequest SCHOOL_COLLECT_LIST = new MsRequest(API_SCHOOL, "collection_list", GET, true);
    public static final MsRequest SCHOOL_COLLECT = new MsRequest(API_SCHOOL, "collect", GET, true);
    public static final MsRequest SCHOOL_UNLOCK_LIST = new MsRequest(API_SCHOOL, "my_area", GET, true);
    public static final MsRequest SCHOOL_SEARCH = new MsRequest(API_SCHOOL, "search", GET, true);

    private static final String API_NOTE = "api/note";
    public static final MsRequest NOTE_POST = new MsRequest(API_NOTE, "post", POST, true);
    public static final MsRequest GETNOTE_POST = new MsRequest(API_NOTE, "note_list", GET, true);
    public static final MsRequest NOTE_LIST_BY_DAY = new MsRequest(API_NOTE, "note_list_by_day", GET, true);
    public static final MsRequest EDIT_NOTE_POST = new MsRequest(API_NOTE, "update", POST, true);
    public static final MsRequest DELETE_NOTE_POST = new MsRequest(API_NOTE, "delete", POST, true);

    private static final String API_TRADE = "api/trade";
    public static final MsRequest TRADE_VIP_INFO = new MsRequest(API_TRADE, "vip_info", GET, true);
    public static final MsRequest TRADE_RECHARGE_INFO = new MsRequest(API_TRADE, "recharge_info", GET, true);
    public static final MsRequest TRADE_TRADE_LIST = new MsRequest(API_TRADE, "trade_list", GET, true);
    public static final MsRequest TRADE_GOLD_TO_POINT = new MsRequest(API_TRADE, "price_to_credit", POST, true);
    public static final MsRequest TRADE_BUY_VIP = new MsRequest(API_TRADE, "buy_vip", POST, true);
    public static final MsRequest TRADE_BUY_VIP_BY_MONEY = new MsRequest(API_TRADE, "buy_vip_by_money", POST, true);
    public static final MsRequest TRADE_BUY_VIP_BY_GOLD = new MsRequest(API_TRADE, "buy_vip_by_gold", POST, true);
    public static final MsRequest TRADE_RECHARGE_GOLD = new MsRequest(API_TRADE, "recharge_gold", POST, true);

    private static final String API_TASK = "api/task";
    public static final MsRequest MY_TASK_LIST = new MsRequest(API_TASK, "my_task", GET, true);
    public static final MsRequest TRIGGER_EVENT = new MsRequest(API_TASK, "trigger_event", POST, true);

    private static final String API_BANNER = "api/banner";
    public static final MsRequest BANNER_LIST = new MsRequest(API_BANNER, "banner_list", GET, true);

    private static final String API_TRIBE = "api/tribe";
    public static final MsRequest TRIBE_HOT_LIST = new MsRequest(API_TRIBE, "list_hot_tribes", GET, true);
    public static final MsRequest TRIBE_LATEST_POST_LIST = new MsRequest(API_TRIBE, "list_latest_threads", GET, true);
    public static final MsRequest TRIBE_TYPES_LIST = new MsRequest(API_TRIBE, "list_tribe_types", GET, true);
    public static final MsRequest TRIBE_LIST_BY_TYPE = new MsRequest(API_TRIBE, "list_tribe_by_category", GET, true);
    public static final MsRequest TRIBE_LATEST_TRIBE_LIST = new MsRequest(API_TRIBE, "list_latest_tribes", GET, true);
    public static final MsRequest TRIBE_LIST_HOT_THREADS = new MsRequest(API_TRIBE, "list_hot_threads", GET, true);
    public static final MsRequest TRIBE_ASSIST = new MsRequest(API_TRIBE, "assist_tribe", POST, true);
    public static final MsRequest TRIBE_LIST_PICK_THREADS = new MsRequest(API_TRIBE, "list_pick_threads", GET, true);
    public static final MsRequest TRIBE_CANCEL_FOLLOW = new MsRequest(API_TRIBE, "cancel_follow_tribe", POST, true);
    public static final MsRequest TRIBE_FOLLOW_WITH = new MsRequest(API_TRIBE, "follow_tribe", POST, true);
    public static final MsRequest TRIBE_LATEST_TOPICS = new MsRequest(API_TRIBE, "list_latest_topics", GET, true);
    public static final MsRequest TRIBE_CONCERN_TRIBE = new MsRequest(API_TRIBE, "list_follow_tribes", GET, true);
    public static final MsRequest TRIBE_LIST_MEN_RANK = new MsRequest(API_TRIBE, "list_tribe_men_rank", GET, true);
    public static final MsRequest TRIBE_SEARCH = new MsRequest(API_TRIBE, "search_tribes", GET, true);
    public static final MsRequest TRIBE_LIST_THREAD_BY_TOPIC = new MsRequest(API_TRIBE, "list_thread_by_topic", GET, true);
    public static final MsRequest TRIBE_CREATE_CHAT_ROOM = new MsRequest(API_TRIBE, "create_tribe_chat_room", POST, true);
    public static final MsRequest TRIBE_ACCESS_CHAT_ROOM = new MsRequest(API_TRIBE, "enter_tribe_chat_room", POST, true);
    public static final MsRequest TRIBE_EXIT_CHAT_ROOM = new MsRequest(API_TRIBE, "exit_tribe_chat_room", POST, true);
    public static final MsRequest TRIBE_DELETE_CHAT_ROOM = new MsRequest(API_TRIBE, "delete_tribe_chat_room", POST, true);
    public static final MsRequest TRIBE_GET_CHAT_ROOMS = new MsRequest(API_TRIBE, "list_tribe_chatRoom", GET, true);
    public static final MsRequest TRIBE_GET_CHAT_ROOM_USERS = new MsRequest(API_TRIBE, "List_tribe_chatRoom_member", GET, true);
    public static final MsRequest TRIBE_CHANGE_ROOM_INFO = new MsRequest(API_TRIBE, "edit_tribe_chat_room", POST, true);
    public static final MsRequest TRIBE_HOT_SEARCH = new MsRequest(API_TRIBE, "list_hot_search_tribes", GET, true);
    public static final MsRequest TRIBE_ADD_FRIEND_TO_CHAT = new MsRequest(API_TRIBE, "add_tribe_chatRoom_member", POST, true);
    public static final MsRequest TRIBE_REMOVE_FRIEND_TO_CHAT = new MsRequest(API_TRIBE, "remove_tribe_chatRoom_member", POST, true);
    public static final MsRequest TRIBE_CHATROOM_BY_ID = new MsRequest(API_TRIBE, "list_tribe_chatRoom_by_id", GET, true);
    public static final MsRequest TRIBE_GET_INFO_BY_ID = new MsRequest(API_TRIBE, "list_tribe_by_id", GET, true);
    public static final MsRequest TRIBE_INTEREST_CIRCLE = new MsRequest(API_TRIBE, "interest_circle", GET, true);
    public static final MsRequest TRIBE_INTEREST_CIRCLE_FRESH = new MsRequest(API_TRIBE, "interest_Fresh", GET, true);
    public static final MsRequest TRIBE_ALL_TRIBES = new MsRequest(API_TRIBE, "all_tribes", GET, true);

    private static final String API_LIVE = "api/live";
    public static final MsRequest CREATE_LIVE_ROOM = new MsRequest(API_LIVE, "Create_live_room", POST, true);
    public static final MsRequest ENTER_LIVE = new MsRequest(API_LIVE, "Enter_live", POST, true);
    public static final MsRequest EXIT_LIVE = new MsRequest(API_LIVE, "Exit_live", POST, true);
    public static final MsRequest LIST_LIVE = new MsRequest(API_LIVE, "list_live", GET, true);
    public static final MsRequest EDIT_LIVE = new MsRequest(API_LIVE, "edit_live", POST, true);
    public static final MsRequest END_LIVE = new MsRequest(API_LIVE, "end_live", POST, true);

    public static final MsRequest LIVE_SEND_GIFT = new MsRequest(API_LIVE, "send_gift", POST, true);
    public static final MsRequest LIVE_LIST_GIFTS = new MsRequest(API_LIVE, "list_gifts", GET, true);

    public static final MsRequest LIVE_INCOME = new MsRequest(API_LIVE, "income", GET, true);
    public static final MsRequest FOLLOW_HOST = new MsRequest(API_LIVE, "focus_host", POST, true);
    public static final MsRequest CANCEL_HOST = new MsRequest(API_LIVE, "cancleFocus", POST, true);
    public static final MsRequest FOLLOW_LIST = new MsRequest(API_LIVE, "list_host_follows", GET, true);
    public static final MsRequest GET_HOST_USER_INFO = new MsRequest(API_LIVE, "find_host_by_id", GET, true);
    public static final MsRequest PRAISE_HOST = new MsRequest(API_LIVE, "thumpHost", POST, true);
    public static final MsRequest FIND_USER_BY_NICK = new MsRequest(API_LIVE, "find_by_nick_in_live", GET, true);
    public static final MsRequest FIND_AUDIENCE_LIST = new MsRequest(API_LIVE, "List_live_members", GET, true);
    public static final MsRequest GET_GIFT_LIST = new MsRequest(API_LIVE, "list_live_gift", GET, true);
    public static final MsRequest GET_TOPIC_LIST = new MsRequest(API_LIVE, "list_topics", GET, true);
    public static final MsRequest LIST_TOPICS = new MsRequest(API_LIVE, "list_topics", GET, true);
    public static final MsRequest LIST_MAIN_TOPICS = new MsRequest(API_LIVE, "list_main_topics", GET, true);
    public static final MsRequest GET_LIVE_END_INFO = new MsRequest(API_LIVE, "end_live_info", GET, true);
    public static final MsRequest BARRAGE_DEDUCT_GOLD = new MsRequest(API_LIVE, "barrage_deduct_gold", POST, true);
    public static final MsRequest ACTIVITY_STREAM_STATUS = new MsRequest(API_LIVE, "activity_stream_status", GET, true);

    public static final MsRequest SEND_CONNECTION_REQUEST = new MsRequest(API_LIVE, "send_connection_request", POST, true);
    public static final MsRequest LIVE_CONNECTION = new MsRequest(API_LIVE, "connection", POST, true);
    public static final MsRequest LIVE_CONNECTION_CLOSE = new MsRequest(API_LIVE, "close_connection", POST, true);
    public static final MsRequest LIVE_CONNECTION_LIST = new MsRequest(API_LIVE, "list_connection_request", GET, true);
    public static final MsRequest LIVE_CANCLE_CONNECTION_REQUEST = new MsRequest(API_LIVE, "cancle_connection_request", POST, true);
    public static final MsRequest LIVE_GET_ADMIN_STATUS = new MsRequest(API_LIVE, "query_live_admin_status", GET, true);
    public static final MsRequest LIVE_MAKE_SILENCE = new MsRequest(API_LIVE, "make_silence", POST, true);
    public static final MsRequest LIVE_CANCEL_MAKE_SILENCE = new MsRequest(API_LIVE, "remove_silence", POST, true);
    public static final MsRequest LIVE_DELETE_ADMIN = new MsRequest(API_LIVE, "remove_host_manager", POST, true);
    public static final MsRequest LIVE_ADD_ADMIN = new MsRequest(API_LIVE, "add_host_manager", POST, true);


    public static final MsRequest CONTRIBUTE_RANKING = new MsRequest(API_LIVE, "Contribute_ranking", GET, true);
    public static final MsRequest MY_TOTAL_INCOME = new MsRequest(API_LIVE, "my_total_income", GET, true);
    public static final MsRequest LIST_MY_OWN_LIVE = new MsRequest(API_LIVE, "List_my_own_live", GET, true);
    public static final MsRequest LIVE_MY_IS_SILENCE = new MsRequest(API_LIVE, "query_silence", GET, true);

    private static final String API_DEAL = "api/deal";
    public static final MsRequest LIST_RECHARGE_RECORDS = new MsRequest(API_DEAL, "List_recharge_records", GET, true);
    public static final MsRequest LIST_WITHDRAWAL_RECORDS = new MsRequest(API_DEAL, "List_withdrawal_records", GET, true);
    public static final MsRequest LIST_CONSUMPTION_RECORDS = new MsRequest(API_DEAL, "List_consumption_records", GET, true);
    public static final MsRequest DEAL_RECHARGE_ANDROID = new MsRequest(API_DEAL, "recharge_android", POST, true);
    public static final MsRequest DEAL_TRADE_PRODUCTION_LIST = new MsRequest(API_DEAL, "trade_production_list", GET, true);
}
