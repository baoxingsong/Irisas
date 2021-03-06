package OtherFunctions.ReSdiWithWindowsedMsa;

import edu.unc.genomics.Contig;
import edu.unc.genomics.io.WigFileException;
import edu.unc.genomics.io.WigFileFormatException;
import edu.unc.genomics.io.WigFileReader;
import me.songbx.impl.FastaIndexEntryImpl;
import me.songbx.impl.MsaFileReadImpl;
import me.songbx.model.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this is not done yet, got some problem for the boundary region insertions
 * */

public class IndelSnpPlinkFromMsaAction {
	private int threadNumber;
	private ArrayList<String> names;
	private HashMap<String, ArrayList<MsaFile>> msaFileLocationsHashmap;
	private HashMap<String, ArrayList<MsaFileRecord>> msaFileRecordsHashMap = new HashMap<String, ArrayList<MsaFileRecord>>();
	private String refName;
	private String genomeFolder;
	private boolean merge; // the merge is not implemented yet
	private int sizeOfGapForMerge;
	private PrintWriter outTped;
	public IndelSnpPlinkFromMsaAction(ArrayList<String> names, HashMap<String, ArrayList<MsaFile>> msaFileLocationsHashmap,
                                      String refName, String genomeFolder, int threadNumber, boolean merge, int sizeOfGapForMerge, PrintWriter outTped){
		this.names=names;
		this.msaFileLocationsHashmap=msaFileLocationsHashmap;
		this.genomeFolder=genomeFolder;
		this.refName = refName;
		this.threadNumber=threadNumber;
		this.merge=merge;
		this.sizeOfGapForMerge=sizeOfGapForMerge;
		this.outTped=outTped;
		this.doIt();
	}

	private HashMap<Character, Integer> outputCode = new HashMap<Character, Integer>();

	private int getDnaCode( char c ){
	    if ( outputCode.containsKey(c) ){
	        return outputCode.get(c);
        }else{
	        return outputCode.get('N');
        }
    }

