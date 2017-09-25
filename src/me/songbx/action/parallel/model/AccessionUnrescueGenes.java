package me.songbx.action.parallel.model;

import java.util.HashMap;
import java.util.HashSet;

public class AccessionUnrescueGenes {
	private HashMap<String, HashSet<String>> accessionGenes = new HashMap<String, HashSet<String>>();

	public synchronized HashMap<String, HashSet<String>> getAccessionGenes() {
		return accessionGenes;
	}

	public synchronized void setAccessionGenes(
			HashMap<String, HashSet<String>> accessionGenes) {
		this.accessionGenes = accessionGenes;
	}
	public synchronized void addRecord(String accession, String gene){
		if(!accessionGenes.containsKey(accession)){
			accessionGenes.put(accession, new HashSet<String>());
		}
		accessionGenes.get(accession).add(gene);
	}
}
