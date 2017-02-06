package com.tjut.mianliao.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.tjut.mianliao.R;

/**
 * Response from MotherShip :)
 */
public class MsResponse {
	private static final String TAG = "MsResponse";

	/**
	 * Only code 1 means success, all other codes mean failure
	 */
	public static final int MS_SUCCESS = 1;
	/**
	 * Generic fail code
	 */
	public static final int MS_FAILED = 0;

	public static final int HTTP_NOT_CONNECTED = 10;
	public static final int HTTP_TIMEOUT = 11;
	public static final int HTTP_NETWORK_ERROR = 12;
	public static final int HTTP_INVALID_URL = 13;
	public static final int MS_CANCELLED = 21;
	public static final int MS_PARSE_FAILED = 22;
	public static final int MS_STATUS_ERROR = 23;

	public static final int MS_UNKNOWN_REQUEST_ACTION = 101;
	public static final int MS_INVALID_REQUEST_TYPE = 102;
	public static final int MS_MISSING_PARAMETER = 103;
	public static final int MS_INVALID_PARAMETER = 104;
	public static final int MS_DATABASE_ERROR = 105;
	public static final int MS_ATTACHMENT_ERROR = 106;
	public static final int MS_MAIL_ERROR = 107;
	public static final int MS_MAINTAINING = 108;

	public static final int MS_VERSION_TOO_OLD = 119;

	public static final int MS_USER_WRONG_TOKEN = 201;
	public static final int MS_USER_NOT_EXIST = 202;
	public static final int MS_USER_WRONG_PASSWORD = 203;
	public static final int MS_USER_IDS_NOT_AUTHED = 204;
	public static final int MS_USER_IDS_NOT_ALLOWED = 205;
	public static final int MS_LOGIN_TOO_FREQUENT = 208;
	public static final int MS_IDS_SERVER_ERROR = 210;
	public static final int MS_USER_INVALID_USERNAME = 211;
	public static final int MS_USER_EXISTING_USERNAME = 212;
	public static final int MS_USER_NOT_AUTHED = 213;
	public static final int MS_USER_NUMBER_ALREADY_V = 214;
	public static final int MS_USER_ALREADY_CHECKIN_TODAY = 215;
	public static final int MS_USER_INVALID_NICK = 216;
	public static final int MS_USER_VERIFICATION_FAILED = 217;
	public static final int MS_USER_VERIFICATION_EMAIL_USED = 218;
	public static final int MS_TASK_SIGN_NUM_UP_TO_TOP = 222;

	public static final int MS_TICKET_INVALID = 301;
	public static final int MS_TICKET_ALREADY_CHECKED = 302;
	public static final int MS_TICKET_NOT_AUTHED = 303;

	public static final int MS_QUESTION_NO_MORE = 401;
	public static final int MS_QUESTION_NOT_EXIST = 402;

	public static final int MS_BROADCAST_NOT_EXIST = 501;
	public static final int MS_BROADCAST_ALREADY_UP = 502;
	public static final int MS_BROADCAST_COMMENT_NOT_EXIST = 508;

	public static final int MS_COURSE_NO_SEASON_FOUND = 601;
	public static final int MS_COURSE_NOT_EXIST = 602;
	public static final int MS_COURSE_ALREADY_JOINT = 603;
	public static final int MS_COURSE_USER_HASNT_JOINT = 604;
	public static final int MS_COURSE_JSON_ERROR = 605;
	public static final int MS_COURSE_JSON_AMOUNT_ERROR = 606;
	public static final int MS_COURSE_JSON_SEMESTER_MISSING = 607;
	public static final int MS_COURSE_JSON_SEMESTER_INVALID = 608;
	public static final int MS_COURSE_JSON_NAME_MISSING = 609;
	public static final int MS_COURSE_JSON_TEACHER_MISSING = 610;
	public static final int MS_COURSE_JSON_ENTRIES_MISSING = 611;
	public static final int MS_COURSE_JSON_ENTRIES_ISNT_ARRAY = 612;
	public static final int MS_COURSE_JSON_ENTRIES_EMPTY = 613;
	public static final int MS_COURSE_JSON_CLASSROOM_MISSING = 614;
	public static final int MS_COURSE_JSON_WEEKDAY_MISSING = 615;
	public static final int MS_COURSE_JSON_WEEKDAY_INVALID = 616;
	public static final int MS_COURSE_JSON_WEEKS_MISSING = 617;
	public static final int MS_COURSE_JSON_WEEKS_INVALID = 618;
	public static final int MS_COURSE_JSON_P1_MISSING = 619;
	public static final int MS_COURSE_JSON_P1_INVALID = 620;
	public static final int MS_COURSE_JSON_P2_MISSING = 621;
	public static final int MS_COURSE_JSON_P2_INVALID = 622;
	public static final int MS_COURSE_JSON_P1_LADGER_THAN_P2 = 623;
	public static final int MS_COURSE_JOIN_NEW_COURSE_FAILURE = 624;

