package me.songbx.model.Transcript;

import java.util.ArrayList;

import me.songbx.model.MapSingleRecord;
import me.songbx.model.CDS.CdsLiftSequence;

public class TranscriptLiftStartEndSequenceAasequenceIndel extends Transcript implements Comparable<TranscriptLiftStartEndSequenceAasequenceIndel>{
	
	public TranscriptLiftStartEndSequenceAasequenceIndel(Transcript transcript) {
		super(transcript);
		this.setCdsHashSet(null);
		// TODO Auto-generated constructor stub
	}

	private ArrayList<CdsLiftSequence> cdsLiftSequenceArrayList = new ArrayList<CdsLiftSequence>();
	private Integer liftStart;
	private Integer liftEnd;
	private Integer start;
	private Integer end;
	
	private String sequence;
	private ArrayList<MapSingleRecord> MapSingleRecords =new ArrayList<MapSingleRecord>();
	private String aaSequence=new String();
	private String neutralSelectionSequence = new String();// the intron sequence and some inter-genetic sequence at two terminals of this gene 
	private boolean indeled = false;
	
	private String fullequence;
	private String metaInformation="";
	public String getNeutralSelectionSequence() {
		return neutralSelectionSequence;
	}



	public void setNeutralSelectionSequence(String neutralSelectionSequence) {
		this.neutralSelectionSequence = neutralSelectionSequence;
	}

	private boolean orfLost=false;
	
	@Override
	public synchronized int compareTo( TranscriptLiftStartEndSequenceAasequenceIndel arg0) {
		if(this.equals(arg0)){
			return 0;
		}else{
			TranscriptLiftStartEndSequenceAasequenceIndel e0 = this;
			TranscriptLiftStartEndSequenceAasequenceIndel e1 = arg0;
			if(e0.getChromeSomeName().equals(e1.getChromeSomeName())){
				if(e0.getStart() == e1.getStart() && e0.getEnd() == e1.getEnd()){
					return 0;
				}else if(e0.getStart() > e1.getStart()){
					return 1;
				} else if (e0.getStart() == e1.getStart() && e0.getEnd() > e1.getEnd()){
					return 1;
				}else if (e0.getStart() == e1.getStart() && e0.getEnd() < e1.getEnd()){
					return -1;
				}else if(e0.getStart() < e1.getStart()){
					return -1;
				}else{
					return 0;
				}
			}else{
				return e0.getChromeSomeName().compareTo(e1.getChromeSomeName());
			}
		}
	}



	public synchronized Integer getLiftStart() {
		return liftStart;
	}

	public synchronized void setLiftStart(Integer liftStart) {
		this.liftStart = liftStart;
	}

	public synchronized Integer getLiftEnd() {
		return liftEnd;
	}

	public synchronized void setLiftEnd(Integer liftEnd) {
		this.liftEnd = liftEnd;
	}

	public synchronized Integer getStart() {
		return start;
	}

	public synchronized void setStart(Integer start) {
		this.start = start;
	}

	public synchronized Integer getEnd() {
		return end;
	}

	public synchronized void setEnd(Integer end) {
		this.end = end;
	}

	public synchronized String getSequence() {
		return sequence;
	}

	public synchronized void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public synchronized ArrayList<MapSingleRecord> getMapSingleRecords() {
		return MapSingleRecords;
	}

	public synchronized void setMapSingleRecords(ArrayList<MapSingleRecord> mapSingleRecords) {
		MapSingleRecords = mapSingleRecords;
	}

	public synchronized String getAaSequence() {
		return aaSequence;
	}

	public synchronized void setAaSequence(String aaSequence) {
		this.aaSequence = aaSequence;
	}

	public synchronized boolean isIndeled() {
		return indeled;
	}

	public synchronized void setIndeled(boolean indeled) {
		this.indeled = indeled;
	}
	public synchronized ArrayList<CdsLiftSequence> getCdsLiftSequenceArrayList() {
		return cdsLiftSequenceArrayList;
	}
	public synchronized void setCdsLiftSequenceArrayList(
			ArrayList<CdsLiftSequence> cdsLiftSequenceArrayList) {
		this.cdsLiftSequenceArrayList = cdsLiftSequenceArrayList;
	}

	public synchronized String getFullequence() {
		return fullequence;
	}

	public synchronized void setFullequence(String fullequence) {
		this.fullequence = fullequence;
	}

	public synchronized String getMetaInformation() {
		return metaInformation;
	}

	public synchronized void setMetaInformation(String metaInformation) {
		this.metaInformation = metaInformation;
	}

	public synchronized boolean isOrfLost() {
		return orfLost;
	}



	public synchronized void setOrfLost(boolean orfLost) {
		this.orfLost = orfLost;
	}
	
}
