package OtherFunctions.IndelGwas.SNPgwas;

import OtherFunctions.PopulationStructure.Model.MarkerPostion;
import OtherFunctions.PopulationStructure.Model.MarkerPostionS;
import me.songbx.action.parallel.model.SysOutPut;
import me.songbx.action.parallel.model.SysHashMap;
import edu.unc.genomics.Contig;
import edu.unc.genomics.io.WigFileException;
import edu.unc.genomics.io.WigFileFormatException;
import edu.unc.genomics.io.WigFileReader;
import me.songbx.impl.ChromoSomeReadImpl;
import me.songbx.impl.MapFileImpl;
import me.songbx.model.MapSingleRecord;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* generate wig file
samtools mpileup 515A.bam | perl -ne 'BEGIN{print "track type=wiggle_0 name=515A description=515A\n"};($c, $start, undef, $depth) = split; if ($c ne $lastC) { print "variableStep chrom=$c\n"; };$lastC=$c; print "$start\t$depth\n";'  > 515A.wig
13 Mar. 2016
*/

public class SdiSnpToPed{
	
	private int threadNumber = 5;
	private String accessionListFile;
	private String refName;
	private String genomeFolder;
	private String sdiLocation;
	private int minimumCoverage = 2;

	public SdiSnpToPed(){

	}
	public synchronized void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	public synchronized void setAccessionListFile(String accessionListFile) {
		this.accessionListFile = accessionListFile;
	}
	public synchronized void setRefName(String refName) {
		this.refName = refName;
	}
	public synchronized void setGenomeFolder(String genomeFolder) {
		this.genomeFolder = genomeFolder;
	}
	public synchronized void setSdiLocation(String sdiLocation) {
		this.sdiLocation = sdiLocation;
	}
	public synchronized void setMinimumCoverage(int minimumCoverage) {
		this.minimumCoverage = minimumCoverage;
	}

	public SdiSnpToPed(String [] argv ){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number (Default 5)\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -g   the folder where the genome sequences and bw files are located\n");
        helpMessage.append("  -s   the folder where sdi files are located\n");
        helpMessage.append("  -d   the minimum coverage (Default 2)\n");
        
		Options options = new Options();
        options.addOption("t",true,"threadnumber");
        options.addOption("l",true,"accessionListFile");
        options.addOption("r",true,"refName");
        options.addOption("g",true,"genomeFolder");
        options.addOption("s",true,"sdiLocation");
        options.addOption("d",true,"minimumCoverage");
        
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
        if(cmd.hasOption("d")){
        	minimumCoverage = Integer.parseInt(cmd.getOptionValue("d"));
        }
        doIt();
	}
	
