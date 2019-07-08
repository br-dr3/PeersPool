package com.github.brdr3.peerspool.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObliviousMap<K, V> implements Map {

    HashMap<K, Tuple<V, Date>> base;
    Long obliviateTime;
    
    public ObliviousMap(Long time) {
        obliviateTime = time;
        base = new HashMap<>();
    }
    
    public ObliviousMap () {
        this(new Long(10000));
    }

    @Override
    public int size() {
        return this.entrySet().size();
    }

    @Override
    public boolean isEmpty() {
        return this.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return base.values()
                   .stream()
                   .map(v -> v.getX())
                   .filter(v -> v.equals(value))
                   .count() >= 1;
    }

    @Override
    public Object get(Object key) {
        return base.get(key).getX();
    }

    @Override
    public Object put(Object key, Object value) {
        K k = (K) key;
        V v = (V) value;
        return base.put(k, new Tuple<>(v, new Date()));
    }

    @Override
    public Object remove(Object key) {
        return base.remove(key);
    }

    @Override
    public void putAll(Map m) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public Set keySet() {
        return this.keySet();
    }

    @Override
    public Collection values() {
        return base.values().stream().map(v -> v.getX()).collect(Collections.toList());
    }

    @Override
    public Set entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
