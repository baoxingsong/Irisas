package me.songbx.model.Transcript;

import java.util.HashSet;
import java.util.Iterator;


import me.songbx.model.Strand;
import me.songbx.model.CDS.Cds;

/**
 * hashCode equals, cdsHashSet, strand, chromeSome.getName
 * @author song
 * @version 1.0, 2014-07-09
 */

public class Transcript{
	private String name;
	private HashSet<Cds> cdsHashSet = new HashSet<Cds>();
	private Strand strand;
	private String chromeSomeName;
	
	/**
	 * @param name
	 */
	public Transcript (String  name){
		this.name=name;
	}
	
	/**
	 * built a new transcript, with the name, strand, chromeSome, cdsHashSet information of parameter transcript object
	 * @param transcript
	 */
	public Transcript (Transcript transcript){
		this.name=transcript.getName();
		this.strand=transcript.getStrand();
		this.chromeSomeName=transcript.getChromeSomeName();
		this.cdsHashSet = transcript.getCdsHashSet();
	}
	
	public synchronized String getName() {
		return name;
	}
	public String getChromeSomeName() {
		return chromeSomeName;
	}

	public void setChromeSomeName(String chromeSomeName) {
		this.chromeSomeName = chromeSomeName;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}
	public synchronized HashSet<Cds> getCdsHashSet() {
		return cdsHashSet;
	}
	public synchronized void setCdsHashSet(HashSet<Cds> cdsHashSet) {
		this.cdsHashSet = cdsHashSet;
	}
	public synchronized Strand getStrand() {
		return strand;
	}
	public synchronized void setStrand(Strand strand) {
		this.strand = strand;
	}
	
	
	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cdsHashSet == null) ? 0 : prime);
		result = prime * result
				+ ((chromeSomeName == null) ? 0 : chromeSomeName.hashCode());
		result = prime * result + ((strand == null) ? 0 : strand.hashCode());
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
		Transcript other = (Transcript) obj;
		if (cdsHashSet == null) {
			if (other.cdsHashSet != null)
				return false;
		}
		
		if (chromeSomeName == null) {
			if (other.chromeSomeName != null)
				return false;
		} else if (!chromeSomeName.equals(other.chromeSomeName))
			return false;
		if (strand != other.strand)
			return false;
		
		Iterator<Cds> ci = cdsHashSet.iterator();
		while(ci.hasNext()){
			Cds ciw = ci.next();
			if(! other.getCdsHashSet().contains(ciw)){
				return false;
			}
		}
		Iterator<Cds> oi = other.getCdsHashSet().iterator();
		while(oi.hasNext()){
			Cds oiw = oi.next();
			if(! this.getCdsHashSet().contains(oiw)){
				return false;
			}
		}
		
		return true;
	}
}