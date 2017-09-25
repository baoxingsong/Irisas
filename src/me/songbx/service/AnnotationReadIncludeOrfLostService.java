package me.songbx.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * the sranscriptLiftStartEndSequenceAasequenceIndels in every collectors are
 * validated. The validation include length%3=0, start with start code, end with
 * end code, no middle stop code, And the splice site are AG, GT or same with
 * COL
 * 
 * @author song
 * @version 1.0, 2014-07-09
 */

public class AnnotationReadIncludeOrfLostService {

	private StandardGeneticCode standardGeneticCode = new StandardGeneticCode();

	private AnnotationReadImpl annotationReadImpl;

	private ChromoSomeReadService targetChromeSomeRead;
	private ChromoSomeReadService referenceChromeSomeRead;
	private MapFileService mapFile;

	private HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>> transcriptArrayList = new HashMap<String, ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>>();
	private HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel> transcriptHashMap = new HashMap<String, TranscriptLiftStartEndSequenceAasequenceIndel>();
	
	public AnnotationReadIncludeOrfLostService(){
		
	}
	
	public AnnotationReadIncludeOrfLostService(
			ChromoSomeReadService targetChromeSomeRead,
			ChromoSomeReadService referenceChromeSomeRead,
			MapFileService mapFile) {
		this.targetChromeSomeRead = targetChromeSomeRead;
		this.referenceChromeSomeRead = referenceChromeSomeRead;
		this.mapFile = mapFile;
	}

	public synchronized void builtAnnotationReadImpl(String fileLocation) {
		annotationReadImpl = new AnnotationReadImpl(fileLocation);
	}

