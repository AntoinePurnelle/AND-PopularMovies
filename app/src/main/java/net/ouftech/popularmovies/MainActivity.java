package net.ouftech.popularmovies;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import net.ouftech.popularmovies.commons.BaseActivity;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity {

    @NonNull
    @Override
    protected String getLotTag() {
        return "MainActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
    }
}
