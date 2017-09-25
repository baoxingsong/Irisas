package me.songbx.service;

import java.util.HashMap;
import java.util.Iterator;

import me.songbx.model.Strand;
import me.songbx.model.Gene.Gene;

public class WGMSAAnnotationReadIncludeOrfLostExtendService {
	
	private ChromoSomeReadService targetChromeSomeRead;
	private MapFileService mapFile;
	
	private HashMap<String, HashMap<String, Gene>> geneHashMapHashMap = new HashMap<String, HashMap<String, Gene>>();
	
	private WGMSAAnnotationReadImplToGeneService annotationReadImplToGeneService;
	public WGMSAAnnotationReadIncludeOrfLostExtendService(ChromoSomeReadService targetChromeSomeRead, MapFileService mapFile, WGMSAAnnotationReadImplToGeneService annotationReadImplToGeneService){
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
		
		int liftStart = mapFile.getChangedFromBasement("Chr1", 1);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		int liftend = mapFile.getChangedFromBasement("Chr1", 3631);
		liftend=liftend+500;
		if(liftend > targetChromeSomeRead.getChromoSomeById("Chr1").getSequence().length()){
			liftend = targetChromeSomeRead.getChromoSomeById("Chr1").getSequence().length();
		}
		Gene gene = new Gene(liftStart, liftend, "Chr1");
		String sequence = targetChromeSomeRead.getSubSequence("Chr1", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr1Start");
		if(!geneHashMapHashMap.containsKey("Chr1")){
			geneHashMapHashMap.put("Chr1", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr1").put(gene.getName(), gene);
		
		liftStart = mapFile.getChangedFromBasement("Chr1", 30425192);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = targetChromeSomeRead.getChromoSomeById("Chr1").getSequence().length();
		
		gene = new Gene(liftStart, liftend, "Chr1");
		sequence = targetChromeSomeRead.getSubSequence("Chr1", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr1End");
		if(!geneHashMapHashMap.containsKey("Chr1")){
			geneHashMapHashMap.put("Chr1", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr1").put(gene.getName(), gene);
		
		
		
		liftStart = mapFile.getChangedFromBasement("Chr2", 1);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = mapFile.getChangedFromBasement("Chr2", 1025);
		liftend=liftend+500;
		if(liftend > targetChromeSomeRead.getChromoSomeById("Chr2").getSequence().length()){
			liftend = targetChromeSomeRead.getChromoSomeById("Chr2").getSequence().length();
		}
		gene = new Gene(liftStart, liftend, "Chr2");
		sequence = targetChromeSomeRead.getSubSequence("Chr2", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr2Start");
		if(!geneHashMapHashMap.containsKey("Chr2")){
			geneHashMapHashMap.put("Chr2", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr2").put(gene.getName(), gene);
		
		liftStart = mapFile.getChangedFromBasement("Chr2", 19689409);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = targetChromeSomeRead.getChromoSomeById("Chr2").getSequence().length();
		
		gene = new Gene(liftStart, liftend, "Chr2");
		sequence = targetChromeSomeRead.getSubSequence("Chr2", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr2End");
		if(!geneHashMapHashMap.containsKey("Chr2")){
			geneHashMapHashMap.put("Chr2", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr2").put(gene.getName(), gene);
		
		
		liftStart = mapFile.getChangedFromBasement("Chr3", 1);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = mapFile.getChangedFromBasement("Chr3", 4342);
		liftend=liftend+500;
		if(liftend > targetChromeSomeRead.getChromoSomeById("Chr3").getSequence().length()){
			liftend = targetChromeSomeRead.getChromoSomeById("Chr3").getSequence().length();
		}
		gene = new Gene(liftStart, liftend, "Chr3");
		sequence = targetChromeSomeRead.getSubSequence("Chr3", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr3Start");
		if(!geneHashMapHashMap.containsKey("Chr3")){
			geneHashMapHashMap.put("Chr3", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr3").put(gene.getName(), gene);
		
		liftStart = mapFile.getChangedFromBasement("Chr3", 2095105);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = targetChromeSomeRead.getChromoSomeById("Chr3").getSequence().length();
		
		gene = new Gene(liftStart, liftend, "Chr3");
		sequence = targetChromeSomeRead.getSubSequence("Chr3", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr3End");
		if(!geneHashMapHashMap.containsKey("Chr3")){
			geneHashMapHashMap.put("Chr3", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr3").put(gene.getName(), gene);
		
		
		liftStart = mapFile.getChangedFromBasement("Chr4", 1);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = mapFile.getChangedFromBasement("Chr4", 1180);
		liftend=liftend+500;
		if(liftend > targetChromeSomeRead.getChromoSomeById("Chr4").getSequence().length()){
			liftend = targetChromeSomeRead.getChromoSomeById("Chr4").getSequence().length();
		}
		gene = new Gene(liftStart, liftend, "Chr4");
		sequence = targetChromeSomeRead.getSubSequence("Chr4", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr4Start");
		if(!geneHashMapHashMap.containsKey("Chr4")){
			geneHashMapHashMap.put("Chr4", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr4").put(gene.getName(), gene);
		
		liftStart = mapFile.getChangedFromBasement("Chr4", 18583244);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = targetChromeSomeRead.getChromoSomeById("Chr4").getSequence().length();
		
		gene = new Gene(liftStart, liftend, "Chr4");
		sequence = targetChromeSomeRead.getSubSequence("Chr4", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr4End");
		if(!geneHashMapHashMap.containsKey("Chr4")){
			geneHashMapHashMap.put("Chr4", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr4").put(gene.getName(), gene);
		
		
		liftStart = mapFile.getChangedFromBasement("Chr5", 1);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = mapFile.getChangedFromBasement("Chr5", 1223);
		liftend=liftend+500;
		if(liftend > targetChromeSomeRead.getChromoSomeById("Chr5").getSequence().length()){
			liftend = targetChromeSomeRead.getChromoSomeById("Chr5").getSequence().length();
		}
		gene = new Gene(liftStart, liftend, "Chr5");
		sequence = targetChromeSomeRead.getSubSequence("Chr5", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr5Start");
		if(!geneHashMapHashMap.containsKey("Chr5")){
			geneHashMapHashMap.put("Chr5", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr5").put(gene.getName(), gene);
		
		liftStart = mapFile.getChangedFromBasement("Chr5", 26969546);
		liftStart =  liftStart-500;
		if(liftStart <= 0){
			liftStart = 1;
		}
		liftend = targetChromeSomeRead.getChromoSomeById("Chr5").getSequence().length();
		
		gene = new Gene(liftStart, liftend, "Chr5");
		sequence = targetChromeSomeRead.getSubSequence("Chr5", liftStart, liftend, Strand.POSITIVE);
		gene.setSequence(sequence);
		gene.setName("Chr5End");
		if(!geneHashMapHashMap.containsKey("Chr5")){
			geneHashMapHashMap.put("Chr5", new HashMap<String, Gene>());
		}
		geneHashMapHashMap.get("Chr5").put(gene.getName(), gene);
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

	public synchronized WGMSAAnnotationReadImplToGeneService getAnnotationReadImplToGeneService() {
		return annotationReadImplToGeneService;
	}

	public synchronized void setAnnotationReadImplToGeneService(
			WGMSAAnnotationReadImplToGeneService annotationReadImplToGeneService) {
		this.annotationReadImplToGeneService = annotationReadImplToGeneService;
	}
}
