package OtherFunctions.IndelGwas.SNPgwas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.action.parallel.model.SysOutPut;
import me.songbx.impl.ChromoSomeReadImpl;
import me.songbx.util.MyThreadCount;
import OtherFunctions.PopulationStructure.Model.MarkerPostion;
import OtherFunctions.PopulationStructure.Model.MarkerPostionS;

public 
class SDIRead  extends Thread{
	private String folderLocation;
	private MarkerPostionsMap markerPostionsMap;
//	private MyThreadCount threadCount;
	private String accessionName;
	private ChromoSomeReadImpl chromoSomeReadImpl;
	public SDIRead(String folderLocation, MarkerPostionsMap markerPostionsMap,/*
			MyThreadCount threadCount, */String accessionName, ChromoSomeReadImpl chromoSomeReadImpl){
		this.folderLocation=folderLocation;
		this.markerPostionsMap=markerPostionsMap;
		//this.threadCount=threadCount;
		this.accessionName=accessionName;
		this.chromoSomeReadImpl=chromoSomeReadImpl;	
	}
	public void run( ){
		try {
			System.out.println("reading the sdi file of " + accessionName);
			BufferedReader reader = new BufferedReader(new FileReader(folderLocation + File.separator + accessionName + ".sdi"));
			String tempString;
//			Pattern p = Pattern.compile("^([ATCGatcg]$)"); // if there is an IUPAC code, we treat it as same with reference and ignore it
//			Matcher m;
			Character newAllelic;
			int position;
			while ((tempString = reader.readLine()) != null) {
				String[] thisLine = tempString.split("\\s");
				if( (thisLine[2].compareTo("0")==0) && (thisLine[3].length()==1) ) {
					//m = p.matcher(thisLine[4]);
					newAllelic = thisLine[4].charAt(0);
					if ( newAllelic=='A' || newAllelic=='T' || newAllelic=='C' || newAllelic=='G' || newAllelic=='a' || newAllelic=='t' || newAllelic=='c' || newAllelic=='g' ) {
						position = Integer.parseInt(thisLine[1]);
						if (chromoSomeReadImpl.getChromoSomeHashMap().containsKey(thisLine[0])) {
							if (markerPostionsMap.get(thisLine[0]).getMarkerPostions().containsKey(position)) {
								markerPostionsMap.get(thisLine[0]).getMarkerPostions().get(position).getStates().add(newAllelic);
							} else {
								MarkerPostion markerPostion = new MarkerPostion(thisLine[0], position);
								markerPostion.getStates().add(newAllelic);
								markerPostionsMap.get(thisLine[0]).put(position, markerPostion);
							}
							//System.out.println(tempString);
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(folderLocation + accessionName + ".sdi end");
		//threadCount.countDown();
	}
}
