// Copyright 2013 OpenMarket
package com.openmarket.softphone.fragments;

import java.util.List;

import com.openmarket.softphone.PhoneCall;
import com.openmarket.softphone.PhoneCall.CameraOrientation;
import com.openmarket.softphone.PhoneCall.CameraType;
import com.openmarket.softphone.PhoneCall.Type;
import com.openmarket.softphone.PhoneCallListener;
import com.openmarket.softphone.PhoneException;
import com.openmarket.softphone.PhoneManager;
import com.openmarket.softphone.User;
import com.openmarket.softphone.view.CameraPreview;
import com.openmarket.softphone.view.GLRemoteVideoView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A basic video fragment which just maintains the local preview and the remote video view (including rotations). This fragment
 * can either be nested in another fragment (see {@link CallFragment}) or it can be used on its own in an Activity (see 
 * {@link com.openmarket.softphone.activity.BasicCallFragmentActivity}).
 */
public class CallVideoFragment extends Fragment {
    private static final String LOG_TAG = "CallVideoFragment";
    
    /**
     * The resource ID for the layout which contains ARG_GLREMOTEVIDEOVIEW and ARG_CAMERAPREVIEW
     */
    public static final String ARG_CALL_VIDEO_VIEW_LAYOUT = "call_layout";
    
    /**
     * The resource ID for the GLRemoteVideoView contained within ARG_CALL_VIDEO_VIEW_LAYOUT
     */
    public static final String ARG_GLREMOTEVIDEOVIEW = "gl_remote_video_view";
    
    /**
     * The resource ID for the CameraPreview contained within ARG_CALL_VIDEO_VIEW_LAYOUT
     */
    public static final String ARG_CAMERAPREVIEW = "camera_preview";
    
    /**
     * This provides touch events from the views to the underlying activity or fragment.
     */
    public static interface OnCallVideoFragmentListener {
        /**
         * Triggered when the camera preview is touched.
         * @param event The motion event corresponding to the preview.
         * @return True if the listener has consumed the event, false otherwise.
         */
        public boolean onCameraPreviewTouch(MotionEvent event);
        
        /**
         * Triggered when the remote video view is touched.
         * @param event The motion event corresponding to the view.
         * @return True if the listener has consumed the event, false otherwise.
         */
        public boolean onGLRemoteVideoViewTouch(MotionEvent event);
    }
    
    private CameraPreview mCameraPreview;
    private GLRemoteVideoView mRemoteVideoView;
    private OnCallVideoFragmentListener mListener;
    private PhoneCall mCurrentCall;
    private CameraOrientation mCurrentOri = CameraOrientation.UNKNOWN;
    private MyOrientationEventListener mOrientationEventListener;
    private Handler mCallbackHandler;
    
    public CallVideoFragment() {
        // intentionally blank, fragments must have blank constructors
    }
    
    /**
     * <p>Places a call via the PhoneManager to the peers given and type of call specified.</p>
     * 
     * <p>NB: Any {@link PhoneCallListener} provided will have all callbacks triggered on the UI thread. To set a different thread
     * for callbacks, see {@link CallVideoFragment#setCallbackHandler(Handler)}.</p>
     * 
     * @param peers The peers to call. This will do nothing if this is null or empty.
     * @param typeOfCall The type of call to make.
     * @param listener The PhoneCallListener to monitor phone call events for this call.
     * @return The call just placed, or null if it failed.
     */
    public PhoneCall placeCall(List<User> peers, PhoneCall.Type typeOfCall, PhoneCallListener listener) {
        try {
            // Wrap their listener in our own one so we can handle the onRemoteCameraMetadata callback for them.
            mCurrentCall = PhoneManager.getInstance().placeCall(peers, typeOfCall, new CallVideoFragmentPhoneCallListener(listener));
        }
        catch (PhoneException e) {
            mCurrentCall = null;
        }

        return mCurrentCall;
    }
    
