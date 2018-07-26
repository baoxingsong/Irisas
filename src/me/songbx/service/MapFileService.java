package me.songbx.service;

import java.util.ArrayList;
import java.util.HashMap;

import me.songbx.impl.MapFileImpl;
import me.songbx.model.MapSingleRecord;

/**
 * @author song
 * @version 1.0, 2014-07-09
 */

public class MapFileService {
	private MapFileImpl mapFileImpl;
	public MapFileService(String mapFileLocation){
		mapFileImpl = new MapFileImpl(mapFileLocation);
	}
	
	
	
	/**
	 * if could not find the chrosomeName, the basement would be returned.
	 * to accelerate, a binary search algorithm is used here
	 * @param chromsomeName
	 * @param basement the coordinate of reference genome
	 * @return the coordinate of target genome
	 */
	public synchronized int getChangedFromBasement(String chromsomeName,int basement){
		return mapFileImpl.getChangedFromBasement(chromsomeName, basement);
	}
	
	
	/**
	 * if could not find the chrosomeName, the changed would be returned.
	 * to accelerate, a binary search algorithm is used here
	 * @param chromsomeName
	 * @param changed the coordinate of target genome
	 * @return the coordinate of reference genome
	 */
	public synchronized int getBasementFromChanged(String chromsomeName, int changed){
		return mapFileImpl.getBasementFromChanged(chromsomeName, changed);
	}
	
	public synchronized MapSingleRecord getRecordByBasing(String chromsomeName, int basement){
		return mapFileImpl.getRecordByBasing(chromsomeName, basement);
	}
	public synchronized void saveRamForIntergeneticRegionSequenceExtraction(){
		mapFileImpl.saveRamForIntergeneticRegionSequenceExtraction();
	}
	public synchronized HashMap<String, ArrayList<MapSingleRecord>> getIndelRecords() {
		return mapFileImpl.getIndelRecords();
	}

	public synchronized void setIndelRecords(HashMap<String, ArrayList<MapSingleRecord>> records) {
		mapFileImpl.setIndelRecords(records);
	}
	public synchronized HashMap<String, ArrayList<MapSingleRecord>> getAllRecords() {
		return mapFileImpl.getAllRecords();
	}
	public synchronized void setAllRecords(HashMap<String, ArrayList<MapSingleRecord>> allRecords) {
		mapFileImpl.setAllRecords(allRecords);
	}
	public synchronized void releaseRam() {
		mapFileImpl.releaseRam();
	}
}
