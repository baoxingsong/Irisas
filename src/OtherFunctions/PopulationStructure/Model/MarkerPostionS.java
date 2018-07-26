package OtherFunctions.PopulationStructure.Model;

import java.util.HashMap;

public class MarkerPostionS{
	private HashMap<Integer, MarkerPostion> markerPostions = new HashMap<Integer, MarkerPostion>();
	public synchronized HashMap<Integer, MarkerPostion> getMarkerPostions() {
		return markerPostions;
	}
	public synchronized void setMarkerPostions(HashMap<Integer, MarkerPostion> markerPostions) {
		this.markerPostions = markerPostions;
	}
	public synchronized void put( Integer position, MarkerPostion markerPostion ){
		this.markerPostions.put(position, markerPostion);
	}
}