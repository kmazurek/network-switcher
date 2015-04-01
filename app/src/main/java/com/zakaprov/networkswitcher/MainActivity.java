package com.zakaprov.networkswitcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.estimote.sdk.BeaconManager;


public class MainActivity extends ActionBarActivity
{
    public static final String APP_START_INTENT = "com.zakaprov.networkswitcher.AppStartIntent";

    private ToggleButton btnToggleReceiver;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggleReceiver = (ToggleButton) findViewById(R.id.toggleReceiverState);
        btnStop = (Button) findViewById(R.id.btnStop);

        setUpListeners();
        sendBroadcast(new Intent(APP_START_INTENT));
    }

    private void setUpListeners()
    {
        btnToggleReceiver.setChecked(isReceiverEnabled());

        btnToggleReceiver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                setReceiverState(isChecked);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                BeaconManager manager = BeaconServiceReceiver.getBeaconManager(MainActivity.this);
                manager.disconnect();
            }
        });
    }

    private boolean isReceiverEnabled()
    {
        return SharedPrefs.getSharedPref(this, SharedPrefs.RECEIVER_ENABLED_KEY);
    }

    private void setReceiverState(boolean state)
    {
        int newState = state ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        ComponentName receiver = new ComponentName(this, BeaconServiceReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver, newState, PackageManager.DONT_KILL_APP);
        SharedPrefs.setSharedPref(this, SharedPrefs.RECEIVER_ENABLED_KEY, state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
