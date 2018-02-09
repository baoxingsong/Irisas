package OtherFunctions.IndelGwas.SNPgwas;

import OtherFunctions.PopulationStructure.Model.MarkerPostion;
import OtherFunctions.PopulationStructure.Model.MarkerPostionS;
import edu.unc.genomics.Contig;
import edu.unc.genomics.io.WigFileException;
import edu.unc.genomics.io.WigFileFormatException;
import edu.unc.genomics.io.WigFileReader;
import me.songbx.impl.ChromoSomeReadImpl;
import me.songbx.impl.MapFileImpl;
import me.songbx.model.MapSingleRecord;
import me.songbx.util.MyThreadCount;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/* generate wig file
samtools mpileup 515A.bam | perl -ne 'BEGIN{print "track type=wiggle_0 name=515A description=515A\n"};($c, $start, undef, $depth) = split; if ($c ne $lastC) { print "variableStep chrom=$c\n"; };$lastC=$c; print "$start\t$depth\n";'  > 515A.wig
13 Mar. 2016
*/

public class SdiSnpToPedMultipleAllic{
	
	private int threadNumber = 5;
	private String accessionListFile;
	private String refName;
	private String genomeFolder;
	private String sdiLocation;

	public SdiSnpToPedMultipleAllic(){

	}
	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	public void setAccessionListFile(String accessionListFile) {
		this.accessionListFile = accessionListFile;
	}
	public void setRefName(String refName) {
		this.refName = refName;
	}
	public void setGenomeFolder(String genomeFolder) {
		this.genomeFolder = genomeFolder;
	}
	public void setSdiLocation(String sdiLocation) {
		this.sdiLocation = sdiLocation;
	}

