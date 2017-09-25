package me.songbx.model;

public class MsaSingleRecord implements Comparable<MsaSingleRecord>{
	private int start;
	private int end;
	private String accesionName;
	private String sequence;
	
	public MsaSingleRecord(int start, int end, String accesionName,
			String sequence) {
		super();
		this.start = start;
		this.end = end;
		this.accesionName = accesionName;
		this.sequence = sequence;
	}
	
	public MsaSingleRecord(String accesionName,
			String sequence) {
		super();
		this.accesionName = accesionName;
		this.sequence = sequence;
	}
	
	public synchronized String getAccesionName() {
		return accesionName;
	}
	public synchronized void setAccesionName(String accesionName) {
		this.accesionName = accesionName;
	}
	public synchronized String getSequence() {
		return sequence;
	}
	public synchronized void setSequence(String sequence) {
		this.sequence = sequence;
	}
	@Override
	public int compareTo(MsaSingleRecord arg0) {
		return this.getStart()-arg0.getStart();
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
}


