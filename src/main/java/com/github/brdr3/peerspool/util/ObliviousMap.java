package com.github.brdr3.peerspool.util;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ObliviousMap<K, V> implements Map {

    volatile ConcurrentHashMap<K, Tuple<V, Date>> base;
    Long obliviateTime;
    
    Thread obliviator;
    
    public ObliviousMap(Long time) {
        obliviateTime = time;
        base = new ConcurrentHashMap<>();
        
        obliviator = new Thread() {
            @Override
            public void run() {
                obliviate(obliviateTime);
            }
        };
        
        obliviator.start();
    }
    
    public ObliviousMap () {
        this(new Long(10000));
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {        
        return base.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return base.containsKey(key);
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
    public V get(Object key) {
        return base.get(key).getX();
    }

    @Override
    public Object put(Object key, Object value) {
        K k = (K) key;
        V v = (V) value;
        
        return base.put(k, new Tuple<>(v, new Date()));
    }

    @Override
    public V remove(Object key) {
        return base.remove(key).getX();
    }

    @Override
    public void putAll(Map m) {
        base.putAll(m);
    }

    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public Set keySet() {
        return base.keySet();
    }

    @Override
    public Collection values() {
        return base.values().stream().map(v -> v.getX()).collect(Collectors.toSet());
    }

    @Override
    public Set entrySet() {
        return base.entrySet().stream()
                    .map(v -> {
                        return new EntryImpl(v.getKey(), v.getValue().getX());
                    }).collect(Collectors.toSet());
    }
    
    public void obliviate(Long obliviateTime) {
        System.out.println("obliviator > Hi!");
        while(true) {
                for(Entry<K, Tuple<V, Date>> e: base.entrySet()) {
                    if(e.getValue().getY().getTime() + obliviateTime <= new Date().getTime()) {
                        System.out.println("obliviator > forgetting about " + e.getKey());
                        base.remove(e.getKey());
                    }
                }
            
            try {
                Thread.sleep(obliviateTime/2);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private class EntryImpl implements Entry<K, V> {
        private K key;
        private V value;

        private EntryImpl(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
            
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }
    }
}
