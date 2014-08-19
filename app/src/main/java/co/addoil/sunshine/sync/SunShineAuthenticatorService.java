package co.addoil.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by chandominic on 19/8/14.
 */
public class SunShineAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private SunShineAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new SunShineAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
