package me.songbx.model;

import java.util.ArrayList;

public class TwoSeqOfMsaResult implements Comparable<TwoSeqOfMsaResult>{
	private int refStart;
	private int refEnd;
	private int resultStart;
	private int resultEnd;
	private ArrayList<Character> refSeq;
	private ArrayList<Character> resultSeq;
	
	public TwoSeqOfMsaResult(int refStart, int refEnd, ArrayList<Character> refSeq,
			int resultStart, int resultEnd, ArrayList<Character> resultSeq) {
		super();
		this.refStart = refStart;
		this.refEnd = refEnd;
		this.refSeq = refSeq;
		this.resultStart = resultStart;
		this.resultEnd = resultEnd;
		this.resultSeq = resultSeq;
	}
	
	public TwoSeqOfMsaResult(int refStart, int refEnd,  ArrayList<Character> refSeq,  ArrayList<Character> resultSeq) {
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
	public synchronized ArrayList<Character> getRefSeq() {
		return refSeq;
	}
	public synchronized void setRefSeq(ArrayList<Character> refSeq) {
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
	public synchronized ArrayList<Character> getResultSeq() {
		return resultSeq;
	}
	public synchronized void setResultSeq(ArrayList<Character> resultSeq) {
		this.resultSeq = resultSeq;
	}
	@Override
	public synchronized int compareTo(TwoSeqOfMsaResult o) {
		return refStart-o.getRefStart();
	}
}
