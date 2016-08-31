package com.example.taek.commutingchecker.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.SocketIO;

/**
 * Created by Awesometic on 2016-07-02.
 */
public class EntryActivity extends AppCompatActivity {

    private SignupFragment fragSignup;
    private WaitFragment fragWait;

    public static SocketIO mSocket;

    public static String smartphoneAddr;
    public static boolean amIRegistered;
    public static boolean permitted;
    public static String employee_number;
    public static String employee_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Fragments
        fragSignup = SignupFragment.newInstance();
        fragWait = WaitFragment.newInstance();

        FragmentManager fragmentManager = getFragmentManager();

        try {
            mSocket = new SocketIO();
            mSocket.connect();
            do {
                Thread.sleep(100);
            } while (mSocket.connected() == false);

            // Getting a public key from server
            mSocket.getServersRsaPublicKey();
            do {
                Thread.sleep(100);
            } while (mSocket.isServersPublicKeyInitialized() == false);

            smartphoneAddr = MainActivity.myMacAddress;
            mSocket.amIRegistered(smartphoneAddr);
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (amIRegistered == false) {
            // If not registered
            fragmentManager.beginTransaction()
                    .replace(R.id.content_entry_frame, fragSignup)
                    .detach(fragSignup).attach(fragSignup)
                    .commit();
            MainActivity.mainActivity.finish();

        } else if (permitted == false) {
            // If registered but not permitted
            fragmentManager.beginTransaction()
                    .replace(R.id.content_entry_frame, fragWait)
                    .detach(fragWait).attach(fragWait)
                    .commit();
            MainActivity.mainActivity.finish();

        } else {
            // If registered and permitted
            this.finish();

        }
    }

    @Override
    protected void onDestroy(){
        if (mSocket.connected() == true)
            mSocket.close();
        super.onDestroy();
    }
}