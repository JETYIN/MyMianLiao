package com.tjut.mianliao.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.ProgressDialog;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.VisibleDelay;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.settings.Settings;

public class Utils {
    private static final String TAG = "Utils";

    private static final long KB = 1024L;
    private static final long MB = 1024L * 1024L;

    private static final int MAX_BITMAP_EDGE = 720;

    private static final String SERVER_PROTOCOL = "https://";

    private static final String PRODUCT_MODEL = "product_model";
    private static final String PRODUCT_VERSION = "product_version";
    private static final String PRODUCT_MANUFACTURER = "product_manufacturer";
    private static final String APP_VERSION = "app_version";

    private static final String PREFIX_CALL = "tel:";
    private static final String PREFIX_SENDTO = "mailto:";

    public static final Pattern URL_PATTERN = Pattern
            .compile("^http(s){0,1}://[a-zA-Z0-9]{3,7}\\.tjut\\.cc(:.*[0-9])?/.*");

    public static final Pattern USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z]\\w{5,31}$");
    public static final Pattern NICK_NAME_PATTERN = Pattern.compile("^\\S{2,}$");
    public static final Pattern PHONE_PATTERN = Pattern.compile("[\\+]?[0-9 .-]+");
    public static final Pattern QQ_PATTERN = Pattern.compile("^\\d{5,15}$");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);
    public static final Pattern REF_FRIEND_PATTERN = Pattern.compile("(@|﹫|＠)\\S+");//(@|﹫|＠)[-A-Z0-9\u4E00-\u9FA5]+
    public static final Pattern AVATAR_PLAY_PATTERN = Pattern.compile("(:|：)\\S+(:|：)");
    public static final Pattern TOPIC_MATCH_PATTERN = Pattern.compile("(#|＃)[^#＃@﹫＠]+?(#|＃)");//@#]+?#
    public static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");
    public static final Pattern NORMAL_INPUT_PATTERN = Pattern.compile("[\u4e00-\u9fa5_a-zA-Z#＃@﹫＠]");
    public static final Pattern ML_SPECIAL_SYMBOL_PATTERN = Pattern.compile("[#＃]");
    public static final Pattern URL_MATCH_PATTERN = Pattern.compile("((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|(www.[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)", Pattern.CASE_INSENSITIVE);
    public static final Pattern CREATE_LIVING_TOPIC = Pattern.compile("([#].{0,}?[#])");
    public static final String COMMA_DELIMITER = ",";
    public static final String COLON_EN = ":";
    public static final String COLON_CN = "：";
    public static final String TO_LOCAL_ADDRESS = "http://to_local/";

    private static final HashMap<EncodeHintType, Object> QR_HINT = new HashMap<EncodeHintType, Object>();
    private static final int DAY_MILLS = 24 * 60 * 60 * 1000;

    private static Settings mSettings;
    private static ProgressDialog mProgressDialog;

    static {
        QR_HINT.put(EncodeHintType.CHARACTER_SET, "utf-8");
        QR_HINT.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        QR_HINT.put(EncodeHintType.MARGIN, 1);
    }

    private static boolean sDebug;
    private static boolean sMtaEnabled;
    private static boolean sUseDevServer;
    private static boolean sUseStgServer;
    private static String sServerDomain;
    private static String sChatServerDomain;
    private static String sJidSuffix;

    private static String sServerAddress;

    private static volatile PackageInfo sPackageInfo;

    private static HashMap<String, String> sProductInfoMap;
    private static String sProductInfo;
    private static String sLeUid;
    private static String sLeSecretKey;
    private static String sLivePushDomain;
    private static String sLivePullDomain;
    private static String sLivePoint;
    private static String sLiveSignKey;
    private static String sLiveUu;

    private Utils() {
    }

    /**
     * Should be called on application.onCreate
     */
    public static void init(Context context) {
        Resources res = context.getResources();
        mSettings = Settings.getInstance(context);
        sDebug = res.getBoolean(R.bool.debug);
        sUseDevServer = res.getBoolean(R.bool.use_dev_server);
        sUseStgServer = res.getBoolean(R.bool.use_stg_server);
        sLeUid = res.getString(R.string.le_uid);
        sLeSecretKey = res.getString(R.string.le_secret_key);
        sLivePushDomain = res.getString(R.string.live_push_domain);
        sLivePullDomain = res.getString(R.string.live_pull_domain);
        sLivePoint = res.getString(R.string.live_point);
        sLiveSignKey = res.getString(R.string.live_sign_key);
        sLiveUu = context.getString(R.string.live_uu);
        if (sUseDevServer) {
            sMtaEnabled = false;
            sServerDomain = res.getString(R.string.server_domain_dev);
            sChatServerDomain = res.getString(R.string.server_domain_dev_chat);
            ;
        } else {
            sMtaEnabled = !sDebug;
            if (sUseStgServer) {
                sServerDomain = res.getString(R.string.server_domain_stg);
                sChatServerDomain = res.getString(R.string.server_domain_stg_chat);
            } else {
                sServerDomain = res.getString(R.string.server_domain_prod);
                sChatServerDomain = res.getString(R.string.server_domain_prod_chat);
            }
        }
        sJidSuffix = "@" + sChatServerDomain;
    }

    public static String getJidSuffix() {
        return sJidSuffix;
    }

    public static boolean isDebug() {
        return sDebug;
    }

    public static String getLivePushDomain() {
        return sLivePushDomain;
    }

    public static String getLivePullDomain() {
        return sLivePullDomain;
    }

    public static String getLivePoint() {
        return sLivePoint;
    }

    public static String getLiveSignKey() {
        return sLiveSignKey;
    }

    public static String getLiveUu() {
        return sLiveUu;
    }

    public static String getChatServerDomain() {
        return sChatServerDomain;
    }

    public static String getServerAddress() {
        if (sServerAddress == null) {
            sServerAddress = SERVER_PROTOCOL + sServerDomain;
            sServerAddress += "/";
        }
        logD(TAG, "server address : " + sServerAddress);
        return sServerAddress;
    }

    public static String getLeUid() {
        return sLeUid;
    }

    public static String getLeSecretKey() {
        return sLeSecretKey;
    }

    public static String getShareServerAddress() {
        return "http://" + sServerDomain + "/";
    }

    public static String getRichMediaRequestUrl(int id) {
        return new StringBuilder(getServerAddress()).append("webapp/view/thread/?id=").append(id).toString();
    }

    public static String getFullUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return Utils.getServerAddress() + url;
    }

    public static String getLineSeparator() {
        return "\n";
    }

    public static PackageInfo getPackageInfo(Context context) {
        context = context == null ? MianLiaoApp.getAppContext() : context;
        if (sPackageInfo == null) {
            String pkgName = context.getPackageName();
            try {
                sPackageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            } catch (NameNotFoundException e) {
            }
        }
        return sPackageInfo;
    }

    public static String getDeviceInfoForWifi(Context ctx) {
        StringBuilder sb = new StringBuilder();
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        sb.append("ip=").append(intToIp(wifiInfo.getIpAddress()));
        sb.append("&mac=").append(wifiInfo.getMacAddress());
        sb.append("&").append(getProductInfo(ctx));
        return sb.toString();
    }

    public static String getDevicesIp(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return intToIp(wifiInfo.getIpAddress());
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    public static synchronized HashMap<String, String> getProductInfoMap(Context context) {
        if (sProductInfoMap == null) {
            sProductInfoMap = new HashMap<String, String>();
            PackageInfo pkgInfo = getPackageInfo(context);
            String version = pkgInfo == null ? "" : pkgInfo.versionName;
            sProductInfoMap.put(APP_VERSION, version);
            sProductInfoMap.put(PRODUCT_MODEL, Build.MODEL);
            sProductInfoMap.put(PRODUCT_MANUFACTURER, Build.MANUFACTURER);
            sProductInfoMap.put(PRODUCT_VERSION, Build.VERSION.RELEASE);
        }
        return sProductInfoMap;
    }

    public static String getProductInfo(Context context) {
        if (sProductInfo == null) {
            StringBuilder sb = new StringBuilder();
            HashMap<String, String> pim = getProductInfoMap(context);
            for (Map.Entry<String, String> entry : pim.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(urlEncode(entry.getValue()));
            }
            sProductInfo = sb.toString();
        }
        return sProductInfo;
    }

    public static boolean getMtaEnabled() {
        return sMtaEnabled;
    }

    public static boolean getUseDevServer() {
        return sUseDevServer;
    }

    public interface SpanCallback {
        public Object createSpan();
    }

    public static boolean verifyNickname(String nick) {
        return nick != null && NICK_NAME_PATTERN.matcher(nick).matches();
    }

    public static boolean verifyEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean verifyPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean verifyQQ(String qq) {
        return qq != null && QQ_PATTERN.matcher(qq).matches();
    }

    public static CharSequence getTimeDesc(long time) {
        long startTime = time > System.currentTimeMillis() ? System.currentTimeMillis() - 61000 : time;
        return DateUtils.getRelativeTimeSpanString(startTime);
    }

    public static CharSequence getTimeDesc(Context context, long time) {
        long startTime = time > System.currentTimeMillis() ? System.currentTimeMillis() - 61000 : time;
        return DateUtils.getRelativeDateTimeString(context, startTime, DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);
    }

    public static CharSequence getRefFriendText(CharSequence src, Context context) {
        Matcher matcher = REF_FRIEND_PATTERN.matcher(src);
        SpannableStringBuilder ssb = SpannableStringBuilder.valueOf(src);
        while (matcher.find()) {
            int start = matcher.start();
            ssb.replace(start, start + 1, "@");
        }
        return Utils.getColoredText(ssb, REF_FRIEND_PATTERN, context.getResources().getColor(R.color.ref_friend));
    }

    public static CharSequence getColoredText(CharSequence src, Pattern pattern, int color) {
        if (TextUtils.isEmpty(src) || pattern == null || !pattern.matcher(src).find()) {
            return src;
        }
        SpannableString ss = SpannableString.valueOf(src);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            ss.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    public static CharSequence getColoredText(CharSequence src, final int color, int start, int end) {
        return getSpannedText(src, new SpanCallback() {
            @Override
            public Object createSpan() {
                return new ForegroundColorSpan(color);
            }
        }, start, end);
    }

    public static CharSequence getUnderlinedText(CharSequence src, int start, int end) {
        return getSpannedText(src, new SpanCallback() {
            @Override
            public Object createSpan() {
                return new UnderlineSpan();
            }
        }, start, end);
    }

    public static CharSequence getSpannedText(CharSequence src, SpanCallback callback, int start, int end) {
        if (TextUtils.isEmpty(src)) {
            return src;
        }

        int len = src.length();
        if (end < start || end < 0 || start > len) {
            return src;
        }

        if (start < 0) {
            start = 0;
        }
        if (end > len) {
            end = len;
        }
        SpannableString ss = SpannableString.valueOf(src);
        ss.setSpan(callback.createSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static CharSequence getSpannedText(CharSequence text, CharSequence key, SpanCallback callback, boolean repeat) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(key)) {
            return text;
        }

        SpannableString ss = SpannableString.valueOf(text);
        String upperText = text.toString().toLowerCase();
        int txtLen = upperText.length();
        String upperKey = key.toString().toLowerCase();
        int keyLen = upperKey.length();
        int start = upperText.indexOf(upperKey);

        while (start >= 0 && start < txtLen) {
            int end = start + keyLen;
            ss.setSpan(callback.createSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = repeat ? upperText.indexOf(upperKey, end) : txtLen;
        }

        return ss;
    }

    public static CharSequence getColoredText(CharSequence text, CharSequence key, final int color) {
        return getColoredText(text, key, color, true);
    }

    public static CharSequence getColoredText(CharSequence text, CharSequence key, final int color, boolean repeat) {
        return getSpannedText(text, key, new SpanCallback() {
            @Override
            public Object createSpan() {
                return new ForegroundColorSpan(color);
            }
        }, repeat);
    }

    public static CharSequence getStyledText(CharSequence text, CharSequence key, final int style, boolean repeat) {
        return getSpannedText(text, key, new SpanCallback() {
            @Override
            public Object createSpan() {
                return new StyleSpan(style);
            }
        }, repeat);
    }

    public static CharSequence getSizedText(CharSequence text, CharSequence key, final int size, boolean repeat) {
        return getSpannedText(text, key, new SpanCallback() {
            @Override
            public Object createSpan() {
                return new AbsoluteSizeSpan(size);
            }
        }, repeat);
    }

    public static boolean deleteFile(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            if (file.isFile()) {
                return file.delete();
            }
        }
        return false;
    }

    public static boolean renameFile(String oldName, String newName) {
        File old = new File(oldName);
        if (old.exists()) {
            File nn = new File(newName);
            return old.renameTo(nn);
        }
        return false;
    }

    public static boolean copy(String srcName, String dstName) {
        File src = new File(srcName);
        File dst = new File(dstName);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return true;
        } catch (FileNotFoundException e) {
            logW(TAG, "copy error: " + e.getMessage());
        } catch (Exception e) {
            logW(TAG, "copy error: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        boolean available = ni != null && ni.isConnected();
        logD(TAG, "isNetworkAvailable: " + available);
        return available;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean shouldDownloadImage(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null
                && ni.isConnected()
                && (ni.getType() == ConnectivityManager.TYPE_WIFI || !Settings.getInstance(context)
                .downloadPicturesWithWifi());
    }

    public static boolean isExtStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getMianLiaoDir() {
        if (isExtStorageAvailable()) {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/MianLiao");
            if (dir.isDirectory() || dir.mkdir()) {
                return dir;
            }
        }
        return null;
    }

    public static Bitmap makeQrCodeBitmap(String info, int size) {
        return makeQrCodeBitmap(info, size, size, Color.BLACK, Color.WHITE);
    }

    public static Bitmap makeQrCodeBitmap(String info, int width, int height, int colorDark, int colorLight) {
        BitMatrix bm;
        try {
            bm = new MultiFormatWriter().encode(info, BarcodeFormat.QR_CODE, width, height, QR_HINT);
        } catch (WriterException e) {
            Utils.logW(TAG, e.getMessage());
            return null;
        } catch (NoSuchMethodError e) {
            Utils.logW(TAG, e.getMessage());
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bm.get(x, y) ? colorDark : colorLight);
            }
        }

        return bmp;
    }

    public static void copyToClipboard(Context context, int labelId, CharSequence text) {
        copyToClipboard(context, context.getString(labelId), text);
    }

    public static void copyToClipboard(Context context, CharSequence label, CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static void actionCall(Context context, String data) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        if (!TextUtils.isEmpty(data)) {
            intent.setData(data.startsWith(PREFIX_CALL) ? Uri.parse(data) : Uri.parse(PREFIX_CALL + data));
        }
        context.startActivity(intent);
    }

    public static void actionSendTo(Context context, String data) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        if (!TextUtils.isEmpty(data)) {
            intent.setData(data.startsWith(PREFIX_SENDTO) ? Uri.parse(data) : Uri.parse(PREFIX_SENDTO + data));
        }
        String title = context.getString(R.string.prof_send_email);
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void actionView(Context context, String data, String type, int flags) {
        Uri uri = TextUtils.isEmpty(data) ? null : Uri.parse(data);
        actionView(context, uri, type, flags);
    }

    public static void actionView(Context context, Uri uri, String type, int flags) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        if (flags > 0) {
            intent.setFlags(flags);
        }
        context.startActivity(Intent.createChooser(intent, null));
    }

    public static void setImage(View parent, int id, int resid) {
        ((ImageView) parent.findViewById(id)).setImageResource(resid);
    }

    public static void setImage(View parent, int id, Drawable drawable) {
        ((ImageView) parent.findViewById(id)).setImageDrawable(drawable);
    }

    public static void setText(View parent, int id, int resid) {
        ((TextView) parent.findViewById(id)).setText(resid);
    }

    public static void setText(View parent, int id, CharSequence text) {
        ((TextView) parent.findViewById(id)).setText(text);
    }

    public static void viewImages(Context context, ArrayList<String> urls, int index) {
        Intent intent = new Intent(context, ImageActivity.class).putExtra(ImageActivity.EXTRA_IMAGE_INDEX, index)
                .putStringArrayListExtra(ImageActivity.EXTRA_IMAGE_URLS, urls);
        context.startActivity(intent);
    }

    public static int generateIdentify(String name) {
        int hashCode = name.hashCode();
        return Math.max(1, Math.abs(hashCode & 0xFFFF));
    }

    public static void downloadFile(final Context context, final String url, final String fileName) {
        new LightDialog(context).setTitleLd(R.string.att_download_dialog_title)
                .setMessage(R.string.att_download_dialog_message).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        dm.enqueue(request);
                    }
                }).show();
    }

    public static boolean unzipFile(String zipPath, String destPath) {
        ZipFile zipFile = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        byte[] buffer = new byte[4096];
        try {
            zipFile = new ZipFile(zipPath);
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                File file = new File(destPath, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.exists() && !file.mkdirs()) {
                        return false;
                    }
                } else {
                    File parentFile = file.getParentFile();
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        return false;
                    }
                    bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    int readLen = 0;
                    while ((readLen = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, readLen);
                    }
                    bis.close();
                    bos.close();
                }
            }
        } catch (IOException e) {
            Utils.logE(TAG, e.getMessage());
            return false;
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
            }
        }

        return true;
    }

    public static HashMap<String, String> getDictFromPlist(InputStream is) {
        HashMap<String, String> dict = null;
        String key = null, value = null;
        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        dict = new HashMap<String, String>();
                        break;

                    case XmlPullParser.START_TAG:
                        if ("key".equals(parser.getName())) {
                            eventType = parser.next();
                            key = parser.getText();
                        } else if ("string".equals(parser.getName())) {
                            eventType = parser.next();
                            value = parser.getText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("string".equals(parser.getName())) {
                            dict.put(key, value);
                        }
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        }

        return dict;
    }

    public static String getAttSizeString(Context context, long size) {
        if (size < KB) {
            return context.getString(R.string.size_bytes, size);
        } else if (size < MB) {
            return context.getString(R.string.size_kilobytes, size / (float) KB);
        } else {
            return context.getString(R.string.size_megabytes, size / (float) MB);
        }
    }

    /**
     * @param src source string to be encoded
     * @return utf-8 encoded string
     */
    public static String urlEncode(String src) {
        if (!TextUtils.isEmpty(src)) {
            try {
                return URLEncoder.encode(src, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return "";
    }

    /**
     * @param src source string to be decoded
     * @return utf-8 decoded string
     */
    public static String urlDecode(String src) {
        if (!TextUtils.isEmpty(src)) {
            try {
                return URLDecoder.decode(src, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logE(TAG, e.getMessage());
            } catch (IllegalArgumentException e) {
                logE(TAG, e.getMessage());
            }
        }
        return "";
    }

    public static String join(CharSequence delimiter, JSONArray ja) {
        StringBuilder sb = new StringBuilder();
        if (ja != null) {
            boolean firstTime = true;
            for (int i = 0; i < ja.length(); i++) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(delimiter);
                }
                sb.append(ja.optString(i));
            }
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, int[] values) {
        StringBuilder sb = new StringBuilder();
        if (values != null) {
            boolean firstTime = true;
            for (int i = 0; i < values.length; i++) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(delimiter);
                }
                sb.append(values[i]);
            }
        }
        return sb.toString();
    }

    public static void showInput(EditText etView) {
        if (etView != null) {
            if (!etView.hasFocus()) {
                etView.requestFocus();
            }

            InputMethodManager imm = (InputMethodManager) etView.getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                imm.showSoftInput(etView, 0);
            }
        }
    }

    public static void hideInput(EditText etView) {
        if (etView != null) {
            InputMethodManager imm = (InputMethodManager) etView.getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etView.getWindowToken(), 0);
        }
    }

    public static void toggleInput(EditText editText, VisibleDelay vd) {
        if (editText == null || vd == null) {
            return;
        }

        if (!editText.hasFocus()) {
            editText.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (vd.isVisible()) {
            vd.setVisibleDelayed(false);
            if (imm.isAcceptingText()) {
                imm.toggleSoftInputFromWindow(editText.getWindowToken(), 0, 0);
            }
        } else {
            vd.setVisibleDelayed(true);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void dispatchDelEvent(EditText etView) {
        etView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        etView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }

    public static void showGpsHintDialog(final Context context) {
        new LightDialog(context).setTitleLd(R.string.around_gps_hint_title).setMessage(R.string.around_gps_hint_desc)
                .setNegativeButton(R.string.setting_no_hint, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.getInstance(context).setGpsHint(false);
                    }
                }).setPositiveButton(R.string.setting_set_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).show();
    }

    public static void pickAttachment(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intentHanlderExists(activity, intent)) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(activity, R.string.att_install_file_picker, Toast.LENGTH_SHORT).show();
        }
    }

    public static int[] getImageSize(String pathName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        return new int[]{opts.outWidth, opts.outHeight};
    }

    public static boolean isImageSizeExceeded(String pathName, int targetSize) {
        int[] size = getImageSize(pathName);
        return Math.max(size[0], size[1]) > targetSize;
    }

    public static Bitmap fileToBitmap(String pathName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        opts.inSampleSize = Math.max(opts.outWidth, opts.outHeight) / MAX_BITMAP_EDGE;
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, opts);
    }

    private static boolean intentHanlderExists(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isNewerThanKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isNewerThanKitKat) {
            return getPathNew(context, uri);
        } else {
            return getPathOld(context, uri);
        }
    }

    private static String getPathOld(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPathNew(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Toast.makeText(context, R.string.att_error_invalid, Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isToday(long time) {
        long day = time / DAY_MILLS;
        long today = System.currentTimeMillis() / DAY_MILLS;

        return day == today;
    }

    /**
     * This log will be turned off if not in debug mode.
     */
    public static void logD(String tag, String msg) {
        if (sDebug) {
            Log.d(tag, msg == null ? "" : msg);
        }
    }

    /**
     * This log will be turned off if not in debug mode.
     */
    public static void logW(String tag, String msg) {
        if (sDebug) {
            Log.w(tag, msg == null ? "" : msg);
        }
    }

    public static void logE(String tag, String msg) {
        Log.e(tag, msg == null ? "" : msg);
    }

    /**
     * style : 1 --> 00:00 ; 2 --> 00'00"
     *
     * @param time
     * @param style while style is 1,it
     * @return style-->2 :while time of minute less than 1 ,while return second
     */
    public static String getTimeStrByInt(int time, int style) {
        /**录制74s**/
        int timeMin = time / 60;
        int timeSec = time % 60;
        String minStr, secStr;
        minStr = timeMin >= 10 ? String.valueOf(timeMin) : "0" + timeMin;
        secStr = timeSec >= 10 ? String.valueOf(timeSec) : "0" + timeSec;
        if (style == 1) {
            return new StringBuilder().append(minStr).append(":").append(secStr).toString();
        } else {
            if (timeMin == 0) {
                return new StringBuilder().append(timeSec).append("\"").toString();
            }
            return new StringBuilder().append(timeMin).append("'").append(timeSec).append("\"").toString();
        }
    }

    /**
     * get MianLiao Dir absolutepath
     *
     * @return
     */
    public static String getFileUnzipPath() {
        return getMianLiaoDir().getAbsolutePath();
    }

    public static String getElementUnzipfilePath(String url) {
        StringBuilder sb = new StringBuilder(getFileUnzipPath()).append(File.separator);
        sb.append(getFileNameByUrl(url));
        return sb.toString();
    }

    public static String getFileNameByUrl(String url) {
        StringBuilder sb = new StringBuilder();
        int indexSep = url.lastIndexOf(File.separator);
        int indexDot = url.lastIndexOf(".");
        if (indexSep != -1 && indexDot != -1) {
            sb.append(url.substring(indexSep + 1, indexDot));
        } else {
            sb.append(url);
        }
        return sb.toString();
    }

    public static File getFileByPath(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            for (File fi : file.listFiles()) {
                return getFileByPath(fi.getAbsolutePath());
            }
        }
        return file;
    }

    public static String getDistanceDes(int distance) {
        distance = distance <= 0 ? 0 : distance;
        DecimalFormat df = new DecimalFormat(".#");
        String dis;
        if (distance >= 0 && distance <= 10) {
            dis = "10m内";
        } else if (distance < 1000) {
            dis = distance + "m";
        } else if (distance <= 1000 * 1000) {
            double dist = distance / 1000.0;
            dis = df.format(dist) + "km";
        } else {
            dis = "月球上";
        }
        return dis;
    }

    public static Object FileToObject(String fileName) {
        Object object = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            String basePath = getMianLiaoDir().getAbsolutePath();
            fis = new FileInputStream(basePath + "/" + fileName);
            ois = new ObjectInputStream(fis);
            object = ois.readObject();
            logD(TAG, "FileToObject:" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    public static void ObjectToFile(Object obj, String fileName) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            String basePath = getMianLiaoDir().getAbsolutePath();
            fos = new FileOutputStream(basePath + "/" + fileName);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            logD(TAG, "ObjectToFile:" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static boolean isSameDay(long milliseconds) {
        Calendar compar = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        compar.setTimeInMillis(milliseconds);
        int comparDay = compar.get(Calendar.DAY_OF_MONTH);
        int currentDay = current.get(Calendar.DAY_OF_MONTH);
        if (comparDay == currentDay) {

            return true;
        }
        return false;
    }

    public static boolean isSameDay(long millisecondsL, long millisecondsR) {
        Calendar calendarL = Calendar.getInstance();
        Calendar calendarR = Calendar.getInstance();
        calendarL.setTimeInMillis(millisecondsL);
        calendarR.setTimeInMillis(millisecondsR);
        int lDay = calendarL.get(Calendar.DAY_OF_MONTH);
        int rDay = calendarR.get(Calendar.DAY_OF_MONTH);
        if (lDay == rDay) {
            return true;
        }
        return false;
    }

    /**
     * this method can return a time string like 2015-08-15 or 08:30 or
     * 2015-08-15 08:15 when you give a type and milliseconds
     *
     * @param type
     * @param milliseonds
     * @return it will return a time string like 08:30 while the type is 1, or,
     * it return a string like 2015-08-15 while the type is 2, or, it
     * return a string like 2015-08-15 08:32 while the type is 3,or, it
     * return a string like 08-25 13:20 while the type is other number
     */
    @SuppressLint("SimpleDateFormat")
    public static String getTimeString(int type, long milliseonds) {
        SimpleDateFormat sdf;
        switch (type) {
            case 1:
                sdf = new SimpleDateFormat("HH:mm");
                break;
            case 2:
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case 3:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                break;
            case 4:
                sdf = new SimpleDateFormat("MM-dd HH:mm");
                break;
            case 5:
                sdf = new SimpleDateFormat("yyyy年M月d日");
                break;
            case 6:
                sdf = new SimpleDateFormat("yyyy年M月d日 HH:mm");
                break;
            case 7:
                sdf = new SimpleDateFormat("MM.dd");
                break;
            case 8:
                sdf = new SimpleDateFormat("MM-dd");
                break;
            case 9:
                sdf = new SimpleDateFormat("yyyy.MM.dd");
                break;
            case 10:
                sdf = new SimpleDateFormat("HH:mm:ss");
                break;
            default:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                break;
        }
        return sdf.format(new Date(milliseonds));
    }

    public static CharSequence getPostShowTimeString(long milliseonds) {
        long timeDec = System.currentTimeMillis() - milliseonds;
        long mill4hour = 3600 * 1000;
        if (timeDec < 60 * 1000) {
            return "刚刚";
        } else if (timeDec < mill4hour * 3) {
            return getTimeDesc(milliseonds);
        } else if (timeDec < mill4hour * 24) {
            if (isSameDay(milliseonds, System.currentTimeMillis())) {
                return getTimeString(1, milliseonds);
            } else {
                return getTimeString(4, milliseonds);
            }
        } else {
            return getTimeString(4, milliseonds);
        }
    }

    public static int getCurrentWeekOfYear() {
        Calendar calendarY = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.getTime().getDay();
        calendar.set(calendar.getTime().getYear() + 1900, 0, 1, 0, 0, 0);
        return (int) Math
                .ceil((calendarY.getTimeInMillis() - calendar.getTimeInMillis() + day * 86400000.0) / 86400000.0 / 7.0);
    }

    public static boolean isMianLiaoService(UserInfo userInfo) {
        if (userInfo != null && userInfo.account != null) {
            if (userInfo.account.startsWith("mlserv")) {
                return true;
            }
        }
        return false;
    }

    /**
     * default image size 600 * 600 60p 70q (ali image)
     *
     * @param url
     * @return
     */
    public static String getImagePreviewSmall(String url) {
        return AliImgSpec.POST_THUMB_SQUARE.makeUrl(url);
    }

    public static void showProgressDialog(Context context, String mContent) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setDialogContent(mContent);
        mProgressDialog.show();
    }

    public static void showProgressDialog(Context context, int mResId) {
        if (context == null || mResId <= 0) {
            return;
        }
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setDialogContent(mResId);
        mProgressDialog.show();
    }

    public static void hidePgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static boolean isRunningBackground(Context context) {
        android.app.ActivityManager activityManager = (android.app.ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        // get the info from the currently running task
        List<android.app.ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        String topActivityPackageName = taskInfo.get(0).topActivity.getPackageName();
        return (!context.getPackageName().equals(topActivityPackageName));
    }

    public static int getDisplayWidth() {
        Context context = MianLiaoApp.getAppContext();
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeight() {
        Context context = MianLiaoApp.getAppContext();
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getDisplayCellPixels() {
        Context context = MianLiaoApp.getAppContext();
        return context.getResources().getDimensionPixelSize(R.dimen.contact_info_margin_bottom);
    }

    public static void toast(CharSequence content) {
        Context context = MianLiaoApp.getAppContext();
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static long getFileSize(Context context, String filePath) {
        long size = 0;
        try {
            File file = new File(filePath);
            FileInputStream stream = new FileInputStream(file);
            size = stream.available();
        } catch (IOException e) {
            e.printStackTrace();
            logE(TAG, "get file size error ： " + e.getMessage());
        }
        return size;
    }

    public static int getMediaFileDuration(Context context, String filePath) {
        if (filePath == null || filePath.length() <= 0) {
            return 0;
        }
        int duration = 0;
        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(filePath);
            player.prepare();
            duration = player.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            logE(TAG, "get media file duration error : " + e.getMessage());
        }
        return duration;
    }

    public static int getYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DATE);
    }

    /**
     * @param fileName
     * @return file's postfix like '.jpg/.png'
     */
    public static String getFilePostfix(String fileName) {
        System.out.println("-------------------filename = " + fileName);
        int index = fileName.lastIndexOf(".");
        System.out.println("-------------------index" + index);
        return fileName.substring(index, fileName.length());
    }

    public static boolean saveBitmap(Bitmap bitmap, String fileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            return true;
        } catch (IOException e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    public static boolean isHasAt(String content) {
        if (content == null || content.length() == 0) {
            return false;
        }
        return content.contains("@");
    }

    public static boolean isHasTopic(String content) {
        if (content == null || content.length() == 0) {
            return false;
        }
        return content.contains("#");

    }

    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static boolean isNumber(CharSequence input) {
        return NUMBER_PATTERN.matcher(input).matches();
    }

    public static boolean contains(String content, CharSequence subStr) {
        if (content == null || subStr == null) {
            logE(TAG, "data error with null");
            return false;
        }
        if (content.contains(subStr)) {
            return true;
        }
        return false;
    }

    public static String getFormatNum(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 10000) {
            return String.format("%.1f", count * 1.0 / 1000) + "k";
        } else {
            return String.format("%.1f", count * 1.0 / 10000) + "w";
        }
    }

    public static <T> ArrayList<T> copy(ArrayList<T> datas) {
        ArrayList<T> copyData = new ArrayList<>();
        for (T t : datas) {
            copyData.add(t);
        }
        return copyData;
    }

    public static String parseJid(String jid) {
        int index = jid.indexOf("@");
        if (index == -1) {
            return jid + Utils.getJidSuffix();
        }
        jid = jid.replace(jid.substring(index, jid.length()), Utils.getJidSuffix());
        return jid;
    }

    public static boolean isNormalContent(CharSequence input) {
        return NORMAL_INPUT_PATTERN.matcher(input).matches();
    }

    public static boolean isMlSpecSymnol(CharSequence input) {
        return ML_SPECIAL_SYMBOL_PATTERN.matcher(input).matches();
    }

    public static boolean isMatcherAt(CharSequence input) {
        return REF_FRIEND_PATTERN.matcher(input).find();
    }

    public static boolean isMatcherTopic(CharSequence input) {
        return TOPIC_MATCH_PATTERN.matcher(input).find();
    }

    public static byte[] BitmapBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
