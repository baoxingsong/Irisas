package me.songbx.service;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.CDS.Cds;
import me.songbx.model.CDS.CdsLiftSequence;
import me.songbx.model.Transcript.Transcript;
import me.songbx.model.Transcript.TranscriptLiftStartEndSequenceAasequenceIndel;
import me.songbx.util.StandardGeneticCode;

/**
 * the sranscriptLiftStartEndSequenceAasequenceIndels in every collectors are validated.
 * The validation include length%3=0, start with start code, end with end code, no middle stop code,
 * And the splice site are AG, GT or same with COL
 * @author song
 * @version 1.0, 2014-07-09
 */

public class GeneSequenceOfAnnotationReadService {
	/*
	private StandardGeneticCode standardGeneticCode = new StandardGeneticCode();
	
	private AnnotationReadImpl 	annotationReadImpl;
	
	private ChromoSomeReadService targetChromeSomeRead;
	private ChromoSomeReadService referenceChromeSomeRead;
	private MapFileService mapFile;
	
	private HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>> transcriptArrayList = new HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>>();
	private HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> transcriptHashMap = new HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel>();
	//private HashSet<TranscriptLiftStartEndSequenceAasequenceIndel> transcriptHashSet = new HashSet<TranscriptLiftStartEndSequenceAasequenceIndel>();
	
	public GeneSequenceOfAnnotationReadService(ChromoSomeReadService targetChromeSomeRead, ChromoSomeReadService referenceChromeSomeRead, MapFileService mapFile){
		this.targetChromeSomeRead=targetChromeSomeRead;
		this.referenceChromeSomeRead=referenceChromeSomeRead;
		this.mapFile=mapFile;
	}
	public synchronized void builtAnnotationReadImpl(String fileLocation){
		annotationReadImpl = new AnnotationReadImpl(fileLocation);
	}
	public synchronized void updateInformation() {
		
		HashMap<String, HashSet<Transcript>> sstranscriptHashSet=annotationReadImpl.getTranscriptHashSet();
		Iterator<String> chrNamesIt = sstranscriptHashSet.keySet().iterator();
		while(chrNamesIt.hasNext()){
			ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel> temptranscriptHashSet = new ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>();
			String key = chrNamesIt.next();
			for(Transcript transcript : sstranscriptHashSet.get(key)){
				TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel = new TranscriptLiftStartEndSequenceAasequenceIndel(transcript);
				for(Cds cds : transcript.getCdsHashSet()){
					CdsLiftSequence cdsLiftSequence = new CdsLiftSequence(cds);
					transcriptLiftStartEndSequenceAasequenceIndel.getCdsLiftSequenceArrayList().add(cdsLiftSequence);
				}
				temptranscriptHashSet.add(transcriptLiftStartEndSequenceAasequenceIndel);
			}
			
			for(TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel : temptranscriptHashSet){
				transcriptLiftStartEndSequenceAasequenceIndel=updateTranscriptLiftStartEndSequenceAasequenceIndel(transcriptLiftStartEndSequenceAasequenceIndel, key);
				transcriptLiftStartEndSequenceAasequenceIndel.setCdsLiftSequenceArrayList(null);
				String name = transcriptLiftStartEndSequenceAasequenceIndel.getChromeSomeName();
					if(!transcriptArrayList.containsKey(name)){
						transcriptArrayList.put(name, new ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>());
					}
					transcriptArrayList.get(name).add(transcriptLiftStartEndSequenceAasequenceIndel);
					transcriptHashMap.put(transcriptLiftStartEndSequenceAasequenceIndel.getName(), transcriptLiftStartEndSequenceAasequenceIndel);
			}
		}
		
		
		
	}
	
	private synchronized TranscriptLiftStartEndSequenceAasequenceIndel updateTranscriptLiftStartEndSequenceAasequenceIndel(TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel, String chName){
		int start = 0;
		int end = 0;
				
		int i = 0;
		for(CdsLiftSequence cdsLiftSequence : transcriptLiftStartEndSequenceAasequenceIndel.getCdsLiftSequenceArrayList()){
			if(0 == i){
				start = cdsLiftSequence.getStart();
				end = cdsLiftSequence.getEnd();
				
			}else{
				if(start > cdsLiftSequence.getStart()){
					start = cdsLiftSequence.getStart();
				}//end if
				if(end < cdsLiftSequence.getEnd()){
					end = cdsLiftSequence.getEnd();
				}//end if
			}//end else
			i++;
		}//end for
		int liftStart = mapFile.getChangedFromBasement(chName, start);
		int liftEnd = mapFile.getChangedFromBasement(chName, end);
		
		transcriptLiftStartEndSequenceAasequenceIndel.setStart(start);
		transcriptLiftStartEndSequenceAasequenceIndel.setEnd(end);
		transcriptLiftStartEndSequenceAasequenceIndel.setLiftStart(liftStart);
		transcriptLiftStartEndSequenceAasequenceIndel.setLiftEnd(liftEnd);
		
		String fullSequence = targetChromeSomeRead.getSubSequence(chName, liftStart, liftEnd, transcriptLiftStartEndSequenceAasequenceIndel.getStrand());
		transcriptLiftStartEndSequenceAasequenceIndel.setFullequence(fullSequence);
    	return transcriptLiftStartEndSequenceAasequenceIndel;
	}
	

	public synchronized StandardGeneticCode getStandardGeneticCode() {
		return standardGeneticCode;
	}

	public synchronized void setStandardGeneticCode(StandardGeneticCode standardGeneticCode) {
		this.standardGeneticCode = standardGeneticCode;
	}

	public synchronized AnnotationReadImpl getAnnotationReadImpl() {
		return annotationReadImpl;
	}

	public synchronized void setAnnotationReadImpl(AnnotationReadImpl annotationReadImpl) {
		this.annotationReadImpl = annotationReadImpl;
	}

	public synchronized ChromoSomeReadService getTargetChromeSomeRead() {
		return targetChromeSomeRead;
	}

	public synchronized void setTargetChromeSomeRead(ChromoSomeReadService targetChromeSomeRead) {
		this.targetChromeSomeRead = targetChromeSomeRead;
	}

	public synchronized ChromoSomeReadService getReferenceChromeSomeRead() {
		return referenceChromeSomeRead;
	}

	public synchronized void setReferenceChromeSomeRead(
			ChromoSomeReadService referenceChromeSomeRead) {
		this.referenceChromeSomeRead = referenceChromeSomeRead;
	}

	public synchronized MapFileService getMapFile() {
		return mapFile;
	}

	public synchronized void setMapFile(MapFileService mapFile) {
		this.mapFile = mapFile;
	}

	public synchronized HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>> getTranscriptArrayList() {
		return transcriptArrayList;
	}

	public synchronized void setTranscriptArrayList(
			HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>> transcriptArrayList) {
		this.transcriptArrayList = transcriptArrayList;
	}

	public synchronized HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> getTranscriptHashMap() {
		return transcriptHashMap;
	}

	public synchronized void setTranscriptHashMap(
			HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> transcriptHashMap) {
		this.transcriptHashMap = transcriptHashMap;
	}
*/
}
