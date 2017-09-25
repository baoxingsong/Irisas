package me.songbx.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.model.MsaSingleRecord;

public class GeneCenteredMsaReadImpl {
	private HashMap<String, HashMap<String, ArrayList<MsaSingleRecord>>> hashMapHashMapArrayListMsaSingleRecords;
	private String chrName;
	public GeneCenteredMsaReadImpl(String fileLocation, HashMap<String, HashMap<String, ArrayList<MsaSingleRecord>>> hashMapHashMapArrayListMsaSingleRecords, String chrName){
		this.hashMapHashMapArrayListMsaSingleRecords=hashMapHashMapArrayListMsaSingleRecords;
		this.chrName=chrName;
		File file = new File(fileLocation);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			Pattern p = Pattern.compile("^>(.*)");
			StringBuffer sequenceBuffer = new StringBuffer();
			String sequenceName = "";
			while ((tempString = reader.readLine()) != null) {
				Matcher m = p.matcher(tempString);
				if(m.find()){
					if (sequenceName.length() > 0
							&& sequenceBuffer.length() > 0) {
						Pattern pName = Pattern.compile("^(\\w+)\\S*_(\\d*)_(\\d*)$");
						Matcher mName = pName.matcher(sequenceName);
						
						if(mName.find()){
							String accessionName = mName.group(1);
							int start = Integer.parseInt(mName.group(2));
							int end = Integer.parseInt(mName.group(3));
							MsaSingleRecord msaSingleRecord = new MsaSingleRecord(start, end, accessionName, sequenceName);
							if(!hashMapHashMapArrayListMsaSingleRecords.containsKey(chrName)){
								hashMapHashMapArrayListMsaSingleRecords.put(chrName, new HashMap<String, ArrayList<MsaSingleRecord>>());
							}
							if(!hashMapHashMapArrayListMsaSingleRecords.get(chrName).containsKey(accessionName)){
								hashMapHashMapArrayListMsaSingleRecords.get(chrName).put(accessionName, new ArrayList<MsaSingleRecord>());
							}
							hashMapHashMapArrayListMsaSingleRecords.get(chrName).get(accessionName).add(msaSingleRecord);
						}else{
							System.err.println(mName);
						}
					}
					sequenceName = m.group(1);
					sequenceBuffer = new StringBuffer();
				}else {
					sequenceBuffer.append(tempString);
				}
			}
			if (sequenceName.length() > 0 && sequenceBuffer.length() > 0) {
				Pattern pName = Pattern.compile("^(\\w+)\\S*_(\\d*)_(\\d*)$");
				Matcher mName = pName.matcher(sequenceName);
				
				if(mName.find()){
					String accessionName = mName.group(1);
					int start = Integer.parseInt(mName.group(2));
					int end = Integer.parseInt(mName.group(3));
					MsaSingleRecord msaSingleRecord = new MsaSingleRecord(start, end, accessionName, sequenceName);
					if(!hashMapHashMapArrayListMsaSingleRecords.containsKey(chrName)){
						hashMapHashMapArrayListMsaSingleRecords.put(chrName, new HashMap<String, ArrayList<MsaSingleRecord>>());
					}
					if(!hashMapHashMapArrayListMsaSingleRecords.get(chrName).containsKey(accessionName)){
						hashMapHashMapArrayListMsaSingleRecords.get(chrName).put(accessionName, new ArrayList<MsaSingleRecord>());
					}
					hashMapHashMapArrayListMsaSingleRecords.get(chrName).get(accessionName).add(msaSingleRecord);
				}else{
					System.err.println(mName);
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

				}
			}
		}
	}
}
