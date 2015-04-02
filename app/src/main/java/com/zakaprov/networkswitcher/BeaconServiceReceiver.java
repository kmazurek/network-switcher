package com.zakaprov.networkswitcher;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;

public class BeaconServiceReceiver extends BroadcastReceiver
{
    private enum ScanningModes
    {
        HOME_MODE, AWAY_MODE
    }

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final String ESTIMOTE_SERVICE_NAME = "com.estimote.sdk.service.BeaconService";
    private static final Integer MAJOR = 13371;
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("rid", ESTIMOTE_PROXIMITY_UUID, null, null);

    private static BeaconManager beaconManager;

    public static BeaconManager getBeaconManager(Context context)
    {
            if (beaconManager == null)
                beaconManager = new BeaconManager(context.getApplicationContext());

        return beaconManager;
    }

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        String intentAction = intent.getAction();

            if (intentAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    if (state == BluetoothAdapter.STATE_ON)
                        tryStartService(context);
            }
            else if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED) ||
                intentAction.equals(MainActivity.APP_START_INTENT) ||
                intentAction.equals(Intent.ACTION_USER_PRESENT))
            {
                    tryStartService(context);
            }
    }

    private void tryStartService(Context context)
    {
        if (!isServiceRunning(context) && isBluetoothAvailable(context))
        {
            beaconManager = getBeaconManager(context);
            beaconManager.setMonitoringListener(new CustomMonitoringListener(context));

            openConnection();
        }
    }

    private boolean isServiceRunning(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

            for (ActivityManager.RunningServiceInfo serviceInfo : runningServices)
            {
                if (serviceInfo.service.getClassName().equalsIgnoreCase(ESTIMOTE_SERVICE_NAME))
                    return true;
            }

        return false;
    }

    private boolean isBluetoothAvailable(Context context)
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled())
            {
                fireNotification(context);
                return false;
            }

        return true;
    }

    private void fireNotification(Context context)
    {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_bluetooth)
                        .setContentTitle("Bluetooth is disabled")
                        .setContentText("Please enable Bluetooth in order to use the NetworkSwitcher.")
                        .setAutoCancel(true);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("Please enable Bluetooth in order to use the NetworkSwitcher.");
        notificationBuilder.setStyle(style);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, enableBtIntent, 0);
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(1337, notificationBuilder.build());
    }

    private void openConnection()
    {
        setScanningMode(ScanningModes.AWAY_MODE);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady()
            {
                try
                {
                    beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void closeConnection()
    {
        beaconManager.disconnect();
    }

    private void setScanningMode(ScanningModes mode)
    {
        switch(mode)
        {
            case HOME_MODE:
                beaconManager.setBackgroundScanPeriod(10 * 1000, 30 * 1000);
                break;
            case AWAY_MODE:
                beaconManager.setBackgroundScanPeriod(5 * 1000, 15 * 1000);
                break;
        }
    }

    private class CustomMonitoringListener implements BeaconManager.MonitoringListener
    {
        private Context context;

        public CustomMonitoringListener(Context context)
        {
            this.context = context;
        }

        @Override
        public void onEnteredRegion(Region region, List<Beacon> beacons)
        {
            LogUtil.d("onEnteredRegion. Region entered: " + region.toString());

            for (Beacon beacon : beacons)
            {
                LogUtil.d(beacon.toString());

                if (beacon.getMajor() == MAJOR)
                {
                    WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    manager.setWifiEnabled(true);

                    setScanningMode(ScanningModes.HOME_MODE);
                }
            }
        }

        @Override
        public void onExitedRegion(Region region)
        {
            LogUtil.d("onExitedRegion. Region exited: " + region.toString());

            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            manager.setWifiEnabled(false);

            setScanningMode(ScanningModes.AWAY_MODE);
        }
    }
}
