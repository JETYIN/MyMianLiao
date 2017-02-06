package com.openmarket.softphone.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.openmarket.softphone.PhoneCall.Type;
import com.openmarket.softphone.R;
import com.openmarket.softphone.User;
import com.openmarket.softphone.fragments.CallFragment;

/**
 * This activity contains a single {@link CallFragment} which manages the entire call, including the on-screen buttons. The activity is only responsible
 * for placing or taking a call, and responding to when the call is over. This makes the Activity very lightweight, as all the logic is contained within
 * CallFragment. 
 */
public class CallFragmentActivity extends FragmentActivity implements CallFragment.OnCallFragmentListener {

    public static final String ACTION_INCOMING = "com.openmarket.softphone.CallFragmentActivity.INCOMING";
    public static final String ACTION_CALL = "com.openmarket.softphone.CallFragmentActivity.CALL";

    public static final String EXTRA_USERS = "users";
    public static final String EXTRA_USER = "user";
    public static final String EXTRA_TYPE = "type";
    
    public static final String CALL_FRAGMENT_TAG = "call_frag";

    CallFragment mCallFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_fragment_activity);

        // Make sure that the layout specified has a container
        if (findViewById(R.id.fragment_container) != null) {
            // try to find an existing call fragment
            mCallFragment = (CallFragment)getSupportFragmentManager().findFragmentByTag(CALL_FRAGMENT_TAG);
            if (mCallFragment == null) {
                // Dynamically add the call fragment (you could also do this in XML)
                mCallFragment = new CallFragment();
                Bundle args = new Bundle();
                // allow calls to be backgrounded via the HOME button without terminating.
                args.putBoolean(CallFragment.KEY_ALLOW_BACKGROUNDING, true);
                mCallFragment.setArguments(args);
    
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_container, mCallFragment, CALL_FRAGMENT_TAG);
                transaction.commit();
            }
            else {
                // bring the existing fragment back
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, mCallFragment, CALL_FRAGMENT_TAG);
                transaction.commit();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Manually handle configuration changes. Inspiration taken from Android's Phone app by setting:
     * android:configChanges="orientation|keyboardHidden" such that we don't get destroyed and recreated on those config
     * changes
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Handle the intent used to spawn this activity. This should be either to place or take a call.
     * 
     * @param intent The intent which contains information on a call.
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            setIntent(null);
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
            mCallFragment.placeCall(peers, callType);
        }
        else if (action.equals(ACTION_INCOMING)) {
            mCallFragment.takeCall(intent);
        }
        setIntent(null);
    }

    @Override
    public void onCallFragmentFinished() {
        // You could just hide the fragment, but let's quit instead.
        finish();
    }

}
