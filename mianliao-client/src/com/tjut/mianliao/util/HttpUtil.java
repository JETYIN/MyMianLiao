package com.tjut.mianliao.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.tjut.mianliao.UserState;
import com.tjut.mianliao.data.AccountInfo;

public class HttpUtil {
    
    private static final String TAG = "HttpUtil";

    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int DEFAULT_READ_TIMEOUT = 10000;

    private static final String DEFAULT_CHARSET_NAME = "UTF-8";

    private static SSLSocketFactory sSslSocketFactory;

    private HttpUtil() {}

    public static String getUrl(Context context, MsRequest request, String parameters) {
        return new StringBuilder(request.getUrl()).append("?")
                .append(getParams(context, request, parameters))
                .toString();
    }

    public static String getParams(Context context, MsRequest request, String parameters) {
        StringBuilder sb = new StringBuilder("client_os=android");
        if (request.requireAuth()) {
            AccountInfo account = AccountInfo.getInstance(context);
            sb.append("&uid=").append(account.getUserId())
                .append("&token=").append(Utils.urlEncode(account.getToken()));
        }
        if (!TextUtils.isEmpty(parameters)) {
            sb.append("&").append(parameters);
        }
        return sb.toString();
    }

    public static void handleHttps(Context ctx, URLConnection connection) {
        if (connection instanceof HttpsURLConnection && (sSslSocketFactory != null || buildSslSocketFactory(ctx))) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sSslSocketFactory);
        }
    }

    /**项目使用的事https协议，需要获取预先下载好的tjut.cer认证证书**/
    public static synchronized boolean buildSslSocketFactory(Context ctx) {
        if (sSslSocketFactory != null) {
            return true;
        }
        try {   
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(ctx.getAssets().open("tjut.cer"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, tmf.getTrustManagers(), null);

            sSslSocketFactory = sslCtx.getSocketFactory();
            return true;
        } catch (Exception e) {
            Utils.logE(TAG, "Error setting up ssl: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static final String get(Context context, String url) throws Exception {
        URL getUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        handleHttps(context, connection);
        connection.setConnectTimeout(DEFAULT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        connection.connect();
        String result = getResponse(connection);
        connection.disconnect();
        return result;
    }

    public static final String post(String url, Context context, Map<String, String> postHeaders,
            String postEntity) throws Exception {
        URL postUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        handleHttps(context, connection);
        connection.setConnectTimeout(DEFAULT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty(" Content-Type ", " application/x-www-form-urlencoded ");

        if (postHeaders != null) {
            for (Entry<String, String> entry : postHeaders.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (postEntity != null) {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(postEntity);
            out.flush();
            out.close(); // flush and close
        }

        String response = getResponse(connection);
        connection.disconnect();
        return response;
    }

    public static String getResponse(HttpURLConnection connection) throws IOException {
        int statusCode = -1;
        try {
            statusCode = connection.getResponseCode();
        } catch (Exception e) {
        }
        BufferedReader bufferedReader = null;
        if (statusCode == HttpsURLConnection.HTTP_OK) {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), DEFAULT_CHARSET_NAME));
        } else if (statusCode != -1) {
            Utils.logD(TAG, "HTTP ERROR: " + statusCode + " : " + connection.getURL());
            bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), DEFAULT_CHARSET_NAME));
        }
        StringBuilder sbStr = new StringBuilder();
        if (bufferedReader != null) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sbStr.append(line);
            }
            bufferedReader.close();
        }
        return new String(sbStr.toString().getBytes(DEFAULT_CHARSET_NAME), DEFAULT_CHARSET_NAME);
    }

    public static final MsResponse msPost(Context ctx, String api, String request,
            String parameters) {
        return msPost(ctx, new MsRequest(api, request, MsRequest.POST, true), parameters);
    }

    /**
     * Helper function to make a post request to MotherShip. It handles necessary authorization.
     */
    public static final MsResponse msPost(Context ctx, MsRequest request, String parameters) {
        MsResponse result = new MsResponse();
        result.request = request;
        String response = null;
        try {
            response = post(request.getUrl(), ctx, null, getParams(ctx, request, parameters));
            JSONObject json = new JSONObject(response);
            Utils.logD(TAG, "msPost " + request.getDesc() + ", " + parameters +
                    ", Response: " + json.toString());
            result.code = json.optInt(MsResponse.PARAM_CODE);
            result.response = json.optString(MsResponse.PARAM_RESPONSE);
            result.json = json;
        } catch (SocketTimeoutException e) {
            result.code = MsResponse.HTTP_TIMEOUT;
        } catch (IOException e) {
            Utils.logD(TAG, "msPost " + request.getDesc() + " IO Error: " + e.getMessage());
            result.code = MsResponse.HTTP_NETWORK_ERROR;
        } catch (JSONException e) {
            Utils.logD(TAG, "msPost " + request.getDesc() + " JSON Error: " + response);
            result.code = MsResponse.MS_PARSE_FAILED;
        } catch (Exception e) {
            Utils.logD(TAG, "msPost " + request.getDesc() + " STATUS Error: " + response);
            result.code = MsResponse.MS_STATUS_ERROR;
        }

        UserState.getInstance().update(result.code);

        return result;
    }

    /**
     * Help handles auth on http post, and initial response analyze.
     *
     * @return A string for the success response, null otherwise.
     */
    public static final String post(Context appCtx, String api, String request, String parameters) {
        MsResponse result = msPost(appCtx, api, request, parameters);
        if (result.code == MsResponse.MS_SUCCESS) {
            return result.response;
        }
        return null;
    }

    public static final MsResponse msGet(Context ctx, String api, String request, String
            parameters) {
        return msGet(ctx, new MsRequest(api, request, MsRequest.GET, true), parameters);
    }

    public static final MsResponse msRequest(Context ctx, MsRequest request, String parameters) {
        if (request.getType() == MsRequest.POST) {
            return msPost(ctx, request, parameters);
        } else {
            return msGet(ctx, request, parameters);
        }
    }

    /**
     * Helper function to make a get request to MotherShip. It handles necessary authorization.
     */
    public static final MsResponse msGet(Context ctx, MsRequest request, String parameters) {
        /**请求成功**/
        MsResponse result = new MsResponse();
        result.request = request;
        String response = null;
        try {
            response = get(ctx, getUrl(ctx, request, parameters));
            JSONObject json = new JSONObject(response);
            Utils.logD(TAG, "msGet " + request.getDesc() + ", " + parameters +
                    ", Response: " + json.toString());
            result.code = json.optInt(MsResponse.PARAM_CODE);
            result.response = json.optString(MsResponse.PARAM_RESPONSE);
            result.json = json;
        } catch (SocketTimeoutException e) {
            result.code = MsResponse.HTTP_TIMEOUT;
        } catch (IOException e) {
            Utils.logD(TAG, "msGet " + request.getDesc() + " IO Error: " + e.getMessage());
            result.code = MsResponse.HTTP_NETWORK_ERROR;
        } catch (JSONException e) {
            Utils.logD(TAG, "msGet " + request.getDesc() + " JSON Error: " + response);
            result.code = MsResponse.MS_PARSE_FAILED;
        } catch (Exception e) {
            Utils.logD(TAG, "msGet " + request.getDesc() + " Status Error: " + response);
            result.code = MsResponse.MS_STATUS_ERROR;
        }

        UserState.getInstance().update(result.code);

        return result;
    }

    /**
     * Help handles auth on http get, and initial response analyze.
     *
     * @return A string for the success response, null otherwise.
     */
    public static final String get(Context appCtx, String api, String request, String parameters) {
        MsResponse result = msGet(appCtx, api, request, parameters);
        if (result.code == MsResponse.MS_SUCCESS) {
            return result.response;
        }
        return null;
    }

    /**
     * @param context
     * @param httpAddress Full address for the download Url.
     * @param fileName    Absolute path of the target location for the download file.
     * @return true if download successful, false otherwise.
     */
    public static boolean downLoad(Context context, String httpAddress, String fileName) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String fileNameTmp = fileName + ".tmp";
        File tmpFile = new File(fileNameTmp);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        try {
            URL url = new URL(httpAddress);
            connection = (HttpURLConnection) url.openConnection();
            handleHttps(context, connection);
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Utils.logW(TAG, "Download file failed: " + fileName + " Response: "
                        + connection.getResponseCode() + " " + connection.getResponseMessage());
                return false;
            }

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(tmpFile.getAbsolutePath());

            byte[] data = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            // verify file before rename.
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            return tmpFile.renameTo(file);
        } catch (Exception e) {
            tmpFile.delete();
            Utils.logW(TAG, "Failed to download file: " + e.getMessage());
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception ignored) { }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }
}