    /**
     * <p>Takes a call via the PhoneManager via the Intent provided to you.</p>
     * 
     * <p>NB: Any {@link PhoneCallListener} provided will have all callbacks triggered on the UI thread. To set a different thread
     * for callbacks, see {@link CallVideoFragment#setCallbackHandler(Handler)}.</p>
     * 
     * @param intent The Intent provided to you.
     * @param listener The PhoneCallListener to monitor phone call events for this call.
     * @return The call just placed, or null if it failed.
     */
    public PhoneCall takeCall(Intent intent, PhoneCallListener listener) {
        // Wrap their listener in our own one so we can handle the onRemoteCameraMetadata callback for them.
        mCurrentCall = PhoneManager.getInstance().takeCall(intent, new CallVideoFragmentPhoneCallListener(listener));
        return mCurrentCall;
    }
    
    /**
     * Links this fragment with an existing PhoneCall. Useful when resuming a backgrounded call.
     * @param call The PhoneCall to resume. Must not be null.
     * @param listener The PhoneCallListener to attach to the PhoneCall. Must not be null.
     */
    public void resumeCall(PhoneCall call, PhoneCallListener listener) {
        if (call == null || listener == null) {
            throw new NullPointerException("Cannot resume a call without a valid PhoneCall and PhoneCallListener");
        }
        mCurrentCall = call;
        mCurrentCall.setPhoneCallListener(new CallVideoFragmentPhoneCallListener(listener));
    }
    
    /**
     * Inform the fragment that the call has started and that it should be displaying and transmitting video.
     */
    public void startCall() {
        if (mCurrentCall != null) {
            mCurrentCall.setCameraPreview(mCameraPreview);
            mCurrentCall.setRemoteVideoView(mRemoteVideoView);
        }
        setOrientationEventListenerEnabled(true);
    }
    
    /**
     * Inform the fragment that the call has ended and that it should stop showing video and transmitting.
     */
    public void stopCall() {
        if (mCurrentCall != null) {
            mCurrentCall.setCameraPreview(null);
            mCurrentCall.setRemoteVideoView(null);
        }
        setOrientationEventListenerEnabled(false);
        mCurrentCall = null;
        
        setRemoteVideoViewVisible(false);
        setCameraPreviewVisible(false);
    }
    
    /**
     * Toggles the front and back camera, if the device has both.
     */
    public void toggleCamera() {
        if (mCameraPreview != null) {
            mCameraPreview.toggleCamera();
        }
    }
    
    /**
     * <p>Set a custom handler to use for all callbacks. </p>
     * When you take or place a call through this fragment, the {@link PhoneCallListener} you provide is edited to force all
     * callbacks to go through the UI thread via the fragment's attached Activity Handler. You can
     * override this behaviour to use a different thread by setting your own Handler to use here.
     * @param handler The new handler which will receive all callbacks from {@link PhoneCallListener}.
     * @see #placeCall(List, Type, PhoneCallListener)
     * @see #takeCall(Intent, PhoneCallListener)
     */
    public void setCallbackHandler(Handler handler) {
        mCallbackHandler = handler;
    }
    
    /**
     * Set the CallVideoFragmentListener which will listen for video-related events coming from this fragment.
     * @param listener The listener to attach.
     */
    public void setCallVideoFragmentListener(OnCallVideoFragmentListener listener) {
        mListener = listener;
    }
    
    /**
     * Set whether the camera preview window should be shown or not.
     * @param visible true to make it visible, false to make it invisible.
     */
    public void setCameraPreviewVisible(boolean visible) {
        if (mCameraPreview == null) {
            return;
        }
        
        if (visible) {
            mCameraPreview.setVisibility(View.VISIBLE);
        }
        else {
            mCameraPreview.setVisibility(View.GONE);
        }
    }
    
