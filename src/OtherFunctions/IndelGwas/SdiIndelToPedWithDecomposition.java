package OtherFunctions.IndelGwas;

import me.songbx.util.MyThreadCount;
import org.apache.commons.cli.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SdiIndelToPedWithDecomposition {

	private int threadNumber = 5;
	private String accessionListFile;
	private String sdiLocation;
	public SdiIndelToPedWithDecomposition(){

	}
	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	public void setAccessionListFile(String accessionListFile) {
		this.accessionListFile = accessionListFile;
	}
	public void setSdiLocation(String sdiLocation) {
		this.sdiLocation = sdiLocation;
	}

	public SdiIndelToPedWithDecomposition(String[] argv){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number. (Default 5)\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -s   the folder where sdi files are located\n");
        
        Options options = new Options();
        options.addOption("t",true,"threadnumber");
        options.addOption("l",true,"accessionListFile");
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
        	System.err.println("Please, check the parameters.");
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
        doit();
	}
	
	public void doit(){

		HashMap<String, Accession> allAccessions = new HashMap<String, Accession>(); // accession name and accession object
		HashMap<String, HashSet<Indel>> allInsertations = new HashMap<String, HashSet<Indel>>(); // this String is chromosome name
		HashMap<String, HashSet<Indel>> allDeletions = new HashMap<String, HashSet<Indel>>(); // this String is chromosome name

		ArrayList<String> accessionNames = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				accessionNames.add(tempString);
			}
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// read all the sdi files and get all the possiable INDEL records
		for(String accessionId : accessionNames){
			try {
				Accession accession = new Accession(accessionId);
				BufferedReader reader = new BufferedReader(new FileReader(sdiLocation + File.separator + accessionId + ".sdi"));
	            String tempString = null;
				Pattern p2 = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+");
	            while ((tempString = reader.readLine()) != null) {
	            	Matcher m2 = p2.matcher(tempString);
	            	if(m2.find()){
	            		if( !(m2.group(3).equals("0") && m2.group(4).length() == 1) ){
	            			Indel indel = new Indel( Integer.parseInt(m2.group(2)), Integer.parseInt(m2.group(3)), m2.group(1) );
	            			if( allInsertations.containsKey(m2.group(1)) ){
	            				
	            			}else{
								allInsertations.put(m2.group(1), new HashSet<Indel>());
								allDeletions.put(m2.group(1), new HashSet<Indel>());
	            			}
	            			if( indel.getLength() > 0 ){
								allInsertations.get(m2.group(1)).add(indel);
								accession.addInsertation(indel);
							}else{
								allDeletions.get(m2.group(1)).add(indel);
								accession.addDeletion(indel); // since the original sdi file is sorted, so this arrayList data should be ordered already
							}
	            		}
	            	}
	            }
	            allAccessions.put(accessionId, accession);
	            reader.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		// genotype end
		System.out.println("sdi files reading end");
		ArrayList<String> chrArrayList = new ArrayList<String>();


		// deletion begin
		HashMap<String, ArrayList<Indel>> allDeletionArrayLists = new HashMap<String, ArrayList<Indel>>();
		for( String key : allDeletions.keySet() ){
			for( Indel indel : allDeletions.get(key) ){
				if( allDeletionArrayLists.containsKey(key) ){

				}else{
					chrArrayList.add(key);
					allDeletionArrayLists.put(key, new ArrayList<Indel>());
				}
				allDeletionArrayLists.get(key).add(indel);
			}
		}
		// decompose all the overlapped deletions in a region
		for( String key : allDeletions.keySet() ){
			Collections.sort(allDeletionArrayLists.get(key));
			boolean ifThereOverlap = true;
			while( ifThereOverlap ){
				ifThereOverlap = false;
				ArrayList<Indel> newIndelList =new ArrayList<Indel>();
				for( int indelIndex = 0; indelIndex < allDeletionArrayLists.get(key).size(); indelIndex++ ){
					ArrayList<Integer> allBreakPoints = new ArrayList<Integer>();
					int start1 = allDeletionArrayLists.get(key).get(indelIndex).getStart();
					int end1 = allDeletionArrayLists.get(key).get(indelIndex).getStart() - allDeletionArrayLists.get(key).get(indelIndex).getLength();
					int theCurrentLargestEnd = end1;
					allBreakPoints.add(start1);
					allBreakPoints.add(end1);
					for ( int indelIndexj = indelIndex+1; indelIndexj < allDeletionArrayLists.get(key).size(); indelIndexj++){
						int start2 = allDeletionArrayLists.get(key).get(indelIndexj).getStart();
						int end2 = allDeletionArrayLists.get(key).get(indelIndexj).getStart() - allDeletionArrayLists.get(key).get(indelIndex).getLength();
						if (start2 >= theCurrentLargestEnd) {  // could be equal here
							Collections.sort(allBreakPoints);
							for ( int breakpoint_index = 0 ; breakpoint_index < (allBreakPoints.size()-1); breakpoint_index++ ) {
								int newStart = allBreakPoints.get(breakpoint_index);
								int length = allBreakPoints.get(breakpoint_index) - allBreakPoints.get(breakpoint_index + 1);
								if( length != 0 ){
									Indel indel = new Indel(newStart, length, key);
									newIndelList.add(indel);
								}
							}
							indelIndex = indelIndexj - 1;
							break; // jump out of this for loop
						}else{
							ifThereOverlap = true;
							if( end2 > theCurrentLargestEnd ){
								theCurrentLargestEnd = end2;
							}
							allBreakPoints.add(start2);
							allBreakPoints.add(end2);
						}
					}
				}
				Collections.sort( newIndelList );
				allDeletionArrayLists.put(key, newIndelList);
			}

			//check overlap between the new deletion and old deletion begin
			for ( Indel del1 : allDeletions.get(key) ){ // this is the dataset before decompose
				int start1 = del1.getStart();
				int end1 = start1 - del1.getLength();
				for ( Indel del2 : allDeletionArrayLists.get(key) ){ // this is the dataset with decompose
					int start2 = del2.getStart();
					if ( start2 > end1 ){
						break;
					}else if(  (start1 <= start2 && start2 < end1) ) { // del2 should be a sub of del1
						del2.getOverlapedIndles().add(del1);
					}
				}
			}
			//check overlap end
		}
		// deletion end



		// insertion begin
		HashMap<String, ArrayList<Indel>> allInsertionArrayLists = new HashMap<String, ArrayList<Indel>>();
		for( String key : allInsertations.keySet() ){
			for( Indel indel : allInsertations.get(key) ){
				if( allInsertionArrayLists.containsKey(key) ){

				}else{
					allInsertionArrayLists.put(key, new ArrayList<Indel>());
				}
				allInsertionArrayLists.get(key).add(indel);
			}
		}
		// decompose all the overlapped INDELs in a region
		for( String key : allDeletions.keySet() ){
			Collections.sort(allInsertionArrayLists.get(key));
//			boolean ifThereOverlap = true;
//			while( ifThereOverlap ){
//				ifThereOverlap = false;
//				ArrayList<Indel> newIndelList =new ArrayList<Indel>();
//				for( int indelIndex = 0; indelIndex < allInsertionArrayLists.get(key).size(); indelIndex++ ){
//					ArrayList<Integer> allBreakPoints = new ArrayList<Integer>();
//					int start1 = allInsertionArrayLists.get(key).get(indelIndex).getStart();
//					int end1 = allInsertionArrayLists.get(key).get(indelIndex).getStart() + Math.abs(allInsertionArrayLists.get(key).get(indelIndex).getLength());
//					int theCurrentLargestEnd = end1;
//					allBreakPoints.add(start1);
//					allBreakPoints.add(end1);
//					for ( int indelIndexj = indelIndex+1; indelIndexj < allInsertionArrayLists.get(key).size(); indelIndexj++){
//						int start2 = allInsertionArrayLists.get(key).get(indelIndexj).getStart();
//						int end2 = allInsertionArrayLists.get(key).get(indelIndexj).getStart() + Math.abs(allInsertionArrayLists.get(key).get(indelIndex).getLength());
//						if (start2 == start1){
//							ifThereOverlap = true;
//							if( end2 > theCurrentLargestEnd ){
//								theCurrentLargestEnd = end2;
//							}
//							allBreakPoints.add(start2);
//							allBreakPoints.add(end2);
//						}
//
//					}
//				}
//				Collections.sort( newIndelList );
//				allDeletionArrayLists.put(key, newIndelList);
//			}
		}
		// insertion end




		HashMap<String, ArrayList<Indel>> allIndelArrayLists = new HashMap<String, ArrayList<Indel>>();

		for( String key : allDeletionArrayLists.keySet() ){
			allIndelArrayLists.put(key, new ArrayList<Indel>());
			for( Indel indel : allDeletionArrayLists.get(key) ){
				allIndelArrayLists.get(key).add(indel);
			}
			for( Indel indel : allInsertionArrayLists.get(key) ){
				allIndelArrayLists.get(key).add(indel);
			}
			Collections.sort(allIndelArrayLists.get(key));
		}

		System.out.println("begin to output");
		try {
			PedOutPut pedOutPut = new PedOutPut();

			MyThreadCount threadCount = new MyThreadCount(0);
			for(String accessionName : accessionNames){
				Accession accession = allAccessions.get(accessionName);
				boolean isThisThreadUnrun=true;
				while(isThisThreadUnrun){
					if(threadCount.getCount() < threadNumber){
						threadCount.plusOne();
						OrginizeThisAccession main = new OrginizeThisAccession(accession, allIndelArrayLists, pedOutPut, chrArrayList, threadCount);
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
			}
			while(threadCount.hasNext()){// wait for all the thread
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			pedOutPut.print("ref" + " " + "ref" + " 0 0 1	1");
			for( String key : chrArrayList ){
				for( Indel indel : allIndelArrayLists.get(key) ){
					pedOutPut.print("  1 1"); // not indeled
				}
			}
			pedOutPut.println();
			PrintWriter outPut2 = new PrintWriter("./indel.map");
			PrintWriter outPut3 = new PrintWriter("./indel_own.map");
			outPut3.println("chrName\tindelid\tstart\tlength");
			for( String key : chrArrayList ){
				for( Indel indel : allIndelArrayLists.get(key) ){
					String chrName = indel.getChrName();
					chrName = chrName.replace("Chr", "");
					outPut2.println(chrName + "\tindel_"+indel.getChrName()+"_" + indel.getStart()+"_"+indel.getLength() + "\t0\t" + indel.getStart());
					outPut3.println(chrName + "\tindel_"+indel.getChrName()+"_" + indel.getStart()+"_"+indel.getLength() + "\t" + indel.getStart()+"\t"+indel.getLength());
				}
			}
			outPut2.close();
			outPut3.close();
			pedOutPut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	class PedOutPut{
		private PrintWriter outPut;
		public PedOutPut() throws FileNotFoundException{
			outPut = new PrintWriter("./indel.ped");
		}
		public synchronized void print( String content){
			outPut.print(content); //
		}
		public synchronized void println( String content){
			outPut.println(content); //
		}
		public synchronized void println( ){
			outPut.println(); //
		}
		public void close(){
			outPut.close();
		}
	}
	
	class OrginizeThisAccession extends Thread{
		private Accession accession;
		private HashMap<String, ArrayList<Indel>> allIndelArrayLists;
		private PedOutPut pedOutPut;
		private MyThreadCount threadCount;
		private ArrayList<String> chrArrayList;
		public OrginizeThisAccession(Accession accession, HashMap<String, ArrayList<Indel>> allIndelArrayLists, PedOutPut pedOutPut, ArrayList<String> chrArrayList, MyThreadCount threadCount ){
			this.accession=accession;
			this.allIndelArrayLists=allIndelArrayLists;
			this.pedOutPut=pedOutPut;
			this.threadCount=threadCount;
			this.chrArrayList=chrArrayList;
		}
		public void run( ){
			StringBuffer content = new StringBuffer();
			String accessionName = accession.getId();
			System.out.println(accessionName + " begin");
			content.append(accessionName + "  " + accessionName + " 0 0 1	1");
			for( String key : chrArrayList ){
				for( Indel indel : allIndelArrayLists.get(key) ){
					if(accession.getDeletionsMap().containsKey(key)){ // the deletionsMap and insertionMap contain same chromosome set
						if( indel.getLength() < 0 ){
							int code = 1; // no indel
							for (Indel ovIndel: indel.getOverlapedIndles()){
								if( accession.getDeletionsMap().get(key).contains(ovIndel) ){
									content.append("  2 2"); // indeled
								}
							}
							content.append("  " + code + " " + code); // not indeled
						}else{// insertations
							int code = 1; // no indel
							for ( Indel i : accession.getInsertationsMap().get(key) ){
								if ( i.getStart() == indel.getStart() ){
									code = 2; // indel
								}else if( i.getStart() > indel.getStart() ){
									break;
								}
							}
							content.append("  " + code + " " + code); // not indeled
						}
					}else{
						content.append("  1 1"); // not indeled
					}
				}
			}
			System.out.println(accessionName + " end");
			pedOutPut.println(content.toString());
			threadCount.countDown();
		}
	}
	
	class Accession{
		private String id;
		// put insertion and deletion into different hash, this would save computational time
		private HashMap<String, HashSet<Indel>> deletionsMap = new HashMap<String, HashSet<Indel>>();
		private HashMap<String, HashSet<Indel>> insertationsMap = new HashMap<String, HashSet<Indel>>();

		public Accession(String id) {
			this.id = id;
		}
		public void addInsertation( Indel indel){
			if( insertationsMap.keySet().contains(indel.getChrName()) ){
				
			}else{
				insertationsMap.put(indel.getChrName(), new HashSet<Indel>());
			}
			insertationsMap.get(indel.getChrName()).add(indel);
		}
		public void addDeletion( Indel indel){
			if( deletionsMap.keySet().contains(indel.getChrName()) ){

			}else{
				deletionsMap.put(indel.getChrName(), new HashSet<Indel>());
			}
			deletionsMap.get(indel.getChrName()).add(indel);
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public HashMap<String, HashSet<Indel>> getInsertationsMap() {
			return insertationsMap;
		}
		public void setInsertationsMap(HashMap<String, HashSet<Indel>> insertationsMap) {
			this.insertationsMap = insertationsMap;
		}
		public HashMap<String, HashSet<Indel>> getDeletionsMap() {
			return deletionsMap;
		}
		public void setDeletionsMap(HashMap<String, HashSet<Indel>> deletionsMap) {
			this.deletionsMap = deletionsMap;
		}
	}
	
	class Indel implements Comparable<Indel>{
		private int start;
		private int length;
		private String chrName;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((chrName == null) ? 0 : chrName.hashCode());
			result = prime * result + length;
			result = prime * result + start;
			return result;
		}
		public boolean be_covered(Indel indel){
			if( indel.getLength()<0 && this.getLength()<0 ){
				if( (indel.getStart() <= this.getStart()) && ( (indel.getStart()+Math.abs(indel.getLength()))>= (this.getStart()+Math.abs(this.getLength())) ) ){
					return true;
				}else{
					return false;
				}
			} else if ( indel.getLength()>0 && this.getLength()>0 ){
				if( indel.getStart() == this.getStart() && indel.getLength()>this.getLength() ){
					return true;
				}else{
					return false;
				}
			} else {
				return false;
			}
		}
		public boolean overlap(Indel indel) {
			if( this.chrName.equals(indel.getChrName()) ){
				int start1 = indel.getStart();
				int start2 = this.getStart();
				if( start1 == start2 ){ // for insertion, only with same start, they are overlap with each other
					return true;
				}else if( indel.getLength()<0 && this.getLength()<0 ){
					int end1 = indel.getStart() + Math.abs(indel.getLength());
					int end2 = this.getStart() + Math.abs(this.getLength());
					if( start1 > start2 && start1 >end2 ){
						return false;
					}else if( start2 > start1 && start2 >end1 ){
						
					} else if( (start1 <= start2 && start2 <= end1) || (start1 <= end2 && end2 <= end1) || (start2 <= start1 && start1 <= end2) || (start2 <= end1 && end1 <= end2) ){
						return true;
					}
				}else{
					return false;
				}
			}
			return false;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Indel other = (Indel) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (chrName == null) {
				if (other.chrName != null)
					return false;
			} else if (!chrName.equals(other.chrName))
				return false;
			if (length != other.length)
				return false;
			if (start != other.start)
				return false;
			return true;
		}
		private SdiIndelToPedWithDecomposition getOuterType() {
			return SdiIndelToPedWithDecomposition.this;
		}
		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public String getChrName() {
			return chrName;
		}
		public void setChrName(String chrName) {
			this.chrName = chrName;
		}
		public Indel(int start, int length, String chrName) {
			super();
			this.start = start;
			this.length = length;
			this.chrName = chrName;
		}
		@Override
		public int compareTo( Indel indel ) {			
			String thisChrName = chrName;
			String indelChrName = indel.getChrName();
//			thisChrName=thisChrName.replaceAll("Chr", "");
//			indelChrName=indelChrName.replaceAll("Chr", "");
//			int thisChrId = Integer.parseInt(thisChrName);
//			int indelChrId = Integer.parseInt(indelChrName);
/*			if( thisChrId!= indelChrId ){
				return thisChrId - indelChrId;
			}else if( this.getStart() != indel.getStart()){
				return this.getStart() - indel.getStart();
			}*/
			if( thisChrName.equalsIgnoreCase(indelChrName) ){
				return this.getStart() - indel.getStart();
			}else if( this.getStart() != indel.getStart()){
				return thisChrName.compareToIgnoreCase(indelChrName);
			}
			return 0;
		}
		private HashSet<Indel> overlapedIndles = new HashSet<Indel>();
		public HashSet<Indel> getOverlapedIndles() {
			return overlapedIndles;
		}
		public void setOverlapedIndles(HashSet<Indel> overlapedIndles) {
			this.overlapedIndles = overlapedIndles;
		}
	}
}



