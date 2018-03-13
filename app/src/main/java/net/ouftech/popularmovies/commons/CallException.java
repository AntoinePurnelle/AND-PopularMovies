package net.ouftech.popularmovies.commons;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by antoi on 13-03-18.
 */

public class CallException extends Exception {

    @NonNull
    protected String getLotTag() {
        return "CallException";
    }

    private int code;
    private String message;
    private String url;
    @Nullable
    private TMDBError tmdbError;

    public CallException(int code, String message, ResponseBody errorBody, Call call) {
        this.code = code;
        this.message = message;

        try {
            tmdbError = new Gson().fromJson(errorBody.string(), TMDBError.class);
        } catch (IOException | JsonSyntaxException e) {
            Logger.w(getLotTag(), String.format("Cannot build TMDBError based on message %s", message), e, false);
        }
        url = call.request().url().toString();
    }

    @Override
    public String toString() {
        return "\n CallException{" +
                "\n  code= " + code +
                ",\n  message= '" + message + '\'' +
                ",\n  for url= '" + url + '\'' +
                ",\n  tmdbError= " + tmdbError +
                "\n }\n";
    }

    public class TMDBError {
        public static final String STATUS_CODE = "status_code";
        public static final String STATUS_MESSAGE = "status_message";

        @SerializedName(STATUS_CODE)
        public  int statusCode;

        @SerializedName(STATUS_MESSAGE)
        public  String statusMessage;

        @Override
        public String toString() {
            return "\n   {" +
                    "\n    statusCode= " + statusCode +
                    ",\n    statusMessage='" + statusMessage + '\'' +
                    "\n   }";
        }
    }
}
