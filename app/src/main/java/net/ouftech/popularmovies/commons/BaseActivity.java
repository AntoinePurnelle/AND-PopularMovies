package net.ouftech.popularmovies.commons;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;

/**
 * Created by antoi on 25-02-18.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private boolean running = false;
    private Unbinder unbinder;

    // region Crashlytics

    @NonNull
    protected abstract String getLotTag();

    /**
     * Logs a message to the logcat in the {@link Log#DEBUG} channel
     *
     * @param msg Message to log
     */
    protected void logd(@NonNull String msg) {
        Logger.d(getLotTag(), msg);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and and reports the exception to and Crashlytics
     *
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public void logw(@NonNull String msg, @NonNull Throwable tr) {
        Logger.w(getLotTag(), msg, tr, true);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param msg    Message to log
     * @param tr     Exception to be reported
     * @param report if true, the exception will be reported to Crashlytics
     */
    public void logw(@NonNull String msg, @NonNull Throwable tr, boolean report) {
        Logger.w(getLotTag(), msg, tr, report);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tr  Exception to be reported
     */
    public void loge(@NonNull Throwable tr) {
        Logger.e(getLotTag(), tr);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public void loge(@NonNull String msg, @NonNull Throwable tr) {
        Logger.e(getLotTag(), msg, tr);
    }

    // endregion Crashlytics


    // region Lifecycle

    @LayoutRes
    protected abstract int getLayoutId();

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        logd(String.format("onCreate %s", this));
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        setRunning(true);
        setContentView(getLayoutId());
        unbinder = ButterKnife.bind(this);
    }

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        logd(String.format("onCreate (PersistableBundle) %s", this));
        super.onCreate(savedInstanceState, persistentState);

        setRunning(true);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    @CallSuper
    @Override
    protected void onStart() {
        logd(String.format("onStart %s", this));
        super.onStart();

        setRunning(true);
    }

    @CallSuper
    @Override
    protected void onResume() {
        logd(String.format("onResume %s", this));
        super.onResume();

        setRunning(true);
    }

    @CallSuper
    @Override
    protected void onPause() {
        logd(String.format("onPause %s", this));
        super.onPause();

        setRunning(false);
    }

    @CallSuper
    @Override
    protected void onStop() {
        logd(String.format("onStop %s", this));
        super.onStop();

        setRunning(false);
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        logd(String.format("onDestroy %s", this));
        super.onDestroy();

        setRunning(false);
        unbinder.unbind();
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        logd(String.format("onSaveInstanceState %s", this));
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        logd(String.format("onSaveInstanceState (PersistableBundle) %s", this));
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    // endregion Lifecycle
}
