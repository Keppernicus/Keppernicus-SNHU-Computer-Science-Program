package com.example.inventorted.ui;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Arrays;


/*
 * a message the viewmodel wants shown, held as a string resource id plus args.
 * lets the viewmodel say which message without needing a context to build it.
 */

public final class UiMessage {

    @StringRes
    private final int resId;
    private final Object[] formatArgs;

    public UiMessage(@StringRes int resId, Object... formatArgs) {
        this.resId = resId;
        this.formatArgs = formatArgs == null ? new Object [0] : formatArgs;
    }

    @StringRes
    public int getResId() {
        return resId;
    }

    @NonNull
    public Object[] getFormatArgs() {
        return formatArgs;
    }

    @NonNull
    @Override
    public String toString() {
        return "UiMessage{resId=" + resId + ", args=" + Arrays.toString(formatArgs) + '}';
    }
}
