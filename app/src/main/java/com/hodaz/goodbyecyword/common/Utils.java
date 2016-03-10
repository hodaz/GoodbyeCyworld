package com.hodaz.goodbyecyword.common;

import android.webkit.CookieManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hodaz on 2016. 3. 10..
 */
public class Utils {
    public static Map<String, String> getCookie() {
        String cookieValue = null;
        Map<String, String> cookieMap = new HashMap<String, String>();

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("http://cy.cyworld.com");
        if (cookies != null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                String[] temp1 = ar1.split("=");
                cookieValue = temp1[1];
                // Log.e("Cyworld Cookie", ar1);
                cookieMap.put(temp1[0], temp1[1]);
            }
        }
        return cookieMap;
    }
}
