package me.songbx.action.parallel.model;

import java.util.HashSet;

public class TranscriptNames {
	private HashSet<String>  allTranscriptNames = new HashSet<String>();
	public synchronized HashSet<String> getAllTranscriptNames() {
		return allTranscriptNames;
	}
	public synchronized void setAllTranscriptNames(HashSet<String> allTranscriptNames) {
		this.allTranscriptNames = allTranscriptNames;
	}
	public synchronized void add(String name) {
		allTranscriptNames.add(name);
	}
}