	public synchronized void updateInformation(boolean wantmRnaSequence,
			boolean wantFullCNDnaSequence, boolean wantCdsList,
			boolean wantSnpMapfilerecords, boolean wantIndelMapfilerecords,
			boolean wantIndelMapfilerecordsSeq) {
		ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel> temptranscriptArrayList = new ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>();
		HashMap<String, HashSet<Transcript>> sstranscriptHashSet = annotationReadImpl
				.getTranscriptHashSet();
		Iterator<String> chrNamesIt = sstranscriptHashSet.keySet().iterator();
		while (chrNamesIt.hasNext()) {
			String key = chrNamesIt.next();
			for (Transcript transcript : sstranscriptHashSet.get(key)) {
				TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel = new TranscriptLiftStartEndSequenceAasequenceIndel(
						transcript);
				System.out.println(transcript.getName());
				for (Cds cds : transcript.getCdsHashSet()) {
					CdsLiftSequence cdsLiftSequence = new CdsLiftSequence(cds);
					cdsLiftSequence = this.updateCdsLiftSequence(
							cdsLiftSequence, key);
					transcriptLiftStartEndSequenceAasequenceIndel
							.getCdsLiftSequenceArrayList().add(cdsLiftSequence);
				}
				temptranscriptArrayList
						.add(transcriptLiftStartEndSequenceAasequenceIndel);
			}
		}

		for (TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel : temptranscriptArrayList) {
			transcriptLiftStartEndSequenceAasequenceIndel = updateTranscriptLiftStartEndSequenceAasequenceIndel(
					transcriptLiftStartEndSequenceAasequenceIndel,
					wantmRnaSequence, wantFullCNDnaSequence, wantCdsList,
					wantSnpMapfilerecords, wantIndelMapfilerecords,
					wantIndelMapfilerecordsSeq);
			String name = transcriptLiftStartEndSequenceAasequenceIndel
					.getChromeSomeName();
			String metaInformation = "";

			boolean orfLOst = true;
			//System.out.println(transcriptLiftStartEndSequenceAasequenceIndel.getName());
			boolean ifSelectTaur10 = ifSpliceSitesOk(
					transcriptLiftStartEndSequenceAasequenceIndel, name);
			
			if (ifSelectTaur10) {
				metaInformation += "_spliceSitesConserved";
				String cdsSequenceString = transcriptLiftStartEndSequenceAasequenceIndel
						.getSequence();
				if (cdsSequenceString.length() < 3) {
					metaInformation += "_exonLengthLessThan3";
				} else {
					metaInformation += "_exonLengthMoreThan3";
					if (ifLengthCouldbeDivedBYThree(cdsSequenceString)) {
						metaInformation += "_exonLengthIsMultipleOf3";
						if (ifNewStopCOde(cdsSequenceString)) {
							metaInformation += "_premutareStopCodon";
						} else {
							metaInformation += "_noPrematureStopCodon";
							if (ifEndWithStopCode(cdsSequenceString)) {
								metaInformation += "_endWithStopCodon";
								if (ifStartWithStartCode(cdsSequenceString)) {
									metaInformation += "_startWithStartCodon_ConservedFunction";
									orfLOst = false;
								} else {
									metaInformation += "_notWithStartCodon";
								}
							} else {
								metaInformation += "_notEndWithStopCodon";
							}
						}
					} else {
						metaInformation += "_exonLengthIsNotMultipleOf3";
					}
				}
			} else {
				metaInformation = "_spliceSitesDestroyed";
			}

			metaInformation += "_col"
					+ transcriptLiftStartEndSequenceAasequenceIndel.getStart()
					+ "-"
					+ transcriptLiftStartEndSequenceAasequenceIndel.getEnd();
			metaInformation += "_local"
					+ transcriptLiftStartEndSequenceAasequenceIndel
							.getLiftStart()
					+ "-"
					+ transcriptLiftStartEndSequenceAasequenceIndel
							.getLiftEnd();
			metaInformation += "_"
					+ transcriptLiftStartEndSequenceAasequenceIndel.getStrand();
			transcriptLiftStartEndSequenceAasequenceIndel.setOrfLost(orfLOst);
			transcriptLiftStartEndSequenceAasequenceIndel
					.setMetaInformation(metaInformation);
			metaInformation = null;
			if (!transcriptArrayList.containsKey(name)) {
				transcriptArrayList
						.put(name,
								new ArrayList<TranscriptLiftStartEndSequenceAasequenceIndel>());
			}
			transcriptArrayList.get(name).add(
					transcriptLiftStartEndSequenceAasequenceIndel);
			transcriptHashMap.put(
					transcriptLiftStartEndSequenceAasequenceIndel.getName(),
					transcriptLiftStartEndSequenceAasequenceIndel);
			
			if(!wantmRnaSequence){
				transcriptLiftStartEndSequenceAasequenceIndel.setSequence(null);
			}
			
			if(!wantCdsList){
				transcriptLiftStartEndSequenceAasequenceIndel.setCdsLiftSequenceArrayList(null);
			}
		}
		temptranscriptArrayList = null;
	}

	private synchronized CdsLiftSequence updateCdsLiftSequence(
			CdsLiftSequence cdsLiftSequence, String chName) {
		cdsLiftSequence.setLiftEnd(mapFile.getChangedFromBasement(chName,
				cdsLiftSequence.getEnd()));
		cdsLiftSequence.setLiftStart(mapFile.getChangedFromBasement(chName,
				cdsLiftSequence.getStart()));
		//System.out.println("chName: " + chName + " cdsLiftSequence.getLiftStart(): " + cdsLiftSequence.getLiftStart() + " cdsLiftSequence.getLiftEnd(): " + cdsLiftSequence.getLiftEnd());  
		//System.out.println("cdsLiftSequence.getTranscript().getStrand(): " + cdsLiftSequence.getTranscript().getStrand() );
		//System.out.println("ChrLength: " + targetChromeSomeRead.getChromoSomeById(chName).getSequence().length());
		cdsLiftSequence.setSequence(targetChromeSomeRead.getSubSequence(chName,
				cdsLiftSequence.getLiftStart(), cdsLiftSequence.getLiftEnd(),
				cdsLiftSequence.getTranscript().getStrand()));
		return cdsLiftSequence;
	}

