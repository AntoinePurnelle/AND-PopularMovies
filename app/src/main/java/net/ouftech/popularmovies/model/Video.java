/*
 * Copyright 2018 Antoine PURNELLE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ouftech.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by antoine.purnelle@ouftech.net on 26-03-18.
 */

public class Video implements Parcelable {

    public static final String ID_KEY = "id";
    public static final String VIDEO_KEY = "key";
    public static final String NAME_KEY = "name";
    public static final String SITE_KEY = "site";
    public static final String TYPE_KEY = "type";


    public static final String TYPE_TRAILER = "Trailer";
    public static final String TYPE_TEASER = "Teaser";
    public static final String TYPE_CLIP = "Clip";

    public static final String SITE_YOUTUBE = "YouTube";

    @SerializedName(ID_KEY)
    public String id;

    @SerializedName(VIDEO_KEY)
    public String key;

    @SerializedName(NAME_KEY)
    public String name;

    @SerializedName(SITE_KEY)
    public String site;

    @SerializedName(TYPE_KEY)
    public String type;


    protected Video(Parcel in) {
        id = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(type);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

}
