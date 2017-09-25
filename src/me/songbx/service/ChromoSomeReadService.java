package me.songbx.service;


import java.util.HashMap;

import me.songbx.impl.ChromoSomeReadImpl;
import me.songbx.model.ChromoSome;
import me.songbx.model.Strand;

/**
 * @author song
 * @version 1.0, 2014-07-09
 */
public class ChromoSomeReadService {

	/**
	 * @param the location of fasta or multiple fasta format file. The meta-line should be like ">Chr2"
	 */
	private ChromoSomeReadImpl chromoSomeReadImpl;
	public ChromoSomeReadService() {
	}
	public ChromoSomeReadService(String fileLocation) {
		chromoSomeReadImpl = new ChromoSomeReadImpl(fileLocation);
	}
	
	/**
	 * get a chromosome with a name, if could find this name, return the chromosome, if could not return null
	 * @param ChromoSome name
	 * @return ChromoSome
	 */
	public synchronized ChromoSome getChromoSomeById(String name) {
		return chromoSomeReadImpl.getChromoSomeById(name);
	}

	/**
	 * if start1 > end1, switch them.
	 * And then, the sequence position counts start from 1, the start1-th character would be returned, the end1-th would not be returned
	 * please make sure start >= 1, end <= the length of chromosome sequence
	 * 
	 * @param chromeSomeName
	 * @param start1
	 *            : the start position of positive sequence 
	 * @param end1
	 *            : the start position of positive sequence
	 * @param strand
	 *            : want positive(POSITIVE) sequence, or reverse complementary
	 *            sequence (NEGATIVE)
	 * @return the wanted sequence
	 */
	public synchronized String getSubSequence(String chromeSomeName, int start1, int end1,
			Strand strand) {
		return chromoSomeReadImpl.getSubSequence(chromeSomeName, start1, end1, strand);
	}

	public synchronized ChromoSomeReadImpl getChromoSomeReadImpl() {
		return chromoSomeReadImpl;
	}
	
	public synchronized HashMap<String, ChromoSome> getChromoSomeHashMap() {
		return chromoSomeReadImpl.getChromoSomeHashMap();
	}
	
	public synchronized void setChromoSomeReadImpl(ChromoSomeReadImpl chromoSomeReadImpl) {
		this.chromoSomeReadImpl = chromoSomeReadImpl;
	}
	
	public synchronized void releaseRam(){
		chromoSomeReadImpl.releaseRam();
	}
	
}
