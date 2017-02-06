package com.tjut.mianliao.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.util.MsTaskListener.MsTaskType;

/**
 * AsyncTask handles MultiPart Http Post to MotherShip. Main purpose is to upload files.
 */
public class MsMhpTask extends MsTask {
    private static final String TAG = "MsMhpTask";

    public static final String LINE_END = "\r\n";
    public static final String TWO_HYPHENS = "--";
    public static final String BOUNDARY = "*b*o*u*n*d*a*r*y*";

    public static final String POST_START = LINE_END;
    public static final String PARAM_START = TWO_HYPHENS + BOUNDARY + LINE_END;
    public static final String PARAM_DESC = "Content-Disposition: form-data; name=\"%1$s\""
            + "%nContent-Type: text/plain;charset=utf-8%n%n";
    public static final String FILE_DESC = "Content-Disposition: form-data; name=\"%1$s\"; "
            + "filename=\"%2$s\"%n%n";
    public static final String PARAM_END = LINE_END;
    public static final String POST_END = TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END;

    public static final int MAX_BUFFER_SIZE = 1 * 1024 * 1024;

    protected HashMap<String, String> mParams;
    protected HashMap<String, String> mFiles;
    private Context mContext;

    public MsMhpTask(Context context, MsTaskType type,
            HashMap<String, String> parameters, HashMap<String, String> files) {
        super(context, type);
        mContext = context;
        init(parameters, files);
    }

    public MsMhpTask(Context context, MsRequest request,
            HashMap<String, String> parameters, HashMap<String, String> files) {
        super(context, request);
        init(parameters, files);
    }

    @Override
    protected MsResponse doInBackground(Void... params) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;

        MsRequest request = getRequest();
        MsResponse response = new MsResponse();
        try {
            URL url = new URL(request.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            HttpUtil.handleHttps(getRefContext(), conn);
            initConn(conn);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(POST_START);

            for (Map.Entry<String, String> entry: mParams.entrySet()) {
                writeParam(dos, entry.getKey(), entry.getValue());

            }

            for (Map.Entry<String, String> entry: mFiles.entrySet()) {
                File sourceFile = new File(entry.getValue());
                if (!sourceFile.isFile()) {
                    continue;
                }
                fis = new FileInputStream(sourceFile);
                writeFile(dos, fis, entry.getKey(), sourceFile.getName());
                fis.close();
            }

            dos.writeBytes(POST_END);

            // Parse response
            String result = HttpUtil.getResponse(conn);

            if (Utils.isDebug()) {
                StringBuilder sbParams = new StringBuilder();
                sbParams.append("Params: ");
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    sbParams.append(PARAM_START);
                    sbParams.append(String.format(Locale.US, PARAM_DESC, entry.getKey()));
                    sbParams.append(entry.getValue() + PARAM_END);
                }
                if (mFiles.size() > 0) {
                    sbParams.append(" Files: ");
                    for (Map.Entry<String, String> entry : mFiles.entrySet()) {
                        sbParams.append(entry.getKey() + ":" + entry.getValue() + ",");
                    }
                }
                Utils.logD(TAG, request.getUrl() + " " + sbParams.toString());
                Utils.logD(TAG, "Response: " + result);
            }

            response = MsResponse.fromJson(result);
        } catch (SocketTimeoutException e) {
            response.code = MsResponse.HTTP_TIMEOUT;
        } catch (MalformedURLException e) {
            Utils.logW(TAG, "MalformedURLException: " + request.getUrl()
                    + " " + e.getMessage());
            response.code = MsResponse.HTTP_INVALID_URL;
        } catch (IOException e) {
            Utils.logW(TAG, "IOException: " + request.getUrl()
                    + " " + e.getMessage());
            response.code = MsResponse.HTTP_NETWORK_ERROR;
        } catch (InterruptedException e) {
            Utils.logW(TAG, "InterruptedException: " + request.getUrl()
                    + " " + e.getMessage());
            response.code = MsResponse.MS_CANCELLED;
        } catch (NullPointerException e) {
            Utils.logW(TAG, "NullPointerException: " + request.getUrl()
                    + " " + e.getMessage());
            response.code = MsResponse.MS_CANCELLED;
        } catch (Exception e) {
            Utils.logW(TAG, e.getCause() + " Exception with : " + request.getUrl()
                    + " " + e.getMessage());
            response.code = MsResponse.MS_CANCELLED;
        } finally {
            try {
                // close the streams
                if (fis != null) {
                    fis.close();
                }
                if (dos != null) {
                    dos.flush();
                    dos.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) { }
        }

        return response;
    }

    public void initConn(HttpURLConnection conn) throws ProtocolException {
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        conn.setConnectTimeout(HttpUtil.DEFAULT_TIMEOUT);
    }

    public void writeParam(DataOutputStream dos, String name, String value)
            throws IOException, InterruptedException {
        if (isCancelled()) {
            throw new InterruptedException("Upload cancelled");
        }
        dos.writeBytes(PARAM_START);
        dos.writeBytes(String.format(Locale.US, PARAM_DESC, name));
        System.out.println("<=== name = " + name + "; value = " + value);
        if (value != null) {
            dos.write(value.getBytes("utf-8"));
        }
        dos.writeBytes(PARAM_END);
    }

    public void writeFile(DataOutputStream dos, FileInputStream fis, String paramName,
            String fileName) throws IOException, InterruptedException {
        if (isCancelled()) {
            throw new InterruptedException("Upload cancelled");
        }
        // create a buffer of maximum size
        int bufferSize = Math.min(fis.available(), MAX_BUFFER_SIZE);
        byte[] buffer = new byte[bufferSize];
        // read file and write it into form...
        int bytesRead = fis.read(buffer, 0, bufferSize);

        dos.writeBytes(PARAM_START);
        dos.write(String.format(Locale.US, FILE_DESC, paramName, fileName).getBytes("utf-8"));
        while (bytesRead > 0) {
            if (isCancelled()) {
                throw new InterruptedException("Upload cancelled");
            }
            dos.write(buffer, 0, bufferSize);
            bufferSize = Math.min(fis.available(), MAX_BUFFER_SIZE);
            bytesRead = fis.read(buffer, 0, bufferSize);
        }
        dos.writeBytes(PARAM_END);
    }

    private void init(HashMap<String, String> parameters, HashMap<String, String> files) {
        mParams = new HashMap<String, String>();
        AccountInfo info = AccountInfo.getInstance(getRefContext());
        if (getRequest().requireAuth()) {
            mParams.put("uid", String.valueOf(info.getUserId()));
            mParams.put("token", info.getToken());
            mParams.put("version", Utils.getPackageInfo(mContext == null ? getRefContext() : mContext).versionName);
        }
        mParams.put("client_os", "android");
        if (parameters != null) {
            mParams.putAll(parameters);
        }

        mFiles = new HashMap<String, String>();
        if (files != null) {
            mFiles.putAll(files);
        }
    }
}