	public static final int MS_CFORUM_FORUM_NOT_EXIST = 901;
	public static final int MS_CFORUM_THREAD_NOT_EXIST = 902;
	public static final int MS_CFORUM_REPLY_NOT_EXIST = 903;
	public static final int MS_CFORUM_NOT_AUTHED = 904;
	public static final int MS_CFORUM_BANNED = 905;
	public static final int MS_CFORUM_THREAD_LOCKED = 906;
	public static final int MS_CFORUM_TOO_FREQUENT = 907;

	public static final int MS_CFORUM_NOT_MEMBER = 908;
	public static final int MS_CFORUM_MEMBER_NOT_YET_REQUESTED = 909;
	public static final int MS_CFORUM_MEMBER_ALREADY_REQUESTED = 910;
	public static final int MS_CFORUM_MEMBER_NOT_YET_JOINED = 911;
	public static final int MS_CFORUM_MEMBER_ALREADY_JOINED = 912;
	public static final int MS_CFORUM_FORUM_ALREADY_EXIST = 913;
	public static final int MS_CFORUM_FORUM_CREATE_TOO_MANY = 914;

	public static final int MS_PROMOTION_NOT_EXIST = 1001;
	public static final int MS_PROMOTION_NOT_AUTHED = 1002;

	public static final int MS_USA_NOT_EXIST = 1101;
	public static final int MS_USA_NOT_AUTHED = 1102;

	public static final int MS_JOB_NOT_AUTHED = 1201;
	public static final int MS_JOB_JOB_NOT_EXIST = 1207;
	public static final int MS_JOB_RESUME_NOT_EXIST = 1211;
	public static final int MS_JOB_RESUME_ALREADY_EXIST = 1212;
	public static final int MS_JOB_RESUME_INVALID_STATUS = 1213;
	public static final int MS_JOB_RESUME_NOT_ALLOWED = 1214;
	public static final int MS_JOB_OFFER_NOT_EXIST = 1215;
	public static final int MS_JOB_OFFER_ALREADY_EXIST = 1216;
	public static final int MS_JOB_OFFER_INVALID_STATUS = 1217;
	public static final int MS_JOB_OFFER_NOT_ALLOWED = 1218;

	public static final int MS_POLICE_USER_READONLY = 1301;
	public static final int MS_POLICE_USER_INACTIVE = 1302;

	public static final int MS_BOUNTY_TASK_NOT_EXIST = 1401;
	public static final int MS_BOUNTY_TASK_INVALID_STATUS = 1402;
	public static final int MS_BOUNTY_TASK_NOT_ALLOWED = 1403;
	public static final int MS_BOUNTY_TASK_TOO_MANY = 1404;
	public static final int MS_BOUNTY_CONTRACT_NOT_EXIST = 1405;
	public static final int MS_BOUNTY_CONTRACT_INVALID_STATUS = 1406;
	public static final int MS_BOUNTY_CONTRACT_NOT_ALLOWED = 1407;
	public static final int MS_BOUNTY_CONTRACT_TOO_MANY = 1408;
	public static final int MS_BOUNTY_CONTRACT_TOO_MANY_IN_TASK = 1409;

	public static final int MS_FAIL_IM_USER_RESOURCE_NOT_EXIST = 1501;

	public static final int MS_FAIL_SCHOOL_POST_CAN_NOT_TODAY = 1907;
	public static final int MS_FAIL_SCHOOL_POST_DAY_MAX = 1908;

	public static final int MS_FAIL_CHANNEL_ICON = 2006;

	public static final int MS_FAIL_TRADE_PRICE_NOT_ENOUGH = 2402;
	public static final int MS_FAIL_TRADE_CREDIT_NOT_ENOUGH = 2403;
	public static final int MS_FAIL_TRADE_RESOURCE_NEED_VIP = 2409;

	public static final int FAIL_TRIBE_CREATE_TOO_MANY_ROOMS = 4014;
	public static final int FAIL_TRIBE_ROOM_FULL = 4015;
	public static final int FAIL_TRIBE_ROOM_NOT_PERMIT = 4016;
	public static final int FAIL_TRIBE_ASSIST_TOO_MANY = 4017;
	public static final int FAIL_HAS_BEEN_BANNED = 4018;

	public static final String PARAM_CODE = "code";
	public static final String PARAM_RESPONSE = "response";

