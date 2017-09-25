package OtherFunctions.PopulationStructure.Model;

import java.util.HashSet;

public class MarkerPostionS{
	private HashSet<MarkerPostion> markerPostions = new HashSet<MarkerPostion>();
	public synchronized HashSet<MarkerPostion> getMarkerPostions() {
		return markerPostions;
	}
	public synchronized void setMarkerPostions(HashSet<MarkerPostion> markerPostions) {
		this.markerPostions = markerPostions;
	}
	public synchronized void add( MarkerPostion markerPostion ){
		this.markerPostions.add(markerPostion);
	}
	public synchronized void remove( MarkerPostion markerPostion ){
		this.markerPostions.remove(markerPostion);
	}
}