package me.songbx.action.parallel.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import me.songbx.model.MapSingleRecord;
import me.songbx.model.Transcript.TranscriptLiftStartEndSequenceAasequenceIndel;

public class MultipleSequenceAlighmentModel {
	private String transcriptName;
	private HashMap<String, String> sequences = new HashMap<String, String>();
	private HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> transcripts = new HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel>();
	private HashMap<String, ArrayList<MapSingleRecord>> mapSingleRecords = new HashMap<String, ArrayList<MapSingleRecord>>();
	public MultipleSequenceAlighmentModel(String transcriptName){
		this.transcriptName=transcriptName;
	}
	public synchronized String getTranscriptName() {
		return transcriptName;
	}
	public synchronized void setTranscriptName(String transcriptName) {
		this.transcriptName = transcriptName;
	}
	
	public synchronized HashMap<String, String> getSequences() {
		return sequences;
	}
	public synchronized void setSequences(HashMap<String, String> sequences) {
		this.sequences = sequences;
	}
	public synchronized HashMap<String, ArrayList<MapSingleRecord>> getMapSingleRecords() {
		return mapSingleRecords;
	}
	public synchronized void setMapSingleRecords(
			HashMap<String, ArrayList<MapSingleRecord>> mapSingleRecords) {
		this.mapSingleRecords = mapSingleRecords;
	}
	public synchronized void addTranscript(String accessionName, TranscriptLiftStartEndSequenceAasequenceIndel transcript){
		sequences.put(accessionName, transcript.getSequence());
		transcripts.put(accessionName, transcript);
	}
	public synchronized void addIndel(String accessionName, MapSingleRecord myMapSingleRecord){
		int position = myMapSingleRecord.getBasement();
		int length = myMapSingleRecord.getChanged();
		if(length < 0){
			int myPosition = position;
			if(sequences.containsKey(accessionName)){
				String sequence = sequences.get(accessionName);
				String temp="";
				for(int i=0; i < -length; i++){
					temp+="-";
				}
				
				if(mapSingleRecords.containsKey(accessionName)){
					for(MapSingleRecord mapSingleRecord : mapSingleRecords.get(accessionName)){
						if(mapSingleRecord.getBasement() < myMapSingleRecord.getBasement() && mapSingleRecord.getChanged()>0){
							myPosition+=mapSingleRecord.getChanged();
						}
					}
				}
				if(myPosition < sequence.length() && myPosition>0){
					sequence = sequence.substring(0, myPosition)+temp+sequence.substring(myPosition, sequence.length());
					sequences.put(accessionName, sequence);
				}else{
					//System.out.println(accessionName+transcriptName+"61sequenceLength:\t"+sequence.length()+"\tmyPosition:\t"+myPosition+"\tposition:\t"+position);
					sequences.remove(accessionName);
				}
			}
			/*
			if(!mapSingleRecords.containsKey(accessionName)){
				mapSingleRecords.put(accessionName, new ArrayList<MapSingleRecord>());
			}
			mapSingleRecords.get(accessionName).add(myMapSingleRecord);
			*/
		}else{
			String temp="";
			for(int i=0; i < length; i++){
				temp+="-";
			}
			Iterator<String> allAccesionNamesIt = sequences.keySet().iterator();
			ArrayList<String> tobeRemoveList = new ArrayList<String>();
			while(allAccesionNamesIt.hasNext()){
				String aName = allAccesionNamesIt.next();
				if(!aName.equals(accessionName) && sequences.containsKey(accessionName)){
					int myPosition = position;
					if(mapSingleRecords.containsKey(aName)){
						for(MapSingleRecord mapSingleRecord : mapSingleRecords.get(aName)){
							if(mapSingleRecord.getBasement() < myMapSingleRecord.getBasement() && mapSingleRecord.getChanged()>0){
								myPosition+=mapSingleRecord.getChanged();
							}
						}
					}
					if(sequences.containsKey(aName)){
						String sequence = sequences.get(aName);
						if(myPosition < sequence.length() && myPosition>0){
							sequence = sequence.substring(0, myPosition)+temp+sequence.substring(myPosition, sequence.length());
							sequences.put(aName, sequence);
						}else{
							//System.out.println(accessionName+transcriptName+"61sequenceLength:\t"+sequence.length()+"\tmyPosition:\t"+myPosition+"\tposition:\t"+position);
							tobeRemoveList.add(aName);
						}
					}					
					//System.err.println(">"+accessionName);
					//System.err.println(sequence);
					if(!mapSingleRecords.containsKey(aName)){
						mapSingleRecords.put(aName, new ArrayList<MapSingleRecord>());
					}
					mapSingleRecords.get(aName).add(myMapSingleRecord);
				}				
			}//end while
			if(!mapSingleRecords.containsKey(accessionName)){
				mapSingleRecords.put(accessionName, new ArrayList<MapSingleRecord>());
			}
			mapSingleRecords.get(accessionName).add(myMapSingleRecord);
			for(String accName : tobeRemoveList){
				sequences.remove(accName);
			}
			tobeRemoveList = new ArrayList<String>();
		}// end if
		
	}
	public synchronized void update() {
		int longestLength = 0;
		HashMap<String, StringBuffer> sequenceSB = new HashMap<String, StringBuffer>();
		Iterator<String> namei1= sequences.keySet().iterator();
		while(namei1.hasNext()){
			String sequence1=sequences.get(namei1.next());
			if(sequence1.length()>longestLength){
				longestLength=sequence1.length();
			}
		}
		Iterator<String> namei2= sequences.keySet().iterator();
		while(namei2.hasNext()){
			String name2=namei2.next();
			String sequence2=sequences.get(name2);
			StringBuffer seb2 = new StringBuffer();
			seb2.append(sequence2);
			while(seb2.length()<longestLength){
				seb2.append("-");
			}
			sequenceSB.put(name2, seb2);
			seb2 = new StringBuffer();
		}
		
		for(int i=0; i<longestLength; i++){
			boolean ifAllLine = true;
			Iterator<String> namei3= sequenceSB.keySet().iterator();
			while(namei3.hasNext()){
				String name3=namei3.next();
				StringBuffer sequence3=sequenceSB.get(name3);
				if(sequence3.charAt(i) != '-'){
					ifAllLine = false;
				}
			}
			if(ifAllLine){
				Iterator<String> namei4= sequenceSB.keySet().iterator();
				while(namei4.hasNext()){
					String name4=namei4.next();
					StringBuffer sequence4=sequenceSB.get(name4);
					sequence4=new StringBuffer().append(sequence4.substring(0, i)).append(sequence4.substring(i+1, sequence4.length()));
					sequenceSB.put(name4, sequence4);
				}
				i--;
				longestLength--;
			}			
		}
		Iterator<String> namei5= sequenceSB.keySet().iterator();
		while(namei5.hasNext()){
			String name5=namei5.next();
			StringBuffer sequence5=sequenceSB.get(name5);
			sequences.put(name5, sequence5.toString());
		}
	}
	public synchronized HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> getTranscripts() {
		return transcripts;
	}
	public synchronized void setTranscripts(
			HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> transcripts) {
		this.transcripts = transcripts;
	}
}