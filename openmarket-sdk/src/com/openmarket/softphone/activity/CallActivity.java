// $Id$
// Copyright 2010 OpenMarket Limited
package com.openmarket.softphone.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.openmarket.softphone.util.DeviceUtilities;
import com.openmarket.softphone.view.CameraPreview;
import com.openmarket.softphone.view.GLRemoteVideoView;
import com.openmarket.softphone.PhoneCall.CameraOrientation;
import com.openmarket.softphone.PhoneCall.CameraType;
import com.openmarket.softphone.PhoneCall.Type;
import com.openmarket.softphone.PhoneCall;
import com.openmarket.softphone.PhoneCallListener;
import com.openmarket.softphone.PhoneException;
import com.openmarket.softphone.PhoneManager;
import com.openmarket.softphone.R;
import com.openmarket.softphone.User;

public class CallActivity extends Activity {
    private static final String LOG_TAG = "CallActivity";

    public static final String ACTION_INCOMING = "com.openmarket.softphone.CallActivity.INCOMING";
    public static final String ACTION_CALL = "com.openmarket.softphone.CallActivity.CALL";

    public static final String EXTRA_USERS = "users";
    public static final String EXTRA_USER = "user";
    public static final String EXTRA_TYPE = "type";

    private static final long OVERLAY_ANIMATION_DURATION = 500;

    private static final String WAKELOCK_KEY = "CALL_WAKE_LOCK";
    private static PowerManager.WakeLock mWakeLock;

    private enum CallTerminatedReason {
        UNDEFINED,
        LOCAL_REJECTED,
        LOCAL_HANGUP,
        REMOTE_BUSY,
        REMOTE_UNAVAILABLE,
        REMOTE_REJECTED,
        REMOTE_HANGUP,
        ANSWERED_ELSEWHERE,
        ERROR,
        POOR_QUALITY,
    }

    private enum CallState {
        NO_CALL,
        CONNECTING,
        RINGING,
        RINGING_REMOTE,
        IN_PROGRESS,
        TERMINATED,
        FAILED,
    }

    private enum PresentationState {
        HIDDEN,
        HIDING,
        PRESENTED,
        PRESENTING,
    }

    private PhoneManager mPM;

    private PhoneCall mCurrentCall;
    private PhoneCall.Type mCurrentCallType;
    private List<User> mCurrentCallPeers;
    private CallState mCallState;
    private Date mCallStartTime;
    private boolean mCallWasAnswered;
    private CallTerminatedReason mAppTermReason;
    private boolean mOverlayIsOn;
    private PresentationState mOverlayPresentationState;
    private boolean mTransitionalOverlayOn;

    private CameraPreview mCameraPreview;
    private GLRemoteVideoView mRemoteVideoView;

    private View mTouchLockOverlay;

    private LinearLayout mTopOverlay;
    private TextView mNameText;
    private TextView mStatusText;

    private LinearLayout mMiddleOverlay;
    private ToggleButton mMuteButton;
    private ToggleButton mSpeakerButton;
    private Button mVideoToggleButton;
    private Button mAddUserButton;

    private LinearLayout mBottomOverlay;
    private Button mLeftBottomButton;
    private Button mRightBottomButton;
    private Button mSingleBottomButton;

    private Runnable mStatusUpdateTask;
    private final Handler mStatusUpdateHandler = new Handler();
    private final Handler mDelayedHandler = new Handler();

    private Ringtone mRingtone;
    private ToneGenerator mToneGenerator;
    private boolean mInboundRinging;
    private boolean mOutboundRinging;
    private boolean mMuted;
    private boolean mSpeakerPhone;
    private int mAudioStream = (DeviceUtilities.isGalaxyTab()) ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL;

    private MyOrientationEventListener mOrientationEventListener;
    // the current orientation we are using to describe the video we are sending to the peer
    private PhoneCall.CameraOrientation mCurrentOri = PhoneCall.CameraOrientation.UNKNOWN;


    private SensorEventListener mProximitySensorListener;
    private boolean mProximityMonitoringOn = false;
    
    private static boolean enableVideoToggle = false;
    private static boolean enableAddUser = false;
    
    private double mPlaybackVolume = 0.5; // This is only used for the galaxy tab 10.1, but could be used for other devices.
        
    public static void setVideoToggleEnable(boolean e) {
        enableVideoToggle = e;
    }
    
    public static void setAddUserEnable(boolean e) {
        enableAddUser = e;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.call);

        mCameraPreview = (CameraPreview)findViewById(R.id.call_camerapreview);
        mRemoteVideoView = (GLRemoteVideoView)findViewById(R.id.call_remotevideoview);
        mCameraPreview.setZOrderMediaOverlay(true);
        mRemoteVideoView.setZOrderMediaOverlay(false);

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int previewWidth = width / 3;
        int previewHeight = (previewWidth * 4) / 3;
        float scale = CallActivity.this.getResources().getDisplayMetrics().density;
        int margin = (int)(10 * scale + 0.5f);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(previewWidth, previewHeight, Gravity.BOTTOM | Gravity.LEFT);
        layoutParams.setMargins(margin, margin, margin, margin);
        mCameraPreview.setLayoutParams(layoutParams);

