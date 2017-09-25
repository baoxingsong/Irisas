package me.songbx.util;

import java.util.HashMap;

import me.songbx.util.exception.codingNotFound;
import me.songbx.util.exception.codingNotThree;

/**
 * @author song
 * @version 1.0, 2014-07-09
 */

public class StandardGeneticCode {
	private HashMap<String, Character> startsStandardGeneticCode = new HashMap<String, Character>();
	private HashMap<String, Character> middleAndEndStandardGeneticCode = new HashMap<String, Character>();
	public StandardGeneticCode(){
		//////////////////////////////////////////
		
		startsStandardGeneticCode.put("TTG", 'M');
		startsStandardGeneticCode.put("CTG", 'M');
		startsStandardGeneticCode.put("ATG", 'M');
		
		startsStandardGeneticCode.put("YTG", 'M');
		startsStandardGeneticCode.put("WTG", 'M');
		startsStandardGeneticCode.put("MTG", 'M');
		startsStandardGeneticCode.put("HTG", 'M');
		//////////////////////////////////////////
		
		////////////////////*
		middleAndEndStandardGeneticCode.put("TAA", '*');
		middleAndEndStandardGeneticCode.put("TAG", '*');
		middleAndEndStandardGeneticCode.put("TGA", '*');
		/////
		middleAndEndStandardGeneticCode.put("TRA", '*');
		middleAndEndStandardGeneticCode.put("TAR", '*');
		
		////////////////////F
		middleAndEndStandardGeneticCode.put("TTT", 'F');
		middleAndEndStandardGeneticCode.put("TTC", 'F');
		/////
		middleAndEndStandardGeneticCode.put("TTY", 'F');
		
		////////////////////L
		middleAndEndStandardGeneticCode.put("TTA", 'L');
		middleAndEndStandardGeneticCode.put("TTG", 'L');
		
		middleAndEndStandardGeneticCode.put("CTC", 'L');
		middleAndEndStandardGeneticCode.put("CTT", 'L');
		middleAndEndStandardGeneticCode.put("CTA", 'L');
		middleAndEndStandardGeneticCode.put("CTG", 'L');
		/////
		middleAndEndStandardGeneticCode.put("TTR", 'L');
		
		middleAndEndStandardGeneticCode.put("YTA", 'L');
		middleAndEndStandardGeneticCode.put("YTG", 'L');
		middleAndEndStandardGeneticCode.put("YTR", 'L');
		
		middleAndEndStandardGeneticCode.put("CTR", 'L');
		middleAndEndStandardGeneticCode.put("CTY", 'L');
		middleAndEndStandardGeneticCode.put("CTS", 'L');
		middleAndEndStandardGeneticCode.put("CTW", 'L');
		middleAndEndStandardGeneticCode.put("CTK", 'L');
		middleAndEndStandardGeneticCode.put("CTM", 'L');
		middleAndEndStandardGeneticCode.put("CTB", 'L');
		middleAndEndStandardGeneticCode.put("CTD", 'L');
		middleAndEndStandardGeneticCode.put("CTH", 'L');
		middleAndEndStandardGeneticCode.put("CTV", 'L');
		middleAndEndStandardGeneticCode.put("CTN", 'L');
		
		////////////////////S
		middleAndEndStandardGeneticCode.put("TCC", 'S');
		middleAndEndStandardGeneticCode.put("TCT", 'S');
		middleAndEndStandardGeneticCode.put("TCA", 'S');
		middleAndEndStandardGeneticCode.put("TCG", 'S');
		
		middleAndEndStandardGeneticCode.put("TCR", 'S');
		middleAndEndStandardGeneticCode.put("TCY", 'S');
		middleAndEndStandardGeneticCode.put("TCS", 'S');
		middleAndEndStandardGeneticCode.put("TCW", 'S');
		middleAndEndStandardGeneticCode.put("TCK", 'S');
		middleAndEndStandardGeneticCode.put("TCM", 'S');
		middleAndEndStandardGeneticCode.put("TCB", 'S');
		middleAndEndStandardGeneticCode.put("TCD", 'S');
		middleAndEndStandardGeneticCode.put("TCH", 'S');
		middleAndEndStandardGeneticCode.put("TCV", 'S');
		middleAndEndStandardGeneticCode.put("TCN", 'S');
		
		middleAndEndStandardGeneticCode.put("AGT", 'S');
		middleAndEndStandardGeneticCode.put("AGC", 'S');
		middleAndEndStandardGeneticCode.put("AGY", 'S');
		
		
		
		
		////////////////////Y
		middleAndEndStandardGeneticCode.put("TAT", 'Y');
		middleAndEndStandardGeneticCode.put("TAC", 'Y');
		middleAndEndStandardGeneticCode.put("TAY", 'Y');
		
		////////////////////C
		middleAndEndStandardGeneticCode.put("TGT", 'C');
		middleAndEndStandardGeneticCode.put("TGC", 'C');
		middleAndEndStandardGeneticCode.put("TGY", 'C');
		
		///////////////////W
		middleAndEndStandardGeneticCode.put("TGG", 'W');
		
		///////////////////P
		middleAndEndStandardGeneticCode.put("CCT", 'P');
		middleAndEndStandardGeneticCode.put("CCC", 'P');
		middleAndEndStandardGeneticCode.put("CCA", 'P');
		middleAndEndStandardGeneticCode.put("CCG", 'P');
		
		middleAndEndStandardGeneticCode.put("CCR", 'P');
		middleAndEndStandardGeneticCode.put("CCY", 'P');
		middleAndEndStandardGeneticCode.put("CCS", 'P');
		middleAndEndStandardGeneticCode.put("CCW", 'P');
		middleAndEndStandardGeneticCode.put("CCK", 'P');
		middleAndEndStandardGeneticCode.put("CCM", 'P');
		middleAndEndStandardGeneticCode.put("CCB", 'P');
		middleAndEndStandardGeneticCode.put("CCD", 'P');
		middleAndEndStandardGeneticCode.put("CCH", 'P');
		middleAndEndStandardGeneticCode.put("CCV", 'P');
		middleAndEndStandardGeneticCode.put("CCN", 'P');
		
		///////////////////H
		middleAndEndStandardGeneticCode.put("CAT", 'H');
		middleAndEndStandardGeneticCode.put("CAC", 'H');
		middleAndEndStandardGeneticCode.put("CAY", 'H');
		
		///////////////////Q
		middleAndEndStandardGeneticCode.put("CAA", 'Q');
		middleAndEndStandardGeneticCode.put("CAG", 'Q');
		middleAndEndStandardGeneticCode.put("CAR", 'Q');
		
		///////////////////R
		middleAndEndStandardGeneticCode.put("CGT", 'R');
		middleAndEndStandardGeneticCode.put("CGC", 'R');
		middleAndEndStandardGeneticCode.put("CGA", 'R');
		middleAndEndStandardGeneticCode.put("CGG", 'R');
		
		middleAndEndStandardGeneticCode.put("CGR", 'R');
		middleAndEndStandardGeneticCode.put("CGY", 'R');
		middleAndEndStandardGeneticCode.put("CGS", 'R');
		middleAndEndStandardGeneticCode.put("CGW", 'R');
		middleAndEndStandardGeneticCode.put("CGK", 'R');
		middleAndEndStandardGeneticCode.put("CGM", 'R');
		middleAndEndStandardGeneticCode.put("CGB", 'R');
		middleAndEndStandardGeneticCode.put("CGD", 'R');
		middleAndEndStandardGeneticCode.put("CGH", 'R');
		middleAndEndStandardGeneticCode.put("CGV", 'R');
		middleAndEndStandardGeneticCode.put("CGN", 'R');
		
		middleAndEndStandardGeneticCode.put("AGA", 'R');
		middleAndEndStandardGeneticCode.put("AGG", 'R');
		middleAndEndStandardGeneticCode.put("AGR", 'R');
		
		middleAndEndStandardGeneticCode.put("MGA", 'R');
		middleAndEndStandardGeneticCode.put("MGG", 'R');
		middleAndEndStandardGeneticCode.put("MGR", 'R');
		
		
		
		///////////////////I
		middleAndEndStandardGeneticCode.put("ATT", 'I');
		middleAndEndStandardGeneticCode.put("ATC", 'I');
		middleAndEndStandardGeneticCode.put("ATA", 'I');
		
		middleAndEndStandardGeneticCode.put("ATY", 'I');
		middleAndEndStandardGeneticCode.put("ATW", 'I');
		middleAndEndStandardGeneticCode.put("ATM", 'I');
		
		///////////////////M
		middleAndEndStandardGeneticCode.put("ATG", 'M');
		
		///////////////////T
		middleAndEndStandardGeneticCode.put("ACT", 'T');
		middleAndEndStandardGeneticCode.put("ACC", 'T');
		middleAndEndStandardGeneticCode.put("ACA", 'T');
		middleAndEndStandardGeneticCode.put("ACG", 'T');
		
		middleAndEndStandardGeneticCode.put("ACR", 'T');
		middleAndEndStandardGeneticCode.put("ACY", 'T');
		middleAndEndStandardGeneticCode.put("ACS", 'T');
		middleAndEndStandardGeneticCode.put("ACW", 'T');
		middleAndEndStandardGeneticCode.put("ACK", 'T');
		middleAndEndStandardGeneticCode.put("ACM", 'T');
		middleAndEndStandardGeneticCode.put("ACB", 'T');
		middleAndEndStandardGeneticCode.put("ACD", 'T');
		middleAndEndStandardGeneticCode.put("ACH", 'T');
		middleAndEndStandardGeneticCode.put("ACV", 'T');
		middleAndEndStandardGeneticCode.put("ACN", 'T');
		
		///////////////////N
		middleAndEndStandardGeneticCode.put("AAT", 'N');
		middleAndEndStandardGeneticCode.put("AAC", 'N');
		middleAndEndStandardGeneticCode.put("AAY", 'N');
		
		///////////////////K
		middleAndEndStandardGeneticCode.put("AAA", 'K');
		middleAndEndStandardGeneticCode.put("AAG", 'K');
		
		middleAndEndStandardGeneticCode.put("AAR", 'K');
		
		///////////////////V
		middleAndEndStandardGeneticCode.put("GTT", 'V');
		middleAndEndStandardGeneticCode.put("GTC", 'V');
		middleAndEndStandardGeneticCode.put("GTA", 'V');
		middleAndEndStandardGeneticCode.put("GTG", 'V');
		
		middleAndEndStandardGeneticCode.put("GTR", 'V');
		middleAndEndStandardGeneticCode.put("GTY", 'V');
		middleAndEndStandardGeneticCode.put("GTS", 'V');
		middleAndEndStandardGeneticCode.put("GTW", 'V');
		middleAndEndStandardGeneticCode.put("GTK", 'V');
		middleAndEndStandardGeneticCode.put("GTM", 'V');
		middleAndEndStandardGeneticCode.put("GTB", 'V');
		middleAndEndStandardGeneticCode.put("GTD", 'V');
		middleAndEndStandardGeneticCode.put("GTH", 'V');
		middleAndEndStandardGeneticCode.put("GTV", 'V');
		middleAndEndStandardGeneticCode.put("GTN", 'V');
				
		///////////////////A
		middleAndEndStandardGeneticCode.put("GCT", 'A');
		middleAndEndStandardGeneticCode.put("GCC", 'A');
		middleAndEndStandardGeneticCode.put("GCA", 'A');
		middleAndEndStandardGeneticCode.put("GCG", 'A');
		
		middleAndEndStandardGeneticCode.put("GCR", 'A');
		middleAndEndStandardGeneticCode.put("GCY", 'A');
		middleAndEndStandardGeneticCode.put("GCS", 'A');
		middleAndEndStandardGeneticCode.put("GCW", 'A');
		middleAndEndStandardGeneticCode.put("GCK", 'A');
		middleAndEndStandardGeneticCode.put("GCM", 'A');
		middleAndEndStandardGeneticCode.put("GCB", 'A');
		middleAndEndStandardGeneticCode.put("GCD", 'A');
		middleAndEndStandardGeneticCode.put("GCH", 'A');
		middleAndEndStandardGeneticCode.put("GCV", 'A');
		middleAndEndStandardGeneticCode.put("GCN", 'A');
		
		///////////////////D
		middleAndEndStandardGeneticCode.put("GAT", 'D');
		middleAndEndStandardGeneticCode.put("GAC", 'D');
		middleAndEndStandardGeneticCode.put("GAY", 'D');
		
		///////////////////E
		middleAndEndStandardGeneticCode.put("GAA", 'E');
		middleAndEndStandardGeneticCode.put("GAG", 'E');
		middleAndEndStandardGeneticCode.put("GAR", 'E');
		
		///////////////////G
		middleAndEndStandardGeneticCode.put("GGT", 'G');
		middleAndEndStandardGeneticCode.put("GGC", 'G');
		middleAndEndStandardGeneticCode.put("GGA", 'G');
		middleAndEndStandardGeneticCode.put("GGG", 'G');
		
		middleAndEndStandardGeneticCode.put("GGR", 'G');
		middleAndEndStandardGeneticCode.put("GGY", 'G');
		middleAndEndStandardGeneticCode.put("GGS", 'G');
		middleAndEndStandardGeneticCode.put("GGW", 'G');
		middleAndEndStandardGeneticCode.put("GGK", 'G');
		middleAndEndStandardGeneticCode.put("GGM", 'G');
		middleAndEndStandardGeneticCode.put("GGB", 'G');
		middleAndEndStandardGeneticCode.put("GGD", 'G');
		middleAndEndStandardGeneticCode.put("GGH", 'G');
		middleAndEndStandardGeneticCode.put("GGV", 'G');
		middleAndEndStandardGeneticCode.put("GGN", 'G');
		
		middleAndEndStandardGeneticCode.put("---", '-');
	}
	
	
	