	private void doIt(){
		try {
			// prepare the chromosome length information by creating fasta index file begin
			for ( String name : names ){
				String fastaPath = genomeFolder + File.separator + name +  ".fa";
				String fastaIndexPath = genomeFolder + File.separator + name +  ".fa.fai";
				File f = new File(fastaIndexPath);
				if(f.exists() && !f.isDirectory()) {

				}else{
					/*try {
						Runtime r = Runtime.getRuntime();
						String[] command = {"samtools", "faidx", "fastaPath"};
						Process p = r.exec(command);
                        p.waitFor();
						p.destroy();
					}catch (final Exception e) {
						System.err.print("genome sequence index file could not be created");
						e.printStackTrace();
						System.exit(1);
					}*/
					new FastaIndexEntryImpl().createFastaIndexFile(fastaPath);
				}
			}
			// prepare the chromosome length information by creating fasta index file end

			//prepare the coverage query hashMap begin
			HashMap<String, WigFileReader> accessionName_WigFileReader_map = new HashMap<String, WigFileReader>();
			for ( String name : names ) {
				if( ! name.equalsIgnoreCase(refName) ){
					String bwFilePosition = genomeFolder + File.separator + name + ".bw";
					Path bwFile = Paths.get(bwFilePosition);
					try {
						WigFileReader wig = WigFileReader.autodetect(bwFile);
						accessionName_WigFileReader_map.put(name, wig);
					}catch (WigFileFormatException | IOException e) {
						e.printStackTrace();
					}
				}
			}
			//prepare the coverage query hashMap end

            // encode nucleic acids into integer begin
			outputCode.put('A', 1);
			outputCode.put('T', 2);
			outputCode.put('G', 3);
			outputCode.put('C', 4);
			outputCode.put('-', 5);
			outputCode.put('N', 6);
            // encode nucleic acids into integer end


			HashSet<String> names_set = new HashSet<String>();
			names_set.addAll(names);
			for(String chrName : msaFileLocationsHashmap.keySet()){

				//chrLengths begin
                HashMap<String, Integer> chrLengths = new  HashMap<String, Integer>();
                // the part after last window begin
                for(String name : names) {
                    BufferedReader reader = new BufferedReader(new FileReader(genomeFolder + File.separator + name + ".fa.fai"));
                    String tempString = null;
                    Pattern p = Pattern.compile(chrName+"\\s+(\\d+)\\s");
                    while ((tempString = reader.readLine()) != null) {
                        Matcher m = p.matcher(tempString);
                        if (m.find()) {
                            int this_name_chr_length = Integer.parseInt(m.group(1));
                            chrLengths.put(name, this_name_chr_length);
                        }
                    }
                }
                //chrLengths end

				String chrNameSimple = chrName;
				chrNameSimple = chrNameSimple.replace("Chr", "");

				ArrayList<MsaFile> msaFileLocations = msaFileLocationsHashmap.get(chrName);
				Collections.sort(msaFileLocations); // sort it

				HashMap<String, Integer> lastEnds = new HashMap<String, Integer>(); // the end position for each sequence from last MSA window
				for(String name : names){
					lastEnds.put(name, 0); // initialize all the values with 0
				}

                int msaFile_index = 0;
                for( MsaFile msaFile : msaFileLocations){
                    msaFile_index++;

					MsaFileRecord msaFileRecord = new MsaFileReadImpl(msaFile.getFilePath(), names_set).getMsaFileRecord();

					int transcriptStart = msaFileRecord.getStart();
					int transcriptEnd = msaFileRecord.getEnd();
					transcriptEnd--;

					//reference begin
					MsaSingleRecord refMsaSingleRecord = msaFileRecord.getRecords().get(refName);
					int msaRefStart = refMsaSingleRecord.getStart();
					int refLetterNumber = 0;
					StringBuffer refSeq_bf = new StringBuffer();
					for (int ai = 0; ai < refMsaSingleRecord.getSequence().length(); ai++) {
						if (refMsaSingleRecord.getSequence().charAt(ai) != '-') {
							refLetterNumber++;
						}
						if (transcriptStart <= (msaRefStart + refLetterNumber - 1) && (msaRefStart + refLetterNumber - 1) < transcriptEnd) {
                            refSeq_bf.append( refMsaSingleRecord.getSequence().charAt(ai) );
						} else if ((msaRefStart + refLetterNumber - 1) == transcriptEnd && refMsaSingleRecord.getSequence().charAt(ai) != '-') {
                            refSeq_bf.append( refMsaSingleRecord.getSequence().charAt(ai) );
						}
					}
                    String refSeq = refSeq_bf.toString();
					//reference end

                    // prepare sequences matrix begin
					char[][] sequences = new char[refSeq.length()][names.size()];
					int index_i = 0;
					HashMap<String, Integer> boundary_indels = new HashMap<String, Integer>();
					for(String name : names){
						int index_j = 0;
						int targetTranscriptStart = 0;
						int targetTranscriptEnd = 0;
						MsaSingleRecord targetMsaSingleRecord = msaFileRecord.getRecords().get(name);
						int msaTargetStart = targetMsaSingleRecord.getStart();

						//deleted the extend sequence only keep the wanted region begin
                        refLetterNumber = 0;
                        int targetLetterNumber = 0;
						for (int ai = 0; ai < refMsaSingleRecord.getSequence().length(); ai++) {
							if (refMsaSingleRecord.getSequence().charAt(ai) != '-') {
								refLetterNumber++;
							}
							if (targetMsaSingleRecord.getSequence().charAt(ai) != '-') {
								targetLetterNumber++;
							}
							if (transcriptStart == (msaRefStart + refLetterNumber - 1) && refMsaSingleRecord.getSequence().charAt(ai) != '-') {
								targetTranscriptStart = msaTargetStart + targetLetterNumber - 1;
								if (targetMsaSingleRecord.getSequence().charAt(ai) == '-') {
									targetTranscriptStart++;
								}
							}
							if ((msaRefStart + refLetterNumber - 1) == transcriptEnd && refMsaSingleRecord.getSequence().charAt(ai) != '-') {
								targetTranscriptEnd = msaTargetStart + targetLetterNumber - 1;
							}
							if (transcriptStart <= (msaRefStart + refLetterNumber - 1) && (msaRefStart + refLetterNumber - 1) < transcriptEnd) {
								sequences[index_j][index_i] = targetMsaSingleRecord.getSequence().charAt(ai);
                                index_j++;
							} else if ((msaRefStart + refLetterNumber - 1) == transcriptEnd && refMsaSingleRecord.getSequence().charAt(ai) != '-') {
								sequences[index_j][index_i] = targetMsaSingleRecord.getSequence().charAt(ai);
                                index_j++;
							}
						}

                        if( (msaFile_index == (msaFileLocations.size())) && targetTranscriptEnd == 0 ){
                            if( chrLengths.containsKey(name) ){
                                targetTranscriptEnd = chrLengths.get(name);
                            }else{
                                System.err.println("There is something wrong to get the chromosome length information. " +
                                        "But it would not affect the final result a lot.");
                                targetTranscriptEnd = lastEnds.get(name);
                            }
                        }

						boundary_indels.put(name, targetTranscriptStart - lastEnds.get(name) );

						// solve boundary problem by adapting the result of previous window begin
						if( lastEnds.get(name) >= targetTranscriptStart ){
							int overLapLength = lastEnds.get(name) - targetTranscriptStart;
							overLapLength++;
							int corrected = 0;
							for( int t=0; t<refSeq.length(); t++ ){
								if(corrected<overLapLength && sequences[t][index_i]!='-'){
									sequences[t][index_i]='-';
									corrected++;
								}
							}
							int largerEnd;
							if( targetTranscriptEnd > lastEnds.get(name) ){
								largerEnd = targetTranscriptEnd;
							} else {
								largerEnd = lastEnds.get(name);
							}
							lastEnds.put(name, largerEnd);
						}else{
							lastEnds.put(name, targetTranscriptEnd);
						} // solve boundary problem by adapting the result of previous window end

						index_i ++;
					}// prepare sequences matrix end

					// output boundary indels begin
					outTped.print(chrNameSimple+" "+ chrNameSimple+"_"+transcriptStart+"_b 0 "+transcriptStart);
					for(String name : names) {
						int boundary_indel_length = boundary_indels.get(name);
						if (boundary_indel_length > 0) {
							outTped.print(" " + boundary_indel_length + " " + boundary_indel_length); // 1 means equal, 2 means 1 bp deletion etc.
						}else{
							outTped.print(" " + 1 + " " + 1);
						}
					}
					outTped.println();
					// output boundary indels end

					//MyThreadCount threadCount = new MyThreadCount(0);
					ExecutorService myExecutor = Executors.newFixedThreadPool(threadNumber);

                    // output matrix start
					TpedOutPutContents tpedOutPutContent_arrayList = new TpedOutPutContents();
                    String lastCol_seq = "";
                    int lastLength = 0;
                    int ref_seq_index = 0;
					for ( int index_array = 0; index_array < refSeq.length(); index_array++ ){
						if( refSeq.charAt(index_array) != '-' ){
							ref_seq_index ++;
						}
					    StringBuffer thisCol_seqB = new StringBuffer();
						HashSet<Character> thisCol = new HashSet<Character>();
						for( int index_array2 = 0; index_array2<names.size(); index_array2++ ){
                            Character c = sequences[index_array][index_array2];
							thisCol.add(c);
							if( c == '-' ){
                                thisCol_seqB.append("2"); // deletion
                            }else {
                                thisCol_seqB.append("1"); // not deletion
                            }
						}
						if( thisCol.size() > 1 ) {
						    if( thisCol.size()==2 && thisCol.contains('-') ){ // there is only INDEL variation
                                String thisCol_seq = thisCol_seqB.toString();
                                if ( lastLength >0){
                                    if( thisCol_seq.equalsIgnoreCase(lastCol_seq) ) { // if the INDEL state of this one is same with last one
                                        lastLength++;
                                    }else { // if the INDEL state of this one is different with last one, then output
										StringBuffer contentsb = new StringBuffer();
										int outPosition = transcriptStart+ref_seq_index-1;
										contentsb.append(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + "_i_"+lastLength + " 0 " + outPosition);
                                        for (int name_index = 0; name_index < names.size(); name_index++) {
                                            char code = lastCol_seq.charAt(name_index);
											contentsb.append(" " + code + " " + code);
                                        }
										TpedOutPutContent tpedOutPutContent = new TpedOutPutContent(index_array, contentsb.toString());
										tpedOutPutContent_arrayList.add(tpedOutPutContent);
                                        lastCol_seq = thisCol_seq;
                                        lastLength = 1;
                                    }
                                } else { // the last is not INDEL
                                    lastCol_seq = thisCol_seq;
                                    lastLength = 1;
                                }
                            } else {
						        if ( lastLength > 0 ){ // last one/several contains only INDEL, this one changed, then try to output
									StringBuffer contentsb = new StringBuffer();
									int outPosition = transcriptStart+ref_seq_index-1;
									contentsb.append(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + "_i_"+lastLength + " 0 " + outPosition);
                                    for (int name_index = 0; name_index < names.size(); name_index++) {
                                        char code = lastCol_seq.charAt(name_index);
										contentsb.append(" " + code + " " + code);
                                    }
									TpedOutPutContent tpedOutPutContent = new TpedOutPutContent(index_array, contentsb.toString());
									tpedOutPutContent_arrayList.add(tpedOutPutContent);

                                    lastLength=0;
                                    lastCol_seq="";
                                }
/*
								boolean isThisThreadUnrun=true;
								while(isThisThreadUnrun){
									if(threadCount.getCount() < threadNumber){
										threadCount.plusOne();
										SnpColumn main = new SnpColumn( tpedOutPutContent_arrayList, transcriptStart, ref_seq_index,
												chrNameSimple, index_array, chrName, sequences, accessionName_WigFileReader_map, threadCount);
										main.start();
										isThisThreadUnrun=false;
									}else{
										try {
											Thread.sleep(1);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
								*/

								myExecutor.execute(new SnpColumn( tpedOutPutContent_arrayList, transcriptStart, ref_seq_index,
										chrNameSimple, index_array, chrName, sequences, accessionName_WigFileReader_map));
//                                // output this genotype begin
//								StringBuffer contentsb = new StringBuffer();
//								int outPosition = transcriptStart+ref_seq_index-1;
//								contentsb.append(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + " 0 " + outPosition);
//                                for (int name_index = 0; name_index < names.size(); name_index++) {
//                                    char this_char = sequences[index_array][name_index];
//                                    char ref_char = sequences[index_array][names.size() - 1];
//
//                                    int code = getDnaCode(ref_char);
//                                    if ( this_char == ref_char && ref_char != '-' ) { // the reference is not deletion
//										if (accessionName_WigFileReader_map.containsKey(names.get(name_index))) { // this is not the reference accession
//											try {
//												Contig result = accessionName_WigFileReader_map.get(names.get(name_index)).query(chrName, outPosition, outPosition);
//												double thisMean = result.mean();
//												if(  Double.isNaN(thisMean) || thisMean<2 ){
//													code = 0; // this is a missing value/low coverage value
//												}
//											} catch (WigFileException e) {
//												e.printStackTrace();
//											}
//										}
//									} /*else if (! outputCode.containsKey(this_char) ) {
//                                    	// sign IUPAC code to reference Seq
//										//code = getDnaCode(ref_char);
//									} */else if ( outputCode.containsKey(this_char) ) {
//										code = getDnaCode(this_char);
//									}
//									contentsb.append(" " + code + " " + code);
//                                }
//								TpedOutPutContent tpedOutPutContent = new TpedOutPutContent(index_array, contentsb.toString());
//								tpedOutPutContent_arrayList.add(tpedOutPutContent);
                                // output this genotype end
                            }
						} else if (lastLength >0) { // if the INDEL state of this one is different with last one, then output
							StringBuffer contentsb = new StringBuffer();
							int outPosition = transcriptStart+ref_seq_index-1;
							contentsb.append(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + "_i_"+lastLength + " 0 " + outPosition);
                            for (int name_index = 0; name_index < names.size(); name_index++) {
                                char code = lastCol_seq.charAt(name_index);
								contentsb.append(" " + code + " " + code);
                            }
							TpedOutPutContent tpedOutPutContent = new TpedOutPutContent(index_array, contentsb.toString());
							tpedOutPutContent_arrayList.add(tpedOutPutContent);
                            lastCol_seq = "";
                            lastLength = 0;
                        }
					}
					/*while(threadCount.hasNext()){// wait for all the thread
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}*/
					myExecutor.shutdown();
					try {
						myExecutor.awaitTermination(20, TimeUnit.MINUTES);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					tpedOutPutContent_arrayList.sort();
					for( TpedOutPutContent tpedOutPutContent : tpedOutPutContent_arrayList.getTpedOutPutContents()  ){
						outTped.println(tpedOutPutContent.getContent());
					}
					// output matrix end
				}


                outTped.print(chrNameSimple + " " + chrNameSimple + "_" + chrLengths.get(refName)+"_end" + " 0 " + chrLengths.get(refName));
                for( String name : names ){
				    int this_name_chr_length;
				    if (chrLengths.containsKey(name)){
				        this_name_chr_length = chrLengths.get(name);
                    }else{
				        this_name_chr_length = lastEnds.get(name);
                    }
                    if ( this_name_chr_length > lastEnds.get(name) ){
                        int boundary_indel_length = this_name_chr_length - lastEnds.get(name) + 1; // because 1 means equal, so we plus 1 here
                        outTped.print(" " + boundary_indel_length + " " + boundary_indel_length);
                    }else{
                        outTped.print(" " + 1 + " " + 1);
                    }
                }
                outTped.println();
                // the part after last window end
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class TpedOutPutContent implements Comparable<TpedOutPutContent>{
		private int position;
		private String content;
		public TpedOutPutContent(int position, String content) {
			this.position = position;
			this.content = content;
		}
		public synchronized int getPosition() {
			return position;
		}
		public synchronized void setPosition(int postion) {
			this.position = postion;
		}
		public synchronized String getContent() {
			return content;
		}
		public synchronized void setContent(String content) {
			this.content = content;
		}
		public synchronized int compareTo ( TpedOutPutContent tpedOutPutContent){
			return this.position - tpedOutPutContent.getPosition();
		}
	}
	class TpedOutPutContents{
		private ArrayList<TpedOutPutContent> tpedOutPutContents = new ArrayList<TpedOutPutContent>();

		public synchronized ArrayList<TpedOutPutContent> getTpedOutPutContents() {
			return tpedOutPutContents;
		}
		public synchronized void setTpedOutPutContents(ArrayList<TpedOutPutContent> tpedOutPutContents) {
			this.tpedOutPutContents = tpedOutPutContents;
		}
		public synchronized void add( TpedOutPutContent tpedOutPutContent){
			this.tpedOutPutContents.add(tpedOutPutContent);
		}
		public synchronized void sort(){
			Collections.sort(tpedOutPutContents);
		}
	}

	class SnpColumn extends Thread{
		private TpedOutPutContents tpedOutPutContent_arrayList;
		private int transcriptStart;
		private int ref_seq_index;
		private String chrNameSimple;
		private int index_array;
		private String chrName;
		private char[][] sequences;
		private HashMap<String, WigFileReader> accessionName_WigFileReader_map;
		//private MyThreadCount threadCount;
		public SnpColumn( TpedOutPutContents tpedOutPutContent_arrayList, int transcriptStart, int ref_seq_index,
						  String chrNameSimple, int index_array, String chrName, char[][] sequences,
						  HashMap<String, WigFileReader> accessionName_WigFileReader_map/*, MyThreadCount threadCount */){
			this.tpedOutPutContent_arrayList = tpedOutPutContent_arrayList;
			this.transcriptStart = transcriptStart;
			this.ref_seq_index = ref_seq_index;
			this.chrNameSimple = chrNameSimple;
			this.index_array = index_array;
			this.chrName = chrName;
			this.sequences = sequences;
			this.accessionName_WigFileReader_map = accessionName_WigFileReader_map;
			//this.threadCount = threadCount;
		}
		public void run( ){
			StringBuffer contentsb = new StringBuffer();
			int outPosition = transcriptStart+ref_seq_index-1;
			contentsb.append(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + " 0 " + outPosition);
			for (int name_index = 0; name_index < names.size(); name_index++) {
				char this_char = sequences[index_array][name_index];
				char ref_char = sequences[index_array][names.size() - 1];

				int code = getDnaCode(ref_char);
				if ( this_char == ref_char && ref_char != '-' ) { // the reference is not deletion
					if (accessionName_WigFileReader_map.containsKey(names.get(name_index))) { // this is not the reference accession
						try {
							Contig result = accessionName_WigFileReader_map.get(names.get(name_index)).query(chrName, outPosition, outPosition);
							double thisMean = result.mean();
							if(  Double.isNaN(thisMean) || thisMean<2 ){
								code = 0; // this is a missing value/low coverage value
							}
						} catch (IOException | WigFileException e) {
							e.printStackTrace();
						}
					}
				} /*else if (! outputCode.containsKey(this_char) ) {
                                    	// sign IUPAC code to reference Seq
										//code = getDnaCode(ref_char);
									} */else if ( outputCode.containsKey(this_char) ) {
					code = getDnaCode(this_char);
				}
				contentsb.append(" " + code + " " + code);
			}
			TpedOutPutContent tpedOutPutContent = new TpedOutPutContent(index_array, contentsb.toString());
			tpedOutPutContent_arrayList.add(tpedOutPutContent);
			//threadCount.countDown();
		}
	}
}
