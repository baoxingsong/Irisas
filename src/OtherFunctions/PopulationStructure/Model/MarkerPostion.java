package OtherFunctions.PopulationStructure.Model;


import java.util.HashSet;

public class MarkerPostion implements Comparable<MarkerPostion> {
	private String chrName;
	private int position;
	private char colNaChar;
	private HashSet<Character> states = new HashSet<Character>();
	public MarkerPostion(String chrName, int position) {
		this.chrName = chrName;
		this.position = position;
	}
	public MarkerPostion(String chrName, int position, char colNaChar) {
		this.chrName = chrName;
		this.position = position;
		this.colNaChar=colNaChar;
	}
	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chrName == null) ? 0 : chrName.hashCode());
		result = prime * result + position;
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
		MarkerPostion other = (MarkerPostion) obj;
		if (chrName == null) {
			if (other.chrName != null)
				return false;
		} else if (!chrName.equals(other.chrName))
			return false;
		if (position != other.position)
			return false;
		return true;
	}
	public synchronized String getChrName() {
		return chrName;
	}
	public synchronized void setChrName(String chrName) {
		this.chrName = chrName;
	}
	public synchronized int getPosition() {
		return position;
	}
	public synchronized void setPosition(int position) {
		this.position = position;
	}
	public synchronized char getColNaChar() {
		return colNaChar;
	}
	public synchronized void setColNaChar(char colNaChar) {
		this.colNaChar = colNaChar;
	}
	public synchronized HashSet<Character> getStates() {
		return states;
	}
	public synchronized void setStates(HashSet<Character> states) {
		this.states = states;
	}

	@Override
	public synchronized int compareTo(MarkerPostion o) {
		MarkerPostion m0 = this;
		MarkerPostion m1 = o;
		if( ! m0.getChrName().equals(m1.getChrName()) ){
			return m0.getChrName().compareTo(m1.getChrName());
		}else{
			return m0.getPosition() - m1.getPosition();
		}
	}	
}