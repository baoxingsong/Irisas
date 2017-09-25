package me.songbx.action.parallel.model;

import java.util.HashMap;

public class TranscriptHashMapLostNumber {
	private HashMap<String, Integer> transcriptLostNumber = new HashMap<String, Integer>();

	public synchronized HashMap<String, Integer> getTranscriptLostNumber() {
		return transcriptLostNumber;
	}

	public synchronized void setTranscriptLostNumber(
			HashMap<String, Integer> transcriptLostNumber) {
		this.transcriptLostNumber = transcriptLostNumber;
	}
	public synchronized void add(String transcriptName){
		if(transcriptLostNumber.containsKey(transcriptName)){
			transcriptLostNumber.put(transcriptName, transcriptLostNumber.get(transcriptName)+1);
		}else{
			transcriptLostNumber.put(transcriptName, 1);
		}
	}	
}
