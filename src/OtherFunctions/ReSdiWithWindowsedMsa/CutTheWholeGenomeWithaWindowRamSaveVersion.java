package OtherFunctions.ReSdiWithWindowsedMsa;

import me.songbx.model.Strand;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.service.ChromoSomeReadServiceWithIndex;
import me.songbx.service.MapFileService;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CutTheWholeGenomeWithaWindowRamSaveVersion {
	
	private int poolSize = 5;
	private int maxMumFileToOpen = 30000;
	private int threadNumber = 5;
	private int windowSize = 10000;
	private int overLapSize = 500;
	
	private String outPutPath;
	private String refName;
	private String accessionListFile;
	private String genomeFolder;
	private boolean append = false;
	private String postFix;
	public CutTheWholeGenomeWithaWindowRamSaveVersion( ){

	}
	public synchronized void setThreadNumber(int _threadNumber){
		this.threadNumber=_threadNumber;
	}
	public synchronized void setWindowSize(int _windowSize){
		this.windowSize=_windowSize;
	}
	public synchronized void setOverLapSize(int _overLapSize){
		this.overLapSize=_overLapSize;
	}
	public synchronized void setOutPutPath(String _outPutPath){
		this.outPutPath=_outPutPath;
	}
	public synchronized void setRefName(String _refName){
		this.refName=_refName;
	}
	public synchronized void setAccessionListFile(String _accessionListFile){
		this.accessionListFile=_accessionListFile;
	}
	public synchronized void setGenomeFolder(String _genomeFolder){
		this.genomeFolder=_genomeFolder;
	}
	private ArrayList<String> fastaFiles = new ArrayList<String>(); // key should be chr, and value should be the path of output fasta file for each window
	public synchronized ArrayList<String> getFastaFiles(){
		return fastaFiles;
	}
	private ArrayList<String> chrsList = new ArrayList<String>();
	public synchronized ArrayList<String> getChrsList(){
		return chrsList;
	}
	public CutTheWholeGenomeWithaWindowRamSaveVersion( String[] argv ){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number. (Default 5)\n");
		helpMessage.append("  -p   [integer] pool size (number of accessions read into RAM, the larger the faster). (Default 5)\n");
        helpMessage.append("  -w   [integer] window size.  (Default 10000)\n");
        helpMessage.append("  -v   [integer] extend size between two neighbour windows (Default 500)\n");
        helpMessage.append("  -o   output folder\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -g   the folder where the genome sequences and sdi files are located\n");
		helpMessage.append("  -append 	append the sequence set to existing files\n");
		helpMessage.append("  -postfix 	if append is true, the postfix out existing files (.mafft for the result of easy pipeline)\n");

		Options options = new Options();
        options.addOption("t",true,"threadnumber");
		options.addOption("p",true,"poolSize");
        options.addOption("w",true,"windowSize");
        options.addOption("v",true,"overLapSize");
        options.addOption("o",true,"outPutPath");
        options.addOption("r",true,"referenceName");
        options.addOption("l",true,"accessionListFile");
        options.addOption("g",true,"genomeFolder");
		options.addOption("append",false,"genomeFolder");
		options.addOption("postfix",true,"genomeFolder");
        
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
		if(cmd.hasOption("p")){
			poolSize = Integer.parseInt(cmd.getOptionValue("p"));
		}
        if( cmd.hasOption("append") ){
        	append = true;
        	if( cmd.hasOption("postfix") ){
				postFix=cmd.getOptionValue("postfix");
			}else{
				postFix="";
			}
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
		String[] cmdArray = {"sh", "-c", "ulimit -n 32224"}; // change the maximum number of opening files
		try {
			Runtime.getRuntime().exec(cmdArray);
		}catch ( IOException e ){
			e.printStackTrace();
		}

		String referenceGenomeFile = genomeFolder + File.separator + refName + ".fa";
		HashSet<String> names = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				tempString=tempString.replaceAll("\\s", "");
				if( tempString.length() > 0 ){
					names.add(tempString);
				}
			}
			if( append ){

			} else {
				names.add(refName);
			}
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ArrayList<Window> windows = new ArrayList<Window>();
		ChromoSomeReadService refChromoSomeRead = new ChromoSomeReadService(referenceGenomeFile);
		for( String chrName: refChromoSomeRead.getChromoSomeHashMap().keySet() ){
			chrsList.add(chrName);
			int chrSize = refChromoSomeRead.getChromoSomeById(chrName).getSequence().length();
			int i = 1;
			while( i<chrSize  ){
				int start = i;
				int end = i + windowSize;
				Window window = new Window(start, end, chrName);
				windows.add(window);
				i+=windowSize;
			}
		}

		for( Window window : windows ){
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

				if( append ){

				}else {
					PrintWriter outPutcds = new PrintWriter(outPutFile); // create new empty file or empty the exists file. Then could append content to this file
					outPutcds.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		ArrayList<Window> windows2 = new ArrayList<Window>(); // because we could not open as much file as we want, so we deal with windows batch by hatch
		int indexI=0;
		for( Window window : windows ){
			indexI++;
			windows2.add(window);
			if( indexI%maxMumFileToOpen ==0 ){
				runit2( names, refChromoSomeRead, windows2);
				windows2.clear();
			}
		}
		if( windows2.size()>0 ) {
			runit2(names, refChromoSomeRead, windows2);
			windows2.clear();
		}
	}

	public void runit2(HashSet<String> names, ChromoSomeReadService refChromoSomeRead, ArrayList<Window> windows2){
		HashMap<Window, MyWriter> bufferedWriters = new HashMap<Window, MyWriter>();
		MyThreadSafeHashMap<Window, StringBuffer> stringBufferMap = new MyThreadSafeHashMap<Window, StringBuffer>();

		ArrayList<String> names2 = new ArrayList<String>();
		for( Window window : windows2 ) {
			String chrName = window.getChr();
			String geneName = ""+window.getStart()+"_"+window.getEnd();
			if( append ){
				geneName += postFix;
			}
			MyWriter outPutcds = new MyWriter(outPutPath + File.separator + chrName + File.separator + geneName);
			bufferedWriters.put(window, outPutcds);
//			stringBufferMap.put(window, new StringBuffer());
		}

		int index = 0;
		MyThreadSafeHashMap<String, ChromoSomeReadServiceWithIndex> genomesMap = new MyThreadSafeHashMap<String, ChromoSomeReadServiceWithIndex>();
		MyThreadSafeHashMap<String, MapFileService> sdisMap = new MyThreadSafeHashMap<String, MapFileService>();


		for( String name1 : names ){
			++index;
			names2.add(name1);
			if( index % poolSize == 0 ) {
				ExecutorService myExecutor = Executors.newFixedThreadPool(threadNumber);
				for( String name : names2 ) {
					String mapfileLocation = genomeFolder + File.separator + name + ".sdi";
					String targetchromeSomeReadFileLocation = genomeFolder + File.separator + name + ".fa";
					myExecutor.execute(new readGenomeAndSdis(genomesMap, sdisMap, name, targetchromeSomeReadFileLocation, mapfileLocation));
				}
				myExecutor.shutdown();
				try {
					myExecutor.awaitTermination(200, TimeUnit.HOURS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				ExecutorService myExecutor2 = Executors.newFixedThreadPool(threadNumber);
				for (Window window : windows2) {
					for( String name : names2 ) {
						myExecutor2.execute(new CutTheWholeGenomeParallel(window, genomesMap,
								sdisMap, stringBufferMap, overLapSize, name,
								refChromoSomeRead, bufferedWriters));
					}
				}
				//int runingThreads = ((ThreadPoolExecutor) myExecutor2).getActiveCount();
				//System.out.println(runingThreads + " threads is runing. With maximum threads " + threadNumber);
				myExecutor2.shutdown();
				try {
					myExecutor2.awaitTermination(2000, TimeUnit.HOURS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				names2.clear();
				genomesMap.clear();
				sdisMap.clear();
				System.gc();
			}
		}

		ExecutorService myExecutor = Executors.newFixedThreadPool(threadNumber);
		for( String name : names2 ) {
			String mapfileLocation = genomeFolder + File.separator + name + ".sdi";
			String targetchromeSomeReadFileLocation = genomeFolder + File.separator + name + ".fa";
			myExecutor.execute(new readGenomeAndSdis(genomesMap, sdisMap, name, targetchromeSomeReadFileLocation, mapfileLocation));
		}
		myExecutor.shutdown();
		try {
			myExecutor.awaitTermination(200, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ExecutorService myExecutor2 = Executors.newFixedThreadPool(threadNumber);
		for (Window window : windows2) {
			for( String name : names2 ) {
				myExecutor2.execute(new CutTheWholeGenomeParallel(window, genomesMap,
						sdisMap, stringBufferMap, overLapSize, name,
						refChromoSomeRead, bufferedWriters));
			}
		}
//			int runingThreads = ((ThreadPoolExecutor) myExecutor2).getActiveCount();
//			System.out.println(runingThreads + " threads is runing. With maximum threads " + threadNumber);
		myExecutor2.shutdown();
		try {
			myExecutor2.awaitTermination(2000, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		names2.clear();
		genomesMap.clear();
		sdisMap.clear();

		for( Window window : windows2 ) {
			//try{
				//bufferedWriters.get(window).flush();
				bufferedWriters.get(window).close();
//			}catch ( IOException e ){
//				e.printStackTrace();
//			}
		}
		bufferedWriters.clear();
		System.gc();
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
		private synchronized CutTheWholeGenomeWithaWindowRamSaveVersion getOuterType() {
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
		private MyThreadSafeHashMap<String, ChromoSomeReadServiceWithIndex> genomesMap;
		private MyThreadSafeHashMap<String, MapFileService> sdisMap;
		private MyThreadSafeHashMap<Window, StringBuffer> stringBufferMap;
		private int overLapSize;
		private String name;
		private ChromoSomeReadService refChromoSomeRead;
		private HashMap<Window, MyWriter> bufferedWriters;
		public CutTheWholeGenomeParallel(Window window, MyThreadSafeHashMap<String, ChromoSomeReadServiceWithIndex> genomesMap,
										 MyThreadSafeHashMap<String, MapFileService> sdisMap,
										 MyThreadSafeHashMap<Window, StringBuffer> stringBufferMap, int overLapSize, String name,
										 ChromoSomeReadService refChromoSomeRead, HashMap<Window, MyWriter> bufferedWriters ){
			this.window = window;
			this.genomesMap=genomesMap;
			this.sdisMap=sdisMap;
			this.stringBufferMap=stringBufferMap;
			this.overLapSize = overLapSize;
			this.name=name;
			this.refChromoSomeRead=refChromoSomeRead;
			this.bufferedWriters=bufferedWriters;
		}
		
		public void run(){
			int start = window.getStart();
			int end = window.getEnd();
			String chrName = window.getChr();

			int liftStart = sdisMap.get(name).getChangedFromBasement(chrName, start);
			liftStart =  liftStart-overLapSize;
			if(liftStart <= 0){
				liftStart = 1;
			}
			/*
			if(liftStart >= targetchromoSomeRead.getFastaIndexEntryImpl().getEntries().get(chrName).getLength()){
				liftStart = targetchromoSomeRead.getFastaIndexEntryImpl().getEntries().get(chrName).getLength();
			}*/
			if(liftStart >= genomesMap.get(name).getFastaIndexEntryImpl().getEntries().get(chrName).getLength() ){//.getFastaIndexEntryImpl().getEntries().get(chrName).getLength()){
				liftStart = genomesMap.get(name).getFastaIndexEntryImpl().getEntries().get(chrName).getLength();
			}

			int liftend = sdisMap.get(name).getChangedFromBasement(chrName, end);
			liftend=liftend+overLapSize;
			if( liftend <= 0 ){
				liftend=1;
			}
			if(liftend >= genomesMap.get(name).getFastaIndexEntryImpl().getEntries().get(chrName).getLength() ){
				liftend = genomesMap.get(name).getFastaIndexEntryImpl().getEntries().get(chrName).getLength();
			}
			
			//if this is the last window for reference, extend the lift end to the end of target sequence. so that no characters left
			if( end >= refChromoSomeRead.getChromoSomeById(chrName).getSequence().length() ){
				liftend = genomesMap.get(name).getFastaIndexEntryImpl().getEntries().get(chrName).getLength();
			}
			
			String se = genomesMap.get(name).getSubSequence(chrName, liftStart, liftend, Strand.POSITIVE);

			//try {
				/*stringBufferMap.get(window).append(">"+name+"_"+liftStart+"_"+liftend+"\n"+se.toUpperCase()+"\n");
				if(stringBufferMap.get(window).length()>100000){ //0.1M
					bufferedWriters.get(window).write(stringBufferMap.get(window).toString());
					bufferedWriters.get(window).flush();
					stringBufferMap.get(window).delete(0, stringBufferMap.get(window).length()); // clean the stringbuffer
				}*/
				bufferedWriters.get(window).write(">"+name+"_"+liftStart+"_"+liftend+"\n"+se.toUpperCase()+"\n");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			threadCount.countDown();
		}
	}

	class MyThreadSafeHashMap <V, T> {
		private HashMap<V, T> map = new HashMap<V, T>();
		public synchronized void put (V v, T t){
			map.put(v, t);
		}
		public synchronized T get( V v){
			return map.get(v);
		}
		public synchronized void clear(){
			map.clear();
		}
	}
	class readGenomeAndSdis extends Thread{
		private MyThreadSafeHashMap<String, ChromoSomeReadServiceWithIndex> genomesMap;
		private MyThreadSafeHashMap<String, MapFileService> sdisMap;
		private String name;
		private String genomePath;
		private String sdiPath;
		public readGenomeAndSdis(MyThreadSafeHashMap<String, ChromoSomeReadServiceWithIndex> genomesMap,
				MyThreadSafeHashMap<String, MapFileService> sdisMap,
				String name, String genomePath, String sdiPath){
			this.genomesMap = genomesMap;
			this.sdisMap=sdisMap;
			this.name = name;
			this.genomePath=genomePath;
			this.sdiPath=sdiPath;
		}

		public void run(){
			MapFileService mapfile = new MapFileService(sdiPath);
			ChromoSomeReadServiceWithIndex targetchromoSomeRead = new ChromoSomeReadServiceWithIndex(genomePath);
			genomesMap.put(name, targetchromoSomeRead);
			sdisMap.put(name, mapfile);
		}
	}

	class MyWriter{
		private BufferedWriter outPutcds;
		public MyWriter( String filePath ) {
			try {
				 this.outPutcds = new BufferedWriter(new FileWriter(filePath, true), 100000 /*buffersize 0.1M*/);
			}catch ( IOException e){
				e.printStackTrace();
			}
		}
		public synchronized void write( String content){
			try {
				outPutcds.write(content);
			}catch ( IOException e){
				e.printStackTrace();
			}
		}
		public void close(){
			try {
				outPutcds.close();
			}catch ( IOException e){
				e.printStackTrace();
			}
		}
	}
}
