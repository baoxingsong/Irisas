package me.songbx.model.CDS;

import me.songbx.model.Strand;
import me.songbx.model.Transcript.Transcript;
/**
 *  * rewrote hashcode and equals, start, end and strand are considered
 * rewrote compareTo, start are firstly considered, then end, and the sequences arrangement considers strand
 * @author song
 * @version 1.0, 2014-07-09
 */

public class Cds implements Comparable<Cds>{
	private Transcript transcript;
	private Integer start;
	private Integer end;
	
	/**
	 * @param start
	 * @param end
	 */
	public Cds(Integer start, Integer end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * built a new Cds, with the start, end, transcript information of parameter Cds object
	 * @param cds
	 */
	public Cds(Cds cds) {
		this.start = cds.getStart();
		this.end = cds.getEnd();
		this.transcript=cds.getTranscript();
	}
	public synchronized Transcript getTranscript() {
		return transcript;
	}
	public synchronized void setTranscript(Transcript transcript) {
		this.transcript = transcript;
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
	
	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (end ^ (end >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + ((this.getTranscript().getStrand() == null) ? 0 : this.getTranscript().getStrand().hashCode());
		return result;
	}
	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cds other = (Cds) obj;
		if (end == null) {
			if (other.getEnd() != null)
				return false;
		} else if (!end.equals(other.getEnd()))
			return false;
		if (start == null) {
			if (other.getStart() != null)
				return false;
		} else if (!start.equals(other.getStart()))
			return false;
		if (this.getTranscript().getStrand() != other.getTranscript().getStrand())
			return false;
		return true;
	}
	@Override
	public synchronized int compareTo( Cds arg0) {
		Cds e0 = this;
		Cds e1 = arg0;
		if(Strand.NEGTIVE == this.getTranscript().getStrand()){
			if(e0.getStart() == e1.getStart() && e0.getEnd() == e1.getEnd()){
				return 0;
			}else if(e0.getStart() > e1.getStart() || (e0.getStart() == e1.getStart() && e0.getEnd() > e1.getEnd())){
				return -1;
			}else{
				return 1;
			}
		}else{
			if(e0.getStart() == e1.getStart() && e0.getEnd() == e1.getEnd()){
				return 0;
			}else if(e0.getStart() > e1.getStart() || (e0.getStart() == e1.getStart() && e0.getEnd() > e1.getEnd())){
				return 1;
			}else{
				return -1;
			}
		}
	}
}
