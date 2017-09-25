package me.songbx.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.CDS.Cds;
import me.songbx.model.Gene.Gene;
import me.songbx.model.Transcript.Transcript;

public class AnnotationReadImplToGeneService {
	private HashMap<String, ArrayList<Gene>> geneHashMapArrayList = new HashMap<String, ArrayList<Gene>>();
	public AnnotationReadImplToGeneService(AnnotationReadImpl annotationReadImpl){
		HashMap<String, HashSet<Transcript>> transcriptHashSet = annotationReadImpl.getTranscriptHashSet();
		Iterator<String> chrNamesIt = transcriptHashSet.keySet().iterator();
		while(chrNamesIt.hasNext()){
			String key = chrNamesIt.next();
			for(Transcript transcript : transcriptHashSet.get(key)){
				int start = Integer.MAX_VALUE;
				int end = 0;
				for(Cds cds : transcript.getCdsHashSet()){
					if(start > cds.getStart()){
						start = cds.getStart();
					}
					if(end < cds.getEnd()){
						end = cds.getEnd();
					}
				}
				Gene gene = new Gene(start, end, transcript.getChromeSomeName());
				String name = ""+start+"_"+end;
				gene.setName(name);
				if(geneHashMapArrayList.containsKey(key)){
					
				}else{
					geneHashMapArrayList.put(key, new ArrayList<Gene>());
				}
				geneHashMapArrayList.get(key).add(gene);
			}
		}
		System.out.println("first trancript to gene done");
		Iterator<String> chrGeneIt = geneHashMapArrayList.keySet().iterator();
		while(chrGeneIt.hasNext()){
			String key = chrGeneIt.next();
			System.out.println(key + "first trancript to gene begin");
			System.out.println(geneHashMapArrayList.get(key).size());
			this.combineGenes(geneHashMapArrayList.get(key));
			Collections.sort( geneHashMapArrayList.get(key) );
			System.out.println(key + "first trancript to gene done");
			System.out.println(geneHashMapArrayList.get(key).size());
		}
	}
	
	private void combineGenes(ArrayList<Gene> glist){
		boolean ifcombined = false;
		for(int i=0; i<glist.size(); i++){
			Gene g1 = glist.get(i);
			boolean ifcombinedb = false;
			for(int j=0; j<glist.size(); j++){
				Gene g2 = glist.get(j);
				if(i!=j && g1!=g2 && if2GenesOverLap(g1, g2)){
					int start;
					int end;
					if(g1.getStart() < g2.getStart()){
						start = g1.getStart();
					}else{
						start = g2.getStart();
					}
					if(g1.getEnd() > g2.getEnd()){
						end = g1.getEnd();
					}else{
						end = g2.getEnd();
					}
					Gene gene = new Gene(start, end, g1.getChromesomeName());
					String name = ""+start+"_"+end;
					gene.setName(name);
					glist.remove(g2);
					glist.remove(g1);
					glist.add(gene);
					ifcombined = true;
					ifcombinedb = true;
					break;
				}
			}
			if(ifcombinedb){
				i--;
			}
		}
		if(ifcombined){
			this.combineGenes(glist);
		}
	}

	private boolean if2GenesOverLap(Gene gene1, Gene gene2){
		if(gene1.getStart() <= gene2.getStart() && gene2.getStart() <= gene1.getEnd()){
			return true;
		}else if(gene1.getStart() <= gene2.getEnd() && gene2.getEnd() <= gene1.getEnd()){
			return true;
		}else if(gene2.getStart()<=gene1.getStart() && gene1.getStart()<=gene2.getEnd()){
			return true;
		}else if(gene2.getStart()<=gene1.getEnd() && gene1.getEnd()<=gene2.getEnd()){
			return true;
		}else{
			return false;
		}
	}

	public synchronized HashMap<String, ArrayList<Gene>> getGeneHashMapArrayList() {
		return geneHashMapArrayList;
	}

	public synchronized void setGeneHashMapArrayList(
			HashMap<String, ArrayList<Gene>> geneHashMapArrayList) {
		this.geneHashMapArrayList = geneHashMapArrayList;
	}

}
