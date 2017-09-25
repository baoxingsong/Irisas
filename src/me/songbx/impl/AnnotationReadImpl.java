package me.songbx.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import me.songbx.model.Strand;
import me.songbx.model.CDS.Cds;
import me.songbx.model.Transcript.Transcript;

/**
 * all the collector of transcripts are unique
 * @author song
 * @version 1.0, 2014-07-09
 * @known bugs, if there are more than one alternative splice, but the CDS structures are same, only the later one would be kept. e.g. AT3G50950
 * @have no plan to fix it, it is useful for gene structure annotation liftover and organization
 */

public class AnnotationReadImpl {

	/**
	 * The index are the names of transcripts, the values are the transcripts
	 */
	private HashMap<String, Transcript> transcriptHashMap = new HashMap<String, Transcript>();
	private HashMap<String, HashSet<Transcript>> transcriptHashSet = new HashMap<String, HashSet<Transcript>>();
	
	/**
	 * @param fileLocation
	 * @param chromoSomeRead
	 */
	
	public AnnotationReadImpl(String fileLocation) {
		File file = new File(fileLocation);
		BufferedReader reader = null;
        try {
        	reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            Pattern p = Pattern.compile("^(\\S*)\t(.*)\tCDS\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(.*)Parent(.*?)[;,].*$");
			Pattern p1 = Pattern.compile("^(\\S*)\t(.*)\tCDS\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(.*)Parent(.*)$");//rice
			Pattern p2 = Pattern.compile("^(\\S*)\\s*(\\S*)\\s*CDS\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)PARENT=(\\S*)$");// for the annotation of Brassica rapa
			Pattern p3 = Pattern.compile("^(\\S*)\\s*(\\S*)\\s*CDS\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)gene_id \"ID=(\\S*)\"\\;.*$");// fot the annotation of fshgene http://mustang.biol.mcgill.ca:8885/download/S.irio/si_fgenesh.gtf
			Pattern p4 = Pattern.compile("^(\\S*)\t(.*)\tCDS\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(.*)transcript_id\\s*\"(\\w+?)\";");
			
			while ((tempString = reader.readLine()) != null) {
            	Matcher m=p.matcher(tempString);
				Matcher m_1=p1.matcher(tempString);
				Matcher m_2=p2.matcher(tempString);
				Matcher m_3=p3.matcher(tempString);
				Matcher m_4=p4.matcher(tempString);
				if(tempString.startsWith("#")){
					
				}else{
					Matcher mf = null;
					if(m.find()){
						mf = m;
					}else if(m_1.find()){
						mf = m_1;
					}else if(m_2.find()){
						mf = m_2;
					}else if(m_3.find()){
						mf = m_3;
					}else if(m_4.find()){
						mf = m_4;
					}
	                if(mf != null){
	                	int start = Integer.parseInt(mf.group(3));
	                	int end = Integer.parseInt(mf.group(4));
	                	
	                	if(start>end){
	                		int temp=start;
	                		start = end;
	                		end = temp;
	                	}
	                	
	                	String ChrId;
	                	if(mf == m_2 || mf==m_3){
	                		ChrId = mf.group(1);
	                	}else{
	                		ChrId = mf.group(1);
	                	}
	                	
	                	Strand strand;
	                	if("-".equals(mf.group(6))){
	                		strand = Strand.NEGTIVE;
	                	}else{
	                		strand = Strand.POSITIVE;
	                	}
	                	
	                	String information = mf.group(9);
                		information=information.replaceAll("=Chr\\d\\.", "");
                		information=information.replaceAll("\\.mrna", "");
                		information=information.replaceAll("=", "");
                		Transcript transcript;
	                	if(transcriptHashMap.containsKey(information)){
	                		transcript = transcriptHashMap.get(information);
	                	}else{
	                		transcript = new Transcript(information);
	                		transcript.setChromeSomeName(ChrId);
	                		transcript.setStrand(strand);
	                		transcriptHashMap.put(information, transcript);
	                	}
	                	Cds cds = new Cds(start, end);
	                	cds.setTranscript(transcript);
	                	transcript.getCdsHashSet().add(cds);
	                }
				}
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	e1.getStackTrace();
                }
            }
        }
        
        Iterator<String> iterator = transcriptHashMap.keySet().iterator();
        while(iterator.hasNext()){
        	String key = iterator.next();
        	Transcript transcript=transcriptHashMap.get(key);
        	String chName = transcript.getChromeSomeName();
        	if(!transcriptHashSet.containsKey(chName)){
        		transcriptHashSet.put(chName, new HashSet<Transcript>());
        	}
        	transcriptHashSet.get(chName).add(transcript);
        }
	}

	public synchronized HashMap<String, HashSet<Transcript>> getTranscriptHashSet() {
		return transcriptHashSet;
	}
	public synchronized void setTranscriptHashSet(
			HashMap<String, HashSet<Transcript>> transcriptHashSet) {
		this.transcriptHashSet = transcriptHashSet;
	}
}
