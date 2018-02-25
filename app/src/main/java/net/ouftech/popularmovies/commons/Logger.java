package net.ouftech.popularmovies.commons;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by antoi on 25-02-18.
 */

public class Logger {

    /**
     * Logs a message to the logcat in the {@link Log#DEBUG} channel
     *
     * @param tag Tag for the log
     * @param msg Message to log
     */
    public static void d(@NonNull String tag, @NonNull String msg) {
        logToCrashlytics(Log.DEBUG, tag, msg);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and and reports the exception to and Crashlytics
     *
     * @param tag Tag for the log
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public static void w(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        w(tag, msg, tr, true);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tag    Tag for the log
     * @param msg    Message to log
     * @param tr     Exception to be reported
     * @param report if true, the exception will be reported to Crashlytics
     */
    public static void w(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr, boolean report) {
        msg = String.format("*** %s ***: %s - [%s]", report ? "WARNING" : "WARNING (NO-REPORT)", msg, tr);
        logToCrashlytics(Log.WARN, tag, msg);
        if (report)
            reportToCrashlytics(tr);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tag Tag for the log
     * @param tr  Exception to be reported
     */
    public static void e(@NonNull String tag, @NonNull Throwable tr) {
        e(tag, tr.getMessage(), tr);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tag Tag for the log
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public static void e(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        msg = String.format("*** ERROR ***: %s - [%s]", msg, tr);
        logToCrashlytics(Log.ERROR, tag, msg);
        reportToCrashlytics(tr);
    }

    /**
     * Logs a message to the logcat, Crashlytics in the right channel
     *
     * @param priority Priority (channel) in which to log
     * @param tag      Tag for the logcat
     * @param msg      Message to log
     */
    private static void logToCrashlytics(int priority, @NonNull String tag, @NonNull String msg) {
        if (Fabric.isInitialized())
            Crashlytics.log(priority, tag, msg);
    }

    /**
     * Reports an exception to Crashlytics
     *
     * @param tr Exception to reportToCrashlytics
     */
    private static void reportToCrashlytics(@NonNull Throwable tr) {
        if (Fabric.isInitialized())
            Crashlytics.logException(tr);
    }
}
