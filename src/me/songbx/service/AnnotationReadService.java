package me.songbx.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.MapSingleRecord;
import me.songbx.model.Strand;
import me.songbx.model.CDS.Cds;
import me.songbx.model.CDS.CdsLiftSequence;
import me.songbx.model.Transcript.Transcript;
import me.songbx.model.Transcript.TranscriptLiftStartEndSequenceAasequenceIndel;
import me.songbx.util.StandardGeneticCode;
import me.songbx.util.exception.codingNotFound;
import me.songbx.util.exception.codingNotThree;

/**
 * the sranscriptLiftStartEndSequenceAasequenceIndels in every collectors are validated.
 * The validation include length%3=0, start with start code, end with end code, no middle stop code,
 * And the splice site are AG, GT or same with COL
 * @author song
 * @version 1.0, 2014-07-09
 */

public class AnnotationReadService {
	/*
	private StandardGeneticCode standardGeneticCode = new StandardGeneticCode();
	
	private AnnotationReadImpl 	annotationReadImpl;
	
	private ChromoSomeReadService targetChromeSomeRead;
	private ChromoSomeReadService referenceChromeSomeRead;
	private MapFileService mapFile;
	
	private HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>> transcriptArrayList = new HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>>();
	private HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> transcriptHashMap = new HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel>();
	//private HashSet<TranscriptLiftStartEndSequenceAasequenceIndel> transcriptHashSet = new HashSet<TranscriptLiftStartEndSequenceAasequenceIndel>();
	
	public AnnotationReadService(ChromoSomeReadService targetChromeSomeRead, ChromoSomeReadService referenceChromeSomeRead, MapFileService mapFile){
		this.targetChromeSomeRead = targetChromeSomeRead;
		this.referenceChromeSomeRead = referenceChromeSomeRead;
		this.mapFile=mapFile;
	}
	public synchronized void builtAnnotationReadImpl(String fileLocation){
		annotationReadImpl = new AnnotationReadImpl(fileLocation);
	}
	public synchronized void updateInformation() {
		this.updateInformation(false);
	}
	public synchronized void updateInformation(boolean addAasequence) {
		ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel> temptranscriptHashSet = new ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>();
		HashMap<String, HashSet<Transcript>> sstranscriptHashSet=annotationReadImpl.getTranscriptHashSet();
		Iterator<String> chrNamesIt = sstranscriptHashSet.keySet().iterator();
		while(chrNamesIt.hasNext()){
			String key = chrNamesIt.next();
			for(Transcript transcript : sstranscriptHashSet.get(key)){
				TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel = new TranscriptLiftStartEndSequenceAasequenceIndel(transcript);
				for(Cds cds : transcript.getCdsHashSet()){
					CdsLiftSequence cdsLiftSequence = new CdsLiftSequence(cds);
					cdsLiftSequence=this.updateCdsLiftSequence(cdsLiftSequence, key);
					transcriptLiftStartEndSequenceAasequenceIndel.getCdsLiftSequenceArrayList().add(cdsLiftSequence);
				}
				temptranscriptHashSet.add(transcriptLiftStartEndSequenceAasequenceIndel);
			}
		}
		
		for(TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel : temptranscriptHashSet){
			transcriptLiftStartEndSequenceAasequenceIndel=updateTranscriptLiftStartEndSequenceAasequenceIndel(transcriptLiftStartEndSequenceAasequenceIndel, addAasequence);
			String name = transcriptLiftStartEndSequenceAasequenceIndel.getChromeSomeName();
			boolean ifSelectTaur10 = ifSpliceSitesOk(transcriptLiftStartEndSequenceAasequenceIndel, name);
			
			

			if(ifSelectTaur10 && this.ifTakeTair10(transcriptLiftStartEndSequenceAasequenceIndel) ){
				if(!transcriptArrayList.containsKey(name)){
					transcriptArrayList.put(name, new ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>());
				}
				transcriptArrayList.get(name).add(transcriptLiftStartEndSequenceAasequenceIndel);
				transcriptHashMap.put(transcriptLiftStartEndSequenceAasequenceIndel.getName(), transcriptLiftStartEndSequenceAasequenceIndel);
				//transcriptLiftStartEndSequenceAasequenceIndel.setCdsHashSet(null);
				//transcriptLiftStartEndSequenceAasequenceIndel.setCdsLiftSequenceArrayList(null);
			}
		}
		//System.err.println("transcript update over");
		Iterator<Entry<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>>> iterTranscriptList = transcriptArrayList.entrySet().iterator();
	    while(iterTranscriptList.hasNext()){
	    	ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel> ta = iterTranscriptList.next().getValue();
	    	Collections.sort(ta);
	    }
	}
	private synchronized CdsLiftSequence updateCdsLiftSequence(CdsLiftSequence cdsLiftSequence, String chName){
		cdsLiftSequence.setLiftEnd(mapFile.getChangedFromBasement(chName, cdsLiftSequence.getEnd()));
		cdsLiftSequence.setLiftStart(mapFile.getChangedFromBasement(chName, cdsLiftSequence.getStart()));
		cdsLiftSequence.setSequence(targetChromeSomeRead.getSubSequence(chName, cdsLiftSequence.getLiftStart(), cdsLiftSequence.getLiftEnd(), cdsLiftSequence.getTranscript().getStrand()));
		return cdsLiftSequence;
	}
	
	private synchronized TranscriptLiftStartEndSequenceAasequenceIndel updateTranscriptLiftStartEndSequenceAasequenceIndel(TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel, boolean addAasequence){
		int start = 0;
		int end = 0;
		
		int liftStart = 0;
		int liftEnd = 0;
		
		int i = 0;
		ArrayList<CdsLiftSequence> cdsLiftSequenceArrayList = new ArrayList<CdsLiftSequence>(); // transfer hashset to arraylist
		
		for(CdsLiftSequence cdsLiftSequence : transcriptLiftStartEndSequenceAasequenceIndel.getCdsLiftSequenceArrayList()){
			cdsLiftSequenceArrayList.add(cdsLiftSequence);
			if(0 == i){
				start = cdsLiftSequence.getStart();
				end = cdsLiftSequence.getEnd();
				
				liftStart = cdsLiftSequence.getLiftStart();
				liftEnd = cdsLiftSequence.getLiftEnd();
			}else{
				if(start > cdsLiftSequence.getStart()){
					start = cdsLiftSequence.getStart();
					liftStart = cdsLiftSequence.getLiftStart();
				}//end if
				if(end < cdsLiftSequence.getEnd()){
					end = cdsLiftSequence.getEnd();
					liftEnd = cdsLiftSequence.getLiftEnd();
				}//end if
			}//end else
			i++;
		}//end for
		
		
		
		
		transcriptLiftStartEndSequenceAasequenceIndel.setStart(start);
		transcriptLiftStartEndSequenceAasequenceIndel.setEnd(end);
		transcriptLiftStartEndSequenceAasequenceIndel.setLiftStart(liftStart);
		transcriptLiftStartEndSequenceAasequenceIndel.setLiftEnd(liftEnd);
		
		Collections.sort(cdsLiftSequenceArrayList);
		
		
		StringBuffer sequenceStringBuffer =new StringBuffer();
    	for(CdsLiftSequence c : cdsLiftSequenceArrayList){
    		sequenceStringBuffer.append(c.getSequence());
    		//System.out.println("c.getSequence():\t"+c.getSequence());
    		String chName = transcriptLiftStartEndSequenceAasequenceIndel.getChromeSomeName();
    		if(mapFile.getRecords().containsKey(chName)){
    			ArrayList<MapSingleRecord> records = mapFile.getRecords().get(chName);
    			for(MapSingleRecord mapSingleRecord : records){
	    			if(c.getStart() <= mapSingleRecord.getBasement()-1 && mapSingleRecord.getBasement() < c.getEnd()){
	    				if(mapSingleRecord.getChanged() != 0){
	    					MapSingleRecord newMapSingleRecord = new MapSingleRecord(mapSingleRecord.getBasement(), mapSingleRecord.getChanged());
		    				transcriptLiftStartEndSequenceAasequenceIndel.getMapSingleRecords().add(newMapSingleRecord);
	    					transcriptLiftStartEndSequenceAasequenceIndel.setIndeled(true);
        				}//end if
	    			}
    			}// end for
    		}//end if
    	}//end for
    	
    	String sequence = sequenceStringBuffer.toString();
    	sequenceStringBuffer =new StringBuffer();
    	
    	Collections.sort(transcriptLiftStartEndSequenceAasequenceIndel.getMapSingleRecords());
    	
    	if(! ifLengthCouldbeDivedBYThree(sequence)){
    		Pattern p = Pattern.compile("^\\wATG");
    		Matcher m = p.matcher(sequence);
			if( m.find()){
				sequence=sequence.substring(1, sequence.length());
				cdsLiftSequenceArrayList.get(0).setSequence(cdsLiftSequenceArrayList.get(0).getSequence().substring(1, cdsLiftSequenceArrayList.get(0).getSequence().length()));
				
				if(transcriptLiftStartEndSequenceAasequenceIndel.getStrand() == Strand.POSITIVE){
					transcriptLiftStartEndSequenceAasequenceIndel.setLiftStart(liftStart+1);
					cdsLiftSequenceArrayList.get(0).setLiftStart(liftStart+1);
				}else{
					transcriptLiftStartEndSequenceAasequenceIndel.setLiftEnd(liftEnd - 1);
					cdsLiftSequenceArrayList.get(0).setLiftEnd(liftEnd - 1);
				}
			}
		}
    	if(! ifLengthCouldbeDivedBYThree(sequence)){
    		Pattern p1 = Pattern.compile("TAA\\w$");
    		Matcher m1 = p1.matcher(sequence);
    		Pattern p2 = Pattern.compile("TAG\\w$");
    		Matcher m2 = p2.matcher(sequence);
    		Pattern p3 = Pattern.compile("TGA\\w$");
    		Matcher m3 = p3.matcher(sequence);
    		if(m1.find() || m2.find() || m3.find()){
    			sequence=sequence.substring(0, sequence.length()-1);
    			cdsLiftSequenceArrayList.get(cdsLiftSequenceArrayList.size()-1).setSequence(cdsLiftSequenceArrayList.get(cdsLiftSequenceArrayList.size()-1).getSequence().substring(0, cdsLiftSequenceArrayList.get(cdsLiftSequenceArrayList.size()-1).getSequence().length()-1));
				
    			if(transcriptLiftStartEndSequenceAasequenceIndel.getStrand() == Strand.POSITIVE){
					transcriptLiftStartEndSequenceAasequenceIndel.setLiftEnd(liftEnd-1);
					cdsLiftSequenceArrayList.get(cdsLiftSequenceArrayList.size()-1).setLiftEnd(liftEnd-1);
				}else{
					transcriptLiftStartEndSequenceAasequenceIndel.setLiftStart(liftStart + 1);
					cdsLiftSequenceArrayList.get(cdsLiftSequenceArrayList.size()-1).setLiftStart(liftStart + 1);
				}
    		}
    	}
    	transcriptLiftStartEndSequenceAasequenceIndel.setCdsLiftSequenceArrayList(cdsLiftSequenceArrayList);
    	transcriptLiftStartEndSequenceAasequenceIndel.setSequence(sequence);
    	
    	if(addAasequence){
	    	StringBuffer aasb = new StringBuffer();
			for(int ij=0;ij<sequence.length()-3;ij+=3){
				try {
					aasb.append(standardGeneticCode.getGeneticCode(sequence.substring(ij, ij+3), (0 == ij)));
				} catch (codingNotThree e) {
					e.printStackTrace();
				} catch (codingNotFound e) {
					e.printStackTrace();
				}
			}//end for
			transcriptLiftStartEndSequenceAasequenceIndel.setAaSequence(aasb.toString());
    	}
		return transcriptLiftStartEndSequenceAasequenceIndel;
	}
	

	
	private synchronized boolean ifTakeTair10(TranscriptLiftStartEndSequenceAasequenceIndel t){
		String cdsSequenceString = t.getSequence();
		if(cdsSequenceString.length()<=3){
			return false;
		}

		if(!ifLengthCouldbeDivedBYThree(cdsSequenceString)){
			return false;
		}
		if(ifNewStopCOde(cdsSequenceString)){
			return false;
		}
		if(!ifEndWithStopCode(cdsSequenceString)){
			return false;
		}
		if(!ifStartWithStartCode(cdsSequenceString)){
			return false;
		}
		return true;
	}
	private synchronized boolean ifLengthCouldbeDivedBYThree(String cdsSequence){
		String cdsSequenceString = cdsSequence;
		if(0 != cdsSequenceString.length()%3){
			return false;
		}
		return true;
	}

	private synchronized boolean ifNewStopCOde(String cdsSequence){
		String cdsSequenceString = cdsSequence;
		for(int jj=0; jj<cdsSequenceString.length()-3; jj+=3){
    		try {
				if('*' == standardGeneticCode.getGeneticCode(cdsSequenceString.substring(jj, jj+3), (0 == jj))){// new stop code
					return true;
				}
			} catch (codingNotThree | codingNotFound e) {
				//e.printStackTrace();
			}
    	}
		return false;
	}
	private synchronized boolean ifEndWithStopCode(String cdsSequence){
		String cdsSequenceString = cdsSequence;
		try {
			if(('*' != standardGeneticCode.getGeneticCode(cdsSequenceString.substring(cdsSequenceString.length()-3, cdsSequenceString.length()), false)) && 
					'X' != standardGeneticCode.getGeneticCode(cdsSequenceString.substring(cdsSequenceString.length()-3, cdsSequenceString.length()), false)
					){
				return false; // stop code disappeared
			}
		} catch (codingNotThree e) {
			//e.printStackTrace();
		} catch (codingNotFound e) {
			//e.printStackTrace();
		}
		return true;
	}


	private synchronized boolean ifStartWithStartCode(String cdsSequence){
		String cdsSequenceString = cdsSequence;
		try {
			if(('M' != standardGeneticCode.getGeneticCode(cdsSequenceString.substring(0, 3), true)) && 
					'X' != standardGeneticCode.getGeneticCode(cdsSequenceString.substring(0, 3), true)
					){
				return false; // start code disappeared
			}
		} catch (codingNotThree e) {
			//e.printStackTrace();
		} catch (codingNotFound e) {
			//e.printStackTrace();
		}
		return true;
	}
	private synchronized boolean ifSpliceSitesOk(TranscriptLiftStartEndSequenceAasequenceIndel t2, String chromeSomeName){
		boolean ifSelectTaur10 = true;
		for(int iiii=1; iiii < t2.getCdsLiftSequenceArrayList().size(); iiii++){
			int le;
			int ts;
			String s1;
			String s2;
			int let;
			int tst;
			String s1t;
			String s2t;
			if(t2.getStrand() == Strand.POSITIVE){
				le = t2.getCdsLiftSequenceArrayList().get(iiii-1).getEnd();
				ts = t2.getCdsLiftSequenceArrayList().get(iiii).getStart();
				s1 = referenceChromeSomeRead.getSubSequence(chromeSomeName, le+1, le+2, t2.getStrand());
				s2 = referenceChromeSomeRead.getSubSequence(chromeSomeName, ts-2, ts-1, t2.getStrand());
				
				let = t2.getCdsLiftSequenceArrayList().get(iiii-1).getLiftEnd();
				tst = t2.getCdsLiftSequenceArrayList().get(iiii).getLiftStart();
				s1t = targetChromeSomeRead.getSubSequence(chromeSomeName, let+1, let+2, t2.getStrand());
				s2t = targetChromeSomeRead.getSubSequence(chromeSomeName, tst-2, tst-1, t2.getStrand());
			}else{
				le = t2.getCdsLiftSequenceArrayList().get(iiii-1).getStart();
				ts = t2.getCdsLiftSequenceArrayList().get(iiii).getEnd();
				//System.out.println(""+chromeSomeName+"\t"+(le-2)+"\t"+(le-1)+"\t"+t2.getStrand());
				s1 = referenceChromeSomeRead.getSubSequence(chromeSomeName, le-2, le-1, t2.getStrand());
				s2 = referenceChromeSomeRead.getSubSequence(chromeSomeName, ts+2, ts+1, t2.getStrand());
				
				let = t2.getCdsLiftSequenceArrayList().get(iiii-1).getLiftStart();
				tst = t2.getCdsLiftSequenceArrayList().get(iiii).getLiftEnd();
				s1t = targetChromeSomeRead.getSubSequence(chromeSomeName, let-2, let-1, t2.getStrand());
				s2t = targetChromeSomeRead.getSubSequence(chromeSomeName, tst+2, tst+1, t2.getStrand());
			}
			
			s2=this.agIUPACcodesTranslation(s2);
			s1=this.gtIUPACcodesTranslation(s1);
			s2t=this.agIUPACcodesTranslation(s2t);
			s1t=this.gtIUPACcodesTranslation(s1t);
			
			if("GT".equals(s1t) && "AG".equals(s2t)){
				
			}else{
				if(!s1t.equals(s1)){
					ifSelectTaur10 = false;
				}
				if(!s2t.equals(s2)){
					ifSelectTaur10 = false;
				}
			}
		}
		return ifSelectTaur10;
	}
	private synchronized String agIUPACcodesTranslation(String ag){
		String agString = ag;
		if(('A'==ag.charAt(0) || 'R'==ag.charAt(0) || 'W'==ag.charAt(0) || 'M'==ag.charAt(0) || 'D'==ag.charAt(0) || 'H'==ag.charAt(0) || 'V'==ag.charAt(0) || 'N'==ag.charAt(0)) && ('G'==ag.charAt(1)||'K'==ag.charAt(1)||'R'==ag.charAt(1)||'S'==ag.charAt(1)||'B'==ag.charAt(1)||'D'==ag.charAt(1)||'V'==ag.charAt(1)||'N'==ag.charAt(1))){
			agString = "AG";
		}
		return agString;
	}
	private synchronized String gtIUPACcodesTranslation(String gt){
		String gtString = gt;
		if(('G'==gtString.charAt(0)||'K'==gtString.charAt(0)||'R'==gtString.charAt(0)||'S'==gtString.charAt(0)||'B'==gtString.charAt(0)||'D'==gtString.charAt(0)||'V'==gtString.charAt(0)||'N'==gtString.charAt(0))&&('T'==gtString.charAt(1)||'K'==gtString.charAt(1)||'Y'==gtString.charAt(1)||'W'==gtString.charAt(1)||'U'==gtString.charAt(1)||'B'==gtString.charAt(1)||'D'==gtString.charAt(1)||'H'==gtString.charAt(1)||'N'==gtString.charAt(1))){
			gtString="GT";
		}
		return gtString;
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
