package com.hodaz.goodbyecyword;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import com.hodaz.goodbyecyword.common.Defines;
import com.hodaz.goodbyecyword.common.Utils;
import com.hodaz.goodbyecyword.database.DBAdapter;
import com.hodaz.goodbyecyword.model.Folder;
import com.hodaz.goodbyecyword.model.Post;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private ArrayList<Post> mPostList;
    private static final int MAX_RETRY_COUNT = 100;
    private NotificationManager nm;
    private Notification.Builder builder;
    private Notification.Builder progBuilder;
    private ImageLoader imageLoader;

    public PhotoStoreIntentService() {
        super("PhotoStoreIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initImageLoader();
        initNoti();
    }

    private void initImageLoader(){
        imageLoader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(PhotoStoreIntentService.this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .build();
        imageLoader.init(config);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            String cyID = intent.getStringExtra("CyID");
            Folder folder = intent.getParcelableExtra("Folder");
            if (folder != null ) {
                String folderID = folder.id;
                String folderTitle = folder.title;

                showNoti("이미지 주소를 수집 중입니다.", folderTitle);

                String lastPostID = null;
                String lastDate = null;
                int storeCnt = 0;

                try {
                    String url = String.format(Defines.URL_GET_CONTENT_LIST, cyID, folderID);
                    Document doc = Jsoup.connect(url).timeout(30000).cookies(Utils.getCookie()).get();
                    Elements posts = doc.getElementsByClass("post");

                    CommonLog.e(TAG, "onHandleIntent : " + doc.toString());

                    //TODO : max integer size 보다가 게시물이 많으면 에러가 날것 같군요
                    mPostList = new ArrayList<Post>(posts.size());

                    for (Element e : posts) {
                        String postId = e.attr("id");
                        String imgUrl = e.select("figure").attr("style");
                        imgUrl = imgUrl.replace("background-image:url('", "");
                        imgUrl = imgUrl.replace("');", "");
                        imgUrl = imgUrl.replace("width=269&height=269", "v=0&width=3000&");
                        imgUrl = imgUrl.replace("file_down", "vm_file_down");

                        CommonLog.e(TAG, postId + "\n" + imgUrl);

                        Post post = new Post();
                        post.folderID = folderID;
                        post.folderTitle = folderTitle;
                        post.postID = postId;
                        post.postImg = imgUrl;

                        lastPostID = post.postID.split("_")[0];
                        lastDate = post.postID.split("_")[1];

                        CommonLog.w(TAG, "count : " + (++storeCnt));
                        CommonLog.w(TAG, "postImg : " +  post.postImg);
                        CommonLog.w(TAG, "postID" + post.postID);
                        CommonLog.w(TAG, "-----------------------------------------------");
                        mPostList.add(post);
                    }

                    while (true) {
                        if (lastPostID != null && lastDate != null) {
                            String urlMore = String.format(Defines.URL_GET_CONTENT_MORE_LIST, cyID, folderID, lastPostID, lastDate);
                            Document docMore = Jsoup.connect(urlMore).timeout(30000).cookies(Utils.getCookie()).get();
                            Elements postsMore = docMore.getElementsByClass("post");

                            if (postsMore.size() == 0) {
                                CommonLog.e(TAG, folderTitle + " 조회 끝. DB 저장 시작!");
                                break;
                            }

                            for (Element e : postsMore) {
                                String postId = e.attr("id");
                                String imgUrl = e.select("figure").attr("style");
                                imgUrl = imgUrl.replace("background-image:url('", "");
                                imgUrl = imgUrl.replace("');", "");
                                imgUrl = imgUrl.replace("width=269&height=269", "width=3000&height=3000");
                                imgUrl = imgUrl.replace("file_down", "vm_file_down");

                                Post post = new Post();
                                post.folderID = folderID;
                                post.folderTitle = folderTitle;
                                post.postID = postId;
                                post.postImg = imgUrl;

                                lastPostID = post.postID.split("_")[0];
                                lastDate = post.postID.split("_")[1];

                                CommonLog.w(TAG, "count : " + (++storeCnt));
                                CommonLog.w(TAG, "postImg : " +  post.postImg);
                                CommonLog.w(TAG, "postID" + post.postID);
                                CommonLog.w(TAG, "-----------------------------------------------");
                                mPostList.add(post);
                            }
                        }
                    }

                    // DB 저장
                    showNoti("수집한 정보를 저장 중입니다.", folderTitle);

                    DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                    dbAdapter.open();

                    for (Post post : mPostList) {
                        Cursor cursor = dbAdapter.selectPost(post.postID);
                        if (cursor.getCount() == 0) {
                            dbAdapter.insertPost(post);
                        }
                        cursor.close();
                    }

                    dbAdapter.close();

                    // 사진 다운로드
                    initProgressNoti("사진을 다운로드 중입니다.", folderTitle);
                    savePostImages(folderTitle);

                    // 다운로드 완료
                    showNoti("다운로드를 완료하였습니다.", "갤러리에서 '" + folderTitle + "' 폴더를 확인해주세요.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void savePostImages(String folderTitle){
        showProgressNoti(mPostList.size(), 0);

        if (mPostList != null && mPostList.size() > 0) {
            File storageDir = null;

            // 특문 제거
            String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
            String removeSpecialChar = folderTitle.replaceAll(match, "");
            removeSpecialChar = removeSpecialChar.replace(" ", "");

            // 저장될 디렉토리를 구한다.
            storageDir = new File(String.format(Defines.EXTRA_PICTURE_OUTPUT_URI, removeSpecialChar));

            // 디렉토리 없으면 생성
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            int countFailure = 0;
            int countProgess = 0;

            for(Post post : mPostList) {
                Bitmap bitmap = imageLoader.loadImageSync(post.postImg);

                File saveFile = null;
                try {
                    saveFile = createImageFile(post.postID, storageDir);
                } catch (IOException e) {
                }

                if (saveFile == null) {
                    countFailure++;
                    continue;
                }

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream( saveFile );
                    bitmap.compress( Bitmap.CompressFormat.JPEG, 90, os );

                } catch( FileNotFoundException e ) {
                    countFailure++;
                    //e.printStackTrace();
                    continue;
                } catch( Exception e ) {
                    countFailure++;
                    //e.printStackTrace();
                    continue;
                } finally {
                    try {
                        if (os != null ) {
                            os.close();
                            countProgess++;
                            showProgressNoti(mPostList.size(), countProgess);
                        }
                    } catch( Exception ex ) {
                    }
                }

                galleryAddPic( saveFile );
            }
        }
    }

    private File createImageFile(String postTitle, File storageDir) throws IOException {
        File imageFile = null;
        /*final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String baseFileName = IMAGE_FILE_PREFIX + "_" + timeStamp;*/
        int retry = 0;

        while (retry < MAX_RETRY_COUNT) {
            String fileName = postTitle;
            if (retry > 0) {
                // 파일명이 중복되어 있으면 (xx)를 추가하여 재시도
                fileName = fileName + " (" + retry+")";
            }
            fileName = fileName + ".jpg";

            CommonLog.e(TAG, "createImageFile : " + postTitle);

            File file = new File(storageDir, fileName);
            if (!file.exists()) {
                imageFile = file;
                break;
            }

            retry++;
        }

        return imageFile;
    }

    /** 저장된 이미지 파일을 MediaStore에 추가 */
    private void galleryAddPic(File saveFile) {
        if (saveFile != null) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(saveFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }
    }

    private void initNoti() {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new Notification.Builder(this);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
    }

    private void showNoti(String contentTitle, String contentText) {
        builder.setTicker(contentText + " " + contentTitle);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification.BigTextStyle style = new Notification.BigTextStyle(builder);
            style.setBigContentTitle(contentTitle);
            style.bigText(contentText);

            builder.setStyle(style);
        }
        nm.notify(223, builder.build());
    }

    private void initProgressNoti(String contentTitle, String contentText) {
        if (mPostList != null && mPostList.size() > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            progBuilder = new Notification.Builder(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                progBuilder.setPriority(Notification.PRIORITY_HIGH);
            }
            progBuilder.setSmallIcon(R.mipmap.ic_launcher);
            progBuilder.setContentIntent(pendingIntent);
            progBuilder.setAutoCancel(false);
            progBuilder.setOngoing(true);
            progBuilder.setWhen(System.currentTimeMillis());
            progBuilder.setContentTitle(contentTitle);
            progBuilder.setContentText(contentText);
        }
    }

    private void showProgressNoti(int max, int progress) {
        progBuilder.setProgress(max, progress, false);
        nm.notify(223, progBuilder.build());
    }
}