// $Id$
// Copyright 2011 OpenMarket Limited
// Evaluation SDK - all materials STRICTLY CONFIDENTIAL AND PROPRIETARY
package com.openmarket.softphone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.openmarket.softphone.PhoneManager.LoginState;
import com.openmarket.softphone.activity.CallActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Singleton that is used to perform actions such as login, logout and placing calls.
 * Similar to {@link PhoneManager} but handles a lot more for you.
 */
public class SimplePhoneManager {
    
    private boolean mMakingCall;

    public static SimplePhoneManager getInstance() {
        if (sInstance == null) {
            sInstance = new SimplePhoneManager();
        }
        return sInstance;
    }

    /** Sets custom PhoneManagerSettings.
     * @deprecated Use {@link #login(Credentials, Context, PendingIntent, PhoneLoginListener, PhoneManagerSettings)} instead.
     * @param settings The PhoneManagerSettings to use.
     */
    public void setSettings(PhoneManagerSettings settings) {
        mPhoneManager.setSettings(settings);
    }

    /**
     * Starts the phone.
     * @param context any context.
     */
    public void start(Context context) {
        mPhoneManager.start(context);
    }
    
    /**
     * Starts the phone 
     * @param context any context
     * @param mimeTypes a list of MIME types to enable.
     */
    public void start(Context context, List<String> mimeTypes) {
        mPhoneManager.start(context, mimeTypes.toArray(new String[] {}));
    }
    
    /**
     * Starts the phone 
     * @param context any context
     * @param mimeTypes an array of MIME types to enable.
     * @param settings Custom override settings to use.
     */
    public void start(Context context, String[] mimeTypes, OverrideDeviceSettings settings) {
        mPhoneManager.start(context, mimeTypes, settings);
    }
    
    /**
     * Starts the phone.
     * @param context any context.
     * @param settings Custom override settings to use.
     */
    public void start(Context context, OverrideDeviceSettings settings) {
        mPhoneManager.start(context, settings);
    }
    
    /**
     * Starts the phone 
     * @param context any context
     * @param mimeTypes a list of MIME types to enable.
     * @param settings Custom override settings to use.
     */
    public void start(Context context, List<String> mimeTypes, OverrideDeviceSettings settings) {
        mPhoneManager.start(context, mimeTypes.toArray(new String[] {}), settings);
    }
    
    /**
     * Starts the phone 
     * @param context any context
     * @param mimeTypes an array of MIME types to enable.
     */
    public void start(Context context, String[] mimeTypes) {
        mPhoneManager.start(context, mimeTypes);
    }
    
    /**
     * Stops the phone.
     */
    public void stop() {
        mPhoneManager.stop();
    }

    /**
     * Logs a user into the OpenMarket Telephony Platform
     * Only one user may be logged in at a time.
     * A user needs to be logged in before they can place or receive calls. 
     * @param credentials credentials for a user on the OpenMarket Telephony Platform.
     * @param context the context in which to receive incoming calls.
     * @param listener a callback for when the phone finishes logging in.
     * @see Credentials
     * @see PhoneLoginListener
     */
    public void login(Credentials credentials, Context context, PhoneLoginListener listener) {
        login(credentials, context, listener, null);
    }
    
    /**
     * Logs a user into the OpenMarket Telephony Platform
     * Only one user may be logged in at a time.
     * A user needs to be logged in before they can place or receive calls. 
     * @param credentials credentials for a user on the OpenMarket Telephony Platform.
     * @param context the context in which to receive incoming calls.
     * @param listener a callback for when the phone finishes logging in.
     * @param settings the PhoneManagerSettings to use when logging in.
     * @see Credentials
     * @see PhoneLoginListener
     */
    public void login(Credentials credentials, Context context, PhoneLoginListener listener, PhoneManagerSettings settings) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.setAction(CallActivity.ACTION_INCOMING);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, Intent.FILL_IN_DATA);
        mPhoneManager.login(credentials, context, pendingIntent, listener, settings);
    }

    /**
     * Logs the current user out of the OpenMarket Telephony Platform.
     */
    public void logout() {
        mPhoneManager.logout();
    }

    /**
     * Get the state of the current user.
     */
    public LoginState getLoginState() {
        return mPhoneManager.getLoginState();
    }
    
    /** 
     * Manages the phone manager when the activity is no longer in the foreground.
     *  The user will be logged off if they are not placing a call or about to receive a call.
     *  This should be called in the activity onPause. In order to receive calls when the activity
     *  is not in the foreground, use push notifications via Google Cloud Messaging (GCM).
     */
    public void onPause(){
        if (mPhoneManager.isCallIncoming() || mMakingCall) {
            //do not logoff the user
            Log.d("SimplePhoneManager","Detected onPause due to incoming/outgoing call.");
        }
        else{
            //no incoming/outgoing calls, safe to log the user out. They should be using push notifications.
            Log.d("SimplePhoneManager","No incoming/outgoing calls detected onPause, logging off.");
            logout();
            stop();
        }
        mMakingCall = false;
    }
    
    /**
     * Place an outbound call.
     * @param context the context in which to start the call.
     * @param toCall the user to call.
     * @param type whether the call is should be a voice call or a video call.
     * @throws PhoneException
     * @see User
     * @see PhoneCall.Type
     */
    public void placeCall(Context context, User toCall, PhoneCall.Type type) {
        this.placeCall(context, (ArrayList<User>)Arrays.asList(new User[] {toCall}), type);
    }

    /**
     * Place an outbound call to one or more users.
     * @param context the context in which to start the call.
     * @param toCall the users to call.
     * @param type whether the call is should be a voice call or a video call.
     * @throws PhoneException
     * @see User
     * @see PhoneCall.Type
     */
    public void placeCall(Context context, ArrayList<User> toCall, PhoneCall.Type type) {
        mMakingCall = true;
        Intent callIntent = new Intent(context, CallActivity.class);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setAction(CallActivity.ACTION_CALL);
        callIntent.putParcelableArrayListExtra(CallActivity.EXTRA_USERS, toCall);
        callIntent.putExtra(CallActivity.EXTRA_TYPE, type);
        context.startActivity(callIntent);
    }

    private static SimplePhoneManager sInstance;

    protected PhoneManager mPhoneManager;

    protected SimplePhoneManager() {
        mPhoneManager = PhoneManager.getInstance();
    }

}
