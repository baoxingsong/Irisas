package OtherFunctions.ReSdiWithWindowsedMsa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import OtherFunctions.ReSdiWithWindowsedMsa.FirstLastList.Data;
import me.songbx.action.parallel.model.MsaFileRecordArrayList;
import me.songbx.impl.MsaFileReadImpl;
import me.songbx.model.ChromoSome;
import me.songbx.model.MapSingleRecord;
import me.songbx.model.MsaFileRecord;
import me.songbx.model.MsaSingleRecord;
import me.songbx.model.Strand;
import me.songbx.model.TwoSeqOfMsaResult;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.util.MyThreadCount;
/**
 * this is the link version much faster than array list version 
 */
public class ReSDIFromMsaActionLinkVersion {
	private ArrayList<String> names;
	private HashMap<String, ArrayList<String>> msaFileLocationsHashmap;
	private int threadNumber;

	
	private HashMap<String, ArrayList<MsaFileRecord>> msaFileRecordsHashMap = new HashMap<String, ArrayList<MsaFileRecord>>();
	private String outputDir;
	
	private String refName;
	private String genomeFolder;
	private ChromoSomeReadService refChromoSomeRead;
	public ReSDIFromMsaActionLinkVersion(ArrayList<String> names, HashMap<String, ArrayList<String>> msaFileLocationsHashmap, int threadNumber, String outputDir,
		String refName, String genomeFolder, ChromoSomeReadService refChromoSomeRead){
		this.names=names;
		this.msaFileLocationsHashmap=msaFileLocationsHashmap;
		this.threadNumber=threadNumber;
		this.outputDir=outputDir;
		this.genomeFolder=genomeFolder;
		this.refName = refName;
		this.refChromoSomeRead = refChromoSomeRead;
		this.doIt();
		System.gc();
	}

