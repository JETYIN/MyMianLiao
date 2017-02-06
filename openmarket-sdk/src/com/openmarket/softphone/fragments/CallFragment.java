package com.openmarket.softphone.fragments;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openmarket.softphone.PhoneCall;
import com.openmarket.softphone.PhoneCall.CameraOrientation;
import com.openmarket.softphone.PhoneCall.CameraType;
import com.openmarket.softphone.PhoneCall.PhoneCallState;
import com.openmarket.softphone.PhoneCall.Type;
import com.openmarket.softphone.PhoneCallListener;
import com.openmarket.softphone.R;
import com.openmarket.softphone.User;
import com.openmarket.softphone.fragments.CallVideoFragment.OnCallVideoFragmentListener;
import com.openmarket.softphone.util.DeviceUtilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * <p>
 * The CallFragment contains the entire UI for placing and receiving calls. It is designed to be extremely easy to use.
 * The fragment must be attached to your activity either dynamically or via XML and
 * {@link CallFragment.OnCallFragmentListener} must be implemented by that Activity.
 * </p>
 * After this is done, you can call {@link #CallFragment.placeCall(List, Type)} or
 * {@link #CallFragment.takeCall(Intent)} to place or receive a call.
 */
public class CallFragment extends Fragment {
    private static final String LOG_TAG = "CallFragment";
    
    /**
     * An input argument which when set to true will allow the call to be backgrounded by pressing the HOME button, rather
     * than terminating the call.
     */
    public static final String KEY_ALLOW_BACKGROUNDING = "allow_background";

    /**
     * This listener MUST be implemented by the Activity wishing to use this fragment. This contains the primary means
     * of communication from the Fragment to your Activity.
     */
    public static interface OnCallFragmentListener {
        /**
         * Called when the fragment has finished dealing with the call and wishes to be removed from display.
         */
        public void onCallFragmentFinished();
    };
    
    private OnCallFragmentListener mListener;   
    
    
    
    private enum CallTerminatedReason {
        UNDEFINED, LOCAL_REJECTED, LOCAL_HANGUP, REMOTE_BUSY, REMOTE_UNAVAILABLE, REMOTE_REJECTED, REMOTE_HANGUP,
        ANSWERED_ELSEWHERE, ERROR, POOR_QUALITY,
    }
    
    private enum PresentationState {
        HIDDEN, HIDING, PRESENTED, PRESENTING,
    }
    
    // Animation specific fields
    private static final long OVERLAY_ANIMATION_DURATION_MS = 500; // how long UI overlay animations take to appear in ms
    private boolean mOverlayIsOn;
    private PresentationState mOverlayPresentationState;
    private boolean mShouldDismissCall; // Indicates if the fragment should finished after a delayed amount of time.

    // Phone call specific fields
    private PhoneCall mCurrentCall;
    private PhoneCall.Type mCurrentCallType;
    private PhoneCall.PhoneCallState mCurrentCallState = PhoneCall.PhoneCallState.FAILED;
    private List<User> mCurrentCallPeers;
    private boolean mCallWasAnswered;
    private CallTerminatedReason mAppTermReason = CallTerminatedReason.ERROR;
    
    // Background phone call specific fields
    private boolean mAllowBackgrounding;
    private static PhoneCall sStoredCall;
    
    // UI Elements
    private LinearLayout mTopOverlay;
    private TextView mNameText;
    private TextView mStatusText;

    private LinearLayout mMiddleOverlay;
    private ToggleButton mMuteButton;
    private ToggleButton mSpeakerButton;
    private Button mVideoToggleButton;
    private Button mAddUserButton;

    private LinearLayout mBottomOverlay;
    private Button mAcceptButton;
    private Button mRejectButton;
    private Button mEndCallButton;
    private Button mCallBackButton;
    private Button mReturnButton;

    // Task which will run at a specific interval
    private Runnable mStatusUpdateTask;
    
    // This will implicitly attach itself to the UI thread, and will allow Runnables to be posted to the UI thread.
    private final Handler mHandler = new Handler();

    // Audio fields
    private Ringtone mRingtone;
    private ToneGenerator mToneGenerator;
    private boolean mInboundRinging;
    private boolean mOutboundRinging;
    private int mAudioStream = (DeviceUtilities.isGalaxyTab()) ? AudioManager.STREAM_MUSIC
                                                              : AudioManager.STREAM_VOICE_CALL;
    
    // Wake lock fields to prevent the device from sleeping whilst a call is in progress
    private static final String WAKELOCK_KEY = "CALL_WAKE_LOCK";
    private static PowerManager.WakeLock mWakeLock; 

    // Proximity monitoring fields (for disabling UI buttons when the device is held against the face)
    private SensorEventListener mProximitySensorListener;
    private boolean mProximityMonitoringOn = false;
    private View mTouchLockOverlay;

    // Fragment specific fields
    private boolean mFragmentAttachedToActivity; // indicates fragment attachment status to an activity
    private CallVideoFragment mCallVideoFragment;
    private static final String FRAG_TAG_CALL_VIDEO = "tag_call_video_fragment"; // tag used to identify the nested CallVideoFragment

    /**
     * Places a call via the PhoneManager to the peers given and type of call specified.
     * 
     * @param peers The peers to call. This will do nothing if this is null or empty.
     * @param typeOfCall The type of call to make.
     * @return The call just placed, or null if it failed.
     */
    public PhoneCall placeCall(List<User> peers, PhoneCall.Type typeOfCall) {
        if (peers == null || typeOfCall == null || peers.size() == 0) {
            Log.e(LOG_TAG, "Cannot place call: null or missing args.");
            return null;
        }

        if (mCurrentCall != null) {
            Log.e(LOG_TAG, "You cannot place more than one call at once.");
            return null;
        }
        
        if (mCallVideoFragment == null) {
            Log.e(LOG_TAG, "The fragment must be fully created before you can place a call.");
            return null;
        }
        
        Log.i(LOG_TAG, "Placing a call.");
        mCurrentCall = mCallVideoFragment.placeCall(peers, typeOfCall, new MyPhoneCallListener());
        
        mCallVideoFragment.setRemoteVideoViewVisible(false);
        mCallVideoFragment.setCameraPreviewVisible(false);
        
        mCurrentCallType = typeOfCall;
        mCurrentCallPeers = peers;
        
        if (mCurrentCall != null) {
            mCurrentCallType = mCurrentCall.getType();
            mCurrentCallPeers = mCurrentCall.getPeers();
        }
        else {
            failCall();
        }
        
        return mCurrentCall;
    }

    /**
     * Take a call using the INCOMING Intent you were supplied with.
     * 
     * @param intent The intent given.
     * @return the call if the call was taken successfully, determined by PhoneManager.takeCall(Intent, PhoneCallListener)
     *         returning true, else null.
     */
    public PhoneCall takeCall(Intent intent) {
        if (intent == null) {
            Log.e(LOG_TAG, "Cannot take call: null intent.");
            return null;
        }
        
        if (mCallVideoFragment == null) {
            Log.e(LOG_TAG, "The fragment must be fully created before you can take a call.");
            return null;
        }
        
        Log.i(LOG_TAG, "Taking a call.");
        mCurrentCall = mCallVideoFragment.takeCall(intent, new MyPhoneCallListener());
        
        mCallVideoFragment.setRemoteVideoViewVisible(false);
        mCallVideoFragment.setCameraPreviewVisible(false);

        if (mCurrentCall != null) {
            mCurrentCallType = mCurrentCall.getType();
            mCurrentCallPeers = mCurrentCall.getPeers();
            return mCurrentCall;
        }
        else {
            failCall();
        }
        
        return null;
    }
    
    /**
     * Resumes a backgrounded call.
     * @return The backgrounded call or null if one does not exist.
     */
    private PhoneCall resumeStoredCall() {
        
        if (mCallVideoFragment == null) {
            Log.e(LOG_TAG, "The fragment must be fully created before you can resume a call.");
            return null;
        }
        
        if (sStoredCall == null) {
            Log.e(LOG_TAG, "There is no stored call to resume.");
            return null;
        }

        mCurrentCall = sStoredCall;
        sStoredCall = null;
        mCallVideoFragment.resumeCall(mCurrentCall, new MyPhoneCallListener());
        mCallVideoFragment.setRemoteVideoViewVisible(false);
        mCallVideoFragment.setCameraPreviewVisible(false);
        
        mCurrentCallType = mCurrentCall.getType();
        mCurrentCallPeers = mCurrentCall.getPeers();     
        
        PhoneCallState state = mCurrentCall.getCallState();
        
        // Set states which the call -should- be in had the fragment never been left.
        switch (state) {
        case CONNECTING:
            break;
        case IN_PROGRESS:
            setCallInProgress();
            break;
        case RINGING:
            setInboundRinging(true);
            break;
        case RINGING_REMOTE:
            setOutboundRinging(true);
            break;
        case TERMINATED:
        case FAILED:
            mAppTermReason = (state == PhoneCallState.TERMINATED ? CallTerminatedReason.REMOTE_HANGUP : CallTerminatedReason.ERROR);
            mCallWasAnswered = (state == PhoneCallState.TERMINATED ? true : false);
            mShouldDismissCall = true;
            break;
        }
        
        updateUiElements();
        return mCurrentCall;
    }

    /**
     * Fails a call gracefully, updating UI state and stopping any current call.
     */
    private void failCall() {
        Log.e(LOG_TAG, "Failing call.");
        mCurrentCallState = PhoneCallState.FAILED;
        updateUiElements();
        stopCall();
    }

    /**
     * Updates the UI elements based on the current call state.
     */
    private void updateUiElements() {
        if (mCurrentCall == null) {
            // this will execute if the call fails to be placed or taken, and will set the FAILED UI layout.
            Log.e(LOG_TAG, "updateUiElements() Current call is null, cannot update call state!");
        }
        else {
            mCurrentCallState = mCurrentCall.getCallState();
            Log.d(LOG_TAG,"updateUiElements() call state: "+mCurrentCall.getCallState().name());
        }

        // Update the ringing state
        if (mCurrentCallState == PhoneCallState.RINGING) {
            setInboundRinging(true);
        }
        else if (mInboundRinging) {
            setInboundRinging(false);
        }
        if (mCurrentCallState == PhoneCallState.RINGING_REMOTE) {
            setOutboundRinging(true);
        }
        else {
            setOutboundRinging(false);
        }

        // Update name if we know it
        if (mCurrentCallPeers != null && !mCurrentCallPeers.isEmpty()) {
            mNameText.setText(mCurrentCallPeers.get(0).getName());
        }
        else {
            mNameText.setText("");
        }

        // Change enable/disable video based on call type
        if (mCurrentCall != null && mCurrentCall.getType() == Type.VIDEO) {
            mVideoToggleButton.setText(R.string.om_disable_video);
        }
        else {
            mVideoToggleButton.setText(R.string.om_enable_video);
        }

        setupProximityMonitoring();

        // Update UI Overlay
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Initialise default state and modify based on the current state
                mVideoToggleButton.setEnabled(false);
                mAddUserButton.setEnabled(false);
                
                mRejectButton.setVisibility(View.GONE);
                mAcceptButton.setVisibility(View.GONE);
                mCallBackButton.setVisibility(View.GONE);
                mReturnButton.setVisibility(View.GONE);
                mEndCallButton.setVisibility(View.GONE);
                
                switch (mCurrentCallState) {
                case CONNECTING: {
                    mEndCallButton.setVisibility(View.VISIBLE);
                    mStatusText.setText(R.string.om_connecting);
                    
                }
                    break;
                case RINGING: {
                    mRejectButton.setVisibility(View.VISIBLE);
                    mAcceptButton.setVisibility(View.VISIBLE);
                    
                    if (mCurrentCallType == PhoneCall.Type.VOICE) {
                        mAcceptButton.setText(R.string.om_accept);
                    }
                    else {
                        mAcceptButton.setText(R.string.om_accept);
                    }
                    
                    mStatusText.setText(R.string.om_ringing);
                }
                    break;
                case RINGING_REMOTE: {
                    mEndCallButton.setVisibility(View.VISIBLE);
                    mStatusText.setText(R.string.om_ringing);
                }
                    break;
                case IN_PROGRESS: {
                    mVideoToggleButton.setEnabled(true);
                    mAddUserButton.setEnabled(true);
                    mEndCallButton.setVisibility(View.VISIBLE);
                    if (mCurrentCallType == PhoneCall.Type.VIDEO) {
                        mReturnButton.setVisibility(View.VISIBLE);
                        mReturnButton.setText(R.string.om_return_back);
                    }
                }
                    break;
                case TERMINATED: {
                    if (mCallWasAnswered) {
                        mReturnButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        mCallBackButton.setVisibility(View.VISIBLE);
                        mReturnButton.setVisibility(View.VISIBLE);
                    }
                    
                    mStatusText.setText(termReasonString());
                }
                    break;
                case FAILED: {
                    mStatusText.setText(R.string.om_call_failed);
                    mCallBackButton.setVisibility(View.VISIBLE);
                    mReturnButton.setVisibility(View.VISIBLE);
                }
                    break;
                }
            }
        });
    }

    /**
     * Changes the state of the UI overlay buttons.
     * @param overlayOn true to turn the overlay on, false to turn it off.
     * @param animated true to animate the change, false to apply it immediately.
     */
    private void setOverlayOn(final boolean overlayOn, final boolean animated) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                boolean newOverlayOn = overlayOn;
                if (mCurrentCallType == PhoneCall.Type.VOICE) {
                    newOverlayOn = true;
                }
                if (mCurrentCallState != PhoneCallState.IN_PROGRESS) {
                    newOverlayOn = true;
                }

                AnimationSet topAnimation = null;
                AnimationSet middleAnimation = null;
                AnimationSet bottomAnimation = null;
                if (newOverlayOn &&
                    (mOverlayPresentationState == PresentationState.HIDDEN || mOverlayPresentationState == PresentationState.HIDING)) {
                    mOverlayPresentationState = PresentationState.PRESENTING;

                    Animation animation = null;

                    topAnimation = new AnimationSet(true);
                    animation =
                            new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                   Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION_MS : 0);
                    topAnimation.addAnimation(animation);

                    middleAnimation = new AnimationSet(true);
                    animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION_MS : 0);
                    middleAnimation.addAnimation(animation);

                    bottomAnimation = new AnimationSet(true);
                    animation =
                            new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                   Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION_MS : 0);
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
                            if (mShouldDismissCall) {
                                dismissDelayed();
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
                }
                else if (!newOverlayOn &&
                         (mOverlayPresentationState == PresentationState.PRESENTED || mOverlayPresentationState == PresentationState.PRESENTING)) {
                    mOverlayPresentationState = PresentationState.HIDING;

                    Animation animation = null;

                    topAnimation = new AnimationSet(true);
                    animation =
                            new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                   Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION_MS : 0);
                    topAnimation.addAnimation(animation);

                    middleAnimation = new AnimationSet(true);
                    animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION_MS : 0);
                    middleAnimation.addAnimation(animation);

                    bottomAnimation = new AnimationSet(true);
                    animation =
                            new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                   Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
                    animation.setDuration(animated ? OVERLAY_ANIMATION_DURATION_MS : 0);
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
                }
                else if (mShouldDismissCall) {
                    dismissDelayed();
                }

                mOverlayIsOn = overlayOn;
            }
        });
    }

    /**
     * Toggles the display of the UI overlay. The toggle is always animated.
     */
    private void toggleOverlay() {
        setOverlayOn(!mOverlayIsOn, true);
    }

    private void onMuteButtonClick(View v) {
        if (mCurrentCall == null) {
            return;
        }
        
        double captureLevel = mCurrentCall.getAudioCaptureLevel();
        // if capture level is 0, it's muted, so we want to unmute it.
        final boolean newMuteStatus = (captureLevel == 0.0 ? false : true);
        
        Log.i(LOG_TAG,"Updating mute state: muted:"+newMuteStatus);
        
        if (newMuteStatus) {
            mCurrentCall.setAudioCaptureVolume(0.0);
        }
        else {
            mCurrentCall.setAudioCaptureVolume(1.0);
        }
        
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMuteButton.setChecked(newMuteStatus);
            }
        });
    }
    
    private void onAcceptButtonClick(View v) {
        if (mCurrentCallState == PhoneCallState.RINGING) {
            if (mCurrentCall == null) {
                return;
            }
            startCall();
            mCurrentCall.accept();
            setCallInProgress();
        }
    }
    
    private void onRejectButtonClick(View v) {
        if (mCurrentCallState == PhoneCallState.RINGING) {
            if (mCurrentCall == null) {
                return;
            }
            mAppTermReason = CallTerminatedReason.LOCAL_REJECTED;
            mCurrentCall.end();
            updateUiElements();
            dismiss();
        }
    }
    
    private void onEndCallButtonClick(View v) {
        if (mCurrentCallState == PhoneCallState.IN_PROGRESS || mCurrentCallState == PhoneCallState.RINGING_REMOTE || 
                mCurrentCallState == PhoneCallState.CONNECTING) {
            hangupCall(CallTerminatedReason.LOCAL_HANGUP);
        }
    }
    
    private void onReturnButtonClick(View v) {
        if (mCurrentCallState == PhoneCallState.IN_PROGRESS) {
            if (mCurrentCallType == Type.VIDEO) {
                setOverlayOn(false, true);
            }
        }
        else {
            dismiss();
        }
    }
    
    private void onCallBackButtonClick(View v) {
        if (mCurrentCallState == PhoneCallState.FAILED ||
                (mCurrentCallState == PhoneCallState.TERMINATED && !mCallWasAnswered) && mCurrentCallPeers != null &&
                !mCurrentCallPeers.isEmpty()) {
           placeCall(mCurrentCallPeers, mCurrentCallType);
       }
    }
    
    private void onSpeakerphoneButtonClick(View v) {
        if (mCurrentCall == null) {
            return;
        }
        
        boolean isSpeakerOn = mCurrentCall.isSpeakerphoneOn();
        final boolean newSpeakerOn = !isSpeakerOn;
        
        setSpeakerPhone(newSpeakerOn);
    }

    private void resetSpeakerPhoneState() {
        AudioManager audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        final boolean speakerPhone = audioManager.isSpeakerphoneOn();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mSpeakerButton.setChecked(speakerPhone);
                if (mCurrentCall != null) {
                    mCurrentCall.setSpeakerPhone(speakerPhone);
                }
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
                AudioManager audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                    Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibration (ms){ OFF, ON, OFF, ON, OFF, ON}
                    long[] pattern = { 0L, 400L, 250L, 600L, 250L, 400L };
                    Log.i(LOG_TAG, "Vibrator.vibrate()" + Thread.currentThread().getId());
                    vibrator.vibrate(pattern, 2);
                }
            }
            else {
                if (mRingtone != null) {
                    Log.i(LOG_TAG, "RingTone.stop()" + Thread.currentThread().getId());
                    mRingtone.stop();
                }
                Log.i(LOG_TAG, "Vibrator.cancel()" + Thread.currentThread().getId());
                Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.cancel();
            }
        }
        mInboundRinging = inboundRinging;
    }

    private void setOutboundRinging(boolean outboundRinging) {
        if (mOutboundRinging != outboundRinging) {
            if (outboundRinging == true) {
                Log.i(LOG_TAG, "ToneGenerator.startTone()" + Thread.currentThread().getId());
                if (mToneGenerator != null) {
                    mToneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE);
                }
            }
            else {
                Log.i(LOG_TAG, "ToneGenerator.stopTone()" + Thread.currentThread().getId());
                if (mToneGenerator != null) {
                    mToneGenerator.stopTone();
                }
                if (!DeviceUtilities.isGalaxyAce()) {
                    // Reenable voip for galaxy-tab (bug 23034)
                    ((AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE)).setParameters("voip=on");
                }
            }
        }
        mOutboundRinging = outboundRinging;
    }

    private void setProximityMonitoring(boolean on) {
        if (mProximityMonitoringOn != on) {
            mProximityMonitoringOn = on;
            SensorManager sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
            Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (on) {
                sensorManager.registerListener(mProximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_UI);
            }
            else {
                sensorManager.unregisterListener(mProximitySensorListener);
                mTouchLockOverlay.setVisibility(View.GONE);
            }
        }
    }

    private void setSpeakerPhone(final boolean speakerPhone) {
        if (mCurrentCall != null) {
            mCurrentCall.setSpeakerPhone(speakerPhone);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mSpeakerButton.setChecked(speakerPhone);
                }
            });
        }
    }
    
    private synchronized void startCall() {
        
        mCallVideoFragment.startCall();
        Log.d(LOG_TAG,"startCall()");
        
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKELOCK_KEY);
        }

        mCallWasAnswered = false;
        mAppTermReason = CallTerminatedReason.UNDEFINED;

        mWakeLock.acquire();

        if (mCurrentCallType == PhoneCall.Type.VIDEO) {
            mCallVideoFragment.setRemoteVideoViewVisible(true);
        }
        else {
            mCallVideoFragment.setRemoteVideoViewVisible(false);
        }

        mStatusUpdateTask = new Runnable() {
            public void run() {
                final Date currentTime = new Date();
                Date startTime = null;
                
                if (mCurrentCall != null) {
                    startTime = mCurrentCall.getStartTime();
                }
                
                if (mCurrentCallState == PhoneCallState.IN_PROGRESS && startTime != null) {
                    long diffInMs = currentTime.getTime() - startTime.getTime();
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                    long secs = diffInSec % 60;
                    long mins = (diffInSec - secs) / 60;
                    mStatusText.setText(String.format("%d:%02d", mins, secs));
                }
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.removeCallbacks(mStatusUpdateTask);
        mHandler.postDelayed(mStatusUpdateTask, 100); 

        if (mCurrentCall.isConference()) {
            Log.i(LOG_TAG, "Starting a conference call.");
        }
        else {
            Log.i(LOG_TAG, "Starting a peer-to-peer call.");
        }
    }

    private synchronized void stopCall() {
        Log.d(LOG_TAG,"stopCall()");
        
        // Inform the call video fragment that it should stop sending video and hide the cameras.
        mCallVideoFragment.stopCall();
        
        mHandler.removeCallbacks(mStatusUpdateTask);

        mCurrentCall = null;

        setOverlayOn(true, true);
        setProximityMonitoring(false);

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void setupProximityMonitoring() {
        boolean isSpeakerphoneOn = false;
        if (mCurrentCall != null) {
            isSpeakerphoneOn = mCurrentCall.isSpeakerphoneOn();
        }
        
        boolean enabled =
                (!isSpeakerphoneOn) &&
                        (mCurrentCallType == PhoneCall.Type.VOICE) &&
                        (mCurrentCallState == PhoneCallState.IN_PROGRESS || 
                        mCurrentCallState == PhoneCallState.RINGING || 
                        mCurrentCallState == PhoneCallState.RINGING_REMOTE);
        
        setProximityMonitoring(enabled);
    }

    private void hangupCall(CallTerminatedReason reason) {
        if (mCurrentCall == null)
            return;
        mAppTermReason = reason;
        mCurrentCall.end();
        updateUiElements();
        stopCall();
        dismiss();
    }

    private void setCallInProgress() {
        startCall();
        mCallWasAnswered = true;
        if (mCurrentCall.getStartTime() == null) {
            mCurrentCall.setStartTime(new Date());
        }
        
        updateUiElements();

        if (mCurrentCallType == PhoneCall.Type.VIDEO) {
            mCallVideoFragment.setRemoteVideoViewVisible(true);
            mCallVideoFragment.setCameraPreviewVisible(true);
            // outbound video is muted by default from 0.7.13 onwards for privacy
            mCurrentCall.unmuteOutboundVideo();
        }
        else {
            mCallVideoFragment.setRemoteVideoViewVisible(false);
            mCallVideoFragment.setCameraPreviewVisible(false);
        }

        Runnable hideOverlayTask = new Runnable() {
            public void run() {
                if (mFragmentAttachedToActivity) {
                    setOverlayOn(false, true);
                }
            }
        };
        mHandler.postDelayed(hideOverlayTask, 3000);

        Log.i(LOG_TAG, "Call identifier is: " + this.mCurrentCall.getCallIdentifier());
        resetSpeakerPhoneState();
    }

    private void dismiss() {
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
        }
        if (mRingtone != null) {
            mRingtone.stop();
        }
        
        mListener.onCallFragmentFinished();
    }
    
    private void dismissDelayed() {
        Runnable dismissTask = new Runnable() {
            public void run() {
                dismiss();
            }
        };
        mHandler.postDelayed(dismissTask, 1000);
        mShouldDismissCall = false;
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
            if (!mFragmentAttachedToActivity || mCurrentCall == null) {
                return;
            }
            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUiElements();
                }
            });
        }

        @Override
        public void onRemoteEnded(final CallEndReason reason) {
            if (!mFragmentAttachedToActivity) {
                return;
            }

            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCall == null)
                        return;
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
                        if (mCurrentCall.getCallState() == PhoneCallState.TERMINATED && mCallWasAnswered) {
                            mShouldDismissCall = true;
                        }
                        break;
                    case ANSWERED_ELSEWHERE:
                        mAppTermReason = CallTerminatedReason.ANSWERED_ELSEWHERE;
                        mShouldDismissCall = true;
                    }
                    updateUiElements();
                    stopCall();
                }
            });
        }

        @Override
        public void onAnswered() {
            if (!mFragmentAttachedToActivity || mCurrentCall == null) {
                // Check that this onAnswered callback doesn't correspond to a stored call
                if (sStoredCall != null && sStoredCall.getStartTime() == null) {
                    sStoredCall.setStartTime(new Date());
                }
                return;
            }

            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCallInProgress();
                }
            });
        }

        @Override
        public void onFailed(CallErrorCode errorCode, String errorMessage) {
            if (!mFragmentAttachedToActivity) {
                return;
            }

            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCall == null)
                        return;
                    mAppTermReason = CallTerminatedReason.ERROR;
                    updateUiElements();
                    stopCall();
                }
            });
        }

        @Override
        public void onRemoteCameraMetadata(final CameraType type, final CameraOrientation ori) {
            // We don't need to handle this as CallVideoFragment does for us.
        }

        @Override
        public void onTypeChanged(final Type newType) {
            if (!mFragmentAttachedToActivity || mCurrentCall == null) {
                return;
            }

            mCurrentCallType = newType;
            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCallType == PhoneCall.Type.VIDEO) {
                        mCallVideoFragment.setRemoteVideoViewVisible(true);
                        mCallVideoFragment.setCameraPreviewVisible(true);
                        // by default, start sending video immediately.
                        mCurrentCall.unmuteOutboundVideo();
                    }
                    else {
                        mCallVideoFragment.setRemoteVideoViewVisible(false);
                        mCallVideoFragment.setCameraPreviewVisible(false);
                        setOverlayOn(true, true);
                    }

                    updateUiElements();

                    mVideoToggleButton.setEnabled(true);
                }
            });
        }

        @Override
        public void onTypeChangeFailed() {
            if (!mFragmentAttachedToActivity) {
                return;
            }

            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCallPeers == null || mCurrentCallType == null) {
                        return;
                    }
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    String msg =
                            mCurrentCallPeers.get(0).getName() + " declined your request to " +
                                    (mCurrentCallType == Type.VIDEO ? "disable" : "enable") + " video";

                    builder.setMessage(msg).setCancelable(true).setNegativeButton("Dismiss",
                                                                                  new DialogInterface.OnClickListener() {
                                                                                      public void onClick(DialogInterface dialog,
                                                                                                          int id) {
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
        public void onTypeChangeRequested(final Type requestedType) {
            if (!mFragmentAttachedToActivity) {
                return;
            }

            // Dispatch to the thread that is managing UI state.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentCallPeers == null || mCurrentCallType == null) {
                        return;
                    }
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    String msg =
                            mCurrentCallPeers.get(0).getName() + " would like to " +
                                    (mCurrentCallType == Type.VIDEO ? "disable" : "enable") + " video";

                    builder.setMessage(msg).setCancelable(false).setPositiveButton("Accept",
                                                                                   new DialogInterface.OnClickListener() {
                                                                                       public void onClick(DialogInterface dialog,
                                                                                                           int id) {
                                                                                           dialog.cancel();
                                                                                           if (mCurrentCall != null) {
                                                                                               mCurrentCall.acceptCallTypeChange();
                                                                                           }
                                                                                       }
                                                                                   }).setNegativeButton("Decline",
                                                                                                        new DialogInterface.OnClickListener() {
                                                                                                            public void onClick(DialogInterface dialog,
                                                                                                                                int id) {
                                                                                                                dialog.cancel();
                                                                                                                if (mCurrentCall != null) {
                                                                                                                    mCurrentCall.rejectCallTypeChange();
                                                                                                                }
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
            Log.i(LOG_TAG, "Receiving video: " + receivingVideo);
        }

        @Override
        public void onRemoteCallHoldStateChanged(boolean held) {

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
            }
            else {
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


    /******************************************************************
     *************** Fragment lifecycle ******************************
     ******************************************************************/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG,"onCreate");
        
        Bundle args = getArguments();
        if (args != null) {
            mAllowBackgrounding = args.getBoolean(KEY_ALLOW_BACKGROUNDING);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create the nested call video fragment. This cannot be done earlier else the ChildFragmentManager won't have populated
        // it's internal children yet and the check for an existing child fragment will always fail, even if there is one stored.
        createCallVideoFragment();
        
        View view = inflater.inflate(R.layout.call_fragment, container, false);

        mTouchLockOverlay = (View)view.findViewById(R.id.call_touchlockoverlay);
        mTouchLockOverlay.setOnTouchListener(new MyTouchLockTouchListener());

        mOverlayIsOn = true;
        mOverlayPresentationState = PresentationState.PRESENTED;
        mShouldDismissCall = false;

        mProximitySensorListener = new MyProximityEventListener();

        mTopOverlay = (LinearLayout)view.findViewById(R.id.call_top_overlay);
        mNameText = (TextView)view.findViewById(R.id.call_name_text);
        mStatusText = (TextView)view.findViewById(R.id.call_status_text);

        mMiddleOverlay = (LinearLayout)view.findViewById(R.id.call_middle_overlay);
        mMuteButton = (ToggleButton)view.findViewById(R.id.call_mute_btn);
        mSpeakerButton = (ToggleButton)view.findViewById(R.id.call_speaker_btn);
        mVideoToggleButton = (Button)view.findViewById(R.id.toggleVideo);
        mAddUserButton = (Button)view.findViewById(R.id.addUser);

        mBottomOverlay = (LinearLayout)view.findViewById(R.id.call_bottom_overlay);
        mAcceptButton = (Button)view.findViewById(R.id.call_bottom_btn_accept);
        mRejectButton = (Button)view.findViewById(R.id.call_bottom_btn_reject);
        mCallBackButton = (Button)view.findViewById(R.id.call_bottom_btn_call_back);
        mReturnButton = (Button)view.findViewById(R.id.call_bottom_btn_return);
        mEndCallButton = (Button)view.findViewById(R.id.call_bottom_btn_end_call);
        
        mAcceptButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAcceptButtonClick(v);
            }
        });
        
        mRejectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRejectButtonClick(v);
            }
        });
        
        mCallBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBackButtonClick(v);
            }
        });
        
        mReturnButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onReturnButtonClick(v);
            }
        });
        
        mEndCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onEndCallButtonClick(v);
            }
        });

        mMuteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMuteButtonClick(v);
            }
        });

        mSpeakerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSpeakerphoneButtonClick(v);
            }
        });

        mVideoToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentCallType == Type.VIDEO) {
                    mCurrentCall.changeCallType(Type.VOICE);
                }
                else {
                    mCurrentCall.changeCallType(Type.VIDEO);
                }
                mVideoToggleButton.setEnabled(false);
            }
        });

        mAddUserButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Add User");
                alert.setMessage("");

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals(""))
                            return;
                        User user = new User(value, "Example user");
                        mCurrentCall.addUser(user);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                alert.show();
            }
        });



        // Ignore presses when held to the face
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);

        getActivity().setVolumeControlStream(mAudioStream);
        mRingtone = RingtoneManager.getRingtone(getActivity(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        mToneGenerator = new ToneGenerator(mAudioStream, ToneGenerator.MAX_VOLUME);
        mInboundRinging = false;
        mOutboundRinging = false;
        
        // we also want to listen for CallVideoFragment events
        mCallVideoFragment.setCallVideoFragmentListener(new OnCallVideoFragmentListener() {

            @Override
            public boolean onCameraPreviewTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCallVideoFragment.toggleCamera();
                }
                return true;
            }

            @Override
            public boolean onGLRemoteVideoViewTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    toggleOverlay();
                }
                return true;
            }
            
        });

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();

        resetSpeakerPhoneState();
        
        // Attempt to resume a stored call if it exists.
        if (sStoredCall != null) {
            if (mCurrentCall != null) {
                Log.w(LOG_TAG, "onResume: Have a stored call AND a current call..!");
                sStoredCall.end();
            }
            else {
                // Bring the stored call back as the current call
                Log.i(LOG_TAG, "Bringing back a stored call");
                if (resumeStoredCall() != null) { // the call resumed successfully
                    if (mCurrentCall.getCallState() == PhoneCallState.TERMINATED) {
                        stopCall();
                    }
                    return;
                }
            }
        }
        
        // If we have a call (e.g. via take/place call) then set up the UI.
        if (mCurrentCall != null) {
            if (mCurrentCall.getType() != null) {
                mCurrentCallType = mCurrentCall.getType();
            }
            mCurrentCallPeers = mCurrentCall.getPeers();
            updateUiElements();
        }
        else {
            failCall();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCurrentCall != null) {
            if (mAllowBackgrounding && mCurrentCall.getCallState() != PhoneCallState.TERMINATED) {
                // store the call statically to move the call out of the activity lifecycle and into the process lifecycle.
                Log.i(LOG_TAG,"Storing this call.");
                sStoredCall = mCurrentCall;
                mCurrentCall = null;
            }
            else {
                // You may not wish to hang up the call here if other activities can be displayed
                // whilst the call is ongoing
                hangupCall(CallTerminatedReason.LOCAL_HANGUP);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"onDestroy");
        
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
            mToneGenerator.release();
            mToneGenerator = null;
        }
        // We have to stop the ringtone and vibrator as android doesn't seem to maintain refs to them when the activity is recreated
        if (mRingtone != null) {
            mRingtone.stop();
        }
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
        
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentAttachedToActivity = false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFragmentAttachedToActivity = true;

        // Enforce that the activity implements our callbacks, else this will class cast.
        try {
            mListener = (OnCallFragmentListener)activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("The activity " + activity.getLocalClassName() +
                                         " does not implement OnCallFragmentListener.");
        }
    }
    
    private void createCallVideoFragment() {
        // Find the existing nested fragment or make a new one if it isn't found.
        mCallVideoFragment = (CallVideoFragment)getChildFragmentManager().findFragmentByTag(FRAG_TAG_CALL_VIDEO);
        
        if (mCallVideoFragment == null) {
            Log.i(LOG_TAG, "Creating new CallVideoFragment");
            mCallVideoFragment = new CallVideoFragment();
            Bundle videoArgs = new Bundle();
            videoArgs.putInt(CallVideoFragment.ARG_CALL_VIDEO_VIEW_LAYOUT, R.layout.call_video_fragment);
            videoArgs.putInt(CallVideoFragment.ARG_GLREMOTEVIDEOVIEW, R.id.call_remotevideoview);
            videoArgs.putInt(CallVideoFragment.ARG_CAMERAPREVIEW, R.id.call_camerapreview);
            mCallVideoFragment.setArguments(videoArgs);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            // Anchor the fragment to where the fragment container is.
            transaction.add(R.id.call_video_fragment_container, mCallVideoFragment, FRAG_TAG_CALL_VIDEO);
            transaction.commit();
        }
        else {
            Log.i(LOG_TAG, "CallVideoFragment already exists.");
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            // Anchor the fragment to where the fragment container is.
            transaction.replace(R.id.call_video_fragment_container, mCallVideoFragment, FRAG_TAG_CALL_VIDEO);
            transaction.commit();
        }
    }

}