	/**
	 * give me three DNA bases, I will give you a amino acid base, IUPAC code are considered.
	 * And if could not find a slandered amino acid code, a X would be returned
	 * @param coding
	 * @param ifStartCode
	 * @return
	 * @throws codingNotThree
	 * @throws codingNotFound
	 */
	public synchronized Character getGeneticCode(String coding, boolean ifStartCode) throws codingNotThree, codingNotFound{
		if(3 != coding.length()){
			System.out.println("coding:" + coding + "end length:" + coding.length());
			throw new codingNotThree();
		}
		coding = coding.toUpperCase();
		coding.replaceAll("U", "T");
		if(ifStartCode){
			if(startsStandardGeneticCode.containsKey(coding)){
				return startsStandardGeneticCode.get(coding);
			}else{
				//System.err.println("Could not find the coding " + coding + " in start genetic code dataset");
				if(middleAndEndStandardGeneticCode.containsKey(coding)){
					return middleAndEndStandardGeneticCode.get(coding);
				}else{
					//System.err.println("Could not find the coding " + coding + " in genetic code dataset");
					return 'X';
				}
			}
		}else{
			if(middleAndEndStandardGeneticCode.containsKey(coding)){
				return middleAndEndStandardGeneticCode.get(coding);
			}else{
				//System.err.println("Could not find the coding " + coding + " in genetic code dataset");
				return 'X';
			}
		}
	}
}
/*
 * 
 * IUPAC codes
DNA:

Nucleotide Code:  Base:
----------------  -----
A.................Adenine
C.................Cytosine
G.................Guanine
T (or U)..........Thymine (or Uracil)
R.................A or G
Y.................C or T
S.................G or C
W.................A or T
K.................G or T
M.................A or C
B.................C or G or T
D.................A or G or T
H.................A or C or T
V.................A or C or G
N.................any base
. or -............gap

Protein:

Amino Acid Code:  Three letter Code:  Amino Acid:
----------------  ------------------  -----------
A.................Ala.................Alanine
B.................Asx.................Aspartic acid or Asparagine
C.................Cys.................Cysteine
D.................Asp.................Aspartic Acid
E.................Glu.................Glutamic Acid
F.................Phe.................Phenylalanine
G.................Gly.................Glycine
H.................His.................Histidine
I.................Ile.................Isoleucine
K.................Lys.................Lysine
L.................Leu.................Leucine
M.................Met.................Methionine
N.................Asn.................Asparagine
P.................Pro.................Proline
Q.................Gln.................Glutamine
R.................Arg.................Arginine
S.................Ser.................Serine
T.................Thr.................Threonine
V.................Val.................Valine
W.................Trp.................Tryptophan
X.................Xaa.................Any amino acid
Y.................Tyr.................Tyrosine
Z.................Glx.................Glutamine or Glutamic acid


    AAs  = FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG
  Starts = ---M---------------M---------------M----------------------------
  Base1  = TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG
  Base2  = TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG
  Base3  = TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG
 * *
 */
