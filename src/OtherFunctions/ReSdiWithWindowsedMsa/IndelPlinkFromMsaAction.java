package OtherFunctions.ReSdiWithWindowsedMsa;

import me.songbx.impl.MsaFileReadImpl;
import me.songbx.model.MsaFileRecord;
import me.songbx.model.MsaSingleRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this is not done yet, got some problem for the boundary region insertions
 * */

public class IndelPlinkFromMsaAction {
	private ArrayList<String> names;
	private HashMap<String, ArrayList<MsaFile>> msaFileLocationsHashmap;

	private HashMap<String, ArrayList<MsaFileRecord>> msaFileRecordsHashMap = new HashMap<String, ArrayList<MsaFileRecord>>();

	private String refName;
	private String genomeFolder;
	public IndelPlinkFromMsaAction(ArrayList<String> names, HashMap<String, ArrayList<MsaFile>> msaFileLocationsHashmap,
                                   String refName, String genomeFolder){
		this.names=names;
		this.msaFileLocationsHashmap=msaFileLocationsHashmap;
		this.genomeFolder=genomeFolder;
		this.refName = refName;
		this.doIt();
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
					try {
						Process p = new ProcessBuilder("samtools faidx " + fastaPath).start();
						p.waitFor();
					}catch (final Exception e) {
						e.printStackTrace();
						System.err.print("genome sequence index file could not be created");
						System.exit(1);
					}
				}
			}
			// prepare the chromosome length information by creating fasta index file end

            PrintWriter outTped = new PrintWriter(new FileOutputStream("msa_indel.tped",  true), true);
			HashSet<String> names_set = new HashSet<String>();
			names_set.addAll(names);
			for(String chrName : msaFileLocationsHashmap.keySet()){

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
					String msaFileLocation = msaFile.getFilePath();
					MsaFileRecord msaFileRecord = new MsaFileReadImpl(msaFileLocation, names_set).getMsaFileRecord();

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
                            refSeq_bf.append(refMsaSingleRecord.getSequence().charAt(ai));
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
							if (transcriptStart == (msaRefStart + refLetterNumber - 1) && refMsaSingleRecord.getSequence().charAt(ai) != '-') { // this position is unique
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
							if ( targetTranscriptEnd > lastEnds.get(name) ) {
								largerEnd = targetTranscriptEnd;
							} else {
								largerEnd = lastEnds.get(name);
							}
							lastEnds.put(name, largerEnd);
						} else {
							lastEnds.put(name, targetTranscriptEnd);
						} // solve boundary problem by adapting the result of previous window end

						index_i ++;
					} // prepare sequences matrix end

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

                    // output matrix start
                    String lastCol_seq = "";
                    int lastLength = 0;
					int ref_seq_index = -1;
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
						if( thisCol.contains('-') ) {
							String thisCol_seq = thisCol_seqB.toString();
							if ( lastLength >0){
								if( thisCol_seq.equalsIgnoreCase(lastCol_seq) ) { // if the INDEL state of this one is same with last one
									lastLength++;
								}else { // if the INDEL state of this one is different with last one, then output
									int outPosition = transcriptStart+ref_seq_index-1;
									outTped.print(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + "_i_"+lastLength + " 0 " + outPosition);
									for (int name_index = 0; name_index < names.size(); name_index++) {
										char code = lastCol_seq.charAt(name_index);
										outTped.print(" " + code + " " + code);
									}
									outTped.println();

									lastCol_seq = thisCol_seq;
									lastLength = 1;
								}
							} else { // the last is not INDEL
								lastCol_seq = thisCol_seq;
								lastLength = 1;
							}
						}else if (lastLength >0) { // if the INDEL state of this one is different with last one, then output
							int outPosition = transcriptStart+ref_seq_index-1;
                            outTped.print(chrNameSimple + " " + chrNameSimple + "_" + transcriptStart + "_" + index_array + "_i_"+lastLength + " 0 " + outPosition);
                            for (int name_index = 0; name_index < names.size(); name_index++) {
                                char code = lastCol_seq.charAt(name_index);
                                outTped.print(" " + code + " " + code);
                            }
                            outTped.println();

                            lastCol_seq = "";
                            lastLength = 0;
                        }
					}
					// output matrix end
				}


                outTped.print(chrNameSimple + " " + chrNameSimple + "_" + chrLengths.get(refName)+"_end" + " 0 " + chrLengths.get(refName));

				for( String name : names ){
				    int this_name_chr_length;
				    if (chrLengths.containsKey(name)) {
				        this_name_chr_length = chrLengths.get(name);
                    } else {
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
			outTped.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
