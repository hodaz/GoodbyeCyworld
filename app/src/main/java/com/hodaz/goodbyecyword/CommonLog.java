
package com.hodaz.goodbyecyword;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class CommonLog {

    public static boolean DEBUGMODE = com.hodaz.goodbyecyword.BuildConfig.DEBUG;

    public static void d(String tag, Object... args) {
        println(android.util.Log.DEBUG, tag, args);
    }

    public static void i(String tag, Object... args) {
        println(android.util.Log.INFO, tag, args);
    }

    public static void e(String tag, Object... args) {
        println(android.util.Log.ERROR, tag, args);
    }

    public static void e(String tag, Throwable e) {
        e(tag, e.toString(), e);
    }

    public static void e(Throwable e) {
        if (e == null) return;

        e("ERROR", e.toString(), e);
    }

    public static void v(String tag, Object... args) {
        println(android.util.Log.VERBOSE, tag, args);
    }

    public static void w(String tag, Object... args) {
        println(android.util.Log.WARN, tag, args);
    }

    /**
     * 실제로 로그를 찍는 부분.
     *
     * @param level
     * @param tag
     * @param args
     */
    private static void println(int level, String tag, Object... args) {
        // TODO sangkwon 이 것이 어떻게 동작하는지 모르겠다. 나중에 상황봐서 쓰지 뭐 if (
        // android.util.Log.isLoggable( tag, level ) )
        if (DEBUGMODE) {
            if (tag != null && args != null && args.length > 0) {
                StringBuffer sb = new StringBuffer();
                StringBuffer sbLater = null; // Intent와 Throwable 은 뒤쪽에 좀 자세히
                // 출력되도록 수정했다.
                for (Object arg : args) {
                    if (arg == null) {
                        if (sb.length() > 0)
                            sb.append(' ');
                        sb.append("null");
                    } else if (arg instanceof Intent) {
                        /* Intent 는 그 내용들을 자세히 출력한다. */
                        if (sbLater == null)
                            sbLater = new StringBuffer();
                        Intent intent = (Intent) arg;
                        sbLater.append("Intent action:" + intent.getAction());
                        sbLater.append("\ntype/data:" + intent.getType() + "/" + intent.getData());
                        sbLater.append("\n");
                        Bundle extras = intent.getExtras();
                        if (extras != null) {
                            Iterator<String> keys = extras.keySet().iterator();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                sbLater.append(" " + key + "=>" + extras.get(key));
                            }
                        }
                    } else if (arg instanceof Throwable) {
                        /* Exception 은 CallStack 을 전부 출력해준다. */
                        Throwable tr = (Throwable) arg;
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        tr.printStackTrace(pw);
                        if (sbLater == null)
                            sbLater = new StringBuffer();
                        sbLater.append(sw.toString());
                    } else {
                        if (sb.length() > 0)
                            sb.append(' ');
                        sb.append(arg.toString());
                    }
                }

                if (sb != null)
                    android.util.Log.println(level, tag, attachCaller(sb.toString()));
                if (sbLater != null)
                    android.util.Log.println(level, tag, attachCaller(sbLater.toString()));

            }
        }
    }

    //** 로그가 어디에서 출력되었는지 확인할 수 있습니다. 클릭해서 이동도 가능해서 편합니다.*/
    private static String attachCaller(String msg) {
        return msg + " | at " + getCurFunction();
    }

    private static String getCurFunction() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement lmnt = stackTrace[6];

        return lmnt.toString();
    }

    @SuppressLint("NewApi")
    @TargetApi(9)
    public static void setStrictPolicy() {
        if (DEBUGMODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder();
                builder.detectNetwork();
                builder.penaltyLog();
                // builder.penaltyDialog();
                StrictMode.setThreadPolicy(builder.build());
            }
        }
    }

}
