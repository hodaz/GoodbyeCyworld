package com.hodaz.goodbyecyword;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.hodaz.goodbyecyword.common.Defines;
import com.hodaz.goodbyecyword.common.Utils;
import com.hodaz.goodbyecyword.model.Folder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GoodbyeCyworld";
    private static final String KEY_CYWORLD_ID = "KEY_CYWORLD_ID";

    private Context mContext;
    private WebView mWebView;
    private Button mCyID;
    private Button mFolder;
    private Button mBackup;
    private String mCyworldId;
    private boolean existID = false;
    private ArrayList<Folder> mFolderList = new ArrayList<Folder>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mWebView = (WebView) findViewById(R.id.webview);
        mCyID = (Button) findViewById(R.id.cyid);
        mCyID.setOnClickListener(this);
        mFolder = (Button) findViewById(R.id.folder);
        mFolder.setOnClickListener(this);
        mBackup = (Button) findViewById(R.id.backup);
        mBackup.setOnClickListener(this);
        initWebView();
    }

    private void initWebView() {
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setBuiltInZoomControls(true);

        mWebView.addJavascriptInterface(new HttpCrawlingInterface(this), "HtmlViewer");
        mWebView.setWebChromeClient(new WebChromeClient() {

        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                CommonLog.e(TAG, "url : " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (!existID) {
                    view.loadUrl("javascript:window.HtmlViewer.showHTML" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'<script>var currentUrl=\"startc"+url+"endc\"</script></html>');");
                }
                else if (url.contains("logout.jsp")) {
                    PreferenceUtil.getInstance().remove(mContext, KEY_CYWORLD_ID);
                }
            }
        });
        mWebView.loadUrl("http://m.cyworld.com");

//        CookieSyncManager.createInstance(this);
//        CookieManager cookieManager = CookieManager.getInstance();
//        String cookieString = "param=value";
//        cookieManager.setCookie("http://cy.cyworld.com", cookieString);
//        CookieSyncManager.getInstance().sync();
//
//        Map<String, String> abc = new HashMap<String, String>();
//        abc.put("Cookie", cookieString);
//        mWebView.loadUrl("http://abc-site.com/a1/namedFolder/file", abc);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.folder:
                if (mFolder.getText().toString().equals("폴더조회")) {
                    new AsyncTask<Void, Void, Document>() {
                        private String url;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            url = String.format(Defines.URL_GET_FOLDER, mCyID.getText());
                            CommonLog.e(TAG, "api_get_folder : " + url);
                        }

                        @Override
                        protected Document doInBackground(Void... params) {
                            Document doc = null;
                            try {
                                doc = Jsoup.connect(url).timeout(30000).cookies(Utils.getCookie()).get();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return doc;
                        }

                        @Override
                        protected void onPostExecute(Document result) {
                            super.onPostExecute(result);

                            if (result != null) {
                                ArrayList<String> idList = new ArrayList<String>();
                                ArrayList<String> titleList = new ArrayList<String>();

                                Elements menus = result.getElementsByClass("tree3"); //.get(0).getElementsByTag("input");

                                // id 추출
                                int idCount = 0;
                                for (Element e : menus) {
                                    Elements menuidElements = e.getElementsByTag("input");
                                    for (Element e2 : menuidElements) {
                                        //CommonLog.e(TAG, e2.attributes().get("value"));
                                    }

                                    for (int i = 0; i < menuidElements.size(); i++) {
                                        idList.add(menuidElements.get(i).attributes().get("value"));
                                    }

                                    idCount += menuidElements.size();
                                }
                                //CommonLog.e(TAG, "menu count : " + idCount);

                                // title 추출
                                int titleCount = 0;
                                for (Element e : menus) {
                                    Elements titleElements = e.getElementsByTag("em");
                                    for (Element e2 : titleElements) {
                                        //CommonLog.e(TAG, e2.text());
                                    }

                                    for (int i = 0; i < titleElements.size(); i++) {
                                        titleList.add(titleElements.get(i).text());
                                    }

                                    titleCount += titleElements.size();
                                }
                                //CommonLog.e(TAG, "menu count : " + titleCount);

                                for (int i = 0; i < idList.size(); i++) {
                                    Folder folder = new Folder();
                                    folder.id = idList.get(i);
                                    folder.title = titleList.get(i);
                                    mFolderList.add(folder);
                                }

                                mFolder.setText("폴더확인");
                            }
                        }
                    }.execute();
                }
                else if (mFolder.getText().toString().equals("폴더확인")) {
                    /*StringBuilder builder = new StringBuilder();

                    for (Folder f : mFolderList) {
                        builder.append(f.id + "\n" + f.title + "\n");
                    }*/

                    ArrayList<String> list = new ArrayList<>();
                    for(Folder f : mFolderList){
                        list.add(f.title);
                    }

                    new AlertDialog.Builder(mContext).setTitle("폴더목록").setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, PhotoStoreIntentService.class);
                            intent.putExtra("CyID", mCyworldId);
                            intent.putExtra("Folder", mFolderList.get(which));
                            startService(intent);
                        }
                    })
                            .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                }
                break;
            case R.id.backup:
                /*Intent intent = new Intent(this, PhotoStoreIntentService.class);
                intent.putExtra("CyID", mCyworldId);
                intent.putExtra("FolderList", mFolderList);
                startService(intent);*/
                break;
            case R.id.cyid:
                if (mCyID.getText().toString().equals("로그인")) {
                    mWebView.loadUrl("https://cyxso.cyworld.com/mnate/Login.sk?loginstr=redirect&redirection=http://m.cyworld.com/&svccd=mcyworld_ndr.nate.com/main/");
                }
                break;
        }
    }

    class HttpCrawlingInterface {
        private Context ctx;

        HttpCrawlingInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showHTML(String html) {
//            new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
//                    .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();

            String url = html.substring(html.indexOf("startc")+6, html.indexOf("endc"));
            CommonLog.e(TAG, "url : " + url);

            if (url.equals("http://m.cyworld.com/")) {
                int startPos = html.indexOf("내 싸이홈가기");
                if (startPos > 0) {
                    String homeUrl = html.substring(startPos - 37, startPos - 2);
                    CommonLog.e(TAG, "homeUrl : " + homeUrl);

                    if (homeUrl.startsWith("http")) {
                        final String id = homeUrl.replace("http://cy.cyworld.com/home/", "");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                existID = true;
                                mCyID.setText(id);
                                mCyworldId = id;
                            }
                        });

                        PreferenceUtil.getInstance().putString(ctx, KEY_CYWORLD_ID, id);
                        CommonLog.e(TAG, "id : " + id);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
        else {
            finish();
        }
    }
}