        mCameraPreview.setListener(new CameraPreview.CameraPreviewListener() {
            @Override
            public void onPreviewSizeChanged(int width, int height, int padding) {
                Display display = getWindowManager().getDefaultDisplay();
                int displayWidth = display.getWidth();
                int displayHeight = display.getHeight();

                int optimalPreviewWidth = displayWidth / 3;
                int lowerPreviewWidth = displayWidth / 4;
                int upperPreviewWidth = displayWidth * 5 / 12;
                int optimalPreviewHeight = displayHeight / 3;
                int lowerPreviewHeight = displayHeight / 4;
                int upperPreviewHeight = displayHeight * 5 / 12;

                int previewWidth = 0;
                int previewHeight = 0;

                if (width > lowerPreviewWidth && height > lowerPreviewHeight) {
                    int divisor = 1;
                    int bestDiffFromOptimal = Integer.MAX_VALUE;
                    while (true) {
                        if (width / divisor < lowerPreviewWidth && height / divisor < lowerPreviewHeight) {
                            break;
                        } else if ((width % divisor == 0) && (height % divisor == 0)) {
                            int dPreviewWidth = width / divisor;
                            int dPreviewHeight = height / divisor;

                            if (dPreviewWidth >= lowerPreviewWidth && dPreviewWidth <= upperPreviewWidth) {
                                int thisDiffFromOptimal = Math.abs((int)dPreviewWidth - optimalPreviewWidth) + 
                                                          Math.abs((int)dPreviewHeight - optimalPreviewHeight);
                                if (thisDiffFromOptimal < bestDiffFromOptimal) {
                                    previewWidth = (int)dPreviewWidth;
                                    previewHeight = (int)dPreviewHeight;
                                    bestDiffFromOptimal = thisDiffFromOptimal;
                                }
                            }
                        }

                        divisor++;
                    }
                } else {
                    int multiplier = 1;
                    int bestDiffFromOptimal = Integer.MAX_VALUE;
                    while (true) {
                        int dPreviewWidth = width * multiplier;
                        int dPreviewHeight = height * multiplier;

                        if (dPreviewWidth > upperPreviewWidth || dPreviewHeight > upperPreviewHeight) {
                            break;
                        } else {
                            int thisDiffFromOptimal = Math.abs((int)dPreviewWidth - optimalPreviewWidth) + 
                                                      Math.abs((int)dPreviewHeight - optimalPreviewHeight);
                            if (thisDiffFromOptimal < bestDiffFromOptimal) {
                                previewWidth = (int)dPreviewWidth;
                                previewHeight = (int)dPreviewHeight;
                                bestDiffFromOptimal = thisDiffFromOptimal;
                            }
                        }

                        multiplier++;
                    }
                }

                if (previewWidth == 0) {
                    if ((width >= lowerPreviewWidth && width <= upperPreviewWidth) && 
                        (height >= lowerPreviewHeight && height <= upperPreviewHeight))
                    {
                        previewWidth = width;
                        previewHeight = height;
                    } else {
                        previewWidth = optimalPreviewWidth;
                        previewHeight = (previewWidth * height) / width;
                    }
                }

                float scale = CallActivity.this.getResources().getDisplayMetrics().density;
                int margin = (int)(10 * scale + 0.5f);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(previewWidth+(padding*2), previewHeight+(padding*2), Gravity.BOTTOM | Gravity.LEFT);
                layoutParams.setMargins(margin, margin, margin, margin);
                mCameraPreview.setLayoutParams(layoutParams);
            }
        });

        mTouchLockOverlay = (View)findViewById(R.id.call_touchlockoverlay);
        mTouchLockOverlay.setOnTouchListener(new MyTouchLockTouchListener());

        mPM = PhoneManager.getInstance();

        mOverlayIsOn = true;
        mOverlayPresentationState = PresentationState.PRESENTED;
        mTransitionalOverlayOn = false;

        mRemoteVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    CallActivity.this.toggleOverlay();
                }
                return true;
            }
        });
        
        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    CallActivity.this.mCameraPreview.toggleCamera();
                }
                return true;
            }
        });

        mOrientationEventListener = new MyOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);

        mProximitySensorListener = new MyProximityEventListener();

        mTopOverlay = (LinearLayout)findViewById(R.id.call_top_overlay);
        mNameText = (TextView)findViewById(R.id.call_name_text);
        mStatusText = (TextView)findViewById(R.id.call_status_text);

        mMiddleOverlay = (LinearLayout)findViewById(R.id.call_middle_overlay);
        mMuteButton = (ToggleButton)findViewById(R.id.call_mute_btn);
        mSpeakerButton = (ToggleButton)findViewById(R.id.call_speaker_btn);
        mVideoToggleButton = (Button)findViewById(R.id.toggleVideo);
        mAddUserButton = (Button)findViewById(R.id.addUser);

        mBottomOverlay = (LinearLayout)findViewById(R.id.call_bottom_overlay);
        mLeftBottomButton = (Button)findViewById(R.id.call_bottom_btn_left);
        mRightBottomButton = (Button)findViewById(R.id.call_bottom_btn_right);
        mSingleBottomButton = (Button)findViewById(R.id.call_bottom_btn_single);

        // Ignore presses when held to the face 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);

        setVolumeControlStream(mAudioStream);
        mRingtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        mToneGenerator = new ToneGenerator(mAudioStream, ToneGenerator.MAX_VOLUME);
        mInboundRinging = false;
        mOutboundRinging = false;
        
        mVideoToggleButton.setVisibility(enableVideoToggle ? View.VISIBLE : View.GONE);
        mAddUserButton.setVisibility(enableAddUser ? View.VISIBLE : View.GONE);

        sendBroadcast(new Intent("com.tjut.mianliao.xmpp.call.calling"));
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (mCameraPreview != null) {
            mCameraPreview.onResume();
        }

        if (mRemoteVideoView != null) {
            mRemoteVideoView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCameraPreview != null) {
            mCameraPreview.onPause();
        }

        if (mRemoteVideoView != null) {
            mRemoteVideoView.onPause();
        }
        
        if (mCurrentCall != null) {
            // You may not wish to hang up the call here if other activities can be displayed
            // whilst the call is ongoing
            // hangupCall(CallTerminatedReason.LOCAL_HANGUP);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("com.tjut.mianliao.xmpp.call.finished"));
        if (mToneGenerator != null) {
            mToneGenerator.release();
            mToneGenerator = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Manually handle configuration changes.
     * Inspiration taken from Android's Phone app by setting:
     *   android:configChanges="orientation|keyboardHidden"
     * such that we don't get destroyed and recreated on those config changes
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleOverlay();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(DeviceUtilities.isGalaxyTab101()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && mCurrentCall != null) {
                mPlaybackVolume -= 0.1f;
                if(mPlaybackVolume < 0) {
                    mPlaybackVolume = 0;
                }
                mCurrentCall.setAudioPlaybackVolume(mPlaybackVolume);
            }
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && mCurrentCall != null) {
                mPlaybackVolume += 0.1f;
                if(mPlaybackVolume > 1) {
                    mPlaybackVolume = 1;
                }
                mCurrentCall.setAudioPlaybackVolume(mPlaybackVolume);
            }
        }
        return super.onKeyDown(keyCode, event);
    } 

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        
        String action = intent.getAction();
        if (action.equals(ACTION_CALL)) {
            
            List<User> peers;
            if (intent.hasExtra(EXTRA_USERS)) {
                peers = intent.getParcelableArrayListExtra(EXTRA_USERS);
            } else {
                if (!intent.hasExtra(EXTRA_USER)) {
                    Log.e(LOG_TAG, "Tried to start a call with no peers.");
                }

                peers = new ArrayList<User>();
                peers.add((User)intent.getParcelableExtra(EXTRA_USER));
            }

            Type callType = (Type)intent.getSerializableExtra(EXTRA_TYPE);
            try {
                mCurrentCall = mPM.placeCall(peers, callType, new MyPhoneCallListener());
                if (mCurrentCall != null) {
                    mCurrentCall.setCameraPreview(mCameraPreview);
                    mCurrentCall.setRemoteVideoView(mRemoteVideoView);
                    mCurrentCallType = mCurrentCall.getType();
                    mCurrentCallPeers = mCurrentCall.getPeers();
                    setCallState(CallState.CONNECTING);
                    startCall();
                }
            }
            catch (PhoneException e) {
                // XXX: ERROR
                Log.e(LOG_TAG, "Caught phone exception whilst placing call", e);
            }
        } else if (action.equals(ACTION_INCOMING)) {
            mCurrentCall = mPM.takeCall(intent, new MyPhoneCallListener());
            if (mCurrentCall != null) {
                mCurrentCall.setCameraPreview(mCameraPreview);
                mCurrentCall.setRemoteVideoView(mRemoteVideoView);
                mCurrentCallType = mCurrentCall.getType();
                mCurrentCallPeers = mCurrentCall.getPeers();
                setCallState(CallState.RINGING);
                startCall();
            }
        } else {
            // XXX: ERROR
            Log.e(LOG_TAG, "Unexpected intent action in handleIntent.");
        }
        
        // Update the UI if we had an exception or placeCall returned null.
        if (mCurrentCall == null) {
            setCallState(CallState.FAILED);
            stopCall();
        }
    }

    private void setCallState(CallState callState) {
        mCallState = callState;

        if (callState == CallState.RINGING) {
            setInboundRinging(true);
        } else if (isInboundRinging()) {
            setInboundRinging(false);
        }
        if (callState == CallState.RINGING_REMOTE) {
            setOutboundRinging(true);
        } else {
            setOutboundRinging(false);
        }

        updateStatusLabel();
        updateBottomBar();
        setupProximityMonitoring();
        updateButtons();
    }

    private void updateStatusLabel() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (mCurrentCallPeers != null && !mCurrentCallPeers.isEmpty()) {
                    mNameText.setText(mCurrentCallPeers.get(0).getName());
                } else {
                    mNameText.setText("");
                }
                switch (mCallState) {
                    case NO_CALL:
                        mStatusText.setText(R.string.om_not_in_call);
                        break;
                    case CONNECTING:
                        mStatusText.setText(R.string.om_connecting);
                        break;
                    case RINGING:
                    case RINGING_REMOTE:
                        mStatusText.setText(R.string.om_ringing);
                        break;
                    case IN_PROGRESS:
                        break;
                    case TERMINATED:
                        mStatusText.setText(termReasonString());
                        break;
                    case FAILED:
                        mStatusText.setText(R.string.om_call_failed);
                        break;
                }
            }
        });
    }

    private void updateBottomBar() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                boolean doubleOn = true;
                if (mCallState == CallState.RINGING) {
                    mLeftBottomButton.setText(R.string.om_reject);
                    mRightBottomButton.setText(R.string.om_accept);
                } else if (mCallState == CallState.RINGING_REMOTE ||
                           mCallState == CallState.CONNECTING) {
                    mSingleBottomButton.setText(R.string.om_end_call);
                    doubleOn = false;
                } else if ((mCallState == CallState.TERMINATED && mCallWasAnswered) || 
                           mCallState == CallState.NO_CALL) {
                    mSingleBottomButton.setText(R.string.om_return_back);
                    doubleOn = false;
                } else if (mCallState == CallState.FAILED || 
                           (mCallState == CallState.TERMINATED && !mCallWasAnswered)) {
                    mLeftBottomButton.setText(R.string.om_call_back);
                    mRightBottomButton.setText(R.string.om_return_back);
                } else {
                    mLeftBottomButton.setText(R.string.om_end_call);
                    if (mCurrentCallType == PhoneCall.Type.VOICE) {
                        mSingleBottomButton.setText(R.string.om_end_call);
                        doubleOn = false;
                    } else {
                        mRightBottomButton.setText(R.string.om_return_back);
                    }
                }
                if (doubleOn) {
                    mSingleBottomButton.setVisibility(View.GONE);
                    mLeftBottomButton.setVisibility(View.VISIBLE);
                    mRightBottomButton.setVisibility(View.VISIBLE);
                } else {
                    mSingleBottomButton.setVisibility(View.VISIBLE);
                    mLeftBottomButton.setVisibility(View.GONE);
                    mRightBottomButton.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void updateButtons() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (mCurrentCall != null && mCurrentCall.getType() == Type.VIDEO) {
                    mVideoToggleButton.setText(R.string.om_disable_video);
                } else {
                    mVideoToggleButton.setText(R.string.om_enable_video);
                }
                
                switch (mCallState) {
                case IN_PROGRESS:
                    mVideoToggleButton.setEnabled(true);
                    break;
                case NO_CALL:
                case CONNECTING:
                case RINGING:
                case RINGING_REMOTE:
                case TERMINATED:
                case FAILED:
                    mVideoToggleButton.setEnabled(false);
                    break;
                }
            }
        });
    }

    private void setOverlayOn(final boolean overlayOn, final boolean animated) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                boolean newOverlayOn = overlayOn;
                if(mCurrentCallType == PhoneCall.Type.VOICE) newOverlayOn = true;
                if(mCallState != CallState.IN_PROGRESS) newOverlayOn = true;

                AnimationSet topAnimation = null;
                AnimationSet middleAnimation = null;
                AnimationSet bottomAnimation = null;
                if (newOverlayOn && 
                    (mOverlayPresentationState == PresentationState.HIDDEN || 
                     mOverlayPresentationState == PresentationState.HIDING))
                {
                    mOverlayPresentationState = PresentationState.PRESENTING;

                    Animation animation = null;

                    topAnimation = new AnimationSet(true);
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    );
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION : 0);
                    topAnimation.addAnimation(animation);

                    middleAnimation = new AnimationSet(true);
                    animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION : 0);
                    middleAnimation.addAnimation(animation);

                    bottomAnimation = new AnimationSet(true);
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    );
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION : 0);
                    bottomAnimation.addAnimation(animation);

                    topAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            mTopOverlay.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {}
                    });
                    middleAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            mMiddleOverlay.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {}
                    });
                    bottomAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            mBottomOverlay.setVisibility(View.VISIBLE);
                            mOverlayPresentationState = PresentationState.PRESENTED;
                            if (mTransitionalOverlayOn) {
                                Runnable dismissTask = new Runnable() {
                                    public void run() {
                                        CallActivity.this.dismiss();
                                    }
                                };
                                mDelayedHandler.postDelayed(dismissTask, 1000);
                                mTransitionalOverlayOn = false;
                            }
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {}
                    });

                    mTopOverlay.startAnimation(topAnimation);
                    mMiddleOverlay.startAnimation(middleAnimation);
                    mBottomOverlay.startAnimation(bottomAnimation);
                } else if (!newOverlayOn && 
                           (mOverlayPresentationState == PresentationState.PRESENTED || 
                            mOverlayPresentationState == PresentationState.PRESENTING))
                {
                    mOverlayPresentationState = PresentationState.HIDING;

                    Animation animation = null;

                    topAnimation = new AnimationSet(true);
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f
                    );
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION : 0);
                    topAnimation.addAnimation(animation);

                    middleAnimation = new AnimationSet(true);
                    animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION : 0);
                    middleAnimation.addAnimation(animation);

                    bottomAnimation = new AnimationSet(true);
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f
                    );
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION : 0);
                    bottomAnimation.addAnimation(animation);

                    topAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mTopOverlay.setVisibility(View.GONE);
                        }
                    });
                    middleAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mMiddleOverlay.setVisibility(View.GONE);
                        }
                    });
                    bottomAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mBottomOverlay.setVisibility(View.GONE);
                            mOverlayPresentationState = PresentationState.HIDDEN;
                        }
                    });

                    mTopOverlay.startAnimation(topAnimation);
                    mMiddleOverlay.startAnimation(middleAnimation);
                    mBottomOverlay.startAnimation(bottomAnimation);
                } else if (mTransitionalOverlayOn) {
                    Runnable dismissTask = new Runnable() {
                        public void run() {
                            CallActivity.this.dismiss();
                        }
                    };
                    mDelayedHandler.postDelayed(dismissTask, 1000);
                    mTransitionalOverlayOn = false;
                }

                mOverlayIsOn = overlayOn;
            }
        });
    }

    private void toggleOverlay() {
        setOverlayOn(!mOverlayIsOn, true);
    }

    private void toggleMute() {
        setMuted(!mMuted);
    }

    private void toggleSpeaker() {
        setSpeakerPhone(!mSpeakerPhone);
    }

    public void onMuteClick(View v) {
        toggleMute();
    }

    public void onSpeakerClick(View v) {
        toggleSpeaker();
    }
    
    public void onVideoToggleClick(View v) {
        if (mCurrentCallType == Type.VIDEO) {
            mCurrentCall.changeCallType(Type.VOICE);
        } else {
            mCurrentCall.changeCallType(Type.VIDEO);
        }
        mVideoToggleButton.setEnabled(false);
    }
    
    public void onAddUserClick(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.om_add_user);
        alert.setMessage("");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
              String value = input.getText().toString();
              if (value.equals("")) return;
              User user = new User(value, "Example user");
              mCurrentCall.addUser(user);
          }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            
          }
        });

        alert.show();
    }

    public void onLeftBottomClick(View v) {
        if (mCallState == CallState.RINGING) {
            rejectCall();
        } else if (mCallState == CallState.FAILED || (mCallState == CallState.TERMINATED && !mCallWasAnswered) &&
                mCurrentCallPeers != null && !mCurrentCallPeers.isEmpty()) {
            Intent callIntent = new Intent(this, CallActivity.class);
            callIntent.setAction(CallActivity.ACTION_CALL);
            callIntent.putParcelableArrayListExtra(CallActivity.EXTRA_USERS, new ArrayList<User>(mCurrentCallPeers));
            callIntent.putExtra(CallActivity.EXTRA_TYPE, mCurrentCallType);
            startActivity(callIntent);
        } else {
            hangupCall(CallTerminatedReason.LOCAL_HANGUP);
        }
    }

    public void onRightBottomClick(View v) throws PhoneException {
        if (mCallState == CallState.RINGING) {
            answerCall();
        } else if (mCallState == CallState.FAILED || (mCallState == CallState.TERMINATED && !mCallWasAnswered)) {
            dismiss();
        } else {
            returnButtonPressed();
        }
    }

    public void onSingleBottomClick(View v) {
        if (mCallState == CallState.IN_PROGRESS && mCurrentCallType == PhoneCall.Type.VOICE) {
            hangupCall(CallTerminatedReason.LOCAL_HANGUP);
        } else if (mCallState == CallState.RINGING_REMOTE || 
                   mCallState == CallState.CONNECTING) {
            hangupCall(CallTerminatedReason.LOCAL_HANGUP);
        } else {
            dismiss();
        }
    }

    private void resetSpeakerPhoneState() {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        final boolean speakerPhone = audioManager.isSpeakerphoneOn();
        this.runOnUiThread(new Runnable() {
            public void run() {
                mSpeakerButton.setChecked(speakerPhone);
            }
        });
    }

    private void setInboundRinging(boolean inboundRinging) {
        if (mInboundRinging != inboundRinging) {
            if (inboundRinging == true) {
                if (mRingtone != null) {
                    Log.i(LOG_TAG, "RingTone.play()" + Thread.currentThread().getId());
                    mRingtone.play();
                }
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibration (ms){ OFF,   ON,  OFF,   ON,  OFF,   ON}
                    long[] pattern = {  0L, 400L, 250L, 600L, 250L, 400L};
                    Log.i(LOG_TAG, "Vibrator.vibrate()" + Thread.currentThread().getId());
                    vibrator.vibrate(pattern, 2);
                }
            } else {
                if (mRingtone != null) {
                    Log.i(LOG_TAG, "RingTone.stop()" + Thread.currentThread().getId());
                    mRingtone.stop();
                }
                Log.i(LOG_TAG, "Vibrator.cancel()" + Thread.currentThread().getId());
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.cancel();
            }
        }
        mInboundRinging = inboundRinging;
    }

    public boolean isInboundRinging() {
        return mInboundRinging;
    }

    private void setOutboundRinging(boolean outboundRinging) {
        if (mOutboundRinging != outboundRinging) {
            if (outboundRinging == true) {
                Log.i(LOG_TAG, "ToneGenerator.startTone()" + Thread.currentThread().getId());
                if(mToneGenerator != null) {
                	mToneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE);
                }
            } else {
                Log.i(LOG_TAG, "ToneGenerator.stopTone()" + Thread.currentThread().getId());
                if(mToneGenerator != null) {
                	mToneGenerator.stopTone();
                }
                if (!DeviceUtilities.isGalaxyAce()) {
                    // Reenable voip for galaxy-tab (bug 23034)
                    ((AudioManager) getSystemService(AUDIO_SERVICE)).setParameters("voip=on");
                }
            }
        }
        mOutboundRinging = outboundRinging;
    }

    public boolean isOutboundRinging() {
        return mOutboundRinging;
    }

    private void setProximityMonitoring(boolean on) {
        if (mProximityMonitoringOn != on) {
            mProximityMonitoringOn = on;
            SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (on) {
                sensorManager.registerListener(mProximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                sensorManager.unregisterListener(mProximitySensorListener);
                mTouchLockOverlay.setVisibility(View.GONE);
            }
        }
    }

    private void setMuted(final boolean muted) {
        if (mMuted != muted && mCurrentCall != null) {
            mCurrentCall.setAudioCaptureVolume((muted) ? 0.0f : 1.0f);
            this.runOnUiThread(new Runnable() {
                public void run() {
                    mMuteButton.setChecked(muted);
                }
            });
        }
        mMuted = muted;
    }

    public boolean isMuted() {
        return mMuted;
    }

    private void setSpeakerPhone(final boolean speakerPhone) {
        if (mSpeakerPhone != speakerPhone && mCurrentCall != null) {
            mCurrentCall.setSpeakerPhone(speakerPhone);
            this.runOnUiThread(new Runnable() {
                public void run() {
                    mSpeakerButton.setChecked(speakerPhone);
                }
            });
        }
        mSpeakerPhone = speakerPhone;
        setupProximityMonitoring();
    }

    public boolean isSpeakerPhone() {
        return mSpeakerPhone;
    }

    private synchronized void startCall() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKELOCK_KEY);
        }

        resetSpeakerPhoneState();

        mCallWasAnswered = false;
        mAppTermReason = CallTerminatedReason.UNDEFINED;

        mWakeLock.acquire();

        if (mCurrentCallType == PhoneCall.Type.VIDEO) {
            mRemoteVideoView.setVisibility(View.VISIBLE);
            setSpeakerPhone(true);
        } else {
            mRemoteVideoView.setVisibility(View.GONE);
            setSpeakerPhone(false);
        }

        mStatusUpdateTask = new Runnable() {
            public void run() {
                final Date currentTime = new Date();
                if (mCallState == CallState.IN_PROGRESS && mCallStartTime != null) {
                    long diffInMs = currentTime.getTime() - mCallStartTime.getTime();
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                    long secs = diffInSec % 60;
                    long mins = (diffInSec - secs) / 60;
                    mStatusText.setText(String.format("%d:%02d", mins, secs));
                }
                mStatusUpdateHandler.postDelayed(this, 1000);
            }
        };
        mStatusUpdateHandler.removeCallbacks(mStatusUpdateTask);
        mStatusUpdateHandler.postDelayed(mStatusUpdateTask, 100);

        updateStatusLabel();
        
        // XXX: this is a complete bodge
        // default our orientation to 'natural' on the S2 in case the listener doesn't fire immediately
        mOrientationEventListener.onOrientationChanged(0);
        
        mOrientationEventListener.enable();
        
        if (mCurrentCall.isConference()) {
            Log.i(LOG_TAG, "Starting a conference call.");
        } else {
            Log.i(LOG_TAG, "Starting a peer-to-peer call.");
        }
    }

    private synchronized void stopCall() {
        mStatusUpdateHandler.removeCallbacks(mStatusUpdateTask);
        
        if (mCurrentCall != null) {
            mCurrentCall.setCameraPreview(null);
            mCurrentCall.setRemoteVideoView(null);
        }
        
        mCurrentCall = null;
        mCallStartTime = null;
    
        setOverlayOn(true, true);
        setProximityMonitoring(false);
    
        mCurrentOri = PhoneCall.CameraOrientation.UNKNOWN;
        mOrientationEventListener.disable();
    
        mCameraPreview.setVisibility(View.GONE);
        mRemoteVideoView.setVisibility(View.GONE);
    
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    
        setIntent(null);
    }

    private void setupProximityMonitoring() {
        boolean enabled = (!mSpeakerPhone) && 
                          (mCurrentCallType == PhoneCall.Type.VOICE) && 
                          (mCallState == CallState.IN_PROGRESS || 
                           mCallState == CallState.RINGING || 
                           mCallState == CallState.RINGING_REMOTE);
        setProximityMonitoring(enabled);
    }

    private void returnButtonPressed() {
        switch (mCurrentCallType) {
            case VIDEO:
                setOverlayOn(false, true);
                break;
            case VOICE:
            default:
                break;
        }
    }

    private void answerCall() {
        if (mCurrentCall == null) return;
        mCurrentCall.accept();
        callAnswered();
    }

    private void hangupCall(CallTerminatedReason reason) {
        if (mCurrentCall == null) return;
        mAppTermReason = reason;
        setCallState(CallState.TERMINATED);
        mCurrentCall.end();
        stopCall();
        dismiss();
    }

    private void rejectCall() {
        if (mCurrentCall == null) return;
        mAppTermReason = CallTerminatedReason.LOCAL_REJECTED;
        setCallState(CallState.TERMINATED);
        mCurrentCall.end();
        stopCall();
        dismiss();
    }

    private void callAnswered() {
        mCallWasAnswered = true;
        CallActivity.this.mCallStartTime = new Date();
        setCallState(CallState.IN_PROGRESS);

        if (mCurrentCallType == PhoneCall.Type.VIDEO) {
            mCameraPreview.setVisibility(View.VISIBLE);            
            mRemoteVideoView.setVisibility(View.VISIBLE);
            // outbound video is muted by default from 0.7.13 onwards for privacy
            mCurrentCall.unmuteOutboundVideo();
            setSpeakerPhone(true);
        } else {
            mCameraPreview.setVisibility(View.GONE);
            mRemoteVideoView.setVisibility(View.GONE);
            setSpeakerPhone(false);
        }

        Runnable hideOverlayTask = new Runnable() {
            public void run() {
                CallActivity.this.setOverlayOn(false, true);
            }
        };
        mDelayedHandler.postDelayed(hideOverlayTask, 3000);
        
        Log.i(LOG_TAG, "Call identifier is: " + this.mCurrentCall.getCallIdentifier());
    }

    private void dismiss() {
        if (mToneGenerator != null) mToneGenerator.stopTone();
        
        finish();
        
        if (mRingtone != null) mRingtone.stop();
    }

    private int termReasonString() {
        switch (mAppTermReason) {
            case UNDEFINED:
                return 0;
            case ERROR:
                return R.string.om_call_error;
            case LOCAL_HANGUP:
                return R.string.om_call_ended;
            case LOCAL_REJECTED:
                return R.string.om_call_rejected;
            case REMOTE_HANGUP:
                if (!mCallWasAnswered)
                    return R.string.om_missed_call;
                else
                    return R.string.om_call_ended;
            case ANSWERED_ELSEWHERE:
                return R.string.om_call_ended;
            case REMOTE_BUSY:
                return R.string.om_user_busy;
            case REMOTE_REJECTED:
                return R.string.om_call_rejected;
            case REMOTE_UNAVAILABLE:
                return R.string.om_user_unavailable;
            case POOR_QUALITY:
                return R.string.om_link_lost;
        }
        return 0;
    }

    private class MyPhoneCallListener implements PhoneCallListener {

        @Override
        public void onRemoteRinging() {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCallState(CallState.RINGING_REMOTE);
                }
            });
        }

        @Override
        public void onRemoteEnded(final CallEndReason reason) {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCall == null) return;
                    switch (reason) {
                    case REMOTE_BUSY:
                        mAppTermReason = CallTerminatedReason.REMOTE_BUSY;
                        mToneGenerator.startTone(ToneGenerator.TONE_SUP_BUSY, 3000);
                        break;
                    case REMOTE_REJECTED:
                        mAppTermReason = CallTerminatedReason.REMOTE_REJECTED;
                        mToneGenerator.startTone(ToneGenerator.TONE_SUP_BUSY, 3000);
                        break;
                    case REMOTE_UNAVAILABLE:
                        mAppTermReason = CallTerminatedReason.REMOTE_UNAVAILABLE;
                        mToneGenerator.startTone(ToneGenerator.TONE_SUP_BUSY, 3000);
                        break;
                    case REMOTE_HUNGUP:
                        mAppTermReason = CallTerminatedReason.REMOTE_HANGUP;
                        break;
                    case ANSWERED_ELSEWHERE:
                        mAppTermReason = CallTerminatedReason.ANSWERED_ELSEWHERE;
                    }
                    // Always finish activity when call ended.
                    mTransitionalOverlayOn = true;
                    setCallState(CallState.TERMINATED);
                    stopCall();
                }
            });
        }

        @Override
        public void onAnswered() {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CallActivity.this.callAnswered();
                }
            });
        }

        @Override
        public void onFailed(CallErrorCode errorCode, String errorMessage) {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCall == null) return;
                    mAppTermReason = CallTerminatedReason.ERROR;
                    setCallState(CallState.TERMINATED);
                    stopCall();
                }
            });
        }

        @Override
        public void onRemoteCameraMetadata(final CameraType type, final CameraOrientation ori) {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(ori) {
                    case LANDSCAPE_UP:
                        mRemoteVideoView.setRemoteRotation(0);
                        break;
                    case PORTRAIT_LEFT:
                        mRemoteVideoView.setRemoteRotation(90);
                        break;
                    case LANDSCAPE_DOWN:
                        mRemoteVideoView.setRemoteRotation(180);
                        break;
                    case PORTRAIT_RIGHT:
                        mRemoteVideoView.setRemoteRotation(270);
                        break;
                    case UNKNOWN:
                        // No action required.
                        break;
                    }
                }
            });
        }

        @Override
        public void onTypeChanged(Type newType) {
            mCurrentCallType = newType;
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCallType == PhoneCall.Type.VIDEO) {
                        mCameraPreview.setVisibility(View.VISIBLE);
                        mRemoteVideoView.setVisibility(View.VISIBLE);
                        setSpeakerPhone(true);
                        // by default, start sending video immediately.
                        mCurrentCall.unmuteOutboundVideo();
                    } else {
                        mCameraPreview.setVisibility(View.GONE);
                        mRemoteVideoView.setVisibility(View.GONE);
                        setSpeakerPhone(false);
                        setOverlayOn(true,  true);
                    }
                    
                    updateBottomBar();
                    updateButtons();
                    
                    mVideoToggleButton.setEnabled(true);
                }
            });
        }

        @Override
        public void onTypeChangeFailed() {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
                    String msg = mCurrentCallPeers.get(0).getName()+" declined your request to "+
                    (mCurrentCallType == Type.VIDEO ? "disable" : "enable")+" video";
                    
                    builder.setMessage(msg)
                           .setCancelable(true)
                           .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                               }
                           });
                    AlertDialog alert = builder.create();
                    alert.show();
                    
                    mVideoToggleButton.setEnabled(true);
                }
            });
        }

        @Override
        public void onTypeChangeRequested(Type requestedType) {
            // Dispatch to the thread that is managing UI state.
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
                    String msg = mCurrentCallPeers.get(0).getName()+" would like to "+
                    (mCurrentCallType == Type.VIDEO ? "disable" : "enable")+" video";
                    
                    builder.setMessage(msg)
                           .setCancelable(false)
                           .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                                   mCurrentCall.acceptCallTypeChange();
                               }
                           })
                           .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                                   mCurrentCall.rejectCallTypeChange();
                               }
                           });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        @Override
        public void onConferenceTransferStarted() {
            
        }

        @Override
        public void onConferenceCallStarted() {
            
        }

        @Override
        public void onConferenceStateChanged(User user) {
            Log.i(LOG_TAG, "Conference state changed for user " + user);
            
        }

        @Override
        public void onReceivingVideo(boolean receivingVideo) {
            Log.i(LOG_TAG, "Receiving video: "+receivingVideo);
        }
        
        public void onRemoteCallHoldStateChanged(boolean held) {
            Log.i(LOG_TAG, "Remote call : "+ (held ? "held" : "unheld"));
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {

        public MyOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // Update the display rotation as it may have changed. The camera preview and the remote video view need to
            // know this orientation so they compensate for the orientation of the display relative to the device when
            // displaying video.
            
            Log.i(LOG_TAG, "Orientation Changed: " + orientation);
            
            mCameraPreview.setDisplayRotationEnum(getWindowManager().getDefaultDisplay().getOrientation());
            mRemoteVideoView.setDisplayRotationEnum(getWindowManager().getDefaultDisplay().getOrientation());
            
            // Update the device rotation for the camera preview and the remote video view so they can can compensate
            // for how the device is being held.
            mCameraPreview.setDeviceRotationDegrees(orientation);
            mRemoteVideoView.setDeviceRotation(orientation);
            
            if (mCurrentCall == null) return;
            if (orientation == ORIENTATION_UNKNOWN) return;
            
            // The best orientation to view the video we are capturing may have changed.  
            CameraOrientation newOri = mCameraPreview.getCameraOrientationEnum();
            
            if (mCurrentOri != newOri && mCurrentCall != null) {
                mCurrentOri = newOri;
                // If the orientation has changed tell the remote device what the new orientation is. 
                mCurrentCall.setDeviceOrientation(mCurrentOri);
            }
        }

    }

    private class MyProximityEventListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
            float max = event.sensor.getMaximumRange();
            float[] values = event.values;
            float proximityValue = values[0];
            if (proximityValue < max) {
                mTouchLockOverlay.setVisibility(View.VISIBLE);
            } else {
                mTouchLockOverlay.setVisibility(View.GONE);
            }
        }

    }

    private class MyTouchLockTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }

    }

}
