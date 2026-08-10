package com.example.inventorted.util;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/*
 * livedata that fires a value once instead of replaying it to every new observer.
 * used for one-time things like toasts and navigation, so they don't fire again
 * on rotation the way normal livedata would.
 */

public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private static final String TAG = "SingleLiveEvent";

    private final AtomicBoolean pending = new AtomicBoolean(false);

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registers; only one will be notified.");
        }
        super.observe(owner, t-> {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    @Override
    public void setValue(@Nullable T value) {
        pending.set(true);
        super.setValue(value);
    }
}
