package me.songbx.model.CDS;
/**
 * @author song
 * @version 1.0, 2014-07-09
 */
public class CdsLiftSequence extends Cds {
	public CdsLiftSequence(Cds cds) {
		super(cds);
		// TODO Auto-generated constructor stub
	}
	
	
	private Integer liftStart;
	private Integer liftEnd;
	private String sequence;
	
	
	

	public synchronized String getSequence() {
		return sequence;
	}

	public synchronized void setSequence(String sequence) {
		this.sequence = sequence;
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
}
