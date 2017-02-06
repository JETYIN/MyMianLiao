package com.tjut.mianliao.xmpp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.Context;

import com.tjut.mianliao.util.Utils;

/*package*/ class XmppConnector {
    private static final String TAG = "XmppConnector";

    private static final String XMPP_SOURCE = "MianLiao";
    private static final String SERVER = Utils.getChatServerDomain();
    private static final String KEYSTOREPASSWORD = "changeit";
    private static final int PORT = 5223; //normal
//    private static final int PORT = 5232;

    private static final String TRUSTSTORE_FILENMAE = "truststore.bks";
    private static final String KEYSTORE_FILENAME = "keystore.bks";

    private boolean mInitialized;
    private String mTrustStorePath;
    private String mKeyStorePath;
    private Context mContext;
    private SmackAndroid mSmackAndroid;
    private XMPPConnection mConnection;

    private final Object mLock = new Object();

    public XmppConnector(Context context) {
        mContext = context;
        mInitialized = init();
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (mConnection != null) {
            mConnection.removeConnectionListener(listener);
            mConnection.addConnectionListener(listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        if (mConnection != null) {
            mConnection.removeConnectionListener(listener);
        }
    }

    public XMPPConnection getConnection() {
        return mConnection;
    }

    public boolean connect(String user, String token) {
        Utils.logD(TAG, "connect: user = " + user + ", token = " + token);
        if (!mInitialized) {
            return false;
        }

        synchronized (mLock) {
            if (mConnection == null) {
                ConnectionConfiguration config = buildConfig();
                if (config == null) {
                    return false;
                }
                mConnection = new XMPPConnection(config);
            }

            try {
                if (!mConnection.isConnected()) {
                    mConnection.connect();
                }
                if (!mConnection.isConnected()) {
                    return false;
                }
                if (!mConnection.isAuthenticated()) {
                    mConnection.login(user, token, XMPP_SOURCE);
                }
                return mConnection.isAuthenticated();
            } catch (XMPPException e) {
                Utils.logD(TAG, "XMPPException: " + e.getMessage());
            } catch (IllegalStateException e) {
                Utils.logD(TAG, "IllegalStateException: " + e.getMessage());
            } catch (IllegalThreadStateException e) {
                Utils.logD(TAG, "IllegalThreadStateException: " + e.getMessage());
            }
        }

        return false;
    }

    public boolean disconnect() {
        Utils.logD(TAG, "disconnect");
        synchronized (mLock) {
            if (mConnection == null || !mConnection.isConnected()) {
                return true;
            }
            mConnection.disconnect();
            return !mConnection.isConnected();
        }
    }

    public void destroy() {
        if (mSmackAndroid != null) {
            mSmackAndroid.onDestroy();
            mSmackAndroid = null;
        }
    }

    /**
     * Initialize SmackAndroid, config and keystore/truststore
     * This action might take some time, so do not use it in UI thread.
     */
    private boolean init() {
        SmackConfiguration.setPacketReplyTimeout(1000 * 20); // 20 sec
        SmackConfiguration.setLocalSocks5ProxyEnabled(true);
        // negative number means try next port if already in use
        SmackConfiguration.setLocalSocks5ProxyPort(-7777);
        SmackConfiguration.setAutoEnableEntityCaps(true);
        SmackConfiguration.setDefaultPingInterval(30);

        mSmackAndroid = SmackAndroid.init(mContext);
        if (mSmackAndroid == null) {
            return false;
        }
        mTrustStorePath = prepareStoreFile(TRUSTSTORE_FILENMAE);
        if (mTrustStorePath == null) {
            return false;
        }
        mKeyStorePath = prepareStoreFile(KEYSTORE_FILENAME);
        if (mKeyStorePath == null) {
            return false;
        }
        return true;
    }

    private ConnectionConfiguration buildConfig() {
        ConnectionConfiguration config = null;
        try {
            config = new AndroidConnectionConfiguration(SERVER, PORT);
        } catch (XMPPException e) {
            Utils.logW(TAG, e.getMessage());
        } catch (ExceptionInInitializerError e) {
            Utils.logE(TAG, e.getMessage());
        }

        if (config != null) {
            config.setReconnectionAllowed(false);
            config.setSendPresence(true);
            config.setCompressionEnabled(false);
            config.setSecurityMode(SecurityMode.disabled);
            config.setTruststoreType("BKS");
            config.setTruststorePath(mTrustStorePath);
            config.setTruststorePassword(KEYSTOREPASSWORD);
            config.setKeystorePath(mKeyStorePath);
            config.setKeystoreType("BKS");
        }

        return config;
    }

    /**
     * Keystore/truststore is saved in assets, in order to make use of them (we
     * need the file path), we need to make a copy of them.
     */
    private String prepareStoreFile(String fileNmae) {
        File outFile = new File(mContext.getFilesDir(), fileNmae);
        if (outFile.exists()) {
            return outFile.getAbsolutePath();
        }

        InputStream src = null;
        FileOutputStream out = null;
        try {
            src = mContext.getAssets().open(fileNmae);
            out = new FileOutputStream(outFile.getAbsolutePath());
            byte[] buf = new byte[1024];
            int len;
            while ((len = src.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return outFile.getAbsolutePath();
        } catch (IOException e) {
            Utils.logW(TAG, "Error preparing store file: " + e.getMessage());
            outFile.delete();
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (src != null) {
                    src.close();
                }
            } catch (IOException e) {
                Utils.logW(TAG, "Error closing file: " + e.getMessage());
            }
        }
    }
}
