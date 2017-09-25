package me.songbx.model;

public class TwoSeqOfMsaResult implements Comparable<TwoSeqOfMsaResult>{
	private int refStart;
	private int refEnd;
	private String refSeq;
	private int resultStart;
	private int resultEnd;
	private String resultSeq;
	
	public TwoSeqOfMsaResult(int refStart, int refEnd, String refSeq,
			int resultStart, int resultEnd, String resultSeq) {
		super();
		this.refStart = refStart;
		this.refEnd = refEnd;
		this.refSeq = refSeq;
		this.resultStart = resultStart;
		this.resultEnd = resultEnd;
		this.resultSeq = resultSeq;
	}
	
	public TwoSeqOfMsaResult(int refStart, int refEnd, String refSeq, String resultSeq) {
		super();
		this.refStart = refStart;
		this.refEnd = refEnd;
		this.refSeq = refSeq;
		this.resultSeq = resultSeq;
	}
	
	public synchronized int getRefStart() {
		return refStart;
	}
	public synchronized void setRefStart(int refStart) {
		this.refStart = refStart;
	}
	public synchronized int getRefEnd() {
		return refEnd;
	}
	public synchronized void setRefEnd(int refEnd) {
		this.refEnd = refEnd;
	}
	public synchronized String getRefSeq() {
		return refSeq;
	}
	public synchronized void setRefSeq(String refSeq) {
		this.refSeq = refSeq;
	}
	public synchronized int getResultStart() {
		return resultStart;
	}
	public synchronized void setResultStart(int resultStart) {
		this.resultStart = resultStart;
	}
	public synchronized int getResultEnd() {
		return resultEnd;
	}
	public synchronized void setResultEnd(int resultEnd) {
		this.resultEnd = resultEnd;
	}
	public synchronized String getResultSeq() {
		return resultSeq;
	}
	public synchronized void setResultSeq(String resultSeq) {
		this.resultSeq = resultSeq;
	}
	@Override
	public int compareTo(TwoSeqOfMsaResult o) {
		return refStart-o.getRefStart();
	}
	
}
