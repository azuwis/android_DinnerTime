package azuwis.dinnertime;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;

public class SchedulingService extends IntentService {
    private static final String TAG = "SchedulingService";

    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_CHECK = "azuwis.dinnertime.action.CHECK";
    public static final String ACTION_UPDATE = "azuwis.dinnertime.action.UPDATE";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "azuwis.dinnertime.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "azuwis.dinnertime.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCheck(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SchedulingService.class);
        intent.setAction(ACTION_CHECK);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionUpdate(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SchedulingService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public SchedulingService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCheck(param1, param2);
            } else if (ACTION_UPDATE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdate(param1, param2);
            }
        }
        AlarmReceiver.completeWakefulIntent(intent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheck(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionUpdate(String param1, String param2) {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences savedUpdate = getApplicationContext().getSharedPreferences("update", Context.MODE_PRIVATE);
        int offset = savedUpdate.getInt("offset", 0);
        offset += calendar.get(Calendar.DAY_OF_WEEK);
        String url = savedUpdate.getString("url", null);
        if (url != null) {
            url = url.replace("materialId=%s", "materialId=" + offset);
            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                Log.d(TAG, "Error trying to get " + url);
                return;
            }
            Elements elems;
            elems = doc.select("p");
            StringBuilder stringBuilder = new StringBuilder();
            for (Element elem : elems) {
                // trim does not work on &nbsp;
                String text = elem.text().trim().replace("\u00a0","");
                if (!text.isEmpty() && !text.matches("[\\d-]+")) {
                    //Log.d(TAG, elem.text());
                    stringBuilder.append(text).append("\n");
                }
            }
            String menu = stringBuilder.toString();
            SharedPreferences savedMenu = getApplicationContext().getSharedPreferences("menu", Context.MODE_PRIVATE);
            SharedPreferences.Editor savedMenuEditor = savedMenu.edit();
            savedMenuEditor.putInt("day", calendar.get(Calendar.DAY_OF_YEAR));
            savedMenuEditor.putString("menu", menu);
            savedMenuEditor.commit();
            //sendNotification(menu);
        }
    }

    private void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_dinner)
                        .setContentTitle(getString(R.string.app_name))
                        .setSound(alarmSound)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