	private synchronized TranscriptLiftStartEndSequenceAasequenceIndel updateTranscriptLiftStartEndSequenceAasequenceIndel(
			TranscriptLiftStartEndSequenceAasequenceIndel transcriptLiftStartEndSequenceAasequenceIndel,
			boolean wantmRnaSequence, boolean wantFullCNDnaSequence,
			boolean wantCdsList, boolean wantSnpMapfilerecords,
			boolean wantIndelMapfilerecords, boolean wantIndelMapfilerecordsSeq) {
		String chName = transcriptLiftStartEndSequenceAasequenceIndel
				.getChromeSomeName();

		int start = 0;
		int end = 0;

		int liftStart = 0;
		int liftEnd = 0;

		int i = 0;
		ArrayList<CdsLiftSequence> cdsLiftSequenceArrayList = new ArrayList<CdsLiftSequence>(); // transfer
																								// hashset
																								// to
																								// arraylist

		for (CdsLiftSequence cdsLiftSequence : transcriptLiftStartEndSequenceAasequenceIndel
				.getCdsLiftSequenceArrayList()) {
			cdsLiftSequenceArrayList.add(cdsLiftSequence);
			if (0 == i) {
				start = cdsLiftSequence.getStart();
				end = cdsLiftSequence.getEnd();

				liftStart = cdsLiftSequence.getLiftStart();
				liftEnd = cdsLiftSequence.getLiftEnd();
			} else {
				if (start > cdsLiftSequence.getStart()) {
					start = cdsLiftSequence.getStart();
					liftStart = cdsLiftSequence.getLiftStart();
				}// end if
				if (end < cdsLiftSequence.getEnd()) {
					end = cdsLiftSequence.getEnd();
					liftEnd = cdsLiftSequence.getLiftEnd();
				}// end if
			}// end else
			i++;
		}// end for
		transcriptLiftStartEndSequenceAasequenceIndel.setStart(start);
		transcriptLiftStartEndSequenceAasequenceIndel.setEnd(end);
		transcriptLiftStartEndSequenceAasequenceIndel.setLiftStart(liftStart);
		transcriptLiftStartEndSequenceAasequenceIndel.setLiftEnd(liftEnd);

		String fullSequence = targetChromeSomeRead.getSubSequence(chName,
				liftStart, liftEnd,
				transcriptLiftStartEndSequenceAasequenceIndel.getStrand());

		Collections.sort(cdsLiftSequenceArrayList);

		StringBuffer sequenceStringBuffer = new StringBuffer();
		for (CdsLiftSequence c : cdsLiftSequenceArrayList) {
			sequenceStringBuffer.append(c.getSequence());
			if (mapFile.getIndelRecords().containsKey(chName)) {
				ArrayList<MapSingleRecord> records = mapFile.getAllRecords()
						.get(chName);
				for (MapSingleRecord mapSingleRecord : records) {
					if (c.getStart() <= mapSingleRecord.getBasement() - 1
							&& mapSingleRecord.getBasement() < c.getEnd()) {
						if (mapSingleRecord.getChanged() != 0) {
							if (wantIndelMapfilerecords) {
								if (wantIndelMapfilerecordsSeq) {
									transcriptLiftStartEndSequenceAasequenceIndel
											.getMapSingleRecords().add(
													mapSingleRecord);
								} else {
									MapSingleRecord newMapSingleRecord = new MapSingleRecord(
											mapSingleRecord.getBasement(),
											mapSingleRecord.getChanged());
									transcriptLiftStartEndSequenceAasequenceIndel
											.getMapSingleRecords().add(
													newMapSingleRecord);
									mapSingleRecord=null;
								}
							}else{
								mapSingleRecord=null;
							}
							transcriptLiftStartEndSequenceAasequenceIndel
									.setIndeled(true);
						} else if (wantSnpMapfilerecords) {
							transcriptLiftStartEndSequenceAasequenceIndel
									.getMapSingleRecords().add(mapSingleRecord);
						}else{
							mapSingleRecord=null;
						}// end if
					}
				}// end for
			}// end if
		}// end for
			// Collections.sort(transcriptLiftStartEndSequenceAasequenceIndel.getMapSingleRecords());
		String sequence = sequenceStringBuffer.toString();
		sequenceStringBuffer = new StringBuffer();
		if (!ifLengthCouldbeDivedBYThree(sequence)) {
			Pattern p = Pattern.compile("^\\wATG");
			Matcher m = p.matcher(sequence);
			if (m.find()) {
				sequence = sequence.substring(1, sequence.length());
				fullSequence = fullSequence.substring(1, fullSequence.length());
				cdsLiftSequenceArrayList.get(0).setSequence(
						cdsLiftSequenceArrayList
								.get(0)
								.getSequence()
								.substring(
										1,
										cdsLiftSequenceArrayList.get(0)
												.getSequence().length()));

				if (transcriptLiftStartEndSequenceAasequenceIndel.getStrand() == Strand.POSITIVE) {
					transcriptLiftStartEndSequenceAasequenceIndel
							.setLiftStart(liftStart + 1);
					cdsLiftSequenceArrayList.get(0).setLiftStart(liftStart + 1);
				} else {
					transcriptLiftStartEndSequenceAasequenceIndel
							.setLiftEnd(liftEnd - 1);
					cdsLiftSequenceArrayList.get(0).setLiftEnd(liftEnd - 1);
				}
			}
		}
		if (!ifLengthCouldbeDivedBYThree(sequence)) {
			Pattern p1 = Pattern.compile("TAA\\w$");
			Matcher m1 = p1.matcher(sequence);
			Pattern p2 = Pattern.compile("TAG\\w$");
			Matcher m2 = p2.matcher(sequence);
			Pattern p3 = Pattern.compile("TGA\\w$");
			Matcher m3 = p3.matcher(sequence);
			if (m1.find() || m2.find() || m3.find()) {
				sequence = sequence.substring(0, sequence.length() - 1);
				fullSequence = fullSequence.substring(0,
						fullSequence.length() - 1);
				cdsLiftSequenceArrayList.get(
						cdsLiftSequenceArrayList.size() - 1).setSequence(
						cdsLiftSequenceArrayList
								.get(cdsLiftSequenceArrayList.size() - 1)
								.getSequence()
								.substring(
										0,
										cdsLiftSequenceArrayList
												.get(cdsLiftSequenceArrayList
														.size() - 1)
												.getSequence().length() - 1));

				if (transcriptLiftStartEndSequenceAasequenceIndel.getStrand() == Strand.POSITIVE) {
					transcriptLiftStartEndSequenceAasequenceIndel
							.setLiftEnd(liftEnd - 1);
					cdsLiftSequenceArrayList.get(
							cdsLiftSequenceArrayList.size() - 1).setLiftEnd(
							liftEnd - 1);
				} else {
					transcriptLiftStartEndSequenceAasequenceIndel
							.setLiftStart(liftStart + 1);
					cdsLiftSequenceArrayList.get(
							cdsLiftSequenceArrayList.size() - 1).setLiftStart(
							liftStart + 1);
				}
			}
		}
		
		//get the intron sequence for sweep analysis: begin
		//StringBuffer intronSequenceSB = new StringBuffer();
		/*
		int sweepExtendLength = 100;
		if(transcriptLiftStartEndSequenceAasequenceIndel.getStrand() == Strand.POSITIVE){
			int start1=cdsLiftSequenceArrayList.get(0).getLiftStart()-sweepExtendLength;
			int end1=cdsLiftSequenceArrayList.get(0).getLiftStart()-1;
			if(start1<1){
				start1 = 1;
			}
			if(end1 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				end1 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
					start1, end1,
					Strand.POSITIVE));
			if(cdsLiftSequenceArrayList.size()>1){
				for(int ij=0; ij < (cdsLiftSequenceArrayList.size()-1); ij++){
					if(cdsLiftSequenceArrayList.get(ij).getLiftEnd()+1 < cdsLiftSequenceArrayList.get(ij+1).getLiftStart()-1){
						intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
							cdsLiftSequenceArrayList.get(ij).getLiftEnd()+1, cdsLiftSequenceArrayList.get(ij+1).getLiftStart()-1,
							Strand.POSITIVE));
					}
				}
			}
			int start2 = cdsLiftSequenceArrayList.get( cdsLiftSequenceArrayList.size()-1 ).getLiftEnd()+1;
			int end2 = cdsLiftSequenceArrayList.get( cdsLiftSequenceArrayList.size()-1 ).getLiftEnd()+sweepExtendLength;
			if(start2<1){
				start2 = 1;
			}
			if(end2 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				end2 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
					start2, end2,
					Strand.POSITIVE));
		}else{
			int start1=cdsLiftSequenceArrayList.get(0).getLiftEnd()+1;
			int end1=cdsLiftSequenceArrayList.get(0).getLiftEnd()+sweepExtendLength;
			if(start1<1){
				start1 = 1;
			}
			if(start1 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				start1 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			if(end1 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				end1 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
					start1, end1,
					Strand.NEGTIVE));
			if(cdsLiftSequenceArrayList.size()>1){
				for(int ij=0; ij < (cdsLiftSequenceArrayList.size()-1); ij++){
					if(cdsLiftSequenceArrayList.get(ij).getLiftStart()-1 > cdsLiftSequenceArrayList.get(ij+1).getLiftEnd()+1){
						intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
							cdsLiftSequenceArrayList.get(ij+1).getLiftEnd()+1, cdsLiftSequenceArrayList.get(ij).getLiftStart()-1,
							Strand.NEGTIVE));
					}
				}
			}
			int end2 = cdsLiftSequenceArrayList.get( cdsLiftSequenceArrayList.size()-1 ).getLiftStart()-1;
			int start2 = cdsLiftSequenceArrayList.get( cdsLiftSequenceArrayList.size()-1 ).getLiftStart()-sweepExtendLength;
			if(start2<1){
				start2 = 1;
			}
			if(end2 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				end2 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
					start2, end2,
					Strand.NEGTIVE));
		}*/
		/*
		int sweepExtendLength = 554000;
		if(transcriptLiftStartEndSequenceAasequenceIndel.getStrand() == Strand.POSITIVE){
			int start1=cdsLiftSequenceArrayList.get(0).getLiftStart()-sweepExtendLength;
			if(start1<1){
				start1 = 1;
			}
			int end2 = cdsLiftSequenceArrayList.get( cdsLiftSequenceArrayList.size()-1 ).getLiftEnd()+sweepExtendLength;
			if(end2 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				end2 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
					start1, end2,
					Strand.POSITIVE));
		}else{
			int start2 = cdsLiftSequenceArrayList.get( cdsLiftSequenceArrayList.size()-1 ).getLiftStart()-sweepExtendLength;
			if(start2<1){
				start2 = 1;
			}
			int end1=cdsLiftSequenceArrayList.get(0).getLiftEnd()+sweepExtendLength;
			if(end1 >= targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()){
				end1 = targetChromeSomeRead.getChromoSomeById(chName).getSequence().length()-1;
			}
			intronSequenceSB.append(targetChromeSomeRead.getSubSequence(chName,
					start2, end1,
					Strand.NEGTIVE));
		}
		transcriptLiftStartEndSequenceAasequenceIndel.setNeutralSelectionSequence(intronSequenceSB.toString());
		 * */
		
		//get the intron sequence for sweep analysis: end
		
		
		if (wantCdsList) {
			transcriptLiftStartEndSequenceAasequenceIndel
					.setCdsLiftSequenceArrayList(cdsLiftSequenceArrayList);
		} else {
			//transcriptLiftStartEndSequenceAasequenceIndel
				//	.setCdsLiftSequenceArrayList(null);
			transcriptLiftStartEndSequenceAasequenceIndel
			.setCdsLiftSequenceArrayList(cdsLiftSequenceArrayList);
			cdsLiftSequenceArrayList=null;
		}

		if (wantmRnaSequence) {
			transcriptLiftStartEndSequenceAasequenceIndel.setSequence(sequence);
		} else {
			transcriptLiftStartEndSequenceAasequenceIndel.setSequence(sequence);
			sequence = null;
		}
		if (wantFullCNDnaSequence) {
			transcriptLiftStartEndSequenceAasequenceIndel
					.setFullequence(fullSequence);
		} else {
			transcriptLiftStartEndSequenceAasequenceIndel.setFullequence(null);
			fullSequence=null;
		}
		return transcriptLiftStartEndSequenceAasequenceIndel;
	}