	public JSONObject json;
	public int code;
	public String response;
	public Object value;
	public MsRequest request;

	public static boolean isSuccessful(MsResponse mr) {
		return mr != null && MS_SUCCESS == mr.code;
	}

	public boolean isSuccessful() {
		return MS_SUCCESS == code;
	}

	public static MsResponse fromJson(String src) {
		MsResponse response = new MsResponse();
		try {
			JSONObject json = new JSONObject(src);
			response.code = json.getInt(PARAM_CODE);
			response.response = json.getString(PARAM_RESPONSE);
			response.json = json;
		} catch (JSONException e) {
			response.code = MS_PARSE_FAILED;
			Utils.logD(TAG, "Parse MsResponse Error: " + e.getMessage());
		}
		return response;
	}

	public JSONObject getJsonObject() {
		return json.optJSONObject(PARAM_RESPONSE);
	}

	public JSONArray getJsonArray() {
		return json.optJSONArray(PARAM_RESPONSE);
	}

	public void showFailInfo(Context ctx, int labelResId) {
		 showInfo(ctx, getFailureDesc(ctx, labelResId, code));
	}

	public void showInfo(Context ctx, CharSequence text) {
		Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
	}

	public void showInfo(Context ctx, int resId) {
		if (resId <= 0) {
			return;
		}
		Toast.makeText(ctx, resId, Toast.LENGTH_SHORT).show();
	}

	public static String getFailureDesc(Context ctx, int labelResId, int code) {
		return ctx.getString(R.string.ms_failure_template,
				ctx.getString(labelResId), getFailureDesc(ctx, code));
	}

	public static String getFailureDesc(Context ctx, int code) {
		int descResId = getFailureDesc(code);
		return descResId == 0 ? String.valueOf(code) : ctx.getString(descResId);
	}

