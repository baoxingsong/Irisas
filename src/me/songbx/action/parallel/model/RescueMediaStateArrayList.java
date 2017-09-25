package me.songbx.action.parallel.model;

import java.util.ArrayList;

public class RescueMediaStateArrayList {
	private ArrayList<RescueMediaState> rescueMediaStates = new ArrayList<RescueMediaState>();
	public synchronized void add(RescueMediaState rescueMediaState){
		rescueMediaStates.add(rescueMediaState);
	}

	public synchronized ArrayList<RescueMediaState> getRescueMediaStates() {
		return rescueMediaStates;
	}

	public synchronized void setRescueMediaStates(
			ArrayList<RescueMediaState> rescueMediaStates) {
		this.rescueMediaStates = rescueMediaStates;
	}
	
}
