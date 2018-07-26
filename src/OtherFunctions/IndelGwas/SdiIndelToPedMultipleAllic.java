package OtherFunctions.IndelGwas;

import me.songbx.util.MyThreadCount;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SdiIndelToPedMultipleAllic {

	private int threadNumber = 5;
	private String accessionListFile;
	private String sdiLocation;
	public SdiIndelToPedMultipleAllic(){

	}
	public synchronized void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	public synchronized void setAccessionListFile(String accessionListFile) {
		this.accessionListFile = accessionListFile;
	}
	public synchronized void setSdiLocation(String sdiLocation) {
		this.sdiLocation = sdiLocation;
	}

	public SdiIndelToPedMultipleAllic(String[] argv){
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
		HashMap<String, HashSet<Indel>> allIndels = new HashMap<String, HashSet<Indel>>(); // this String is chromosome name
		
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
	            			if( allIndels.containsKey(m2.group(1)) ){
	            				
	            			}else{
	            				allIndels.put(m2.group(1), new HashSet<Indel>());
	            			}
	            			allIndels.get(m2.group(1)).add(indel);
	            			accession.add(indel);
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
		System.out.println("sdi read end " + allIndels.size());
		
		
		HashMap<String, ArrayList<Indel>> allIndelArrayLists = new HashMap<String, ArrayList<Indel>>(); // hash to array for sorting purpose

		ArrayList<String> chrArrayList = new ArrayList<String>();
		for( String key : allIndels.keySet() ){
			if( allIndelArrayLists.containsKey(key) ){

			}else{
				chrArrayList.add(key);
				allIndelArrayLists.put(key, new ArrayList<Indel>());
			}
			allIndelArrayLists.get(key).addAll(allIndels.get(key) );
			Collections.sort(allIndelArrayLists.get(key));
		}

		for( String key : allIndelArrayLists.keySet() ){
			for( int i=0; i<allIndelArrayLists.get(key).size();i++ ){
				//System.out.println("i "+i);
				Indel indel = allIndelArrayLists.get(key).get(i);
				int end;
				if(indel.getLength() < 0){
					end = indel.getStart()+Math.abs(indel.getLength());
				}else{
					end = indel.getStart();
				}

				for( int j=i+1; j<allIndelArrayLists.get(key).size();j++ ){
					Indel indel2 = allIndelArrayLists.get(key).get(j);
					if(indel.overlap(indel2)){
						indel.getOverlapedIndles().add(indel2);
						indel2.getOverlapedIndles().add(indel);
					}else if( end < indel2.getStart() ){
						j=allIndelArrayLists.get(key).size();
						break;
					}
				}
			}
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

			PrintWriter outPut2 = new PrintWriter("./sdi_multi_allic_indel.map");
			PrintWriter outPut3 = new PrintWriter("./sdi_multi_allic_indel_own.map");
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
			outPut = new PrintWriter("./sdi_multi_allic_indel.ped");
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
		public synchronized void close(){
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
					HashMap<Indel, Integer> indel_int = new HashMap<Indel, Integer>();
					int indel_index = 3; // this indel start from 3, 1 is no INDEL, 2 is identical INDEL
					if(accession.getIndelsMap().containsKey(indel.getChrName())){
						if( accession.getIndelsMap().get(indel.getChrName()).contains(indel) ){
							content.append("  2 2"); // indeled
						}else if (indel.getLength()<0 ){// deletion
							int covered = 0;
							int code = 1; // default reference

							for (Indel oindel : indel.getOverlapedIndles()) {
								if( indel_int.containsKey(oindel) ){

								} else if (oindel.getLength()<0 ){ // only care about deletion here
									indel_int.put(oindel, indel_index);
									indel_index++;
								}

								if (accession.getIndelsMap().get(indel.getChrName()).contains(oindel)) {
									if( indel.be_covered(oindel) ){
										covered++;
									}else if ( oindel.be_covered(indel) ){
										// do not change the value of code
									}else if (oindel.getLength()<0 ){
										code = indel_int.get(oindel); // this solution is not very good,
										// but we do not have better way to do it
									}
								}
							}
							if (covered > 0) {
								content.append("  2 2"); //be covered
							} else {
								content.append("  " + code + " " + code); //be covered
							}
						}else{// insertion here
							int covered = 0;
							for ( Indel oindel : indel.getOverlapedIndles() ){
								if( accession.getIndelsMap().get(indel.getChrName()).contains(oindel) ){
									if ( indel.be_covered(oindel) ){
										covered++;
									}
								}
							}
							if( covered > 0 ){
								content.append("  2 2");
							} else {
								content.append("  1 1");
							}
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
		private HashSet<Indel> indels = new HashSet<Indel>();
		private HashMap<String, HashSet<Indel>> indelsMap = new HashMap<String, HashSet<Indel>>();
		public synchronized String getId() {
			return id;
		}
		public synchronized void setId(String id) {
			this.id = id;
		}
		public synchronized HashMap<String, HashSet<Indel>> getIndelsMap() {
			return indelsMap;
		}
		public synchronized void setIndelsMap(HashMap<String, HashSet<Indel>> indelsMap) {
			this.indelsMap = indelsMap;
		}
		public synchronized HashSet<Indel> getIndels() {
			return indels;
		}
		public synchronized void setIndels(HashSet<Indel> indels) {
			this.indels = indels;
		}
		public Accession(String id) {
			this.id = id;
		}
		public synchronized void add( Indel indel){
			indels.add(indel);
			if( indelsMap.keySet().contains(indel.getChrName()) ){
				
			}else{
				indelsMap.put(indel.getChrName(), new HashSet<Indel>());
			}
			indelsMap.get(indel.getChrName()).add(indel);
		}
	}
	
	class Indel implements Comparable<Indel>{
		private int start;
		private int length;
		private String chrName;
		private HashSet<Indel> overlapedIndles = new HashSet<Indel>();
		public synchronized  HashSet<Indel> getOverlapedIndles() {
			return overlapedIndles;
		}
		public synchronized  void setOverlapedIndles(HashSet<Indel> overlapedIndles) {
			this.overlapedIndles = overlapedIndles;
		}
		@Override
		public synchronized int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((chrName == null) ? 0 : chrName.hashCode());
			result = prime * result + length;
			result = prime * result + start;
			return result;
		}
		public synchronized boolean be_covered(Indel indel){
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
		public synchronized boolean overlap(Indel indel) {
			if( this.chrName.equals(indel.getChrName()) ){
				int start1 = indel.getStart();
				int end1;
				if( indel.getLength()>0 ){
					end1 = start1;
				}else{
					end1 = start - indel.getLength();
				}
				int start2 = this.getStart();
				int end2;
				if (this.getLength()>0) {
					end2 = start2;
				} else {
					end2 = start2 - this.getLength();
				}

				if ( start1 > end2  ){
					return false;
				} else if ( start2 > end1 ){
					return false;
				}else if ( (start1 >= start2 && start1 <= end2) || (end1 >= start2 && end1 <= end2)
						|| (start2 >= start1 && start2 <= end1) || (end2 >= start1 && end2 <= end1) ){
					return true;
				}else{
					return false;
				}
			}
			return false;
		}
		@Override
		public synchronized boolean equals(Object obj) {
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
		private synchronized SdiIndelToPedMultipleAllic getOuterType() {
			return SdiIndelToPedMultipleAllic.this;
		}
		public synchronized int getStart() {
			return start;
		}
		public synchronized void setStart(int start) {
			this.start = start;
		}
		public synchronized int getLength() {
			return length;
		}
		public synchronized void setLength(int length) {
			this.length = length;
		}
		public synchronized String getChrName() {
			return chrName;
		}
		public synchronized void setChrName(String chrName) {
			this.chrName = chrName;
		}
		public Indel(int start, int length, String chrName) {
			super();
			this.start = start;
			this.length = length;
			this.chrName = chrName;
		}
		@Override
		public synchronized int compareTo( Indel indel ) {
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
	}
}
