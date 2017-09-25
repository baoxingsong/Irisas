package me.songbx.action.parallel.model;

import java.util.ArrayList;
import java.util.Collections;

import me.songbx.model.MsaFileRecord;

public class MsaFileRecordArrayList {
	private ArrayList<MsaFileRecord> msaFileRecords = new ArrayList<MsaFileRecord>();

	public synchronized void add(MsaFileRecord msaFileRecord) {
		this.msaFileRecords.add(msaFileRecord);
	}
	public synchronized ArrayList<MsaFileRecord> getMsaFileRecords() {
		return msaFileRecords;
	}
	public synchronized void setMsaFileRecords(
			ArrayList<MsaFileRecord> msaFileRecords) {
		this.msaFileRecords = msaFileRecords;
	}
	public synchronized void sort(){
		Collections.sort(msaFileRecords);
	}
}
