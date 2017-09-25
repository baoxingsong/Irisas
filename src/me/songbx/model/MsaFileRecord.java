package me.songbx.model;

import java.util.HashMap;

public class MsaFileRecord implements Comparable<MsaFileRecord>{
	private int start;
	private int end;
	private HashMap<String, MsaSingleRecord> records = new HashMap<String, MsaSingleRecord>();
	public MsaFileRecord(){
		
	}
	public MsaFileRecord(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}
	public synchronized int getStart() {
		return start;
	}
	public synchronized void setStart(int start) {
		this.start = start;
	}
	public synchronized int getEnd() {
		return end;
	}
	public synchronized void setEnd(int end) {
		this.end = end;
	}
	public synchronized HashMap<String, MsaSingleRecord> getRecords() {
		return records;
	}
	public synchronized void setRecords(HashMap<String, MsaSingleRecord> records) {
		this.records = records;
	}
	@Override
	public int compareTo(MsaFileRecord o) {
		return this.getStart() - o.getStart();
	}
}


