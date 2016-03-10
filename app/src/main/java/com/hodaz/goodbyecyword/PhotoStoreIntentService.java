package com.hodaz.goodbyecyword;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.hodaz.goodbyecyword.Model.Folder;
import com.hodaz.goodbyecyword.common.Defines;
import com.hodaz.goodbyecyword.common.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PhotoStoreIntentService extends IntentService {

    private static final String TAG = "PhotoStoreIntentService";

    public PhotoStoreIntentService() {
        super("PhotoStoreIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            String cyID = intent.getStringExtra("CyID");
            ArrayList<Folder> folderList = intent.getParcelableArrayListExtra("FolderList");
            if (folderList != null && folderList.size() > 0) {
                String folderID = folderList.get(0).id;
                String folderTitle = folderList.get(0).title;

                showNoti(folderTitle);

                String url = String.format(Defines.URL_GET_CONTENT_LIST, cyID, folderID);

                Document doc = null;
                try {
                    doc = Jsoup.connect(url).timeout(30000).cookies(Utils.getCookie()).get();
                    Elements posts = doc.getElementsByClass("post");
                    Elements photos = doc.getElementsByClass("postImage");

                    for (Element e : posts) {
                        String postId = e.attr("id");
                        String imgUrl = e.select("figure").attr("style");
                        imgUrl = imgUrl.replace("background-image:url('", "");
                        imgUrl = imgUrl.replace("');", "");
                        imgUrl = imgUrl.replace("cythumb.cyworld.com/269x269/", "");
                        imgUrl = imgUrl.replace("file_down", "vm_file_down");

                        CommonLog.e(TAG, postId + "\n" + imgUrl);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showNoti(String folderTitle) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this);
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mCompatBuilder.setTicker("NotificationCompat.Builder");
        mCompatBuilder.setWhen(System.currentTimeMillis());
        // mCompatBuilder.setNumber(10);
        mCompatBuilder.setContentTitle("당신의 추억을 폰으로 저장 중입니다.");
        mCompatBuilder.setContentText("현재 저장 중인 폴더는 '" + folderTitle + "' 입니다.");
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mCompatBuilder.setContentIntent(pendingIntent);
        mCompatBuilder.setAutoCancel(false);
        // mCompatBuilder.setOngoing(true);

        nm.notify(222, mCompatBuilder.build());
    }
}