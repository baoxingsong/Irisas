package OtherFunctions.PopulationStructure.service;

import java.io.BufferedReader;
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
	private MarkerPostionS markerPostions1;
	private MarkerPostionS markerPostions2;
	private MarkerPostionS markerPostions3;
	private MarkerPostionS markerPostions4;
	private MarkerPostionS markerPostions5;
	private MyThreadCount threadCount;
	private String accessionName;
	private ChromoSomeReadImpl chromoSomeReadImpl;
	public SDIRead(String folderLocation, MarkerPostionS markerPostions1, MarkerPostionS markerPostions2, MarkerPostionS markerPostions3, MarkerPostionS markerPostions4, MarkerPostionS markerPostions5,
			MyThreadCount threadCount, String accessionName, ChromoSomeReadImpl chromoSomeReadImpl){
		this.folderLocation=folderLocation;
		this.markerPostions1=markerPostions1;
		this.markerPostions2=markerPostions2;
		this.markerPostions3=markerPostions3;
		this.markerPostions4=markerPostions4;
		this.markerPostions5=markerPostions5;
		this.threadCount=threadCount;
		this.accessionName=accessionName;
		this.chromoSomeReadImpl=chromoSomeReadImpl;
		
	}
	public void run( ){
		try {
			//System.out.println( ss );
			BufferedReader reader = new BufferedReader(new FileReader(folderLocation + "PA" + accessionName + ".sdi"));
			String tempString = null;
			Pattern p = Pattern.compile("(Chr\\d+)	(\\d+)	0	([ATCG])	([ATCG])");
			while ((tempString = reader.readLine()) != null) {
				Matcher m = p.matcher(tempString);
			//	System.out.println(tempString);
				if ( m.find() ){
					//System.out.println(tempString);
					String chrName = m.group(1);
					int position = Integer.parseInt(m.group(2));
					char refNaChar = chromoSomeReadImpl.getChromoSomeById(chrName).getSequence().charAt(position-1);
					MarkerPostion markerPostion = new MarkerPostion(chrName, position, refNaChar);
					if( chrName.equalsIgnoreCase("Chr1") ){
						if( 13700000<=position && position <=15900000 ){
							        
						}else{
							markerPostions1.add(markerPostion);
						}
					}else if( chrName.equalsIgnoreCase("Chr2") ){
						if(2450000<=position && position <=5500000 ){
					        
						}else{
							markerPostions2.add(markerPostion);
						}
					}else if( chrName.equalsIgnoreCase("Chr3") ){
						if(11300000<=position && position <=14300000 ){
					        
						}else{
							markerPostions3.add(markerPostion);
						}
					}else if( chrName.equalsIgnoreCase("Chr4") ){
						if(1800000<=position && position <=5150000 ){
					        
						}else{
							markerPostions4.add(markerPostion);
						}
					}else if( chrName.equalsIgnoreCase("Chr5") ){
						if(11000000<=position && position <=13350000 ){
					        
						}else{
							markerPostions5.add(markerPostion);
						}
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
		threadCount.countDown();
	}
}
