package me.songbx.model;

/**
 * redefined hashCode and equals function, if the names are same, two chromosomes could equal with each other
 * @author song
 * @version 1.0, 2014-07-09
 */
public class ChromoSome {
	
	private String name;
	private String sequence;
	
	public synchronized String getName() {
		return name;
	}
	public synchronized void setName(String name) {
		this.name = name;
	}
	public synchronized String getSequence() {
		return sequence;
	}
	public synchronized void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public ChromoSome(String name, String sequence) {
		super();
		this.name = name;
		this.sequence = sequence;
	}
	public synchronized void realeaseRam(){
		name=null;
		sequence=null;
	}
	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ChromoSome other = (ChromoSome) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