	private void doIt(){
		Iterator<String> chrNameI = msaFileLocationsHashmap.keySet().iterator();
		while(chrNameI.hasNext()){
			String chrName = chrNameI.next();
			ArrayList<String> msaFileLocations = msaFileLocationsHashmap.get(chrName);
			MsaFileRecordArrayList msaFileRecordArrayList = new MsaFileRecordArrayList();
			MyThreadCount threadCount = new MyThreadCount(0);
			for(String msaFileLocation : msaFileLocations){
				boolean isThisThreadUnrun=true;
				while(isThisThreadUnrun){
	                if(threadCount.getCount() < threadNumber){
	                	MsaFileRead msaFileRead = new MsaFileRead(msaFileLocation, msaFileRecordArrayList, threadCount, names);
	                	//System.out.println("msa Reading " + msaFileLocation);
	                    threadCount.plusOne();
	                    msaFileRead.start();
	                    isThisThreadUnrun=false;
	                    break;
	                }else{
	                    try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	                }
	            }
			}
			System.gc();
			while(threadCount.hasNext()){// wait for all the threads
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			msaFileRecordArrayList.sort();
			msaFileRecordsHashMap.put(chrName, msaFileRecordArrayList.getMsaFileRecords());
		}
		//modified on 26 May 2016
		for(String chrName : msaFileLocationsHashmap.keySet()){
			ArrayList<MsaFileRecord> msaFileRecords = msaFileRecordsHashMap.get(chrName);
			Collections.sort(msaFileRecords);
			msaFileRecordsHashMap.put(chrName, msaFileRecords);
		}
		System.out.println("multiple sequences alignment reading is done");
		

		MyThreadCount threadCount = new MyThreadCount(0);
		for(String name : names){
			String targetchromeSomeReadFileLocation;
			targetchromeSomeReadFileLocation = genomeFolder + File.separator + name + ".fa";
			
			boolean isThisThreadUnrun=true;
			while(isThisThreadUnrun){
                if(threadCount.getCount() < threadNumber){
                	NewSdiFile newSdiFile = new NewSdiFile(targetchromeSomeReadFileLocation, refChromoSomeRead, name, threadCount, msaFileRecordsHashMap, outputDir);
                    threadCount.plusOne();
                    newSdiFile.start();
                    isThisThreadUnrun=false;
                    break;
                }else{
                    try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                }
            }
		}
		System.gc();
		while(threadCount.hasNext()){// wait for all the threads
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized ArrayList<String> getNames() {
		return names;
	}
	public synchronized void setNames(ArrayList<String> names) {
		this.names = names;
	}
	public synchronized HashMap<String, ArrayList<String>> getMsaFileLocationsHashmap() {
		return msaFileLocationsHashmap;
	}
	public synchronized void setMsaFileLocationsHashmap(
			HashMap<String, ArrayList<String>> msaFileLocationsHashmap) {
		this.msaFileLocationsHashmap = msaFileLocationsHashmap;
	}
	public synchronized HashMap<String, ArrayList<MsaFileRecord>> getMsaFileRecordsHashMap() {
		return msaFileRecordsHashMap;
	}
	public synchronized void setMsaFileRecordsHashMap(
			HashMap<String, ArrayList<MsaFileRecord>> msaFileRecordsHashMap) {
		this.msaFileRecordsHashMap = msaFileRecordsHashMap;
	}
	class MsaFileRead extends Thread{
		private String msaFileLocation;
		private MsaFileRecordArrayList msaFileRecordArrayList;
		private MyThreadCount threadCount;
		private HashSet<String> names = new HashSet<String>();
		
		public MsaFileRead(String msaFileLocation, MsaFileRecordArrayList msaFileRecordArrayList, MyThreadCount threadCount, ArrayList<String> names){
			this.msaFileLocation=msaFileLocation;
			this.msaFileRecordArrayList=msaFileRecordArrayList;
			this.threadCount=threadCount;
			this.names.addAll(names);
			this.names.add(refName);
		}
		public void run(){
			MsaFileReadImpl msaFileReadImpl = new MsaFileReadImpl(msaFileLocation, names);
			msaFileRecordArrayList.add(msaFileReadImpl.getMsaFileRecord());
			threadCount.countDown();
		}
	}
	
	class NewSdiFile extends Thread{
		private String targetchromeSomeReadFileLocation;
		private ChromoSomeReadService refChromoSomeRead;
		private String name;
		private MyThreadCount threadCount;
		private HashMap<String, ArrayList<MsaFileRecord>> msaFileRecordsHashMap;
		private String outputDir;
		public NewSdiFile(String targetchromeSomeReadFileLocation, ChromoSomeReadService refChromoSomeRead, String name, MyThreadCount threadCount, HashMap<String, ArrayList<MsaFileRecord>> msaFileRecordsHashMap, String outputDir){
			this.targetchromeSomeReadFileLocation=targetchromeSomeReadFileLocation;
			this.refChromoSomeRead=refChromoSomeRead;
			this.name=name;
			this.threadCount=threadCount;
			this.msaFileRecordsHashMap=msaFileRecordsHashMap;
			this.outputDir=outputDir;
		}
		
		public void run(){
			System.out.println(name + " begin");
			ChromoSomeReadService chromoSomeRead = new ChromoSomeReadService(targetchromeSomeReadFileLocation);
			HashMap<String, FirstLastList> sdiRecords = new HashMap<String, FirstLastList>();
			HashMap<String, ArrayList<MapSingleRecord>> sdiRecordsArrays = new HashMap<String, ArrayList<MapSingleRecord>>();
			HashMap<String, ChromoSome> chromoSomeHashMap = chromoSomeRead.getChromoSomeHashMap();
			for(String chrName : chromoSomeHashMap.keySet()){
				if(!sdiRecords.containsKey(chrName)){
					sdiRecords.put(chrName, new FirstLastList());
				}
				if(!sdiRecordsArrays.containsKey(chrName)){
					sdiRecordsArrays.put(chrName, new ArrayList<MapSingleRecord>());
				}
				if(msaFileRecordsHashMap.containsKey(chrName)){
					ArrayList<MsaFileRecord> msaFileRecords = msaFileRecordsHashMap.get(chrName);
					ArrayList<TwoSeqOfMsaResult> twoSeqOfMsaResults = new ArrayList<TwoSeqOfMsaResult>();
					for(int i=0; i<msaFileRecords.size(); i++){
						//System.out.println(name + "\t" + i);
						MsaFileRecord msaFileRecord = msaFileRecords.get(i);
						int transcriptStart = msaFileRecord.getStart();
						int transcriptEnd = msaFileRecord.getEnd();
						transcriptEnd--;
						int targetTranscriptStart = 0;
						int targetTranscriptEnd = 0;
						if(msaFileRecord.getRecords().containsKey(refName)){
							MsaSingleRecord refMsaSingleRecord = msaFileRecord.getRecords().get(refName);
							MsaSingleRecord targetMsaSingleRecord = msaFileRecord.getRecords().get(name);
							//System.out.println(i+" " + name + " " + targetMsaSingleRecord.getEnd());
							int msaRefStart = refMsaSingleRecord.getStart();
							int msaTargetStart=targetMsaSingleRecord.getStart();
							
							int refLetterNumber=0;
							int targetLetterNumber=0;
							
							//deleted the extend sequence only keep the wanted region
							refLetterNumber=0;
							targetLetterNumber=0;
							//String refSeq="";
							//String targetSeq="";
							ArrayList<Character> refSeq = new ArrayList<Character>();
							ArrayList<Character> targetSeq = new ArrayList<Character>();

							for(int ai=0; ai<refMsaSingleRecord.getSequence().length(); ai++){
								if(refMsaSingleRecord.getSequence().charAt(ai) != '-'){
									refLetterNumber++;
								}
								if(targetMsaSingleRecord.getSequence().charAt(ai) != '-'){
									targetLetterNumber++;
								}
								
								if(transcriptStart==(msaRefStart+refLetterNumber-1) && refMsaSingleRecord.getSequence().charAt(ai) != '-'){
									targetTranscriptStart=msaTargetStart+targetLetterNumber-1;
									if(targetMsaSingleRecord.getSequence().charAt(ai)=='-' ){
										targetTranscriptStart++;
									}
								}
								if((msaRefStart+refLetterNumber-1) == transcriptEnd && refMsaSingleRecord.getSequence().charAt(ai) != '-'){
									targetTranscriptEnd = msaTargetStart+targetLetterNumber-1;
								}								
								if(transcriptStart<=(msaRefStart+refLetterNumber-1) && (msaRefStart+refLetterNumber-1)<transcriptEnd){
									refSeq.add(refMsaSingleRecord.getSequence().charAt(ai));
									targetSeq.add(targetMsaSingleRecord.getSequence().charAt(ai));
								}else if((msaRefStart+refLetterNumber-1)==transcriptEnd && refMsaSingleRecord.getSequence().charAt(ai) != '-'){
									refSeq.add(refMsaSingleRecord.getSequence().charAt(ai));
									targetSeq.add(targetMsaSingleRecord.getSequence().charAt(ai));
								}
							}
							if( (i==(msaFileRecords.size()-1)) && targetTranscriptEnd == 0 ){
								targetTranscriptEnd = chromoSomeRead.getChromoSomeById(chrName).getSequence().length();
							}
						//	System.out.println("249 \t" + transcriptStart+" "+transcriptEnd+" "+refSeq+" "+targetTranscriptStart+" "+targetTranscriptEnd+" "+targetSeq);
							//refSeq=refSeq.toUpperCase();
							//targetSeq=targetSeq.toUpperCase();
							TwoSeqOfMsaResult twoSeqOfMsaResult = new TwoSeqOfMsaResult(transcriptStart, transcriptEnd, refSeq, targetTranscriptStart, targetTranscriptEnd, targetSeq);
							twoSeqOfMsaResults.add(twoSeqOfMsaResult);
						}
					}
					Collections.sort(twoSeqOfMsaResults);
					System.out.println(name + "	" + chrName +"	trim finished");
					
					
					//System.out.println("0 start " + twoSeqOfMsaResults.get(0).getResultStart() + " end " + twoSeqOfMsaResults.get(0).getResultEnd() );
					
					//	System.out.println(name+" size " + twoSeqOfMsaResults.size());
					for( int j=1; j<twoSeqOfMsaResults.size(); j++ ){
							if( twoSeqOfMsaResults.size() > 1 ){
						//		System.out.println(name+" j " + j);
								if( j<1 ){
									j=1;
								}
		//						System.out.println(""+j + " begin");
		//						System.out.println(j + " start " + twoSeqOfMsaResults.get(j).getResultStart() + " end " + twoSeqOfMsaResults.get(j).getResultEnd() );
		//						System.out.println(j + " start " + twoSeqOfMsaResults.get(j).getRefStart() + " end " + twoSeqOfMsaResults.get(j).getRefEnd());
								int lastEnd = twoSeqOfMsaResults.get(j-1).getResultEnd();
								if( lastEnd >= twoSeqOfMsaResults.get(j).getResultStart() ){
									int overLapLength = lastEnd - twoSeqOfMsaResults.get(j).getResultStart();
									overLapLength++;
									ArrayList<Character> oldSequence=twoSeqOfMsaResults.get(j).getResultSeq();
									ArrayList<Character> newSequence=oldSequence;
									int corrected = 0;
									for( int t=0; t<oldSequence.size(); t++ ){
										if(corrected<overLapLength && oldSequence.get(t)!='-'){
											newSequence.set(t, '-');//=newSequence.subSequence(0, t)+"-"+newSequence.subSequence(t+1, newSequence.length());
											corrected++;
										}
									}

									int largetEnd;
									if( twoSeqOfMsaResults.get(j).getResultEnd() > twoSeqOfMsaResults.get(j-1).getResultEnd() ){
										largetEnd = twoSeqOfMsaResults.get(j).getResultEnd();
									}else{
										largetEnd = twoSeqOfMsaResults.get(j-1).getResultEnd();
									}
									ArrayList<Character> rt = twoSeqOfMsaResults.get(j-1).getRefSeq();
									rt.addAll(twoSeqOfMsaResults.get(j).getRefSeq());

									ArrayList<Character> tt = twoSeqOfMsaResults.get(j-1).getResultSeq();
									tt.addAll(newSequence);

									TwoSeqOfMsaResult twoSeqOfMsaResult = new TwoSeqOfMsaResult(twoSeqOfMsaResults.get(j-1).getRefStart(), twoSeqOfMsaResults.get(j).getRefEnd(), rt,
											twoSeqOfMsaResults.get(j-1).getResultStart(),largetEnd, tt);
									twoSeqOfMsaResults.set(j, twoSeqOfMsaResult);
									twoSeqOfMsaResults.remove(j-1);
		//							System.out.println("273 " + j);
									j-=2;

								}else if( twoSeqOfMsaResults.get(j).getResultStart() < twoSeqOfMsaResults.get(j-1).getResultStart() ){
									System.out.println("to be complete 302 ");
								}
							}
					}
					
					Collections.sort(twoSeqOfMsaResults);
					System.out.println(name + " sequence alignment data structure prepared");
//					//begin: insert the first SDI record(the SDI record before the first gene)
					MapSingleRecord mapSingleRecord;
					if(twoSeqOfMsaResults.get(0).getRefStart()>1){						
						String ori = refChromoSomeRead.getSubSequence(chrName, 1, twoSeqOfMsaResults.get(0).getRefStart()-1, Strand.POSITIVE);
						String result;
						int resultLength=0;
						if(twoSeqOfMsaResults.get(0).getResultStart()>1){
							result = chromoSomeRead.getSubSequence(chrName, 1, twoSeqOfMsaResults.get(0).getResultStart()-1, Strand.POSITIVE);
							resultLength=result.length();
						}else{
							result = "";
						}
						int changedLength = resultLength-ori.length();
						mapSingleRecord = new MapSingleRecord(1, changedLength, ori, result);

						if(!ori.equals(result)){
							sdiRecords.get(chrName).insertLast(mapSingleRecord);
						}
					}else if(twoSeqOfMsaResults.get(0).getRefStart()==1 && twoSeqOfMsaResults.get(0).getResultStart()>1){
						String result = chromoSomeRead.getSubSequence(chrName, 1, twoSeqOfMsaResults.get(0).getResultStart()-1, Strand.POSITIVE);
						mapSingleRecord = new MapSingleRecord(1, result.length(), "-", result);
						sdiRecords.get(chrName).insertLast(mapSingleRecord);
					}
					//end: insert the first SDI record(the SDI record before the first gene)
					
					//begin: insert the gene region SDI record
					int i=0;
					while(i<(twoSeqOfMsaResults.size()-1)){
						//System.out.println(name + "\t" + i + "\t" + twoSeqOfMsaResults.get(i).getRefStart() );
						//if(twoSeqOfMsaResults.get(i).getResultEnd() <= (twoSeqOfMsaResults.get(i+1).getResultStart()-1)){
							//System.out.println("no overlap");
							int refLetterNumber=0;
							for(int ai=0; ai<twoSeqOfMsaResults.get(i).getRefSeq().size(); ai++){
								if(twoSeqOfMsaResults.get(i).getRefSeq().get(ai) != '-'){
									refLetterNumber++;
								}
								if(twoSeqOfMsaResults.get(i).getRefSeq().get(ai) != twoSeqOfMsaResults.get(i).getResultSeq().get(ai)){
									if(twoSeqOfMsaResults.get(i).getResultSeq().get(ai) == '-'){
										//if(null != sdiRecords.get(chrName).getLast()){
//											MapSingleRecord lastMapSingleRecord=sdiRecords.get(chrName).getLast().getMapSingleRecord();
//											if( lastMapSingleRecord.getChanged()<0 && (lastMapSingleRecord.getBasement()+ Math.abs(lastMapSingleRecord.getChanged()) )==(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber-1) && lastMapSingleRecord.getResult().equals("-")){
//												try {
//													sdiRecords.get(chrName).deleteLast();
//												} catch (Exception e) {
//													e.printStackTrace();
//												}
//												mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart() + refLetterNumber - 1, lastMapSingleRecord.getChanged()-1, lastMapSingleRecord.getOriginal() + twoSeqOfMsaResults.get(i).getRefSeq().get(ai), "-");
//											}else{
//												mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart() + refLetterNumber - 1, -1, "" + twoSeqOfMsaResults.get(i).getRefSeq().get(ai), "-");
//											}
//										}else {
											mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart() + refLetterNumber - 1, -1, "" + twoSeqOfMsaResults.get(i).getRefSeq().get(ai), "-");
											//mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart() + refLetterNumber - 1, -1, "" + twoSeqOfMsaResults.get(i).getRefSeq().get(ai), "-");
										//}
									}else if(twoSeqOfMsaResults.get(i).getRefSeq().get(ai) == '-'){
										if(null != sdiRecords.get(chrName).getLast()){
											MapSingleRecord lastMapSingleRecord=sdiRecords.get(chrName).getLast().getMapSingleRecord();
											if(lastMapSingleRecord.getBasement()==(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber) && lastMapSingleRecord.getChanged()>0 && lastMapSingleRecord.getOriginal().equals("-")){
												try {
													sdiRecords.get(chrName).deleteLast();
												} catch (Exception e) {
													e.printStackTrace();
												}
												mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber, lastMapSingleRecord.getChanged()+1, "-", lastMapSingleRecord.getResult()+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
											}else{
												mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber, 1, "-", ""+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
											}
										}else{
											mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber, 1, "-", ""+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
										}
									}else{
										mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber-1, 0, ""+twoSeqOfMsaResults.get(i).getRefSeq().get(ai), ""+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
									}
									sdiRecords.get(chrName).insertLast(mapSingleRecord);
								}
							}

							//begin: insert the inter-gene region SDI record
							String oriSeq;
							int oriSeqLength=0;
							if((twoSeqOfMsaResults.get(i).getRefEnd()) <(twoSeqOfMsaResults.get(i+1).getRefStart()-1)){
								int start = twoSeqOfMsaResults.get(i).getRefEnd()+1;
								int end = twoSeqOfMsaResults.get(i+1).getRefStart()-1;
								//System.out.println(start + "\t" + end);
								oriSeq = refChromoSomeRead.getSubSequence(chrName, twoSeqOfMsaResults.get(i).getRefEnd()+1, twoSeqOfMsaResults.get(i+1).getRefStart()-1, Strand.POSITIVE);
								oriSeqLength=oriSeq.length();
							}else{
								oriSeq="";
							}
							String resultSeq=null;
							int resultSeqLength=0;
							if(twoSeqOfMsaResults.get(i).getResultEnd() < (twoSeqOfMsaResults.get(i+1).getResultStart()-1)){
								resultSeq = chromoSomeRead.getSubSequence(chrName, twoSeqOfMsaResults.get(i).getResultEnd()+1, twoSeqOfMsaResults.get(i+1).getResultStart()-1, Strand.POSITIVE);
								resultSeqLength=resultSeq.length();
							}else{
								resultSeq="";
							}
							int changedLength1 = resultSeqLength-oriSeqLength;
							mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefEnd()+1, changedLength1, oriSeq, resultSeq);
							
							if(!oriSeq.equals(resultSeq)){	
								sdiRecords.get(chrName).insertLast(mapSingleRecord);
//								System.out.println("inter geneitic");
							}
						i++;
//						System.out.println("here is inner run");
					}
					//end: insert the gene region SDI record
					//System.out.println(name + ": insert the gene region SDI record end");
					if(i == (twoSeqOfMsaResults.size()-1)){
						//System.out.println("i="+i+" twoSeqOfMsaResults.size()-1:"+(twoSeqOfMsaResults.size()-1));
						int refLetterNumber=0;
						for(int ai=0; ai<twoSeqOfMsaResults.get(i).getRefSeq().size(); ai++){
							if(twoSeqOfMsaResults.get(i).getRefSeq().get(ai) != '-'){
								refLetterNumber++;
							}
							if(twoSeqOfMsaResults.get(i).getRefSeq().get(ai) != twoSeqOfMsaResults.get(i).getResultSeq().get(ai)){
								if(twoSeqOfMsaResults.get(i).getResultSeq().get(ai) == '-'){
									mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber-1, -1, ""+twoSeqOfMsaResults.get(i).getRefSeq().get(ai), "-");
								}else if(twoSeqOfMsaResults.get(i).getRefSeq().get(ai) == '-'){
									if(null != sdiRecords.get(chrName).getLast()){
										MapSingleRecord lastMapSingleRecord=sdiRecords.get(chrName).getLast().getMapSingleRecord();
										if(lastMapSingleRecord.getBasement()==(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber) && lastMapSingleRecord.getChanged()>0 && lastMapSingleRecord.getOriginal().equals("-")){
											try {
												sdiRecords.get(chrName).deleteLast();
											} catch (Exception e) {
												e.printStackTrace();
											}
											mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber, lastMapSingleRecord.getChanged()+1, "-", lastMapSingleRecord.getResult()+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
										}else{
											mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber, 1, "-", ""+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
										}
									}else{
										mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber, 1, "-", ""+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
									}
								}else{
									mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(i).getRefStart()+refLetterNumber-1, 0, ""+twoSeqOfMsaResults.get(i).getRefSeq().get(ai), ""+twoSeqOfMsaResults.get(i).getResultSeq().get(ai));
								}
								//System.out.println("423"+mapSingleRecord.getOriginal() +"\t"+mapSingleRecord.getResult());
								sdiRecords.get(chrName).insertLast(mapSingleRecord);
							}
						}
						
					}
					
					int endIndex = twoSeqOfMsaResults.size()-1;
					String oriSeq=null;
					int oriSeqLength=0;
					if((twoSeqOfMsaResults.get(endIndex).getRefEnd()) < refChromoSomeRead.getChromoSomeById(chrName).getSequence().length()){
						oriSeq = refChromoSomeRead.getSubSequence(chrName, twoSeqOfMsaResults.get(endIndex).getRefEnd()+1, refChromoSomeRead.getChromoSomeById(chrName).getSequence().length(), Strand.POSITIVE);
						oriSeqLength=oriSeq.length();
					}else{
						oriSeq="";
					}
					
					if(twoSeqOfMsaResults.get(endIndex).getResultEnd() < (chromoSomeRead.getChromoSomeById(chrName).getSequence().length()-1)){
//						System.out.println("endIndex" + endIndex + " " + chrName + " " + twoSeqOfMsaResults.get(endIndex).getResultEnd() + " " +(chromoSomeRead.getChromoSomeById(chrName).getSequence().length()-1) );
						String resultSeq = chromoSomeRead.getSubSequence(chrName, twoSeqOfMsaResults.get(endIndex).getResultEnd()+1, chromoSomeRead.getChromoSomeById(chrName).getSequence().length(), Strand.POSITIVE);
						int changedLength1 = resultSeq.length()-oriSeqLength;
						mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(endIndex).getRefEnd()+1, changedLength1, oriSeq, resultSeq);
						if(!oriSeq.equalsIgnoreCase(resultSeq)){
							sdiRecords.get(chrName).insertLast(mapSingleRecord);
						}
				//		System.out.println(447 + " " + 1);
					//	System.out.println("447"+mapSingleRecord.getOriginal() +"\t"+mapSingleRecord.getResult());
					}else if(twoSeqOfMsaResults.get(endIndex).getResultEnd() == (chromoSomeRead.getChromoSomeById(chrName).getSequence().length()-1)){
						mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(endIndex).getRefEnd()+1, 0-oriSeqLength+1, oriSeq, ""+chromoSomeRead.getChromoSomeById(chrName).getSequence().charAt(chromoSomeRead.getChromoSomeById(chrName).getSequence().length()-1));
						sdiRecords.get(chrName).insertLast(mapSingleRecord);
						//System.out.println(450 + " " + 2);
				//		System.out.println("450"+mapSingleRecord.getOriginal() +"\t"+mapSingleRecord.getResult());
					}else if(twoSeqOfMsaResults.get(endIndex).getResultEnd() == (chromoSomeRead.getChromoSomeById(chrName).getSequence().length()) && oriSeq.length()>0){
						mapSingleRecord = new MapSingleRecord(twoSeqOfMsaResults.get(endIndex).getRefEnd()+1, 0-oriSeqLength, oriSeq, "-");
						sdiRecords.get(chrName).insertLast(mapSingleRecord);
					//	System.out.println(453 + " " + 3);
						//System.out.println("453"+mapSingleRecord.getOriginal() +"\t"+mapSingleRecord.getResult());
					}else if(oriSeq.length()>0){
//						System.out.println("should never run here");
					}
					System.out.println(name + ": link data structure start");
//					for( int runingCound = 0; runingCound<2; ++runingCound) { // run here for twice, it is better for check the neighbouring unnecessary records
						if (sdiRecords.get(chrName).getFirst() != null && sdiRecords.get(chrName).getFirst().getNext() != null) {
							Data lastOne = sdiRecords.get(chrName).getFirst();
							Data currOne = sdiRecords.get(chrName).getFirst().getNext();

							while (null != currOne) {
								//System.out.println(name + ": link data structure " + currOne.getMapSingleRecord().getBasement());
								Data nextOne = currOne.getNext();
								if (currOne.getMapSingleRecord().getChanged() < 0 && lastOne.getMapSingleRecord().getChanged() < 0 &&
										(lastOne.getMapSingleRecord().getBasement() + Math.abs(lastOne.getMapSingleRecord().getChanged())) == currOne.getMapSingleRecord().getBasement()
										&& lastOne.getMapSingleRecord().getResult().equals("-") && currOne.getMapSingleRecord().getResult().equals("-")) {

									MapSingleRecord mapSingleRecord2 = new MapSingleRecord(lastOne.getMapSingleRecord().getBasement(), lastOne.getMapSingleRecord().getChanged() + currOne.getMapSingleRecord().getChanged(),
											lastOne.getMapSingleRecord().getOriginal() + currOne.getMapSingleRecord().getOriginal(), "-");
									// merge to deletions
									//delete last one begin
									if (currOne.getLast().equals(sdiRecords.get(chrName).getFirst())) {
										try {
											sdiRecords.get(chrName).deleteFirst();
										} catch (Exception e) {
											e.printStackTrace();
										}
										currOne.setMapSingleRecord(mapSingleRecord2);
										lastOne = currOne;
										currOne = nextOne;
									} else {
										lastOne.getLast().setNext(currOne);
										currOne.setLast(lastOne.getLast());
										lastOne.setLast(null);
										lastOne.setNext(null);
										currOne.setMapSingleRecord(mapSingleRecord2);
										lastOne = currOne.getLast(); // here do not go forward, to check the identity of insertion and deletion
									}
									// delete last one end
								} else if (currOne.getMapSingleRecord().getChanged() == 0 && currOne.getMapSingleRecord().getOriginal().endsWith("-") && currOne.getMapSingleRecord().getResult().equals("-")) {
									// delete current one
									// delete records that gives no information
									lastOne.setNext(currOne.getNext());
									currOne.getNext().setLast(lastOne);
									currOne.setLast(null);
									currOne.setNext(null);
									currOne = nextOne;
								} else if (currOne.getMapSingleRecord().getChanged() == 0 && currOne.getMapSingleRecord().getOriginal().equalsIgnoreCase(currOne.getMapSingleRecord().getResult())) {
									//delete current one
									// delete records that gives not information
									lastOne.setNext(currOne.getNext());
									currOne.getNext().setLast(lastOne);
									currOne.setLast(null);
									currOne.setNext(null);
									currOne = nextOne;
								} else if ( (currOne.getMapSingleRecord().getChanged() < 0 && lastOne.getMapSingleRecord().getChanged() > 0
										&& currOne.getMapSingleRecord().getOriginal().equalsIgnoreCase(lastOne.getMapSingleRecord().getResult())  &&
										currOne.getMapSingleRecord().getBasement() == lastOne.getMapSingleRecord().getBasement() )
										|| (currOne.getMapSingleRecord().getChanged() > 0 && lastOne.getMapSingleRecord().getChanged() < 0
										&& currOne.getMapSingleRecord().getResult().equalsIgnoreCase(lastOne.getMapSingleRecord().getOriginal()) &&
										(currOne.getMapSingleRecord().getBasement()-1) == lastOne.getMapSingleRecord().getBasement()) ) { //delete one insertion and next reverse deletion

									//delete current one and prev
									if (((currOne.getLast())) == (sdiRecords.get(chrName).getFirst())) {
										try {
											sdiRecords.get(chrName).deleteFirst();
											sdiRecords.get(chrName).deleteFirst();
											lastOne = sdiRecords.get(chrName).getFirst();
											currOne = sdiRecords.get(chrName).getFirst().getNext();
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else if( currOne == sdiRecords.get(chrName).getLast() ){
										try {
											sdiRecords.get(chrName).deleteLast();
											sdiRecords.get(chrName).deleteLast();
											currOne = sdiRecords.get(chrName).getLast();
											lastOne = currOne.getLast();
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										currOne.getLast().getLast().setNext(currOne.getNext());
										currOne.getNext().setLast(currOne.getLast().getLast());
										Data temp = currOne.getNext();
										currOne.setLast(null);
										currOne.setNext(null);
										lastOne.setLast(null);
										lastOne.setNext(null);
										currOne = temp;
										lastOne = temp.getLast();
									}
								} else {
									lastOne = currOne;
									currOne = nextOne;
								}
							}
						}
					//}
					System.out.println(name + ": link data structure end");

					/*
					ArrayList<MapSingleRecord> sdiRecordsThisOne = new ArrayList<MapSingleRecord>();
					if(sdiRecords.get(chrName).getFirst() != null){
						Data thisone = sdiRecords.get(chrName).getFirst();
						while( thisone != null ){
							sdiRecordsThisOne.add(thisone.getMapSingleRecord());
							thisone=thisone.getNext();
						}
					}
					System.out.println(name+" begin to sort");
					
					boolean ifChanged = true;
					while(ifChanged){
						Collections.sort(sdiRecordsThisOne);
						ifChanged = false;
						ArrayList<MapSingleRecord> sdiRecordsToRomove = new ArrayList<MapSingleRecord>();
						int oldSize = sdiRecordsThisOne.size();
						for(int j=1; j<oldSize; j++){
							if(sdiRecordsThisOne.get(j).getChanged()<0 && sdiRecordsThisOne.get(j-1).getChanged()<0 &&
									(sdiRecordsThisOne.get(j-1).getBasement()+Math.abs(sdiRecordsThisOne.get(j-1).getChanged()))==sdiRecordsThisOne.get(j).getBasement()
									&& sdiRecordsThisOne.get(j-1).getResult().equals("-") && sdiRecordsThisOne.get(j).getResult().equals("-")){
								MapSingleRecord mapSingleRecord2 = new MapSingleRecord(sdiRecordsThisOne.get(j-1).getBasement(), sdiRecordsThisOne.get(j-1).getChanged()+sdiRecordsThisOne.get(j).getChanged(),
										sdiRecordsThisOne.get(j-1).getOriginal()+sdiRecordsThisOne.get(j).getOriginal(), "-");
								
								sdiRecordsToRomove.add(sdiRecordsThisOne.get(j-1));
								sdiRecordsThisOne.set(j, mapSingleRecord2);
								j++;
								ifChanged = true;
							}else if(sdiRecordsThisOne.get(j).getChanged()==0 && sdiRecordsThisOne.get(j).getOriginal().endsWith("-") && sdiRecordsThisOne.get(j).getResult().equals("-")){
								sdiRecordsToRomove.add(sdiRecordsThisOne.get(j));
								ifChanged = true;
							}
						}
						for( MapSingleRecord mapSingleRecordToRemove : sdiRecordsToRomove ){
							sdiRecordsThisOne.remove(mapSingleRecordToRemove);
						}
						
					}
					sdiRecordsArrays.put(chrName, sdiRecordsThisOne);
					*/
				}
			}
			
			int lineNumber = 0;
//			for(String chr : sdiRecordsArrays.keySet()){
//				if(sdiRecordsArrays.get(chr).size()>0){
//					String lastLine="";
//					try {
//						PrintWriter out = new PrintWriter(new FileOutputStream(outputDir+File.separator+name+"_"+chr+".myv2.sdi"), true);
//						for(MapSingleRecord mapSingleRecord : sdiRecordsArrays.get(chr)){
//							if(lineNumber >= 1){
//								out.println(lastLine);
//							}
//							lastLine=chr+"\t"+mapSingleRecord.getBasement()+"\t"+mapSingleRecord.getChanged()+"\t"+mapSingleRecord.getOriginal()+"\t"+mapSingleRecord.getResult();
//							lineNumber++;
//						}
//						out.print(lastLine);
//						out.close();
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
//				}
//			}
			for(String chr : sdiRecords.keySet()){
				if(sdiRecords.get(chr).getFirst() != null){
					String lastLine="";
					try {
						PrintWriter out = new PrintWriter(new FileOutputStream(outputDir+File.separator+name+"_"+chr+".myv2.sdi"), true);
						Data thisone = sdiRecords.get(chr).getFirst();
						while( thisone != null ){
							if(lineNumber >= 1){
								out.println(lastLine);
							}
							lastLine=chr+"\t"+thisone.getMapSingleRecord().getBasement()+"\t"+thisone.getMapSingleRecord().getChanged()+"\t"+thisone.getMapSingleRecord().getOriginal()+"\t"+thisone.getMapSingleRecord().getResult();
							lineNumber++;
							thisone=thisone.getNext();
						}
						out.print(lastLine);
						out.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			threadCount.countDown();
		}
	}
}

class FirstLastList {
	public class Data{
		private MapSingleRecord mapSingleRecord;
		private Data next = null;
		private Data last = null;
		Data(MapSingleRecord mapSingleRecord){
			this.mapSingleRecord = mapSingleRecord;
		}
		public MapSingleRecord getMapSingleRecord(){
			return mapSingleRecord;
		}
		public Data getNext(){
			return next;
		}
		public Data getLast(){
			return last;
		}
		public void setNext( Data data ){
			this.next = data;
		}
		public void setLast( Data  data){
			this.last=data;
		}
		public void setMapSingleRecord( MapSingleRecord  mapSingleRecord){
			this.mapSingleRecord=mapSingleRecord;
		}
	}

    private Data first = null;  
    private Data last = null;  
      
    public void insertFirst(MapSingleRecord mapSingleRecord){  
        Data data = new Data(mapSingleRecord);  
        if(first == null)  
            last = data;  
        data.next = first;
        first.last=data;
        first = data;  
    }  
      
    public void insertLast(MapSingleRecord mapSingleRecord){  
        Data data = new Data(mapSingleRecord);  
        if(first == null){  
            first = data;  
        }else{  
            last.next = data;
            data.last=last;
        }  
        last = data;  
    }  
      
    public MapSingleRecord deleteFirst() throws Exception{  
          if(first == null)  
             throw new Exception("empty");  
          Data temp = first;  
          if(first.next == null)
             last = null;
          first.next.last=null;
          first = first.next;
          return temp.mapSingleRecord;
   }     
      
    public void deleteLast() throws Exception{  
        if(first == null)  
            throw new Exception("empty");  
        if(first.next == null){
            first = null;  
            last = null;  
        }else{
        	last=last.last;
        	last.next=null;
        }
    }
    public Data getFirst(){
    	return first;
    }
    public Data getLast(){
    	return last;
    }
}
