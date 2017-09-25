package me.songbx.action.parallel.model;

import java.util.HashMap;

import me.songbx.model.MapSingleRecord;
import me.songbx.model.Transcript.*;

public class RescueMediaState {
	private MapSingleRecord mapSingleRecord;
	private String transcriptName;
	private HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> rescudS = new HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel>();
	private HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> lostS = new HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel>();
	public RescueMediaState(MapSingleRecord mapSingleRecord, String transcriptName){
		this.mapSingleRecord=mapSingleRecord;
		this.transcriptName=transcriptName;
	}
	
	public synchronized void addRescue(String accessionname, TranscriptLiftStartEndSequenceAasequenceIndel t){
		rescudS.put(accessionname, t);
	}	
	public synchronized void addLost(String accessionname, TranscriptLiftStartEndSequenceAasequenceIndel t){
		lostS.put(accessionname, t);
	}
	
	public synchronized MapSingleRecord getMapSingleRecord() {
		return mapSingleRecord;
	}
	public synchronized void setMapSingleRecord(MapSingleRecord mapSingleRecord) {
		this.mapSingleRecord = mapSingleRecord;
	}
	public synchronized String getTranscriptName() {
		return transcriptName;
	}
	public synchronized void setTranscriptName(String transcriptName) {
		this.transcriptName = transcriptName;
	}
	public synchronized HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> getRescudS() {
		return rescudS;
	}
	public synchronized HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> getLostS() {
		return lostS;
	}	
}
