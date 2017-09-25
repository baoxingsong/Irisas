package me.songbx.service;

import java.util.HashMap;
import java.util.Iterator;

import me.songbx.model.Strand;
import me.songbx.model.Gene.Gene;

public class AnnotationReadIncludeOrfLostExtendService {
	
	private ChromoSomeReadService targetChromeSomeRead;
	private MapFileService mapFile;
	
	private HashMap<String, HashMap<String, Gene>> geneHashMapHashMap = new HashMap<String, HashMap<String, Gene>>();
	
	private AnnotationReadImplToGeneService annotationReadImplToGeneService;
	public AnnotationReadIncludeOrfLostExtendService(ChromoSomeReadService targetChromeSomeRead, MapFileService mapFile, AnnotationReadImplToGeneService annotationReadImplToGeneService){
		this.targetChromeSomeRead=targetChromeSomeRead;
		this.mapFile=mapFile;
		this.annotationReadImplToGeneService=annotationReadImplToGeneService;
		this.updateInformation();
	}
	
	public synchronized void updateInformation() {
		Iterator<String> chrNamesIt = annotationReadImplToGeneService.getGeneHashMapArrayList().keySet().iterator();
		while(chrNamesIt.hasNext()){
			String key = chrNamesIt.next();
			for(Gene g : annotationReadImplToGeneService.getGeneHashMapArrayList().get(key)){
				
				int liftStart = mapFile.getChangedFromBasement(g.getChromesomeName(), g.getStart());
				liftStart =  liftStart-500;
				if(liftStart <= 0){
					liftStart = 1;
				}
				
				int liftend = mapFile.getChangedFromBasement(g.getChromesomeName(), g.getEnd());
				liftend=liftend+500;
				if(liftend > targetChromeSomeRead.getChromoSomeById(g.getChromesomeName()).getSequence().length()){
					liftend = targetChromeSomeRead.getChromoSomeById(g.getChromesomeName()).getSequence().length();
				}
				
				Gene gene = new Gene(liftStart, liftend, g.getChromesomeName());
				
				
				String sequence = targetChromeSomeRead.getSubSequence(g.getChromesomeName(), liftStart, liftend, Strand.POSITIVE);
				gene.setSequence(sequence);
				gene.setName(g.getName());
				if(!geneHashMapHashMap.containsKey(key)){
					geneHashMapHashMap.put(key, new HashMap<String, Gene>());
				}
				geneHashMapHashMap.get(key).put(gene.getName(), gene);
			}
		}
	}

	public synchronized ChromoSomeReadService getTargetChromeSomeRead() {
		return targetChromeSomeRead;
	}

	public synchronized void setTargetChromeSomeRead(
			ChromoSomeReadService targetChromeSomeRead) {
		this.targetChromeSomeRead = targetChromeSomeRead;
	}

	public synchronized MapFileService getMapFile() {
		return mapFile;
	}

	public synchronized void setMapFile(MapFileService mapFile) {
		this.mapFile = mapFile;
	}

	public synchronized HashMap<String, HashMap<String, Gene>> getGeneHashMapHashMap() {
		return geneHashMapHashMap;
	}

	public synchronized void setGeneHashMapHashMap(
			HashMap<String, HashMap<String, Gene>> geneHashMapHashMap) {
		this.geneHashMapHashMap = geneHashMapHashMap;
	}

	public synchronized AnnotationReadImplToGeneService getAnnotationReadImplToGeneService() {
		return annotationReadImplToGeneService;
	}

	public synchronized void setAnnotationReadImplToGeneService(
			AnnotationReadImplToGeneService annotationReadImplToGeneService) {
		this.annotationReadImplToGeneService = annotationReadImplToGeneService;
	}
	


}
