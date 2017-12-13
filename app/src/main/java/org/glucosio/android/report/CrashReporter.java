package org.glucosio.android.report;

import android.support.annotation.NonNull;

/**
 *  This interface is needed for tests to mock static implementation of crash reports.
 */
public interface CrashReporter {

    void log(@NonNull String crashLog);

    void report(@NonNull Throwable throwable);
}
