package me.songbx.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.model.MapSingleRecord;

/**
 * @author song
 * @version 1.0, 2014-07-09
 */

public class MapFileImpl {
	private HashMap<String, ArrayList<MapSingleRecord>> indelRecords = new HashMap<String, ArrayList<MapSingleRecord>>(); // only INDEL mutations
	private HashMap<String, ArrayList<MapSingleRecord>> snpRecords = new HashMap<String, ArrayList<MapSingleRecord>>(); // only SNP mutations
	private HashMap<String, ArrayList<MapSingleRecord>> allRecords = new HashMap<String, ArrayList<MapSingleRecord>>(); // both SNP and INDEL mutations
	public MapFileImpl(String mapFileLocation){
		File file = new File(mapFileLocation);
		if(file.exists()){
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	            Pattern p = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)");
	            Pattern p2 = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)");
	            while ((tempString = reader.readLine()) != null) {
	            	Matcher m = p.matcher(tempString);
	            	Matcher m2 = p2.matcher(tempString);
	            	if(m.find()){
	            		//System.out.println(tempString);
	            		this.addRecord( m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), m.group(4), m.group(5), Integer.parseInt(m.group(6)));
	            	}else if(m2.find()){
	            		//System.out.println(tempString);
	            		this.addRecord( m2.group(1), Integer.parseInt(m2.group(2)), Integer.parseInt(m2.group(3)), m2.group(4), m2.group(5));
	            	}
	            }
			} catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                	
	                }
	            }
	        }
			java.util.Iterator<Entry<String, ArrayList<MapSingleRecord>>> iter = allRecords.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, ArrayList<MapSingleRecord>> entry = iter.next();
				ArrayList<MapSingleRecord> val = entry.getValue();
				Collections.sort(val);
				int lastTotalCHanged = 0;
				for(MapSingleRecord m : val){
					m.setLastTotalChanged(lastTotalCHanged);
					int changedPoint=m.getBasement()+lastTotalCHanged;
					m.setChangedPoint(changedPoint);
					lastTotalCHanged +=m.getChanged();
				}
			}
		}else{
			System.err.println("Could not open file: "+mapFileLocation);
		}
	}
	
	/**
	 * @param chromsomeName
	 * @param basement
	 * @param changed
	 */
	private synchronized void addRecord(String chromsomeName, int basement, int changed, String original, String result, int score){
		MapSingleRecord mapsingleRecord = new MapSingleRecord(basement, changed, original, result, score);
		if(allRecords.containsKey(chromsomeName)){

		}else{
			ArrayList<MapSingleRecord> v = new ArrayList<MapSingleRecord>();
			allRecords.put(chromsomeName, v);
			MapSingleRecord mapsingleRecord0 =  new MapSingleRecord(0, 0, ".",".", 1);
			allRecords.get(chromsomeName).add(mapsingleRecord0);
		}
		allRecords.get(chromsomeName).add(mapsingleRecord);
		if(changed != 0){
			if(indelRecords.containsKey(chromsomeName)){
				
			}else{
				ArrayList<MapSingleRecord> v = new ArrayList<MapSingleRecord>();
				indelRecords.put(chromsomeName, v);
				MapSingleRecord mapsingleRecord0 =  new MapSingleRecord(0, 0, original, result, score);
				indelRecords.get(chromsomeName).add(mapsingleRecord0);
			}
			indelRecords.get(chromsomeName).add(mapsingleRecord);
		}else{
			if( snpRecords.containsKey(chromsomeName) ){
				
			}else{
				ArrayList<MapSingleRecord> v = new ArrayList<MapSingleRecord>();
				snpRecords.put(chromsomeName, v);
				MapSingleRecord mapsingleRecord0 =  new MapSingleRecord(0, 0, original, result, score);
				snpRecords.get(chromsomeName).add(mapsingleRecord0);
			}
			snpRecords.get(chromsomeName).add(mapsingleRecord);
		}
	}
	
	private synchronized void addRecord(String chromsomeName, int basement, int changed, String original, String result){
		MapSingleRecord mapsingleRecord = new MapSingleRecord(basement, changed, original, result);
		if(allRecords.containsKey(chromsomeName)){

		}else{
			ArrayList<MapSingleRecord> v = new ArrayList<MapSingleRecord>();
			allRecords.put(chromsomeName, v);
			MapSingleRecord mapsingleRecord0 =  new MapSingleRecord(0, 0, ".",".", 1);
			allRecords.get(chromsomeName).add(mapsingleRecord0);
		}
		allRecords.get(chromsomeName).add(mapsingleRecord);
		if(changed != 0){
			if(indelRecords.containsKey(chromsomeName)){
				
			}else{
				ArrayList<MapSingleRecord> v = new ArrayList<MapSingleRecord>();
				indelRecords.put(chromsomeName, v);
				MapSingleRecord mapsingleRecord0 =  new MapSingleRecord(0, 0, original, result);
				indelRecords.get(chromsomeName).add(mapsingleRecord0);
			}
			indelRecords.get(chromsomeName).add(mapsingleRecord);
		}else{
			if( snpRecords.containsKey(chromsomeName) ){
				
			}else{
				ArrayList<MapSingleRecord> v = new ArrayList<MapSingleRecord>();
				snpRecords.put(chromsomeName, v);
				MapSingleRecord mapsingleRecord0 =  new MapSingleRecord(0, 0, original, result);
				snpRecords.get(chromsomeName).add(mapsingleRecord0);
			}
			snpRecords.get(chromsomeName).add(mapsingleRecord);
		}
	}
	/**
	 * if could not find the chrosomeName, the basement would be returned.
	 * to accelerate, a binary search algorithm is used here
	 * @param chromsomeName
	 * @param basement the coordinate of reference genome
	 * @return the coordinate of target genome
	 */
	public synchronized int getChangedFromBasement(String chromsomeName,int basement){
		//System.out.println(chromsomeName+" "+basement);
		if(indelRecords.containsKey(chromsomeName)){
			//System.out.println("0000000");
			int start = 0 ;
			int end = indelRecords.get(chromsomeName).size()-1;
			int lastStart = start;
			if( indelRecords.get(chromsomeName).get(start).getBasement() >= basement ){
				return basement;
			}
			if(indelRecords.get(chromsomeName).get(end).getBasement() <= basement){
				MapSingleRecord mEnd = indelRecords.get(chromsomeName).get(end);
				//System.out.println("0000000");
				return basement+mEnd.getChanged()+mEnd.getLastTotalChanged();//allChanged;
			}
			//System.out.println("0000000");
			while(!((indelRecords.get(chromsomeName).get(start).getBasement() < basement) && (indelRecords.get(chromsomeName).get(start+1).getBasement() >= basement))){
				//System.out.println("lll0000000");
				if((indelRecords.get(chromsomeName).get(start).getBasement() < basement)){
					lastStart = start;
					if(1 == (end - start)){
						start = end;
					}else{
						start = (start+end)/2;
					}
					//System.out.println("iiiii0000000");
				}else{
					end = start;
					start = lastStart;
					//System.out.println("ssss0000000");
				}
			}
			//System.out.println("0000000");
			if((indelRecords.get(chromsomeName).get(start).getChanged() < 0) &&  basement<=indelRecords.get(chromsomeName).get(start).getBasement()-indelRecords.get(chromsomeName).get(start).getChanged()){
				return indelRecords.get(chromsomeName).get(start).getChangedPoint();//lastChangedPoint;
			}
			//System.out.println("0000000");
			return basement+indelRecords.get(chromsomeName).get(start+1).getLastTotalChanged();//allChanged;
		}else{
			return basement;
		}
	}
	
	
	/**
	 * if could not find the chrosomeName, the changed would be returned.
	 * to accelerate, a binary search algorithm is used here
	 * @param chromsomeName
	 * @param changed the coordinate of target genome
	 * @return the coordinate of reference genome
	 */
	public synchronized int getBasementFromChanged(String chromsomeName, int changed){
		if(indelRecords.containsKey(chromsomeName)){
			int start = 0 ;
			int end = indelRecords.get(chromsomeName).size()-1;
			int lastStart = start;
			if(indelRecords.get(chromsomeName).get(end).getChangedPoint() <= changed){
				MapSingleRecord mEnd = indelRecords.get(chromsomeName).get(end);
				return changed - (mEnd.getChanged()+mEnd.getLastTotalChanged());
			}
			while(!(indelRecords.get(chromsomeName).get(start).getChangedPoint()<changed && indelRecords.get(chromsomeName).get(start+1).getChangedPoint()>=changed)){
				if((indelRecords.get(chromsomeName).get(start).getChangedPoint() < changed)){
					lastStart = start;
					if(1 == (end - start)){
						start = end;
					}else{
						start = (start+end)/2;
					}
					//System.out.println("111");
				}else{
					end = start;
					start = lastStart;
				}
			}
			if( indelRecords.get(chromsomeName).get(start).getChanged() > 0 && changed <= (indelRecords.get(chromsomeName).get(start - 1).getChangedPoint()+((indelRecords.get(chromsomeName).get(start)).getChanged()))){
				return (indelRecords.get(chromsomeName).get(start)).getBasement();
			}
			return indelRecords.get(chromsomeName).get(start+1).getBasement()-(indelRecords.get(chromsomeName).get(start+1).getChangedPoint() - changed);
		}else{
			return changed;
		}
	}
	
	public synchronized MapSingleRecord getRecordByBasing(String chromsomeName, int basement){
		MapSingleRecord mapSingleRecord = null;
		if(allRecords.containsKey(chromsomeName)){
			
			int start = 1;
			int end = allRecords.get(chromsomeName).size()-1;
			
			if( allRecords.get(chromsomeName).get(start).getBasement() == basement){
				return allRecords.get(chromsomeName).get(start);
			}else if(allRecords.get(chromsomeName).get(end).getBasement() == basement){
				return allRecords.get(chromsomeName).get(end);
			}
			
			while(allRecords.get(chromsomeName).get(start).getBasement() < basement && allRecords.get(chromsomeName).get(end).getBasement() > basement){
				if((end - start) == 1 || end == start){
					return null;
				}
				int nextPoint = (start + end)/2;
				//System.out.println(nextPoint);
				if(allRecords.get(chromsomeName).get(nextPoint).getBasement() < basement){
					start = nextPoint;
				}else if(allRecords.get(chromsomeName).get(nextPoint).getBasement() > basement){
					end = nextPoint;
				}else{
					return allRecords.get(chromsomeName).get(nextPoint);
				}
			}
			
			if( allRecords.get(chromsomeName).get(start).getBasement() == basement){
				return allRecords.get(chromsomeName).get(start);
			}else if(allRecords.get(chromsomeName).get(end).getBasement() == basement){
				return allRecords.get(chromsomeName).get(end);
			}
		}
		return mapSingleRecord;
	}
	
	
	public synchronized HashSet<MapSingleRecord> getOverLapRecordsByBasing(String chromsomeName, int basement){
		// if there is a SNP, then there should be no deletion and the insertion would be ignored
		// here a half inverval algorithm was designed to accelerate it
		HashSet<MapSingleRecord> mapSingleRecords = new HashSet<MapSingleRecord>() ;
		if(snpRecords.containsKey(chromsomeName)){
			int start = 1;
			int end = snpRecords.get(chromsomeName).size()-1;
			if( snpRecords.get(chromsomeName).get(start).getBasement() == basement){
				mapSingleRecords.add( snpRecords.get(chromsomeName).get(start) );
				return mapSingleRecords;
			}else if(snpRecords.get(chromsomeName).get(end).getBasement() == basement){
				mapSingleRecords.add( snpRecords.get(chromsomeName).get(end) );
				return mapSingleRecords;
			}
			
			while(snpRecords.get(chromsomeName).get(start).getBasement() < basement && snpRecords.get(chromsomeName).get(end).getBasement() > basement && (end - start) != 1 && end != start ){
				int nextPoint = (start + end)/2;
				if(snpRecords.get(chromsomeName).get(nextPoint).getBasement() < basement){
					start = nextPoint;
				}else if(snpRecords.get(chromsomeName).get(nextPoint).getBasement() > basement){
					end = nextPoint;
				}else{
					mapSingleRecords.add( snpRecords.get(chromsomeName).get(nextPoint) );
					return mapSingleRecords;
				}
			}
			if( snpRecords.get(chromsomeName).get(start).getBasement() == basement){
				mapSingleRecords.add( snpRecords.get(chromsomeName).get(start) );
				return mapSingleRecords;
			}
			if( snpRecords.get(chromsomeName).get(end).getBasement()==basement ){
				mapSingleRecords.add( snpRecords.get(chromsomeName).get(end) );
				return mapSingleRecords;
			}
		}
		
		if(indelRecords.containsKey(chromsomeName) && mapSingleRecords.size() == 0){
			int start = 1;
			int end = indelRecords.get(chromsomeName).size()-1;
			
			if( indelRecords.get(chromsomeName).get(start).getBasement() == basement){
				mapSingleRecords.add( indelRecords.get(chromsomeName).get(start) );
			}else if(indelRecords.get(chromsomeName).get(end).getBasement() == basement){
				mapSingleRecords.add( indelRecords.get(chromsomeName).get(end) );
			}
			
			while(indelRecords.get(chromsomeName).get(start).getBasement() < basement && indelRecords.get(chromsomeName).get(end).getBasement() > basement && (end - start) != 1 && end != start ){
				int nextPoint = (start + end)/2;
				if(indelRecords.get(chromsomeName).get(nextPoint).getBasement() < basement){
					start = nextPoint;
				}else if(indelRecords.get(chromsomeName).get(nextPoint).getBasement() > basement){
					end = nextPoint;
				}else{
					mapSingleRecords.add( indelRecords.get(chromsomeName).get(nextPoint) );
					break;
				}
			}
			if( indelRecords.get(chromsomeName).get(start).getBasement() <= basement &&  (indelRecords.get(chromsomeName).get(start).getBasement() + Math.abs(indelRecords.get(chromsomeName).get(start).getChanged()))  >= basement){
				mapSingleRecords.add( indelRecords.get(chromsomeName).get(start) );
			}
			if( indelRecords.get(chromsomeName).get(end).getBasement()<=basement && (indelRecords.get(chromsomeName).get(end).getBasement()+Math.abs(indelRecords.get(chromsomeName).get(end).getChanged())) >= basement ){
				mapSingleRecords.add( indelRecords.get(chromsomeName).get(end) );
			}
		}
		return mapSingleRecords;
	}
	
	public synchronized ArrayList<MapSingleRecord> getRecordByPositions(String chromsomeName, int start, int end)
	{
		if(start > end){
			int temp = start;
			start = end;
			end = temp;
		}
		ArrayList<MapSingleRecord> mapSingleRecords = new ArrayList<MapSingleRecord>();
		if (this.indelRecords.containsKey(chromsomeName)) {
			for (MapSingleRecord mapSingleRecord : this.indelRecords.get(chromsomeName)) {
				if ((mapSingleRecord.getBasement() >= start) && (mapSingleRecord.getBasement() <= end)) {
					mapSingleRecords.add(mapSingleRecord);
				}
			}
		}
		return mapSingleRecords;
	}
	public synchronized void releaseRam() {
		ArrayList<String> names = new ArrayList<String>();
		
		Iterator<String> ir = indelRecords.keySet().iterator();
		while(ir.hasNext()){
			String name = ir.next();
			names.add(name);
			indelRecords.get(name).clear();
			indelRecords.put(name, null);
		}
		Iterator<String> ir2 = allRecords.keySet().iterator();
		while(ir2.hasNext()){
			String name = ir2.next();
			allRecords.get(name).clear();
			allRecords.put(name, null);
		}
		for(String n : names){
			indelRecords.remove(n);
			allRecords.remove(n);
		}
	}
	
	public synchronized void saveRamForIntergeneticRegionSequenceExtraction(){
		for(String name : indelRecords.keySet()){
			indelRecords.put(name, null);
		}
		indelRecords = null;
		for(String name : allRecords.keySet()){
			for( int i=0; i < allRecords.get(name).size(); i++){
				MapSingleRecord mapSingleRecord = allRecords.get(name).get(i);
				if((mapSingleRecord.getOriginal().length() > 1 ) ||  (!mapSingleRecord.getOriginal().endsWith("-")) ){
					
				}else{
					allRecords.get(name).remove(i);
					i--;
				}
			}
		}
	}
	
	public synchronized HashMap<String, ArrayList<MapSingleRecord>> getIndelRecords() {
		return indelRecords;
	}

	public synchronized void setIndelRecords(HashMap<String, ArrayList<MapSingleRecord>> records) {
		this.indelRecords = records;
	}

	public synchronized HashMap<String, ArrayList<MapSingleRecord>> getAllRecords() {
		return allRecords;
	}
	public synchronized void setAllRecords(HashMap<String, ArrayList<MapSingleRecord>> allRecords) {
		this.allRecords = allRecords;
	}
	public static void main(String[] argv){
		System.out.println("READIND BEGIN");
		MapFileImpl mapFile = new MapFileImpl("G:\\genomeAnnotation\\19genomes\\19genomes\\edi_0.v7c.sdi");
		int changed = mapFile.getChangedFromBasement("Chr1", 17572929);
		System.out.println( changed );
	}

	public synchronized HashMap<String, ArrayList<MapSingleRecord>> getSnpRecords() {
		return snpRecords;
	}

	public synchronized void setSnpRecords(HashMap<String, ArrayList<MapSingleRecord>> snpRecords) {
		this.snpRecords = snpRecords;
	}

}
