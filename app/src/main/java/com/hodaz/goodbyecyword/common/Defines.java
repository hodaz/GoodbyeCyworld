package com.hodaz.goodbyecyword.common;

import android.os.Environment;

/**
 * Created by hodaz on 2016. 3. 10..
 */
public class Defines {
    public static final String URL_GET_FOLDER = "http://cy.cyworld.com/home/%1$s/menu/?type=folder";
    public static final String URL_GET_CONTENT_LIST = "http://cy.cyworld.com/home/%1$s/postlist?startdate=&enddate=&folderid=%2$s&tagname=&_=1457400136714";
    public static final String URL_GET_CONTENT_MORE_LIST = "http://cy.cyworld.com/home/%1$s/postmore?folderid=%2$s&lastid=%3$s&lastdate=%4$s&tagname=&startdate=&enddate=";
    public static final String EXTRA_PICTURE_OUTPUT_PARENT_URI = Environment.getExternalStorageDirectory() + "/Cyworld/";
    public static final String EXTRA_PICTURE_OUTPUT_URI = Environment.getExternalStorageDirectory() + "/Cyworld/%1$s/";
}