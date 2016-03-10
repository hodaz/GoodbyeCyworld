package com.hodaz.goodbyecyword.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hodaz on 2016. 3. 10..
 */
public class Folder implements Parcelable {
    public String id;
    public String title;

    public Folder() {

    }

    protected Folder(Parcel in) {
        id = in.readString();
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Folder> CREATOR = new Parcelable.Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
}