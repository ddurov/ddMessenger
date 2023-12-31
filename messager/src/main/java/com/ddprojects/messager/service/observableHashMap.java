package com.ddprojects.messager.service;

import java.io.Serializable;
import java.util.HashMap;

public class observableHashMap<K, V> extends HashMap<K, V> implements Serializable {
    private transient OnEventListener<K, V> onEventListener;

    public observableHashMap() {
        super();
    }
    public observableHashMap(HashMap<? extends K, ? extends V> table) {
        super(table);
    }

    @Override
    public void clear() {
        super.clear();
        if (onEventListener != null) onEventListener.onEvent(this);
    }

    @Override
    public V put(K key, V value) {
        V v = super.put(key, value);
        if (onEventListener != null) onEventListener.onEvent(this);
        return v;
    }

    @Override
    public V remove(Object key) {
        V v = super.remove(key);
        if (onEventListener != null) onEventListener.onEvent(this);
        return v;
    }

    public void setOnEventListener(OnEventListener<K, V> listener) {
        onEventListener = listener;
    }

    public interface OnEventListener<K, V> {
        void onEvent(observableHashMap<K, V> map);
    }
}