    /**
     * Set whether the remote video view should be shown or not.
     * @param visible true to make it visible, false to make it invisible.
     */
    public void setRemoteVideoViewVisible(boolean visible) {
        if (mRemoteVideoView == null) {
            return;
        }
        
        if (visible) {
            mRemoteVideoView.setVisibility(View.VISIBLE);
        }
        else {
            mRemoteVideoView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle inputArgs = getArguments();
        
        if (inputArgs == null || !inputArgs.containsKey(ARG_CAMERAPREVIEW) || !inputArgs.containsKey(ARG_GLREMOTEVIDEOVIEW) ||
                !inputArgs.containsKey(ARG_CALL_VIDEO_VIEW_LAYOUT)) {
            throw new NullPointerException("The resource IDs of the call layout, CameraPreview and GLRemoteVideoView must be supplied as arguments.");
        }

        int cameraPreviewResId = inputArgs.getInt(ARG_CAMERAPREVIEW);
        int remoteVideoViewResId = inputArgs.getInt(ARG_GLREMOTEVIDEOVIEW);
        int callLayoutResId = inputArgs.getInt(ARG_CALL_VIDEO_VIEW_LAYOUT);

        View view = inflater.inflate(callLayoutResId, container, false);
        
        mCameraPreview = (CameraPreview)view.findViewById(cameraPreviewResId);
        mRemoteVideoView = (GLRemoteVideoView)view.findViewById(remoteVideoViewResId);
        
        mCameraPreview.setZOrderMediaOverlay(true);
        mRemoteVideoView.setZOrderMediaOverlay(false);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int previewWidth = width / 3;
        int previewHeight = (previewWidth * 4) / 3;
        float scale = getActivity().getResources().getDisplayMetrics().density;
        int margin = (int)(10 * scale + 0.5f);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(previewWidth, previewHeight, Gravity.BOTTOM | Gravity.LEFT);
        layoutParams.setMargins(margin, margin, margin, margin);
        mCameraPreview.setLayoutParams(layoutParams);

        mCameraPreview.setListener(new CameraPreview.CameraPreviewListener() {
            @Override
            public void onPreviewSizeChanged(int width, int height, int padding) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
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
                        }
                        else if ((width % divisor == 0) && (height % divisor == 0)) {
                            int dPreviewWidth = width / divisor;
                            int dPreviewHeight = height / divisor;

                            if (dPreviewWidth >= lowerPreviewWidth && dPreviewWidth <= upperPreviewWidth) {
                                int thisDiffFromOptimal =
                                        Math.abs((int)dPreviewWidth - optimalPreviewWidth) +
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
                }
                else {
                    int multiplier = 1;
                    int bestDiffFromOptimal = Integer.MAX_VALUE;
                    while (true) {
                        int dPreviewWidth = width * multiplier;
                        int dPreviewHeight = height * multiplier;

                        if (dPreviewWidth > upperPreviewWidth || dPreviewHeight > upperPreviewHeight) {
                            break;
                        }
                        else {
                            int thisDiffFromOptimal =
                                    Math.abs((int)dPreviewWidth - optimalPreviewWidth) +
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
                        (height >= lowerPreviewHeight && height <= upperPreviewHeight)) {
                        previewWidth = width;
                        previewHeight = height;
                    }
                    else {
                        previewWidth = optimalPreviewWidth;
                        previewHeight = (previewWidth * height) / width;
                    }
                }

                float scale = getActivity().getResources().getDisplayMetrics().density;
                int margin = (int)(10 * scale + 0.5f);
                FrameLayout.LayoutParams layoutParams =
                        new FrameLayout.LayoutParams(previewWidth + (padding * 2), previewHeight + (padding * 2),
                                                     Gravity.BOTTOM | Gravity.LEFT);
                layoutParams.setMargins(margin, margin, margin, margin);
                mCameraPreview.setLayoutParams(layoutParams);
            }
        });
        
        mRemoteVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mListener != null) {
                    return mListener.onGLRemoteVideoViewTouch(event);
                }
                return false;
            }
        });
        
        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mListener != null) {
                    return mListener.onCameraPreviewTouch(event);
                }
                return false;
            }
        });
        
        mOrientationEventListener = new MyOrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL);
        
        return view;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
        
        if (mCameraPreview != null) {
            mCameraPreview.onPause();
        }

        if (mRemoteVideoView != null) {
            mRemoteVideoView.onPause();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        
        if (mCameraPreview != null) {
            mCameraPreview.onResume();
        }

        if (mRemoteVideoView != null) {
            mRemoteVideoView.onResume();
        }
        
        if (mCurrentCall != null) {
            mCurrentCall.setCameraPreview(mCameraPreview);
            mCurrentCall.setRemoteVideoView(mRemoteVideoView);
        }
        
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    private void setOrientationEventListenerEnabled(boolean enabled) {
        if (enabled) {
            // XXX: this is a complete bodge
            // default our orientation to 'natural' on the S2 in case the listener doesn't fire immediately
            mOrientationEventListener.onOrientationChanged(0);
            mOrientationEventListener.enable();
        }
        else {
            mOrientationEventListener.disable();
        }
    }
    
    /**
     * Utility method to post callbacks, as various checks must be done before the callbacks are fired.
     * @param runnable The runnable which encapsulates the callback.
     */
    private void postCallback(Runnable runnable) {
        if (mCallbackHandler != null) {
            // Use the custom handler preferably.
            mCallbackHandler.post(runnable);
        }
        else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(runnable); 
            }
            else {
                Log.e(LOG_TAG,"Unable to post callback onto attached activity: getActivity() is null.");
            }
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
            Activity activity = getActivity();
            if (activity == null) {
                // we've been detached from the activity
                return;
            }

            mCameraPreview.setDisplayRotationEnum(activity.getWindowManager().getDefaultDisplay().getOrientation());
            mRemoteVideoView.setDisplayRotationEnum(activity.getWindowManager().getDefaultDisplay().getOrientation());

            // Update the device rotation for the camera preview and the remote video view so they can can compensate
            // for how the device is being held.
            mCameraPreview.setDeviceRotationDegrees(orientation);
            mRemoteVideoView.setDeviceRotation(orientation);

            if (mCurrentCall == null)
                return;
            if (orientation == ORIENTATION_UNKNOWN)
                return;

            // The best orientation to view the video we are capturing may have changed.
            CameraOrientation newOri = mCameraPreview.getCameraOrientationEnum();

            if (mCurrentOri != newOri && mCurrentCall != null) {
                mCurrentOri = newOri;
                // If the orientation has changed tell the remote device what the new orientation is.
                mCurrentCall.setDeviceOrientation(mCurrentOri);
            }
        }
    }
    
    /**
     * Wraps an existing phone call listener and adds in our listening events.
     */
    private class CallVideoFragmentPhoneCallListener implements PhoneCallListener {
        
        private PhoneCallListener mExistingListener;
        
        public CallVideoFragmentPhoneCallListener(PhoneCallListener listener) {
            mExistingListener = listener;
        }

        @Override
        public void onRemoteRinging() {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onRemoteRinging();
                    }
                });
            }
        }

        @Override
        public void onAnswered() {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onAnswered();
                    }
                });
            }
        }

        @Override
        public void onRemoteEnded(final CallEndReason reason) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onRemoteEnded(reason);
                    }
                });
            }
        }

        @Override
        public void onFailed(final CallErrorCode errorCode, final String errorMessage) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onFailed(errorCode, errorMessage);
                    }
                });
            }
        }

        @Override
        public void onRemoteCameraMetadata(final CameraType cameraType, final CameraOrientation cameraOrientation) {
            Activity activity = getActivity();
            
            if (activity != null) {
                // Dispatch to the thread that is managing UI state.
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (cameraOrientation) {
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
            
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onRemoteCameraMetadata(cameraType, cameraOrientation);
                    }
                });
            }
        }

        @Override
        public void onReceivingVideo(final boolean receivingVideo) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onReceivingVideo(receivingVideo);
                    }
                });
            }
        }

        @Override
        public void onConferenceTransferStarted() {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onConferenceTransferStarted();
                    }
                });
            }
        }

        @Override
        public void onConferenceCallStarted() {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onConferenceCallStarted();
                    }
                });
            }
        }

        @Override
        public void onTypeChanged(final Type newType) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onTypeChanged(newType);
                    }
                });
            }
        }

        @Override
        public void onTypeChangeFailed() {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onTypeChangeFailed();
                    }
                });
            }
        }

        @Override
        public void onTypeChangeRequested(final Type requestedType) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onTypeChangeRequested(requestedType);
                    }
                });
            }
        }

        @Override
        public void onConferenceStateChanged(final User user) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onConferenceStateChanged(user);
                    }
                });
            }
        }

        @Override
        public void onRemoteCallHoldStateChanged(final boolean held) {
            if (mExistingListener != null) {
                postCallback(new Runnable() {
                    @Override
                    public void run() {
                        mExistingListener.onRemoteCallHoldStateChanged(held);
                    }
                });
            }
        }
        
    }
}
