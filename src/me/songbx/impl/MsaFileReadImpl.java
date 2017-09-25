package me.songbx.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.model.MsaFileRecord;
import me.songbx.model.MsaSingleRecord;

public class MsaFileReadImpl {
	private MsaFileRecord msaFileRecord = new MsaFileRecord();
	public MsaFileReadImpl() {
		
	}

	public MsaFileReadImpl(String fileLocation, HashSet<String> names) {
		File file = new File(fileLocation);
		String inputFileName = file.getName();
		Pattern p1 = Pattern.compile("(\\d+)_(\\d+).\\w+");
		Matcher m1 = p1.matcher(inputFileName);
		if(m1.find()){
			int start = Integer.parseInt(m1.group(1));
			int end = Integer.parseInt(m1.group(2));
			msaFileRecord.setStart(start);
			msaFileRecord.setEnd(end);
		}else{
			System.err.println("The file " + fileLocation + " could not match the stat and end detection regulary");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			Pattern p = Pattern.compile("^>(.*)");
			
			StringBuffer sequenceBuffer = new StringBuffer();
			String sequenceName = "";
			while ((tempString = reader.readLine()) != null) {
				Matcher m = p.matcher(tempString);
				if (m.find()) {
					if (sequenceName.length() > 0
							&& sequenceBuffer.length() > 0) {
						Pattern pa = Pattern.compile("^(\\S+?)_(\\d+)_(\\d+)$");
						Matcher ma=pa.matcher(sequenceName);
						if(ma.find()){
							MsaSingleRecord msaSingleRecord = new MsaSingleRecord(Integer.parseInt(ma.group(2)), Integer.parseInt(ma.group(3)), ma.group(1), sequenceBuffer.toString().toUpperCase());
							if( names.contains( ma.group(1)) ){
								msaFileRecord.getRecords().put(ma.group(1), msaSingleRecord);
							}
						}else{
							System.err.println("not match " + sequenceName);
						}
						//System.out.println(Integer.parseInt(ma.group(2)) + "\t" +Integer.parseInt(ma.group(3))+"\t"+ma.group(1));
					}
					sequenceName = m.group(1);
					sequenceBuffer = new StringBuffer();
				
				} else {
					sequenceBuffer.append(tempString);
				}
			}
			if (sequenceName.length() > 0 && sequenceBuffer.length() > 0) {
				Pattern pa = Pattern.compile("^(\\S+?)_(\\d+)_(\\d+)$");
				Matcher ma=pa.matcher(sequenceName);
				if(ma.find()){
					MsaSingleRecord msaSingleRecord = new MsaSingleRecord(Integer.parseInt(ma.group(2)), Integer.parseInt(ma.group(3)), ma.group(1), sequenceBuffer.toString().toUpperCase());
					if( names.contains( ma.group(1)) ){
						msaFileRecord.getRecords().put(ma.group(1), msaSingleRecord);
					}
				}else{
					System.err.println("not match " + sequenceName);
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

	public synchronized MsaFileRecord getMsaFileRecord() {
		return msaFileRecord;
	}

	public synchronized void setMsaFileRecord(MsaFileRecord msaFileRecord) {
		this.msaFileRecord = msaFileRecord;
	}
}
