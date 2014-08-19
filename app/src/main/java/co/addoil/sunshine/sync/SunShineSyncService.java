package co.addoil.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by chandominic on 20/8/14.
 */
public class SunShineSyncService extends Service {

    public static final String TAG = SunShineSyncService.class.getSimpleName();
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    // Storage for an instance of the sync adapter
    private static SunShineSyncAdapter sSyncAdapter = null;

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        Log.d(TAG, "onCreate " + TAG);

        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SunShineSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
