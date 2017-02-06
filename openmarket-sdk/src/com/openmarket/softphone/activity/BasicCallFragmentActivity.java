package com.openmarket.softphone.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.openmarket.softphone.PhoneCall;
import com.openmarket.softphone.PhoneCall.CameraOrientation;
import com.openmarket.softphone.PhoneCall.CameraType;
import com.openmarket.softphone.PhoneCall.Type;
import com.openmarket.softphone.PhoneCallListener;
import com.openmarket.softphone.R;
import com.openmarket.softphone.User;
import com.openmarket.softphone.fragments.CallVideoFragment;

/**
 * This activity implements all button logic and positioning, only leaving the camera preview and remote video view to the {@link CallVideoFragment}.
 * This can be copied and used directly in projects.
 */
public class BasicCallFragmentActivity extends FragmentActivity {

    public static final String ACTION_INCOMING = "com.openmarket.softphone.BasicCallFragmentActivity.INCOMING";
    public static final String ACTION_CALL = "com.openmarket.softphone.BasicCallFragmentActivity.CALL";

    public static final String EXTRA_USERS = "users";
    public static final String EXTRA_USER = "user";
    public static final String EXTRA_TYPE = "type";

    private CallVideoFragment mCallVideoFragment = null;
    private PhoneCall mCurrentCall;
    private Button mAccept;
    private Button mReject;
    private Button mEnd;
    private Button mTypeChange;
    private Button mAddUser;
    private ToggleButton mMuteButton;
    private ToggleButton mSpeakerphoneButton;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_call);
        
        mContext = this;
        
        createVideoFragment();
        
        // Store references to all the buttons that will be used.
        mAccept = (Button)findViewById(R.id.basic_call_accept);
        mReject = (Button)findViewById(R.id.basic_call_reject);
        mTypeChange = (Button)findViewById(R.id.basic_call_type_change);
        mEnd = (Button)findViewById(R.id.basic_call_end);
        mAddUser = (Button)findViewById(R.id.basic_call_add_user);
        mMuteButton = (ToggleButton)findViewById(R.id.basic_call_mute);
        mSpeakerphoneButton = (ToggleButton)findViewById(R.id.basic_call_speaker);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        handleIntent(getIntent());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (mCurrentCall != null) {
            mCurrentCall.end();
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    public void onRejectClick(View view) {
        if (mCurrentCall != null) {
            mCurrentCall.end();
        }
        finish();
    }

    public void onAcceptClick(View view) {
        if (mCurrentCall != null) {
            mCurrentCall.accept(); // accept the call
            mCallVideoFragment.startCall(); // start media flowing
            mCurrentCall.unmuteOutboundVideo(); // unmute by default when we accept calls
            enableInCallButtons();
        }
    }
    
    public void onEndClick(View view) {
        if (mCurrentCall != null) {
            mCurrentCall.end();
        }
        finish();
    }
    
    public void onTypeChangeClick(View view) {
        if (mCurrentCall != null) {
            // Select the opposite type to what we are currently.
            Type newType = Type.VIDEO;
            if (mCurrentCall.getType() == Type.VIDEO) {
                newType = Type.VOICE;
            }
            mCurrentCall.changeCallType(newType);
            mTypeChange.setEnabled(false); // disable type changes whilst this is pending
        }
    }
    
    public void onAddUserClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.om_add_user);
        alert.setMessage("");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (value.equals(""))
                    return;
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
    
    public void onSpeakerButtonClick(View view) {
        if (mCurrentCall != null) {
            boolean toggledState = !mCurrentCall.isSpeakerphoneOn();
            mCurrentCall.setSpeakerPhone(toggledState);
            mSpeakerphoneButton.setChecked(toggledState);
        }
    }
    
    public void onMuteButtonClick(View view) {
        if (mCurrentCall != null) {
            boolean isCurrentlyMuted = mCurrentCall.getAudioCaptureLevel() == 0.0;
            boolean wantMuted = !isCurrentlyMuted;
            mCurrentCall.setAudioCaptureVolume((wantMuted) ? 0.0f : 1.0f);
            mMuteButton.setChecked(wantMuted);
        }
    }
    
    /**
     * Creates a {@link CallVideoFragment} and add a listener for when the video is touched.
     */
    private void createVideoFragment() {
        // Make sure that the layout specified has a container
        if (findViewById(R.id.basic_call_video_fragment_container) != null) {
            // Dynamically add the CallVideoFragment (you CANNOT do this from XML as you must supply input args)
            // We don't need to check for existing fragments because we do not support backgrounding/rotations in this
            // example.
            mCallVideoFragment = new CallVideoFragment();
            
            Bundle videoArgs = new Bundle();
            // Specify the layout resource which has the remote video view and camera preview
            videoArgs.putInt(CallVideoFragment.ARG_CALL_VIDEO_VIEW_LAYOUT, R.layout.call_video_fragment);
            // Specify the ID of the remote video view within the layout
            videoArgs.putInt(CallVideoFragment.ARG_GLREMOTEVIDEOVIEW, R.id.call_remotevideoview);
            // Specify the ID of the camera preview within the layout
            videoArgs.putInt(CallVideoFragment.ARG_CAMERAPREVIEW, R.id.call_camerapreview);
            mCallVideoFragment.setArguments(videoArgs);
            
            // Toggle the camera when the camera preview is touched.
            mCallVideoFragment.setCallVideoFragmentListener(new CallVideoFragment.OnCallVideoFragmentListener() {
                @Override
                public boolean onCameraPreviewTouch(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mCallVideoFragment.toggleCamera();
                    }
                    return true;
                }
                @Override
                public boolean onGLRemoteVideoViewTouch(MotionEvent event) {
                    // You could bring up an overlay here.
                    return true;
                }
            });
    
            // Anchor the newly created fragment to basic_call_video_fragment_container
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.basic_call_video_fragment_container, mCallVideoFragment);
            transaction.commit();
        }
    }
    
    /**
     * Enables all the buttons which are valid when you are in a call, and disables ones which aren't valid when in a call.
     */
    private void enableInCallButtons() {
        mAccept.setEnabled(false);
        mReject.setEnabled(false);
        mEnd.setEnabled(true);
        mTypeChange.setEnabled(true);
        mAddUser.setEnabled(true);
        mMuteButton.setEnabled(true);
    }

    /**
     * Handle the intent used to spawn this activity. This should be either to place or take a call.
     * 
     * @param intent The intent which contains information on a call.
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();

        if (action.equals(ACTION_CALL)) {
            ArrayList<User> peers;
            if (intent.hasExtra(EXTRA_USERS)) {
                peers = intent.getParcelableArrayListExtra(EXTRA_USERS);
            }
            else {
                peers = new ArrayList<User>();
                peers.add((User)intent.getParcelableExtra(EXTRA_USER));
            }

            Type callType = (Type)intent.getSerializableExtra(EXTRA_TYPE);
            mCurrentCall = mCallVideoFragment.placeCall(peers, callType, new BasicPhoneCallListener());
            mAccept.setEnabled(false);
            mReject.setEnabled(false);
            mEnd.setEnabled(true);
        }
        else if (action.equals(ACTION_INCOMING)) {
            mCurrentCall = mCallVideoFragment.takeCall(intent, new BasicPhoneCallListener());
            mAccept.setEnabled(true);
            mReject.setEnabled(true);
            mEnd.setEnabled(false);
        }
        
        // these buttons are always disabled when setting up a call
        mTypeChange.setEnabled(false);
        mAddUser.setEnabled(false);
        mMuteButton.setEnabled(false);
        
        // make sure the speaker/mute states we think the call is in matches what it really is
        if (mCurrentCall != null) {
            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            final boolean speakerPhone = audioManager.isSpeakerphoneOn();
            mCurrentCall.setSpeakerPhone(speakerPhone);
            mCurrentCall.setAudioPlaybackVolume((mCurrentCall.getAudioCaptureLevel() == 0.0) ? 0.0f : 1.0f);
            mMuteButton.setChecked(mCurrentCall.getAudioCaptureLevel() == 0.0);
            mSpeakerphoneButton.setChecked(speakerPhone);
            
            // Hide the videos if this is a voice call
            if (mCurrentCall.getType() == Type.VOICE) {
                mCallVideoFragment.setCameraPreviewVisible(false);
                mCallVideoFragment.setRemoteVideoViewVisible(false);
            }
        }
    }
    
    /**
     * This listener is passed in to {@link CallVideoFragment} when a call is placed or taken. The fragment modifies this listener
     * so every callback occurs on the UI thread (detected via the fragment's getActivity() method) or on a thread specified by
     * a {@link android.os.Handler}. This example doesn't specify a custom Handler, so all the callbacks below will occur on the UI
     * thread. This is convenient as you can create dialogs and alter UI state without worrying about which thread the callback is on.
     * @see {@link CallVideoFragment#setCallbackHandler(android.os.Handler)
     */
    private class BasicPhoneCallListener implements PhoneCallListener {

        @Override
        public void onRemoteRinging() {}

        @Override
        public void onAnswered() {
            enableInCallButtons();
            mCallVideoFragment.startCall(); // tell the video fragment to start sending media
            mCurrentCall.unmuteOutboundVideo();
        }

        @Override
        public void onRemoteEnded(CallEndReason reason) {
            mCallVideoFragment.stopCall();
            finish();
        }

        @Override
        public void onFailed(CallErrorCode errorCode, String errorMessage) {
            mCallVideoFragment.stopCall();
            finish();
        }

        @Override
        public void onRemoteCameraMetadata(CameraType cameraType, CameraOrientation cameraOrientation) {
            // This is handled for us by CallVideoFragment
        }

        @Override
        public void onReceivingVideo(boolean receivingVideo) {}

        @Override
        public void onConferenceTransferStarted() {}

        @Override
        public void onConferenceCallStarted() {}

        @Override
        public void onTypeChanged(Type newType) {
            if (newType == Type.VIDEO) {
                mCallVideoFragment.setRemoteVideoViewVisible(true);
                mCallVideoFragment.setCameraPreviewVisible(true);
            }
            else {
                mCallVideoFragment.setRemoteVideoViewVisible(false);
                mCallVideoFragment.setCameraPreviewVisible(false);
            }
            mTypeChange.setEnabled(true);
        }

        @Override
        public void onTypeChangeFailed() {
            Toast.makeText(mContext, R.string.om_type_change_failed, Toast.LENGTH_SHORT).show();
            mTypeChange.setEnabled(true);
        }

        @Override
        public void onTypeChangeRequested(final Type requestedType) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.om_change_call_to
                    + (requestedType == Type.VIDEO ? R.string.om_video : R.string.om_voice) + "?");
            builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mCurrentCall.acceptCallTypeChange();
                }
            });
            
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mCurrentCall.rejectCallTypeChange();
                }
            });
            builder.setCancelable(false);
            AlertDialog alert = builder.create();
            alert.show();
        }

        @Override
        public void onConferenceStateChanged(User user) {}

        @Override
        public void onRemoteCallHoldStateChanged(boolean held) {
        }
    }
}
