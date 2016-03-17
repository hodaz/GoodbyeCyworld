package com.hodaz.goodbyecyword.common;

import android.os.Environment;

/**
 * Created by hodaz on 2016. 3. 10..
 */
public class Defines {
    public static final String URL_GET_FOLDER = "http://cy.cyworld.com/home/%1$s/menu/?type=folder";
    public static final String URL_GET_CONTENT_LIST = "http://cy.cyworld.com/home/%1$s/postlist?startdate=&enddate=&folderid=%2$s&tagname=&_=1457400136714";
    public final static String EXTRA_PICTURE_OUTPUT_URI = Environment.getExternalStorageDirectory() + "/Cyworld_%1$s/";
}