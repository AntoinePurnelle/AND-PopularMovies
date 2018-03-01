package net.ouftech.popularmovies.commons;

import android.support.annotation.IntRange;

import java.util.Collection;

/**
 * Created by antoi on 01-03-18.
 */

public class CollectionUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    @IntRange(from = 0)
    public static int getSize(Collection collection) {
        return collection == null ? 0 : collection.size();
    }
}
