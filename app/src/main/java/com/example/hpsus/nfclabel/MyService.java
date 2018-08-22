package com.example.hpsus.nfclabel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    public Timer myTimer = new Timer();
    public int NOTIFY_ID = 1;
    public Date dateNow;
    public Context context;
    public PendingIntent contentIntent;
    public Notification.Builder builder;
    public NotificationManager nm;
    public Notification n;
    private NfcAdapter nfcAdapter;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE","onCreate");

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("NfcLabel")
                .setContentText("monitoring...")
                .setContentIntent(pendingIntent).build();

        startForeground(1037, notification);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();

        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dateNow = new Date();
                NOTIFY_ID = 1;

                SendMessage("ID", ReadID(), NOTIFY_ID);
                NOTIFY_ID = NOTIFY_ID + 1;

                SystemClock.sleep(60000);
            }
        },0,1);
        return Service.START_STICKY_COMPATIBILITY;
    }

    public void SendMessage(CharSequence title, CharSequence mess, Integer NOTIFY_ID) {


        nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);


        builder.setContentIntent(contentIntent)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                //.setLargeIcon(bitmap)
                .setTicker(mess)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText("TAG: "+mess); // Текст уведомления

        n = builder.getNotification();

        n.defaults = Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE;

        nm.notify(NOTIFY_ID, n);


    };

    public  String ReadID() {
        Intent intent = new Intent();
        String action = intent.getAction();
        String res=null;

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Toast.makeText(this,
                    "onResume() - ACTION_TAG_DISCOVERED",
                    Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag == null){
               // textViewInfo.setText("tag == null");
            }else{
                String tagInfo = tag.toString() + "\n";

                tagInfo += "\nTag Id: \n";
                byte[] tagId = tag.getId();
                tagInfo += "length = " + tagId.length +"\n";
                for(int i=0; i<tagId.length; i++){
                    tagInfo += Integer.toHexString(tagId[i] & 0xFF) + " ";
                    res=Integer.toHexString(tagId[i] & 0xFF) + " ";
                }
                tagInfo += "\n";

                String[] techList = tag.getTechList();
                tagInfo += "\nTech List\n";
                tagInfo += "length = " + techList.length +"\n";
                for(int i=0; i<techList.length; i++){
                    tagInfo += techList[i] + "\n ";
                }

                //textViewInfo.setText(tagInfo);
            }
        }else{
            Toast.makeText(this,
                    "onResume() : " + action,
                    Toast.LENGTH_SHORT).show();
        }

        return res;
    }
}
