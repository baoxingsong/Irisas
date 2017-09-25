package me.songbx.action.parallel.model;

import java.util.HashMap;

public class SysHashMap <K, V>{
	HashMap<K, V> map = new HashMap<K, V>();

	public synchronized HashMap<K, V> getMap() {
		return map;
	}

	public synchronized void setMap(HashMap<K, V> map) {
		this.map = map;
		
	}
	public synchronized void put (K key, V value){
		map.put(key, value);
	}
	public synchronized V get (K key){
		return map.get(key);
	}
}