	public synchronized boolean ifLengthCouldbeDivedBYThree(String cdsSequence) {
		if (0 != cdsSequence.length() % 3) {
			return false;
		}
		return true;
	}

	public synchronized boolean ifNewStopCOde(String cdsSequence) {
		for (int jj = 0; jj < cdsSequence.length() - 3; jj += 3) {
			try {
				if ('*' == standardGeneticCode.getGeneticCode(
						cdsSequence.substring(jj, jj + 3), (0 == jj))) {// new
																		// stop
																		// code
					return true;
				}
			} catch (codingNotThree | codingNotFound e) {
				// e.printStackTrace();
			}
		}
		return false;
	}

	public synchronized boolean ifEndWithStopCode(String cdsSequence) {
		try {
			if (('*' != standardGeneticCode.getGeneticCode(
					cdsSequence.substring(cdsSequence.length() - 3,
							cdsSequence.length()), false))
					&& 'X' != standardGeneticCode.getGeneticCode(cdsSequence
							.substring(cdsSequence.length() - 3,
									cdsSequence.length()), false)) {
				return false; // stop code disappeared
			}
		} catch (codingNotThree e) {
			// e.printStackTrace();
		} catch (codingNotFound e) {
			// e.printStackTrace();
		}
		return true;
	}

	public synchronized boolean ifStartWithStartCode(String cdsSequence) {
		try {
			if (('M' != standardGeneticCode.getGeneticCode(
					cdsSequence.substring(0, 3), true))
					&& 'X' != standardGeneticCode.getGeneticCode(
							cdsSequence.substring(0, 3), true)) {
				return false; // start code disappeared
			}
		} catch (codingNotThree e) {
			// e.printStackTrace();
		} catch (codingNotFound e) {
			// e.printStackTrace();
		}
		return true;
	}

