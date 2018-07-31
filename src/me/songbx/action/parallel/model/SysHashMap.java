package me.songbx.action.parallel.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class SysHashMap <K, V>{
	private HashMap<K, V> map;
	public SysHashMap(){
		map = new HashMap<K, V>();
	}
	public SysHashMap( int initialSize){
		map = new HashMap<K, V>(initialSize);
	}
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
	public synchronized boolean containsKey( K key ){
		return map.containsKey(key);
	}
	public synchronized Set<K> keySet() { return map.keySet(); }
	public synchronized Collection<V> values(){
		return map.values();
	}
}