	public static int getFailureDesc(int code) {
		switch (code) {
		case HTTP_NOT_CONNECTED:
			return R.string.no_network;
		case HTTP_TIMEOUT:
			return R.string.connect_timeout;
		case HTTP_NETWORK_ERROR:
			return R.string.network_error;
		case HTTP_INVALID_URL:
			return R.string.invalid_url;
		case MS_CANCELLED:
			return R.string.msr_cancelled;
		case MS_PARSE_FAILED:
			return R.string.msr_parse_failed;

		case MS_UNKNOWN_REQUEST_ACTION:
			return R.string.msr_failed_101;
		case MS_INVALID_REQUEST_TYPE:
			return R.string.msr_failed_102;
		case MS_MISSING_PARAMETER:
			return R.string.msr_missing_parameter;
		case MS_INVALID_PARAMETER:
			return R.string.msr_failed_104;
		case MS_DATABASE_ERROR:
			return R.string.msr_database_error;
		case MS_ATTACHMENT_ERROR:
			return R.string.msr_attachment_error;
		case MS_MAIL_ERROR:
			return R.string.msr_mail_error;
		case MS_MAINTAINING:
			return R.string.msr_maintaining;

		case MS_USER_WRONG_PASSWORD:
			return R.string.prof_old_password_wrong;
		case MS_USER_IDS_NOT_AUTHED:
			return R.string.lgi_wrong_login_info;
		case MS_USER_IDS_NOT_ALLOWED:
			return R.string.fpwd_ids_not_allowed;
		case MS_LOGIN_TOO_FREQUENT:
			return R.string.lgi_failed_too_frequent;
		case MS_IDS_SERVER_ERROR:
			return R.string.lgi_failed_ids_error;

		case MS_USER_INVALID_USERNAME:
			return R.string.reg_format_user_name;
		case MS_USER_EXISTING_USERNAME:
			return R.string.reg_existing_username;
		case MS_USER_WRONG_TOKEN:
			return R.string.lgi_login_expired;
		case MS_USER_NOT_EXIST:
			return R.string.prof_user_not_exist;
		case MS_USER_NOT_AUTHED:
			return R.string.fpwd_login_name_email_not_match;
		case MS_USER_NUMBER_ALREADY_V:
			return R.string.msr_failed_214;
		case MS_USER_ALREADY_CHECKIN_TODAY:
			return R.string.msr_failed_215;
		case MS_USER_INVALID_NICK:
			return R.string.msr_failed_216;
		case MS_USER_VERIFICATION_FAILED:
			return R.string.msr_failed_217;
		case MS_USER_VERIFICATION_EMAIL_USED:
			return R.string.msr_failed_218;

		case MS_COURSE_NO_SEASON_FOUND:
			return R.string.msr_failed_601;

		case MS_BROADCAST_COMMENT_NOT_EXIST:
			return R.string.msr_failed_508;

		case MS_CFORUM_FORUM_NOT_EXIST:
			return R.string.cf_forum_not_exist;
		case MS_CFORUM_THREAD_NOT_EXIST:
			return R.string.cf_post_not_exist;
		case MS_CFORUM_REPLY_NOT_EXIST:
			return R.string.cf_reply_not_exist;
		case MS_CFORUM_NOT_AUTHED:
			return R.string.cf_not_authed;
		case MS_CFORUM_BANNED:
			return R.string.cf_banned;
		case MS_CFORUM_THREAD_LOCKED:
			return R.string.cf_post_locked;
		case MS_CFORUM_TOO_FREQUENT:
			return R.string.cf_too_frequent;

		case MS_CFORUM_NOT_MEMBER:
			return R.string.msr_failed_908;
		case MS_CFORUM_MEMBER_NOT_YET_REQUESTED:
			return R.string.msr_failed_909;
		case MS_CFORUM_MEMBER_ALREADY_REQUESTED:
			return R.string.msr_failed_910;
		case MS_CFORUM_MEMBER_NOT_YET_JOINED:
			return R.string.msr_failed_911;
		case MS_CFORUM_MEMBER_ALREADY_JOINED:
			return R.string.msr_failed_912;
		case MS_CFORUM_FORUM_ALREADY_EXIST:
			return R.string.msr_failed_913;
		case MS_CFORUM_FORUM_CREATE_TOO_MANY:
			return R.string.msr_failed_914;

		case MS_PROMOTION_NOT_EXIST:
			return R.string.msr_failed_1001;
		case MS_USA_NOT_EXIST:
			return R.string.msr_failed_1101;

		case MS_PROMOTION_NOT_AUTHED:
		case MS_USA_NOT_AUTHED:
			return R.string.msr_failed_not_authed;

		case MS_JOB_NOT_AUTHED:
			return R.string.msr_failed_not_authed;
		case MS_JOB_JOB_NOT_EXIST:
			return R.string.msr_failed_1207;
		case MS_JOB_RESUME_NOT_EXIST:
			return R.string.msr_failed_1211;
		case MS_JOB_RESUME_ALREADY_EXIST:
			return R.string.msr_failed_1212;
		case MS_JOB_RESUME_INVALID_STATUS:
			return R.string.msr_failed_invalid_status;
		case MS_JOB_RESUME_NOT_ALLOWED:
			return R.string.msr_failed_not_authed;
		case MS_JOB_OFFER_NOT_EXIST:
			return R.string.msr_failed_1215;
		case MS_JOB_OFFER_ALREADY_EXIST:
			return R.string.msr_failed_1216;
		case MS_JOB_OFFER_INVALID_STATUS:
			return R.string.msr_failed_invalid_status;
		case MS_JOB_OFFER_NOT_ALLOWED:
			return R.string.msr_failed_not_authed;

		case MS_POLICE_USER_READONLY:
			return R.string.msr_failed_1301;
		case MS_POLICE_USER_INACTIVE:
			return R.string.msr_failed_1302;

		case MS_BOUNTY_TASK_NOT_EXIST:
			return R.string.msr_failed_1401;
		case MS_BOUNTY_TASK_INVALID_STATUS:
			return R.string.msr_failed_invalid_status;
		case MS_BOUNTY_TASK_NOT_ALLOWED:
			return R.string.msr_failed_not_authed;
		case MS_BOUNTY_TASK_TOO_MANY:
			return R.string.msr_failed_1404;
		case MS_BOUNTY_CONTRACT_NOT_EXIST:
			return R.string.msr_failed_1405;
		case MS_BOUNTY_CONTRACT_INVALID_STATUS:
			return R.string.msr_failed_invalid_status;
		case MS_BOUNTY_CONTRACT_NOT_ALLOWED:
			return R.string.msr_failed_not_authed;
		case MS_BOUNTY_CONTRACT_TOO_MANY:
			return R.string.msr_failed_1408;
		case MS_BOUNTY_CONTRACT_TOO_MANY_IN_TASK:
			return R.string.msr_failed_1409;
		case FAIL_TRIBE_CREATE_TOO_MANY_ROOMS:
			return R.string.msr_failed_4014;
		case FAIL_TRIBE_ROOM_FULL:
			return R.string.msr_failed_4015;
		case FAIL_TRIBE_ROOM_NOT_PERMIT:
			return R.string.msr_failed_4016;
		case FAIL_TRIBE_ASSIST_TOO_MANY:
			return R.string.msr_failed_4017;

		default:
			return 0;
		}
	}
}
