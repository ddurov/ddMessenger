package com.ddprojects.messager.service;

import java.io.Serializable;
import java.util.Hashtable;

public class observableHashtable<K, V> extends Hashtable<K, V> implements Serializable {
    private static final long SerialVersionUID = 1L;

    private transient OnEventListener<K, V> onEventListener;

    public observableHashtable() {
        super();
    }

    public observableHashtable(int capacity) {
        super(capacity);
    }

    public observableHashtable(int capacity, float loadFactor) {
        super(capacity, loadFactor);
    }

    public observableHashtable(Hashtable<? extends K, ? extends V> table) {
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
        void onEvent(observableHashtable<K, V> map);
    }
}