	public void doIt(){
		ChromoSomeReadImpl chromoSomeReadImpl = new ChromoSomeReadImpl(genomeFolder + File.separator + refName +  ".fa");
		ArrayList<String> accessionNames = new ArrayList<String>(2000);
		try {
            BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
            String tempString;
			while ((tempString = reader.readLine()) != null) {
				if( tempString.equalsIgnoreCase(refName) ){
					
				}else{
					accessionNames.add(tempString);
				}
			}
            reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		ArrayList<String> chrs = new ArrayList<String>(50);
		MarkerPostionsMap markerPostionsMap = new MarkerPostionsMap(50);
		for( String chrName : chromoSomeReadImpl.getChromoSomeHashMap().keySet() ){
			chrs.add(chrName);
			markerPostionsMap.put(chrName, new MarkerPostionS(2000000)); //0.5 M markers on each chromosome
		}
		Collections.sort(chrs);

		ExecutorService myExecutor1 = Executors.newFixedThreadPool(threadNumber);
		for(String accessionName : accessionNames){
			myExecutor1.execute( new SDIRead(sdiLocation, markerPostionsMap, accessionName, chromoSomeReadImpl));
		}
		myExecutor1.shutdown();
		try {
			myExecutor1.awaitTermination(200, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println( "SDI read over" );
		
		for( String key : markerPostionsMap.keySet() ){
            for( int position : markerPostionsMap.get(key).getMarkerPostions().keySet() ){
                char refNaChar = chromoSomeReadImpl.getChromoSomeById(key).getSequence().charAt(markerPostionsMap.get(key).getMarkerPostions().get(position).getPosition()-1);
                markerPostionsMap.get(key).getMarkerPostions().get(position).setColNaChar(refNaChar);
                markerPostionsMap.get(key).getMarkerPostions().get(position).getStates().add(refNaChar);
            }
		}

		SysHashMap<String, ArrayList<MarkerPostion>> markerPostionAs = new SysHashMap<String, ArrayList<MarkerPostion>>();
		// remove those position with more than two states begin
		for( String chr : markerPostionsMap.keySet() ){
			markerPostionAs.put(chr, new ArrayList<MarkerPostion>());
			for( int position : markerPostionsMap.get(chr).getMarkerPostions().keySet()){
				MarkerPostion markerPostion = markerPostionsMap.get(chr).getMarkerPostions().get(position);
				if( markerPostion.getStates().size() == 2 ){
					if( markerPostion.getColNaChar() == 'A' || markerPostion.getColNaChar() == 'T' || markerPostion.getColNaChar() == 'C' || markerPostion.getColNaChar() == 'G' &&
							markerPostion.getColNaChar() == 'a' || markerPostion.getColNaChar() == 't' || markerPostion.getColNaChar() == 'c' || markerPostion.getColNaChar() == 'g'){
						markerPostionAs.get(chr).add(markerPostionsMap.get(chr).getMarkerPostions().get(position));
					}
				}
			}
			if( markerPostionAs.get(chr).size() > 1 ){
				Collections.sort(markerPostionAs.get(chr));
			}
			System.out.println(chr + " size " + markerPostionAs.get(chr).size());
		}// remove those position with more than two states end

		SysOutPut pedOutPut = null;
        try {
            pedOutPut = new SysOutPut("snp.ped");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
		ExecutorService myExecutor = Executors.newFixedThreadPool(threadNumber);
		for(String accessionName : accessionNames){
			myExecutor.execute( new SdiSnpToPedMultipleThread( genomeFolder, accessionName, markerPostionAs, sdiLocation, pedOutPut, chrs, minimumCoverage));
		}
		myExecutor.shutdown();
		try {
			myExecutor.awaitTermination(2000, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("bam file checking end");

		pedOutPut.print("ref" + " " + "ref" + " 0 0 1	1");
		for( String chr : chrs ) {
			if (markerPostionAs.get(chr).size() > 0) {
				for (MarkerPostion markerPostion : markerPostionAs.get(chr)) {
					char theChar = markerPostion.getColNaChar();
					if (theChar == 'A') {
						pedOutPut.print("	A A");
					} else if (theChar == 'T') {
						pedOutPut.print("	T T");
					} else if (theChar == 'G') {
						pedOutPut.print("	G G");
					} else if (theChar == 'C') {
						pedOutPut.print("	C C");
					} else {
						pedOutPut.print("	0 0");
					}
				}
			}
		}
		pedOutPut.print("\n");
		pedOutPut.close();
        try {
            PrintWriter outPutIDMap = new PrintWriter("snp.map");
            for( String chr : chrs ) {
                if (markerPostionAs.get(chr).size() > 0) {
                    for (MarkerPostion markerPostion : markerPostionAs.get(chr)) {
                        String chrname = markerPostion.getChrName();
                        chrname = chrname.replace("Chr", "");
                        outPutIDMap.println(chrname + "\t" + chrname + "_" + markerPostion.getPosition() + "_" + markerPostion.getColNaChar() + "\t0\t" + markerPostion.getPosition());
                    }
                }
            }
            outPutIDMap.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}
}

class SdiSnpToPedMultipleThread extends Thread{
	private String folderLocation;
	private String accessionName;
	private SysHashMap<String, ArrayList<MarkerPostion>> markerPostionAs;
	private String sdiLocation;
	private SysOutPut pedOutPut;
	private ArrayList<String> chrs;
	private int minimumCoverage;
	public SdiSnpToPedMultipleThread(String folderLocation, String accessionName,
                                     SysHashMap<String, ArrayList<MarkerPostion>> markerPostionAs,
									 String sdiLocation, SysOutPut pedOutPut, ArrayList<String> chrs, int minimumCoverage){
		this.folderLocation = folderLocation;
		this.accessionName = accessionName;
		this.markerPostionAs = markerPostionAs;
		this.sdiLocation = sdiLocation;
		this.pedOutPut=pedOutPut;
		this.chrs=chrs;
		this.minimumCoverage=minimumCoverage;
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
			for( String chr : chrs ){
				if(markerPostionAs.get(chr).size()>0) {
                    System.out.println("running " + accessionName + " " + chr);
					for (MarkerPostion markerPostion : markerPostionAs.get(chr)) {
						HashSet<MapSingleRecord> mapSingleRecords = mapFileImpl.getOverLapRecordsByBasing(markerPostion.getChrName(), markerPostion.getPosition());
						if (mapSingleRecords.size() > 0) {
							for (MapSingleRecord mapSingleRecord : mapSingleRecords) {
								Contig result = wig.query(markerPostion.getChrName(), markerPostion.getPosition(), markerPostion.getPosition());
								double thisMean = result.mean();
								if (Double.isNaN(thisMean) || thisMean < minimumCoverage) {
                                    markerPostionHashMap.put(markerPostion, 'N'); // low coverage
								} else {
									if (mapSingleRecord.getBasement() == markerPostion.getPosition() && mapSingleRecord.getChanged() == 0 && mapSingleRecord.getOriginal().length() == 1 && !mapSingleRecord.getOriginal().contains("-") && mapSingleRecord.getResult().length() == 1) {
										markerPostionHashMap.put(markerPostion, mapSingleRecord.getResult().toUpperCase().charAt(0));
									} else if (mapSingleRecord.getBasement() <= markerPostion.getPosition() && mapSingleRecord.getChanged() > 0 && mapSingleRecord.getOriginal().contains("-")) {

									} else if (mapSingleRecord.getBasement() <= markerPostion.getPosition() && mapSingleRecord.getChanged() < 0 && (mapSingleRecord.getBasement() + Math.abs(mapSingleRecord.getChanged()) - 1) >= markerPostion.getPosition() && mapSingleRecord.getResult().contains("-")) {
										markerPostionHashMap.put(markerPostion, '-');
									} else if (mapSingleRecord.getBasement() <= markerPostion.getPosition() && (mapSingleRecord.getBasement() + mapSingleRecord.getOriginal().length()) > markerPostion.getPosition()) {
										markerPostionHashMap.put(markerPostion, 'N');
									}
								}
							}
						} else { // this is added after generating public result begin
							Contig result = wig.query(markerPostion.getChrName(), markerPostion.getPosition(), markerPostion.getPosition());
							double thisMean = result.mean();
							if (Double.isNaN(thisMean) || thisMean < minimumCoverage) {
                                markerPostionHashMap.put(markerPostion, 'N'); // low coverage
							}
						}// this is added after generating public result end
					}
                    System.out.println("done " + accessionName + " " + chr);
				}
			}
		} catch (WigFileFormatException | IOException | WigFileException e) {
			e.printStackTrace();
		}
        StringBuffer thisResult = new StringBuffer();
        thisResult.append(accessionName + " " + accessionName + " 0 0 1	1");
        for( String chr : chrs) {
            if(markerPostionAs.get(chr).size()>0) {
                for (MarkerPostion markerPostion : markerPostionAs.get(chr)) {
                    char theChar;
                    if (markerPostionHashMap.containsKey(markerPostion)) {
                        theChar = markerPostionHashMap.get(markerPostion);
                    } else {
                        theChar = markerPostion.getColNaChar();
                    }
                    if (theChar == 'A') {
                        thisResult.append("	A A");
                    } else if (theChar == 'T') {
                        thisResult.append("	T T");
                    } else if (theChar == 'G') {
                        thisResult.append("	G G");
                    } else if (theChar == 'C') {
                        thisResult.append("	C C");
                    } else {
                        thisResult.append("	0 0");
                    }
                }
            }
        }
        pedOutPut.println(thisResult.toString());
		System.out.println(accessionName + " finished");
	}
}

class MarkerPostionsMap{
	private SysHashMap<String, MarkerPostionS> markerPoMap;
	public MarkerPostionsMap(){
		markerPoMap =new SysHashMap<String, MarkerPostionS>();
	}
	public MarkerPostionsMap( int initialSize){
		markerPoMap =new SysHashMap<String, MarkerPostionS>(initialSize);
	}
	public synchronized MarkerPostionS get(String key) {
		return markerPoMap.get(key);
	}
	public synchronized Set<String> keySet() {
		return markerPoMap.keySet();
	}
	public synchronized boolean containsKey( String key ){
		return markerPoMap.containsKey(key);
	}
	public synchronized void put(String chrName, MarkerPostionS markerPostionS) {
		markerPoMap.put(chrName, markerPostionS);
	}
}