	private synchronized boolean ifSpliceSitesOk(
			TranscriptLiftStartEndSequenceAasequenceIndel t2,
			String chromeSomeName) {
		boolean ifSelectTaur10 = true;
		for (int iiii = 1; iiii < t2.getCdsLiftSequenceArrayList().size(); iiii++) {
			int le;
			int ts;
			String s1;
			String s2;
			int let;
			int tst;
			String s1t;
			String s2t;
			if (t2.getStrand() == Strand.POSITIVE) {
				le = t2.getCdsLiftSequenceArrayList().get(iiii - 1).getEnd();
				ts = t2.getCdsLiftSequenceArrayList().get(iiii).getStart();
				s1 = referenceChromeSomeRead.getSubSequence(chromeSomeName,
						le + 1, le + 2, t2.getStrand());
				s2 = referenceChromeSomeRead.getSubSequence(chromeSomeName,
						ts - 2, ts - 1, t2.getStrand());

				let = t2.getCdsLiftSequenceArrayList().get(iiii - 1)
						.getLiftEnd();
				tst = t2.getCdsLiftSequenceArrayList().get(iiii).getLiftStart();

				s1t = targetChromeSomeRead.getSubSequence(chromeSomeName,
						let + 1, let + 2, t2.getStrand());
				s2t = targetChromeSomeRead.getSubSequence(chromeSomeName,
						tst - 2, tst - 1, t2.getStrand());
			} else {
				le = t2.getCdsLiftSequenceArrayList().get(iiii - 1).getStart();
				ts = t2.getCdsLiftSequenceArrayList().get(iiii).getEnd();
				// System.out.println(""+chromeSomeName+"\t"+(le-2)+"\t"+(le-1)+"\t"+t2.getStrand());
				s1 = referenceChromeSomeRead.getSubSequence(chromeSomeName,
						le - 2, le - 1, t2.getStrand());
				s2 = referenceChromeSomeRead.getSubSequence(chromeSomeName,
						ts + 2, ts + 1, t2.getStrand());

				let = t2.getCdsLiftSequenceArrayList().get(iiii - 1)
						.getLiftStart();
				tst = t2.getCdsLiftSequenceArrayList().get(iiii).getLiftEnd();
				s1t = targetChromeSomeRead.getSubSequence(chromeSomeName,
						let - 2, let - 1, t2.getStrand());
				s2t = targetChromeSomeRead.getSubSequence(chromeSomeName,
						tst + 2, tst + 1, t2.getStrand());
			}
			/*System.out.println("chromeSomeName: " + chromeSomeName + " ts: " + ts + " t2.getStrand(): " + t2.getStrand());
			System.out.println(referenceChromeSomeRead.getChromoSomeById(chromeSomeName).getSequence());
			System.out.println(s2);
			*/
			
			s2 = this.agIUPACcodesTranslation(s2);
			s1 = this.gtIUPACcodesTranslation(s1);
			s2t = this.agIUPACcodesTranslation(s2t);
			s1t = this.gtIUPACcodesTranslation(s1t);
			// System.err.println("at:s1:"+s1);
			// System.err.println("ag:s2:"+s2);

			// System.err.println("gt:s1t:"+s1t);
			// System.err.println("ag:s2t:"+s2t);
			if ( (( "GT".equals(s1t) || "GC".equals(s1t) || "CT".equals(s1t)  || "GG".equals(s1t)) && "AG".equals(s2t)) || ("GT".equals(s1t) && ("CG".equals(s2t) || "TG".equals(s2t))) ) {
				// System.err.println("111");
			} else {
				// System.err.println("222");
				if (!s1t.equals(s1)) {
					ifSelectTaur10 = false;
					// System.err.println("333");
				}
				if (!s2t.equals(s2)) {
					ifSelectTaur10 = false;
					// System.err.println("444");
				}
			}
		}
		return ifSelectTaur10;
	}

