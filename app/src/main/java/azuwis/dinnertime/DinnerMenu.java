package azuwis.dinnertime;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by azuwis on 12/12/14.
 */
public class DinnerMenu {
    private static final String TAG = "DinnerMenu";

    private static final String PREFS_MENU = "menu";
    private static final String PREFS_DAY_OF_YEAR = "day_of_year";

    public static final int NUMBER_OF_MEAL = 4;
    public static final String[] MEALS = {"早餐", "中餐", "晚餐", "夜宵"};
    private static final int[] MEALS_HOUR = {10, 14, 19, 23};
    private static final String[] MEALS_REGEX = {".*早餐.*", ".*(中餐|卤水套餐).*", ".*晚餐.*", ".*夜宵.*"};
    private static final String[] MEALS_TRIM = {"^早餐：?", "^中餐：?", "^晚餐：?", "^夜宵：?"};

    private int mStatus = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_NOT_TODAY = 2;
    public static final int STATUS_NETWORK_ERROR = 3;

    private final Context mContext;
    private final String mUrl;
    private final String[] mMenu = new String[4];

    public DinnerMenu(Context context, String url) {
        mContext = context;
        mUrl = url;
    }

    private void getMenuFromUrl(){
        Document doc;
        try {
            doc = Jsoup.connect(mUrl).get();
        } catch (IOException e) {
            mStatus = STATUS_NETWORK_ERROR;
            Log.d(TAG, "Error try to get url " + mUrl);
            return;
        }
        Elements elems;
        elems = doc.select("p");

        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        String date = sdf.format(new Date());
        if (!elems.toString().contains(date)) {
            mStatus = STATUS_NOT_TODAY;
            Log.d(TAG, "Not today's menu");
            return;
        }

        for (int i = 0; i < NUMBER_OF_MEAL; i++) {
            mMenu[i] = MEALS[i] + "：";
        }
        for (Element elem : elems) {
            // trim does not work on &nbsp;
            String text = elem.text().trim().replace("\u00a0","");
            if (!text.isEmpty() && !text.matches("[\\d-]+")) {
                //Log.d(TAG, elem.text());
                for (int i = 0; i < NUMBER_OF_MEAL; i++) {
                    if (text.matches(MEALS_REGEX[i])) {
                        String savedText = text.replaceFirst(MEALS_TRIM[i], "");
                        mMenu[i] = mMenu[i] + savedText + "\n";
                        break;
                    }
                }
            }
        }
        mStatus = STATUS_SUCCESS;
    }

    public String[] getMenu(){
        // menu already set
        if (mMenu[0] != null) {
            return mMenu;
        }

        // try to get menu from shared prefs
        Calendar calendar = Calendar.getInstance();
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_MENU, Context.MODE_PRIVATE);
        if (prefs.getInt(PREFS_DAY_OF_YEAR, 0) == calendar.get(Calendar.DAY_OF_YEAR)) {
            for (int i = 0; i < NUMBER_OF_MEAL; i++) {
                mMenu[i] = prefs.getString(MEALS[i], "");
            }
            mStatus = STATUS_SUCCESS;
        } else {
            // get menu from url
            getMenuFromUrl();
            saveMenu();
        }
        return mMenu;
    }

    public String getCurrentMenu() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int i = 0;
        for (; i < NUMBER_OF_MEAL; i++) {
            if (hour < MEALS_HOUR[i]) break;
        }
        return getMenu()[i];
    }

    public int getStatus() {
        return mStatus;
    }

    private void saveMenu() {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_MENU, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFS_DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));

        for (int i = 0; i < NUMBER_OF_MEAL; i++) {
            editor.putString(MEALS[i], mMenu[i]);
        }
        editor.commit();
    }
}
