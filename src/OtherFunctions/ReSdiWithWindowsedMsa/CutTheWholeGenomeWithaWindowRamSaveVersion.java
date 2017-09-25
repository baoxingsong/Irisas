package OtherFunctions.ReSdiWithWindowsedMsa;

import me.songbx.model.Strand;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.service.MapFileService;
import me.songbx.util.MyThreadCount;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CutTheWholeGenomeWithaWindowRamSaveVersion {
	
	
	private int threadNumber = 5;
	private int windowSize = 10000;
	private int overLapSize = 500;
	
	private String outPutPath;
	private String refName;
	private String accessionListFile;
	private String genomeFolder;
	public CutTheWholeGenomeWithaWindowRamSaveVersion( ){

	}
	public void setThreadNumber(int _threadNumber){
		this.threadNumber=_threadNumber;
	}
	public void setWindowSize(int _windowSize){
		this.windowSize=_windowSize;
	}
	public void setOverLapSize(int _overLapSize){
		this.overLapSize=_overLapSize;
	}
	public void setOutPutPath(String _outPutPath){
		this.outPutPath=_outPutPath;
	}
	public void setRefName(String _refName){
		this.refName=_refName;
	}
	public void setAccessionListFile(String _accessionListFile){
		this.accessionListFile=_accessionListFile;
	}
	public void setGenomeFolder(String _genomeFolder){
		this.genomeFolder=_genomeFolder;
	}
	private ArrayList<String> fastaFiles = new ArrayList<String>(); // key should be chr, and value should be the path of output fasta file for each window
	public ArrayList<String> getFastaFiles(){
		return fastaFiles;
	}
	private ArrayList<String> chrsList = new ArrayList<String>();
	public ArrayList<String> getChrsList(){
		return chrsList;
	}
	public CutTheWholeGenomeWithaWindowRamSaveVersion( String[] argv ){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number. (Default 5)\n");
        helpMessage.append("  -w   [integer] window size.  (Default 10000)\n");
        helpMessage.append("  -v   [integer] overlap size between two neighbour window (Default 500)\n");
        helpMessage.append("  -o   output folder\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -g   the folder where the genome sequences and sdi files are located\n");

		Options options = new Options();
        options.addOption("t",true,"threadnumber");
        options.addOption("w",true,"windowSize");
        options.addOption("v",true,"overLapSize");
        options.addOption("o",true,"outPutPath");
        options.addOption("r",true,"referenceName");
        options.addOption("l",true,"accessionListFile");
        options.addOption("g",true,"genomeFolder");
        
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
        if(cmd.hasOption("w")){
        	windowSize = Integer.parseInt(cmd.getOptionValue("w"));
        }
        if(cmd.hasOption("v")){
        	overLapSize = Integer.parseInt(cmd.getOptionValue("v"));
        }
        if(cmd.hasOption("o")){
        	outPutPath = cmd.getOptionValue("o");
        }else{
        	System.err.println("-o is missing. Please, check the parameters.");
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
        if(cmd.hasOption("l")){
        	accessionListFile = cmd.getOptionValue("l");
        }else{
        	System.err.println("-l is missing. Please, check the parameters.");
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
        runit();
	}
	
	public void runit(){
		String referenceGenomeFile = genomeFolder + File.separator + refName + ".fa";
		HashSet<String> names = new  HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				tempString=tempString.replaceAll("\\s", "");
				if( tempString.length() > 0 ){
					names.add(tempString);
				}
			}
			names.add(refName);
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		HashMap<Window, HashMap<String, Sequence>> windows = new HashMap<Window, HashMap<String, Sequence>>();
		ChromoSomeReadService refChromoSomeRead = new ChromoSomeReadService(referenceGenomeFile);
		for( String chrName: refChromoSomeRead.getChromoSomeHashMap().keySet() ){
			chrsList.add(chrName);
			int chrSize = refChromoSomeRead.getChromoSomeById(chrName).getSequence().length();
			int i = 1;
			while( i<chrSize  ){
				int start = i;
				int end = i + windowSize;
				Window window = new Window(start, end, chrName);
				windows.put(window, new HashMap<String, Sequence>() );
				i+=windowSize;
			}
		}
		
		for( Window window : windows.keySet() ){
			String chrName = window.getChr();
			String geneName = ""+window.getStart()+"_"+window.getEnd();
			try {
				File file1 = new File(outPutPath);
				if( file1.exists() && file1.isDirectory() ){

				}else{
					file1.mkdir();
				}
				File file = new File(outPutPath + File.separator+ chrName);
				if( file.exists() && file.isDirectory() ){
					
				}else{
					file.mkdir();
				}
				String outPutFile = outPutPath + File.separator+ chrName + File.separator + geneName;
				fastaFiles.add(outPutFile);
				PrintWriter outPutcds = new PrintWriter(outPutFile); // create new empty file or empty the exists file. Then could append content to this file
				
				outPutcds .close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		for( String name : names ){
//			System.err.println(" cutting" + name);
			String mapfileLocation = genomeFolder + File.separator + name + ".sdi";
			String targetchromeSomeReadFileLocation = genomeFolder + File.separator + name + ".fa";
			MapFileService mapfile = new MapFileService(mapfileLocation);
			ChromoSomeReadService targetchromoSomeRead = new ChromoSomeReadService(targetchromeSomeReadFileLocation);
			MyThreadCount threadCount = new MyThreadCount(0);
			
			for( Window window : windows.keySet() ){
				
				boolean isThisThreadUnrun=true;
				while(isThisThreadUnrun){
	                if(threadCount.getCount() < threadNumber){
	                	CutTheWholeGenomeParallel cutTheWholeGenomeParallel = new CutTheWholeGenomeParallel(window, mapfile, overLapSize, targetchromoSomeRead, name, outPutPath, threadCount, refChromoSomeRead);
	                    threadCount.plusOne();
	                	cutTheWholeGenomeParallel.start();
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
			while(threadCount.hasNext()){// wait for all the thread
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Window {
		private int start;
		private int end;
		private String chr;
		public Window( int start, int end, String chr){
			this.start=start;
			this.end=end;
			this.chr=chr;
		}
		@Override
		public synchronized int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((chr == null) ? 0 : chr.hashCode());
			result = prime * result + end;
			result = prime * result + start;
			return result;
		}
		@Override
		public synchronized boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Window other = (Window) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (chr == null) {
				if (other.chr != null)
					return false;
			} else if (!chr.equals(other.chr))
				return false;
			if (end != other.end)
				return false;
			if (start != other.start)
				return false;
			return true;
		}
		public synchronized int getStart() {
			return start;
		}
		public synchronized void setStart(int start) {
			this.start = start;
		}
		public synchronized int getEnd() {
			return end;
		}
		public synchronized void setEnd(int end) {
			this.end = end;
		}
		public synchronized String getChr() {
			return chr;
		}
		public synchronized void setChr(String chr) {
			this.chr = chr;
		}
		private synchronized  CutTheWholeGenomeWithaWindowRamSaveVersion getOuterType() {
			return CutTheWholeGenomeWithaWindowRamSaveVersion.this;
		}
	}
	class Sequence{
		int start;
		int end;
		String seq;
		public Sequence(int start, int end, String seq) {
			this.start = start;
			this.end = end;
			this.seq = seq;
		}
		public synchronized int getStart() {
			return start;
		}
		public synchronized void setStart(int start) {
			this.start = start;
		}
		public synchronized int getEnd() {
			return end;
		}
		public synchronized void setEnd(int end) {
			this.end = end;
		}
		public synchronized String getSeq() {
			return seq;
		}
		public synchronized void setSeq(String seq) {
			this.seq = seq;
		}
	}
	
	class CutTheWholeGenomeParallel extends Thread{
		private Window window;
		private MapFileService mapfile;
		private int overLapSize;
		private ChromoSomeReadService targetchromoSomeRead;
		private String name;
		private String outPutPath;
		private MyThreadCount threadCount;
		private ChromoSomeReadService refChromoSomeRead;
		public CutTheWholeGenomeParallel(Window window, MapFileService mapfile, int overLapSize, ChromoSomeReadService targetchromoSomeRead, String name,  String outPutPath, MyThreadCount threadCount, ChromoSomeReadService refChromoSomeRead){
			this.window = window;
			this.mapfile=mapfile;
			this.overLapSize = overLapSize;
			this.targetchromoSomeRead=targetchromoSomeRead;
			this.name=name;
			this.threadCount=threadCount;
			this.outPutPath=outPutPath;
			this.refChromoSomeRead=refChromoSomeRead;
		}
		
		public void run(){
			int start = window.getStart();
			int end = window.getEnd();
			String chrName = window.getChr();
			
			int liftStart = mapfile.getChangedFromBasement(chrName, start);
			liftStart =  liftStart-overLapSize;
			if(liftStart <= 0){
				liftStart = 1;
			}
			if(liftStart >= targetchromoSomeRead.getChromoSomeById(chrName).getSequence().length()){
				liftStart = targetchromoSomeRead.getChromoSomeById(chrName).getSequence().length();
			}
			
			
			int liftend = mapfile.getChangedFromBasement(chrName, end);
			liftend=liftend+overLapSize;
			if( liftend <= 0 ){
				liftend=1;
			}
			if(liftend >= targetchromoSomeRead.getChromoSomeById(chrName).getSequence().length()){
				liftend = targetchromoSomeRead.getChromoSomeById(chrName).getSequence().length();
			}
			
			//if this is the last window for reference, extend the lift end to the end of target sequence. so that no characters left
			if( end >= refChromoSomeRead.getChromoSomeById(chrName).getSequence().length() ){
				liftend = targetchromoSomeRead.getChromoSomeById(chrName).getSequence().length();
			}
			
			String se = targetchromoSomeRead.getSubSequence(chrName, liftStart, liftend, Strand.POSITIVE);
			
			String geneName = ""+window.getStart()+"_"+window.getEnd();
			try {
				PrintWriter outPutcds = new PrintWriter( new BufferedWriter(new FileWriter(outPutPath + File.separator + chrName + File.separator + geneName, true)));
				outPutcds.println(">"+name+"_"+liftStart+"_"+liftend);
				outPutcds.println(se.toUpperCase());
				outPutcds .close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			threadCount.countDown();
		}
	}
	
	class MyOutPut{
		public synchronized void myPrint(String filePath, String content){
				try {
					PrintWriter outPutcds = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
					outPutcds.print(content);
					outPutcds .close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
		}
	}
}