	private synchronized String agIUPACcodesTranslation(String ag) {
		String agString = ag;
		if ( agString.length()==2 && ('A' == ag.charAt(0) || 'R' == ag.charAt(0) || 'W' == ag.charAt(0)
				|| 'M' == ag.charAt(0) || 'D' == ag.charAt(0)
				|| 'H' == ag.charAt(0) || 'V' == ag.charAt(0) || 'N' == ag
				.charAt(0))
				&& ('G' == ag.charAt(1) || 'K' == ag.charAt(1)
						|| 'R' == ag.charAt(1) || 'S' == ag.charAt(1)
						|| 'B' == ag.charAt(1) || 'D' == ag.charAt(1)
						|| 'V' == ag.charAt(1) || 'N' == ag.charAt(1))) {
			agString = "AG";
		}
		return agString;
	}

	private synchronized String gtIUPACcodesTranslation(String gt) {
		String gtString = gt;
		if ( gtString.length()==2 && ('G' == gtString.charAt(0) || 'K' == gtString.charAt(0)
				|| 'R' == gtString.charAt(0) || 'S' == gtString.charAt(0)
				|| 'B' == gtString.charAt(0) || 'D' == gtString.charAt(0)
				|| 'V' == gtString.charAt(0) || 'N' == gtString.charAt(0))
				&& ('T' == gtString.charAt(1) || 'K' == gtString.charAt(1)
						|| 'Y' == gtString.charAt(1)
						|| 'W' == gtString.charAt(1)
						|| 'U' == gtString.charAt(1)
						|| 'B' == gtString.charAt(1)
						|| 'D' == gtString.charAt(1)
						|| 'H' == gtString.charAt(1) || 'N' == gtString
						.charAt(1))) {
			gtString = "GT";
		}
		return gtString;
	}

	public synchronized StandardGeneticCode getStandardGeneticCode() {
		return standardGeneticCode;
	}

	public synchronized void setStandardGeneticCode(
			StandardGeneticCode standardGeneticCode) {
		this.standardGeneticCode = standardGeneticCode;
	}

	public synchronized AnnotationReadImpl getAnnotationReadImpl() {
		return annotationReadImpl;
	}

	public synchronized void setAnnotationReadImpl(
			AnnotationReadImpl annotationReadImpl) {
		this.annotationReadImpl = annotationReadImpl;
	}

	public synchronized ChromoSomeReadService getTargetChromeSomeRead() {
		return targetChromeSomeRead;
	}

	public synchronized void setTargetChromeSomeRead(
			ChromoSomeReadService targetChromeSomeRead) {
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
}
