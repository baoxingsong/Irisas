package OtherFunctions.IndelGwas.SNPgwas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.impl.ChromoSomeReadImpl;
import me.songbx.util.MyThreadCount;
import OtherFunctions.PopulationStructure.Model.MarkerPostion;
import OtherFunctions.PopulationStructure.Model.MarkerPostionS;

public 
class SDIRead  extends Thread{
	private String folderLocation;
	private MarkerPostionsMap markerPostionsMap;
	private MyThreadCount threadCount;
	private String accessionName;
	private ChromoSomeReadImpl chromoSomeReadImpl;
	public SDIRead(String folderLocation, MarkerPostionsMap markerPostionsMap,
			MyThreadCount threadCount, String accessionName, ChromoSomeReadImpl chromoSomeReadImpl){
		this.folderLocation=folderLocation;
		this.markerPostionsMap=markerPostionsMap;
		this.threadCount=threadCount;
		this.accessionName=accessionName;
		this.chromoSomeReadImpl=chromoSomeReadImpl;	
	}
	public void run( ){
		//System.out.println(folderLocation + accessionName + ".sdi begin");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(folderLocation + File.separator + accessionName + ".sdi"));
			String tempString = null;
			Pattern p = Pattern.compile("(\\S+)\\s+(\\d+)\\s+0\\s+([ATCGatcg])\\s+([ATCGatcg])"); // is there is an IUPAC code, we treat it as same with reference and ignore it
			while ((tempString = reader.readLine()) != null) {
			//	tempString=tempString.toUpperCase();
				Matcher m = p.matcher(tempString);
			//	System.out.println(tempString);
				if ( m.find() ){
					//System.out.println(accessionName + " " + tempString);
					//System.out.println(tempString);
					String chrName = m.group(1);
					int position = Integer.parseInt(m.group(2));
					if( chromoSomeReadImpl.getChromoSomeHashMap().containsKey(chrName) ){
//						char refNaChar = chromoSomeReadImpl.getChromoSomeById(chrName).getSequence().charAt(position-1);
						MarkerPostion markerPostion = new MarkerPostion(chrName, position);
						if( markerPostionsMap.containsKey(chrName) ){
							
						}else{
							markerPostionsMap.put(chrName, new MarkerPostionS());
						}
						markerPostionsMap.get(chrName).add(markerPostion);
					}
				}else{
					//System.out.println( tempString );
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(folderLocation + accessionName + ".sdi end");
		threadCount.countDown();
	}
}
