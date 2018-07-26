package me.songbx.impl;

import me.songbx.model.ChromoSome;
import me.songbx.model.FastaIndexEntry;
import me.songbx.model.Strand;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author song
 * @version 1.0, 2014-07-09
 */
public class ChromoSomeReadImplWithIndex {
	RandomAccessFile reader = null;
	FastaIndexEntryImpl fastaIndexEntryImpl = new FastaIndexEntryImpl();

	public synchronized FastaIndexEntryImpl getFastaIndexEntryImpl() {
		return fastaIndexEntryImpl;
	}

	public synchronized void setFastaIndexEntryImpl(FastaIndexEntryImpl fastaIndexEntryImpl) {
		this.fastaIndexEntryImpl = fastaIndexEntryImpl;
	}

	/**
	 * @param the location of fasta or multiple fasta format file. The meta-line should be like ">Chr2"
	 */
	public ChromoSomeReadImplWithIndex(String fastaFileLocation) {
		String fastaIndexFileLocation = fastaFileLocation + ".fai";
		File f = new File(fastaIndexFileLocation);
		if(f.exists() && !f.isDirectory()) {
			fastaIndexEntryImpl.readFastaIndexFile(fastaIndexFileLocation);
		}else {
			fastaIndexEntryImpl.createFastaIndexFile(fastaFileLocation);
		}
		File file = new File(fastaFileLocation);
		try {
			reader = new RandomAccessFile(file, "r");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeFile() {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get a chromosome with a name, if could find this name, return the chromosome, if could not return null
	 * @param ChromoSome name
	 * @return ChromoSome
	 */
	public synchronized String getChromoSomeById(String name) {
		if (fastaIndexEntryImpl.getEntries().containsKey(name)) {
			FastaIndexEntry entry = fastaIndexEntryImpl.getEntries().get(name);
			int newlines_in_sequence = entry.getLength()/entry.getLine_blen();
			int seqlen = newlines_in_sequence + entry.getLength();
			byte[] b = new byte[seqlen];
			try {
				reader.seek(entry.getOffset());
				reader.read(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String seq = new String(b);
			seq = seq.replaceAll("\\s", "");
			return seq;
		}
		return null;
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
			Strand strand) { // it should be OK without synchronized
		int start;
		int end;
		if (start1 < end1) {
			start = start1;
			end = end1;
		} else {
			start = end1;
			end = start1;
		}

		String subSequence = "";
		if (fastaIndexEntryImpl.getEntries().containsKey(chromeSomeName)) {
			FastaIndexEntry entry = fastaIndexEntryImpl.getEntries().get(chromeSomeName);

			start--;
			if(start<0){
				start=0;
			}

			if( entry.getLength() < end ){
				end = entry.getLength();
			}
			if( entry.getLength() < start ){
				start = entry.getLength();
			}
			if( end<0 ){
				end=0;
			}

			int length = (end - start);

			int newlines_before = start > 0 ? (start - 1) / entry.getLine_blen() : 0;
			int newlines_by_end = (start + length - 1) / entry.getLine_blen();
			int newlines_inside = newlines_by_end - newlines_before;
			int seqlen = length + newlines_inside;
			byte[] b = new byte[seqlen];

			try {
				reader.seek(entry.getOffset() + newlines_before + start );
				reader.read(b);
				subSequence = new String(b);
				subSequence = subSequence.replaceAll("\\s", "");
				if (Strand.NEGTIVE == strand) {
					return getReversecomplementary(subSequence);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return subSequence;
	}

	/**
	 * @param DNA
	 *            /RNA sequence
	 * @return reverse complementary DNA sequence(only T, no U)
	 */
	private synchronized static String getReversecomplementary(String sequence) {
		StringBuffer reversecomplementary = new StringBuffer();
		for (int i = sequence.length() - 1; i >= 0; i--) {
			char c = sequence.charAt(i);
			if ('A' == c) {
				c = 'T';
			} else if ('T' == c) {
				c = 'A';
			} else if ('U' == c) {
				c = 'A';
			} else if ('C' == c) {
				c = 'G';
			} else if ('G' == c) {
				c = 'C';
			} else if ('R' == c) {
				c = 'Y';
			} else if ('Y' == c) {
				c = 'R';
			} else if ('K' == c) {
				c = 'M';
			} else if ('M' == c) {
				c = 'K';
			} else if ('B' == c) {
				c = 'V';
			} else if ('V' == c) {
				c = 'B';
			} else if ('D' == c) {
				c = 'H';
			} else if ('H' == c) {
				c = 'D';
			}
			reversecomplementary.append(c);
		}
		return reversecomplementary.toString();
	}

	public static void main(String[] argv ){ // for testing purpose
		ChromoSomeReadImplWithIndex chromoSomeReadImplWithIndex = new ChromoSomeReadImplWithIndex("/netscratch/dep_tsiantis/grp_gan/song/tsianst/GWAS/prepareForGenotyping/1171B.fa");
		ChromoSomeReadImpl chromoSomeReadImpl = new ChromoSomeReadImpl("/netscratch/dep_tsiantis/grp_gan/song/tsianst/GWAS/prepareForGenotyping/1171B.fa");

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 1, 1, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 1, 1, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 0, 0, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 0, 0, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 1921370, 1932481, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 1921370, 1932481, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 20, 60, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 20, 60, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 120, 260, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 120, 260, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 1220, 1260, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 1220, 1260, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 2220, 2260, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 2220, 2260, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 3320, 3360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 3320, 3360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr1", 12320, 12360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr1", 12320, 12360, Strand.POSITIVE));

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();



		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr2", 20, 60, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr2", 20, 60, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr2", 120, 260, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr2", 120, 260, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr2", 1220, 1260, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr2", 1220, 1260, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr2", 2220, 2260, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr2", 2220, 2260, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr2", 3320, 3360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr2", 3320, 3360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr2", 12320, 12360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr2", 12320, 12360, Strand.POSITIVE));

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();



		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr3", 20, 160, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr3", 20, 160, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr3", 120, 360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr3", 120, 360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr3", 1220, 1360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr3", 1220, 1360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr3", 2220, 2360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr3", 2220, 2360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr3", 3320, 3460, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr3", 3320, 3460, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr3", 12320, 12560, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr3", 12320, 12560, Strand.POSITIVE));
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();



		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr4", 20, 160, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr4", 20, 160, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr4", 120, 360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr4", 120, 360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr4", 1220, 1360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr4", 1220, 1360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr4", 2220, 2360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr4", 2220, 2360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr4", 3320, 3460, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr4", 3320, 3460, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr4", 12320, 12560, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr4", 12320, 12560, Strand.POSITIVE));

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();



		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr5", 20, 160, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr5", 20, 160, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr5", 120, 360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr5", 120, 360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr5", 1220, 1360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr5", 1220, 1360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr5", 2220, 2360, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr5", 2220, 2360, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr5", 3320, 3460, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr5", 3320, 3460, Strand.POSITIVE));

		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("Chr5", 12320, 12560, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("Chr5", 12320, 12560, Strand.POSITIVE));

		chromoSomeReadImplWithIndex.closeFile();


		chromoSomeReadImplWithIndex = new ChromoSomeReadImplWithIndex("/netscratch/dep_tsiantis/grp_gan/song/tsianst/GWAS/prepareForGenotyping/Ibe_See-0.fa");
		chromoSomeReadImpl = new ChromoSomeReadImpl("/netscratch/dep_tsiantis/grp_gan/song/tsianst/GWAS/prepareForGenotyping/Ibe_See-0.fa");
		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("NSCAFA.485", 1, 160, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("NSCAFA.485", 1, 160, Strand.POSITIVE));
		chromoSomeReadImplWithIndex.closeFile();


		chromoSomeReadImplWithIndex = new ChromoSomeReadImplWithIndex("/netscratch/dep_tsiantis/grp_gan/song/tsianst/GWAS/prepareForGenotyping/For-1.fa");
		chromoSomeReadImpl = new ChromoSomeReadImpl("/netscratch/dep_tsiantis/grp_gan/song/tsianst/GWAS/prepareForGenotyping/For-1.fa");
		System.out.println(chromoSomeReadImplWithIndex.getSubSequence("NSCAFA.485", 1, 10509, Strand.POSITIVE));
		System.out.println(chromoSomeReadImpl.getSubSequence("NSCAFA.485", 1, 10509, Strand.POSITIVE));
		chromoSomeReadImplWithIndex.closeFile();
	}
}
