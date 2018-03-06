package net.ouftech.popularmovies.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Country implements Parcelable {
    public static final String ISO_KEY = "iso_3166_1";
    public static final String NAME_KEY = "name";

    @SerializedName(ISO_KEY)
    public String iso;

    @SerializedName(NAME_KEY)
    public String name;

    protected Country(Parcel in) {
        iso = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(iso);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Country> CREATOR = new Parcelable.Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
}
