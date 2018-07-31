package OtherFunctions.PopulationStructure.Model;

import me.songbx.action.parallel.model.SysHashMap;

public class MarkerPostionS{
	private SysHashMap<Integer, MarkerPostion> markerPostions;
	public MarkerPostionS(){
		markerPostions = new SysHashMap<Integer, MarkerPostion>(); //1M markers on each chromosome
	}
	public MarkerPostionS(int initialSize){
		markerPostions = new SysHashMap<Integer, MarkerPostion>(initialSize); //1M markers on each chromosome
	}
	public synchronized SysHashMap<Integer, MarkerPostion> getMarkerPostions() {
		return markerPostions;
	}
	public synchronized void setMarkerPostions(SysHashMap<Integer, MarkerPostion> markerPostions) {
		this.markerPostions = markerPostions;
	}
	public synchronized void put( Integer position, MarkerPostion markerPostion ){
		this.markerPostions.put(position, markerPostion);
	}
}
