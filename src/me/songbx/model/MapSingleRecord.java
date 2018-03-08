package me.songbx.model;

/**
 * rewrote hashcode and equals, basement and changed are considered
 * rewrote compareTo, only basement are condisered
 * @author song
 * @version 1.0, 2014-07-09
 */

public class MapSingleRecord implements Comparable<MapSingleRecord> {
	/**the position in reference genome*/
	private int basement;
	private int changed;
	private int lastTotalChanged;
	private int score;
	private String original=null;
	private String result=null;
	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + basement;
		result = prime * result + changed;
		if( this.getOriginal().length()==1 && this.getChanged()==0 && this.getResult().length()==1 ){
			result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		}
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
		MapSingleRecord other = (MapSingleRecord) obj;
		if (basement != other.basement)
			return false;
		if (changed != other.changed)
			return false;
		
		if( this.getOriginal().length()==1 && this.getChanged()==0 && this.getResult().length()==1 ){
			if (result == null) {
				if (other.result != null)
					return false;
			} else if (!result.equals(other.result))
				return false;
		}
		return true;
	}

	int changedPoint;
	public MapSingleRecord() {
	}
	public MapSingleRecord(int basement, int changed) {
		super();
		this.basement = basement;
		this.changed = changed;
	}
	
	public MapSingleRecord(int basement, int changed, String original, String result, int score) {
		super();
		this.basement = basement;
		this.changed = changed;
		this.original=original;
		this.result=result;
		this.score=score;
	}
	
	public MapSingleRecord(int basement, int changed, String original, String result) {
		super();
		this.basement = basement;
		this.changed = changed;
		this.original=original;
		this.result=result;
	}
	@Override
	public synchronized  int compareTo(MapSingleRecord o) {
		if(this.getBasement() != o.getBasement()){
			return this.getBasement()-o.getBasement();
		}
//		else if(this.getChanged()==0 && o.getChanged()!=0){
//			return -1;
//		}else if(this.getChanged()!=0 && o.getChanged()==0){
//			return 1;
//		}
		else if(this.getChanged() != o.getChanged()){
			return 0-(this.getChanged() - o.getChanged());
		}else{
			return 0;
		}
	}
	@Override
	public synchronized String toString(){
		return ""+this.getBasement()+"\t"+this.getChanged()+"\t"+this.getOriginal()+"\t"+this.getResult();
	}
	public synchronized int getBasement() {
		return basement;
	}
	public synchronized void setBasement(int basement) {
		this.basement = basement;
	}
	public synchronized int getChanged() {
		return changed;
	}
	public boolean ifPureSnp(){
		if( this.changed==0 && this.original.length()==1 && this.original.compareToIgnoreCase("-")!=0 ){
			return true;
		}else{
			return false;
		}
	}
	public synchronized void setChanged(int changed) {
		this.changed = changed;
	}
	public synchronized int getLastTotalChanged() {
		return lastTotalChanged;
	}
	public synchronized void setLastTotalChanged(int lastTotalChanged) {
		this.lastTotalChanged = lastTotalChanged;
	}
	public synchronized String getOriginal() {
		if(null==original || original.length()==0){
			return "-";
		}
		return original;
	}
	public synchronized void setOriginal(String original) {
		this.original = original;
	}
	public synchronized String getResult() {
		if(null==result || result.length()==0){
			return "-";
		}
		return result;
	}
	public synchronized void setResult(String result) {
		this.result = result;
	}
	public synchronized int getChangedPoint() {
		return changedPoint;
	}
	public synchronized void setChangedPoint(int changedPoint) {
		this.changedPoint = changedPoint;
	}
	public synchronized int getScore() {
		return score;
	}
	public synchronized void setScore(int score) {
		this.score = score;
	}
}