package com.mobile.core;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class EventDispatcher {

    private HashMap<String, ArrayList<EventListener>> mListenerMap;

    public interface EventListener {
        void onDispatch(@Nullable Object... params);
    }

    public EventDispatcher() {
        mListenerMap = new HashMap<>();
    }

    private boolean hasEvent(String evtName) {
        return mListenerMap.containsKey(evtName);
    }

    private boolean hasListener(String evtName, EventListener listener) {
        if (!mListenerMap.containsKey(evtName))
            return false;

        ArrayList<EventListener> listeners = mListenerMap.get(evtName);
        return listeners.contains(listener);
    }

    public boolean containsListener(String evtName) {
        if (!hasEvent(evtName))
            return false;

        ArrayList<EventListener> list = mListenerMap.get(evtName);
        return !list.isEmpty();
    }

    public void addEventListener(String evtName, EventListener listener) {
        if (listener == null || hasListener(evtName, listener))
            return;

        if (!hasEvent(evtName)) {
            ArrayList<EventListener> list = new ArrayList<>();
            list.add(listener);
            mListenerMap.put(evtName, list);
        }
        else {
            ArrayList<EventListener> list = mListenerMap.get(evtName);
            list.add(listener);
        }
    }

    public void removeEventListener(String evtName, EventListener listener) {
        if (listener == null || !hasListener(evtName, listener))
            return;

        ArrayList<EventListener> list = mListenerMap.get(evtName);
        list.remove(listener);
    }

    public void removeAllListener() {
        mListenerMap.clear();
    }

    public void dispatch(String evtName, @Nullable Object... params) {
        if (mListenerMap.isEmpty() || !mListenerMap.containsKey(evtName))
            return;

        ArrayList<EventListener> list = mListenerMap.get(evtName);
        if (list == null || list.isEmpty())
            return;

        for (EventListener listener: list) {
            listener.onDispatch(params);
        }
    }

    public void dispatchPop(String evtName, @Nullable Object... params) {
        if (mListenerMap.isEmpty() || !mListenerMap.containsKey(evtName))
            return;

        ArrayList<EventListener> list = mListenerMap.get(evtName);
        if (list == null || list.isEmpty())
            return;

        list.get(list.size() - 1).onDispatch(params);
    }
}