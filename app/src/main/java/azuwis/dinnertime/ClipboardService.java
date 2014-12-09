package azuwis.dinnertime;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ClipboardService extends Service {
    private static final String TAG = "ClipboardService";

    private ClipboardManager mClipboardManager;

    public void onCreate() {
        super.onCreate();
        mClipboardManager =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(
                mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(
                    mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    ClipData clip = mClipboardManager.getPrimaryClip();
                    CharSequence chars = clip.getItemAt(0).getText();
                    if (chars != null ) {
                        String string = chars.toString();
                        if (string.startsWith("http://numenplus.yixin.im/singleNewsWap.do?")) {
                            Log.d(TAG, string);
                            saveId(string);
                        }
                    }
                }
            };

    private void saveId(String url_string) {
        Uri uri = Uri.parse(url_string);
        if (uri.getQueryParameter("companyId").equals("1")) {
            String materialId;
            materialId = uri.getQueryParameter("materialId");
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, materialId, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