	public SdiSnpToPedMultipleAllic(String [] argv ){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number (Default 5)\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -g   the folder where the genome sequences and bw files are located\n");
        helpMessage.append("  -s   the folder where sdi files are located\n");
        
		Options options = new Options();
        options.addOption("t",true,"threadnumber");
        options.addOption("l",true,"accessionListFile");
        options.addOption("r",true,"refName");
        options.addOption("g",true,"genomeFolder");
        options.addOption("s",true,"sdiLocation");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        if(cmd.hasOption("t")){
        	threadNumber = Integer.parseInt(cmd.getOptionValue("t"));
        }
        if(cmd.hasOption("l")){
        	accessionListFile = cmd.getOptionValue("l");
        }else{
        	System.err.println("-l is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("r")){
        	refName = cmd.getOptionValue("r");
        }else{
        	System.err.println("-r is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("g")){
        	genomeFolder = cmd.getOptionValue("g");
        }else{
        	System.err.println("-g is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("s")){
        	sdiLocation = cmd.getOptionValue("s");
        }else{
        	System.err.println("-s is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        try {
			doIt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void doIt() throws FileNotFoundException{
		//String folderLocation = genomeFolder + File.separator;
		ChromoSomeReadImpl chromoSomeReadImpl = new ChromoSomeReadImpl(genomeFolder + File.separator + refName +  ".fa");
		BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
		String tempString = null;
		// read the accession list file begin
		ArrayList<String> accessionNames = new ArrayList<String>();
		try {
			while ((tempString = reader.readLine()) != null) {
				if( tempString.equalsIgnoreCase(refName) ){
					
				}else{
					accessionNames.add(tempString);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// read the accession list file end

		//read all the sdi file and find all the position where has SNP record begin
		int no_sdi_submitted=0;
		MarkerPostionsMap markerPostionsMap =new MarkerPostionsMap();
		MyThreadCount threadCount1 = new MyThreadCount(0);
		for(String ss : accessionNames){
			boolean isThisThreadUnrun=true;
			while(isThisThreadUnrun){
				if(threadCount1.getCount() < threadNumber){
	            	threadCount1.plusOne();
	            	SDIRead main = new SDIRead(sdiLocation, markerPostionsMap, threadCount1, ss, chromoSomeReadImpl);
	            	main.start();
	            	no_sdi_submitted++;
	            	System.out.println( ss + "\tNO of sdi submitted\t" + no_sdi_submitted );
	                isThisThreadUnrun=false;
	            }else{
	            	try {
	    				Thread.sleep(1);
	    			} catch (InterruptedException e) {
	    				e.printStackTrace();
	    			}
	            }
			}
		}
		System.out.println( "all sdi have been submitted, waiting for finishing" );
		while(threadCount1.hasNext()){// wait for all the thread
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println( "SDI read over" );
		//read all the sdi file and find all the position where has SNP record end

		// for all the SNP position find the allele of reference genome begin
		for( String key : markerPostionsMap.keySet() ){
			MarkerPostionS markerPostions = markerPostionsMap.get(key);
			if( chromoSomeReadImpl.getChromoSomeHashMap().containsKey(key) ){
				for( MarkerPostion markerPostion : markerPostions.getMarkerPostions() ){
					char refNaChar = chromoSomeReadImpl.getChromoSomeById(key).getSequence().charAt(markerPostion.getPosition()-1);
					markerPostion.setColNaChar(refNaChar);
				}
			}
		} // for all the SNP position find the allele of reference genome end
		
		int no_bw_submitted=0;
		HashMap<String, HashMap<MarkerPostion, Character>> markerPostionAccessionsHashMap = new HashMap<String, HashMap<MarkerPostion, Character>>();
		MyThreadCount threadCount = new MyThreadCount(0);
		for(String ss : accessionNames){
			boolean isThisThreadUnrun=true;
			while(isThisThreadUnrun){
				if(threadCount.getCount() < threadNumber){
	            	threadCount.plusOne();
	            	SdiSnpToPedMultipleAllicMultipleThread sdiSnpToPedMultipleThread = new SdiSnpToPedMultipleAllicMultipleThread ( genomeFolder, ss, markerPostionsMap, markerPostionAccessionsHashMap, threadCount, sdiLocation);
	            	//System.out.println(ss + " prepare to begin");
	            	sdiSnpToPedMultipleThread.start();
	            	no_bw_submitted++;
	            	System.out.println(ss + "\tNO of bw submitted\t" + no_bw_submitted);
	                isThisThreadUnrun=false;
	            }else{
	            	try {
	    				Thread.sleep(1);
	    			} catch (InterruptedException e) {
	    				e.printStackTrace();
	    			}
	            }
			}
		}
		while(threadCount.hasNext()){// wait for all the thread
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("bam file checking end");
// remove those position with more than two states begin
//		for( String key : markerPostionsMap.keySet() ){
//			MarkerPostionS markerPostions = markerPostionsMap.get(key);
//			ArrayList<MarkerPostion> toRemoveMarkerPostions = new ArrayList<MarkerPostion>();
			//System.out.println("size "+ markerPostions.getMarkerPostions().size());
//			for( MarkerPostion markerPostion : markerPostions.getMarkerPostions() ){
//				HashSet<Character> chars = new HashSet<Character>();
//				for( String accessionName : markerPostionAccessionsHashMap.keySet() ){
//					char theChar;
//					if( markerPostionAccessionsHashMap.get(accessionName).containsKey(markerPostion) ){
//						theChar = markerPostionAccessionsHashMap.get(accessionName).get(markerPostion);
//					}else{
//						theChar = markerPostion.getColNaChar();
//					}
//					if( theChar=='A' || theChar=='T' || theChar=='C' || theChar=='G'){
//						chars.add(theChar);
//					}
//				}
//				char theChar = markerPostion.getColNaChar();
//				if( theChar=='A' || theChar=='T' || theChar=='C' || theChar=='G'){
//					chars.add(theChar);
//				}
//
//				if( chars.size() != 2 ){
//					toRemoveMarkerPostions.add(markerPostion);
//				}
//			}
//			for( MarkerPostion markerPostion : toRemoveMarkerPostions ){
//				markerPostions.remove(markerPostion);
//			}
//		}
		// remove those position with more than two states end
		
		ArrayList<MarkerPostion> markerPostionAs = new ArrayList<MarkerPostion>();
		for( String key : markerPostionsMap.keySet() ){
			MarkerPostionS markerPostions = markerPostionsMap.get(key);
			markerPostionAs.addAll( markerPostions.getMarkerPostions());
		}

		Collections.sort(markerPostionAs);
		PrintWriter outPut = new PrintWriter("sdi_multi_allic_snp.ped");
		for( String accessionName : markerPostionAccessionsHashMap.keySet() ){
			String accessionName2=accessionName;
			accessionName2=accessionName2.replaceAll("\\.SDI", "");
			accessionName2=accessionName2.replaceAll("\\.sdi", "");
			outPut.print(accessionName2 + " " + accessionName2 + " 0 0 1	1");
			for( int i =0; i < markerPostionAs.size(); i++ ){
				MarkerPostion markerPostion = markerPostionAs.get(i);
				char theChar;
				if( markerPostionAccessionsHashMap.get(accessionName).containsKey(markerPostion) ){
					theChar = markerPostionAccessionsHashMap.get(accessionName).get(markerPostion);
				}else{
					theChar = markerPostion.getColNaChar();
				}
				if( theChar == 'A' ){
					outPut.print("	1 1");
				}else if( theChar == 'T' ){
					outPut.print("	2 2");
				}else if( theChar == 'G' ){
					outPut.print("	3 3");
				}else if( theChar == 'C' ){
					outPut.print("	4 4");
				}else if (theChar == '-'){
					outPut.print("	5 5");
				}else if ( theChar == '+' ){ // reverse or some other replacement
					outPut.print("	6 6");
				}else if( theChar == 'N' ){ // low coverage
					outPut.print("	7 7");
				}else {
					outPut.print("	0 0");// missing value
				}
			}
			outPut.print("\n");
		}
		outPut.print("ref" + " " + "ref" + " 0 0 1	1");
		for( int i =0; i < markerPostionAs.size(); i++ ){
			MarkerPostion markerPostion = markerPostionAs.get(i);
			char theChar = markerPostion.getColNaChar();
			if( theChar == 'A' ){
				outPut.print("	1 1");
			}else if( theChar == 'T' ){
				outPut.print("	2 2");
			}else if( theChar == 'G' ){
				outPut.print("	3 3");
			}else if( theChar == 'C' ){
				outPut.print("	4 4");
			}else{
				outPut.print("	0 0");
			}
		}
		outPut.print("\n");
		outPut.close();
		PrintWriter outPutIDMap = new PrintWriter("sdi_multi_allic_snp.map");
		for( int i =0; i < markerPostionAs.size(); i++ ){
			MarkerPostion markerPostion = markerPostionAs.get(i);
			String chrname = markerPostion.getChrName();
			chrname = chrname.replace("Chr", "");
			outPutIDMap.println(chrname + "\t" + chrname+"_"+markerPostion.getPosition()+"_"+markerPostion.getColNaChar() + "\t0\t" + markerPostion.getPosition());
		}
		outPutIDMap.close();
	}
}

class SdiSnpToPedMultipleAllicMultipleThread  extends Thread{
	private String folderLocation;
	private String accessionName;
	private MarkerPostionsMap markerPostionsMap;
	private HashMap<String, HashMap<MarkerPostion, Character>> markerPostionAccessionsHashMap;
	private MyThreadCount threadCount;
	private String sdiLocation;
	public SdiSnpToPedMultipleAllicMultipleThread (String folderLocation, String accessionName, MarkerPostionsMap markerPostionsMap, HashMap<String, HashMap<MarkerPostion, Character>> markerPostionAccessionsHashMap, MyThreadCount threadCount, String sdiLocation){
		this.folderLocation=folderLocation;
		this.accessionName = accessionName;
		this.markerPostionsMap = markerPostionsMap;
		this.markerPostionAccessionsHashMap = markerPostionAccessionsHashMap;
		this.threadCount=threadCount;
		this.sdiLocation=sdiLocation;
	}
	
	public void run( ){
		accessionName=accessionName.replaceAll("\\.sdi", "");
		HashMap<MarkerPostion, Character> markerPostionHashMap = new HashMap<MarkerPostion, Character>();
		
		String bwFilePosition = folderLocation + File.separator + accessionName + ".bw";
		Path bwFile = Paths.get(bwFilePosition);
		WigFileReader wig;
		try {
			wig = WigFileReader.autodetect(bwFile);
		
			MapFileImpl mapFileImpl = new MapFileImpl( sdiLocation + File.separator +accessionName + ".sdi");
			for( String key : markerPostionsMap.keySet() ){	
				for( MarkerPostion markerPostion : markerPostionsMap.get(key).getMarkerPostions() ){
					boolean thisTranscriptIsReliable = true;
					Contig result = wig.query(key, markerPostion.getPosition(), markerPostion.getPosition());
					double thisMean = result.mean();
					if(  Double.isNaN(thisMean) || thisMean<2 ){
						thisTranscriptIsReliable = false;
					}

					HashSet<MapSingleRecord> mapSingleRecords = mapFileImpl.getOverLapRecordsByBasing( markerPostion.getChrName(), markerPostion.getPosition() );
					if( mapSingleRecords.size() > 0 ){
						for( MapSingleRecord mapSingleRecord : mapSingleRecords ){
							//if( thisTranscriptIsReliable ){
								if( mapSingleRecord.getBasement() == markerPostion.getPosition() && mapSingleRecord.getChanged()==0 && mapSingleRecord.getOriginal().length()==1 && !mapSingleRecord.getOriginal().contains("-") && mapSingleRecord.getResult().length()==1 ){
									char alternative = mapSingleRecord.getResult().toUpperCase().charAt(0);
									if( alternative=='A' || alternative=='C' || alternative=='G' || alternative=='T' ) { // if there is an IUPAC code, take it as same with reference
										markerPostionHashMap.put(markerPostion, alternative); // this is a SNP
									}
								} else if ( mapSingleRecord.getBasement() <= markerPostion.getPosition() && mapSingleRecord.getChanged()>0 && mapSingleRecord.getOriginal().contains("-") ){
									// there is an insertion around
								} else if ( mapSingleRecord.getBasement() <= markerPostion.getPosition() && mapSingleRecord.getChanged() < 0 && (mapSingleRecord.getBasement()+Math.abs(mapSingleRecord.getChanged())-1) >= markerPostion.getPosition() && mapSingleRecord.getResult().contains("-") ){
									markerPostionHashMap.put(markerPostion, '-'); // deletion
								} else if ( mapSingleRecord.getBasement() <= markerPostion.getPosition() && (mapSingleRecord.getBasement() + mapSingleRecord.getOriginal().length()) > markerPostion.getPosition() ) {
									markerPostionHashMap.put(markerPostion, '+'); // this for some reversion or fragment replacement.
								}
//							}else {
//								if (mapSingleRecord.getBasement() <= markerPostion.getPosition() && mapSingleRecord.getChanged() < 0 && (mapSingleRecord.getBasement() + Math.abs(mapSingleRecord.getChanged()) - 1) >= markerPostion.getPosition() && mapSingleRecord.getResult().contains("-")) {
//									markerPostionHashMap.put(markerPostion, '-'); // deletion
//								} else {
//									markerPostionHashMap.put(markerPostion, 'N'); // low coverage
//								}
//							}
						}
					}else{
						if( thisTranscriptIsReliable ){ // same with reference

						}else{
							markerPostionHashMap.put(markerPostion, 'N'); // low coverage
						}
					}
					if ( !thisTranscriptIsReliable && mapSingleRecords.size()==1 ){ // if there is no reads and there is only a insertion records, this position is encoded as missing
						for( MapSingleRecord mapSingleRecord : mapSingleRecords ) {
							if ( mapSingleRecord.getBasement() <= markerPostion.getPosition() && mapSingleRecord.getChanged()>0 && mapSingleRecord.getOriginal().contains("-") ){
								markerPostionHashMap.put(markerPostion, 'N'); // low coverage
							}
						}
					}
				}
			}
		} catch (WigFileFormatException | IOException | WigFileException e) {
			e.printStackTrace();
		}finally{
			markerPostionAccessionsHashMap.put(accessionName, markerPostionHashMap);
			threadCount.countDown();
		}
		System.out.println(accessionName + " finished");
	}
}

