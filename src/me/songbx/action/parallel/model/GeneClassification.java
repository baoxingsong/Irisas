package me.songbx.action.parallel.model;

import java.util.HashSet;

public class GeneClassification {
	private HashSet<String> geneNames = new HashSet<String>();
	public synchronized void add(String geneName){
		geneNames.add(geneName);
	}
	public synchronized HashSet<String> getGeneNames() {
		return geneNames;
	}
	public synchronized void setGeneNames(HashSet<String> geneNames) {
		this.geneNames = geneNames;
	}
	
}
