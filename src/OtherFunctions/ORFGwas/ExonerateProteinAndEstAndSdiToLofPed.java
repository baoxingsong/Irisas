package OtherFunctions.ORFGwas;

import me.songbx.action.parallel.model.AnnotationIncludeOrfLostReadReferences;
import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.Transcript.Transcript;
import me.songbx.service.AnnotationReadIncludeOrfLostService;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.service.MapFileService;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExonerateProteinAndEstAndSdiToLofPed {
	
	private String gffFile;
	private String folderOfThisAccession;
	private String sdiFile;
	private String targetgenomeSequence;
	private String referenceGenome;

	public ExonerateProteinAndEstAndSdiToLofPed(){

	}
	public void setGffFile(String gffFile) {
		this.gffFile = gffFile;
	}
	public void setFolderOfThisAccession(String folderOfThisAccession) {
		this.folderOfThisAccession = folderOfThisAccession;
	}
	public void setSdiFile(String sdiFile) {
		this.sdiFile = sdiFile;
	}
	public void setTargetgenomeSequence(String targetgenomeSequence) {
		this.targetgenomeSequence = targetgenomeSequence;
	}
	public void setReferenceGenome(String referenceGenome) {
		this.referenceGenome = referenceGenome;
	}

	public  ExonerateProteinAndEstAndSdiToLofPed(String[] argv ){
		
		StringBuffer helpMessage=new StringBuffer("Integrating effect pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
		helpMessage.append("  -a   reference gene structure annotation in GFF/GTF format\n");
		helpMessage.append("  -f   exonerate working folder for this accession/line\n");
		helpMessage.append("  -s   sdi file of target accession/line\n");
		helpMessage.append("  -m   genome sequence of target accession/line in fasta format\n");
		helpMessage.append("  -r   reference genome sequence in fasta format\n");
		
		Options options = new Options();
		options.addOption("a",true,"gffFile");
		options.addOption("f",true,"folderOfThisAccession");
        options.addOption("s",true,"sdiFile");
        options.addOption("m",true,"targetgenomeSequence");
        options.addOption("r",true,"referenceGenome");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        
        if(cmd.hasOption("a")){
        	gffFile = cmd.getOptionValue("a");
        }else{
        	System.err.println("-a is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        
        if(cmd.hasOption("f")){
        	folderOfThisAccession = cmd.getOptionValue("f");
        }else{
        	System.err.println("-f is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("s")){
        	sdiFile = cmd.getOptionValue("s");
        }else{
        	System.err.println("-s is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("m")){
        	targetgenomeSequence = cmd.getOptionValue("m");
        }else{
        	System.err.println("-m is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("r")){
        	referenceGenome = cmd.getOptionValue("r");
        }else{
        	System.err.println("-r is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
		doIt();
	}
	
	public void doIt(){
		HashSet<Position> allMyPositions = new HashSet<Position>();
		HashMap<String, Integer> positions = new HashMap<String, Integer>();
		HashMap<String, String> chrs = new HashMap<String, String>();
		
		File file1 = new File(gffFile);

		try {
        	BufferedReader reader = null;
        	reader = new BufferedReader(new FileReader(file1));
            String tempString = null;
            //X	FlyBase	mRNA	19961689	19968479	15	+	.	gene_id "FBgn0031081"; gene_symbol "Nep3"; transcript_id "FBtr0070000"; transcript_symbol "Nep3-RA"
            Pattern p = Pattern.compile("^(\\S*)\t(.*)\tmRNA\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\tID=(.*);Parent(.*?)");
            Pattern p1 = Pattern.compile("^(\\S*)\t(.*)\tmRNA\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t.*?ID=(.*?);Name");//rice
            Pattern p2 = Pattern.compile("^(\\S+)\\s+(\\w+)\\s+mRNA\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+.*?transcript_id\\s+\"(\\S+?)\";");
			while ((tempString = reader.readLine()) != null) {
				Matcher m = p.matcher(tempString);
				Matcher m1 = p1.matcher(tempString);
				Matcher m2 = p2.matcher(tempString);
				Matcher mf = null;
				if(m.find()){
					mf = m;
				}else if(m1.find()){
					mf = m1;
				}else if(m2.find()){
					mf = m2;
				}
				if(mf != null){
					String chr =  mf.group(1).replaceAll("Chr", "");
					int position = Integer.parseInt(mf.group(3));
					Position thisPoition = new Position(chr, position);
					
					while( allMyPositions.contains(thisPoition) ){//since some transcripts have same position, change the original position to avoid the result
						//multiple marker share same position in the map file. so that PLINK could deal with it
						position--;
						thisPoition = new Position(chr, position);
					}
					allMyPositions.add(thisPoition);
					
					positions.put(mf.group(8), position);
					chrs.put(mf.group(8),chr);
					//System.out.println(m.group(1) + " " + m.group(3) + " " + m.group(8));
				}
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("gff reading done");
        File folder2 = new File(folderOfThisAccession + File.separator + "ExonerateSequence");
        folder2.mkdirs();
		File folder3 = new File(folderOfThisAccession);
		int n1=0;
		int n2=0;
		int n3=0;
		int n12=0;
		int n13=0;
		int n23=0;
		int n123=0;
		int total=0;
		try {
			PrintWriter outPut = new PrintWriter(folderOfThisAccession + File.separator+"orf.ped");
			outPut.print(folder3.getName() + "  " + folder3.getName() + "  0 0  1  1");
			PrintWriter outPut1 = new PrintWriter(folderOfThisAccession + File.separator+"orf.map");
			PrintWriter outPut2 = new PrintWriter(folderOfThisAccession + File.separator+"ExonerateSequence"+File.separator+"final.fa");
			PrintWriter outPut2c = new PrintWriter(folderOfThisAccession + File.separator+"ExonerateSequence"+File.separator+"cds.fa");
			PrintWriter outPut2p = new PrintWriter(folderOfThisAccession + File.separator+"ExonerateSequence"+File.separator+"protein.fa");
			PrintWriter outPut2l = new PrintWriter(folderOfThisAccession + File.separator+"ExonerateSequence"+File.separator+"liftover.fa");
			PrintWriter outPut3 = new PrintWriter(folderOfThisAccession + File.separator+"summary");
			
			//String mapfileLocation="/biodata/dep_tsiantis/grp_gan/song/rdINDELallHere/inputData/fullSdiFile/"+folder3.getName() + ".sdi";
			//String targetchromeSomeReadFileLocation = "/biodata/dep_tsiantis/grp_gan/song/rdINDELallHere/inputData/fullSdiFile/" + folder3.getName() + ".fa";
			//ChromoSomeReadService referenceChromeSomeRead = new ChromoSomeReadService("/biodata/dep_tsiantis/grp_gan/song/rdINDELallHere/inputData/fullSdiFile/col_0.fa");
			String mapfileLocation = sdiFile;
			String targetchromeSomeReadFileLocation = targetgenomeSequence;
			ChromoSomeReadService referenceChromeSomeRead = new ChromoSomeReadService(referenceGenome);
			AnnotationReadImpl annotationReadImpl = new AnnotationReadImpl(file1.getAbsolutePath());
			AnnotationIncludeOrfLostReadReferences annotationReadReferences = new AnnotationIncludeOrfLostReadReferences();
			MapFileService mapfile=new MapFileService(mapfileLocation);
			ChromoSomeReadService targetchromoSomeRead = new ChromoSomeReadService(targetchromeSomeReadFileLocation);
			AnnotationReadIncludeOrfLostService annotationRead = new AnnotationReadIncludeOrfLostService(targetchromoSomeRead, referenceChromeSomeRead, mapfile);
			annotationRead.setAnnotationReadImpl(annotationReadImpl);
			annotationRead.updateInformation(true, false, false, false, false, false);
			annotationReadReferences.put(folder3.getName(), annotationRead);
			System.out.println("lift over done");
			for ( String key:  annotationReadImpl.getTranscriptHashSet().keySet() ){
				for(Transcript transcript : annotationReadImpl.getTranscriptHashSet().get(key)){
					String name = transcript.getName();
					AnnotationReadIncludeOrfLostService annotationReadIncludeOrfLostService = new AnnotationReadIncludeOrfLostService();
					String cdsSequenceString = "";
					String cdsSequenceString2 = "";
					//Begin be check the protein alignment
					File file = new File(folderOfThisAccession + File.separator + "proalignment" + File.separator + name);
					boolean ifOrfLost = true;
					String metaInformation="";
					if( file.isFile() && file.exists() && !file.getName().equalsIgnoreCase("core") ){
						BufferedReader reader = new BufferedReader(new FileReader(file));
						String tempString = null;
						Pattern p = Pattern.compile("^\\s*\\d+\\s*:\\s*([\\w\\.\\s\\>\\<\\-\\{\\}\\*]+)\\s*:\\s*\\d+\\s*$");
						StringBuffer sequenceBuffer = new StringBuffer();
						int lineNumber = 0;
						while ((tempString = reader.readLine()) != null) {
							Matcher m = p.matcher(tempString);
							if ( m.find() ){
								lineNumber++;
								if( lineNumber % 2 == 0 ){
									sequenceBuffer.append(m.group(1));
								}
							}
						}
						reader.close();
						
						cdsSequenceString = sequenceBuffer.toString();
						//System.out.println(cdsSequenceString);
						cdsSequenceString=cdsSequenceString.replaceAll("\\{", "");
						cdsSequenceString=cdsSequenceString.replaceAll("\\-", "");
						cdsSequenceString=cdsSequenceString.replaceAll("\\<", "");
						cdsSequenceString=cdsSequenceString.replaceAll("\\>", "");
						cdsSequenceString=cdsSequenceString.replaceAll("\\}", "");
						cdsSequenceString=cdsSequenceString.replaceAll("\\s", "");
						cdsSequenceString=cdsSequenceString.replaceAll("[a-z]+[a-z\\.]+[a-z]+", "");
						cdsSequenceString=cdsSequenceString.replaceAll("\\*", "");
						
						if (cdsSequenceString.length() < 3) {
							metaInformation += "_exonlengthLessThan3";
						} else {
							metaInformation += "_exonlengthMoreThan3";
							if (annotationReadIncludeOrfLostService.ifLengthCouldbeDivedBYThree(cdsSequenceString)) {
								metaInformation += "_exonlengthIsMultipleOf3";
								if (annotationReadIncludeOrfLostService.ifNewStopCOde(cdsSequenceString)) {
									metaInformation += "_premutureStopCoden";
								} else {
									metaInformation += "_noPremutureStopCoden";
									if (annotationReadIncludeOrfLostService.ifEndWithStopCode(cdsSequenceString)) {
										metaInformation += "_endWithStopCoden";
										if (annotationReadIncludeOrfLostService.ifStartWithStartCode(cdsSequenceString)) {
											metaInformation += "_startWithStartCoden_ConservedFunction";
											ifOrfLost = false;
										} else {
											metaInformation += "_notWithStartCoden";
										}
									} else {
										metaInformation += "_notEndWithStopCoden";
									}
								}
							} else {
								metaInformation += "_exonlengthIsNotMultipleOf3";
							}
						}
					}
					
					//Begin be check CDS alignment
					File file2 = new File(folderOfThisAccession + File.separator + "alignment" + File.separator + name);
					boolean ifOrfLost2 = true;
					String metaInformation2="";
					if(   file2.isFile() && file2.exists() && !file2.getName().equalsIgnoreCase("core") ){	
						BufferedReader reader2 = new BufferedReader(new FileReader(file2));
						String tempString2 = null;
						Pattern p2 = Pattern.compile("^\\s*\\d+\\s*:\\s*([\\w\\.\\s\\>\\<\\-\\{\\}\\*]+)\\s*:\\s*\\d+\\s*$");
						StringBuffer sequenceBuffer2 = new StringBuffer();
						int lineNumber2 = 0;
						while ((tempString2 = reader2.readLine()) != null) {
							Matcher m2 = p2.matcher(tempString2);
							if ( m2.find() ){
								lineNumber2++;
								if( lineNumber2 % 2 == 0 ){
									sequenceBuffer2.append(m2.group(1));
								}
							}
						}
						reader2.close();
						
						cdsSequenceString2 = sequenceBuffer2.toString();
						//System.out.println(cdsSequenceString);
						cdsSequenceString2=cdsSequenceString2.replaceAll("\\-", "");
						cdsSequenceString2=cdsSequenceString2.replaceAll("\\{", "");
						cdsSequenceString2=cdsSequenceString2.replaceAll("\\}", "");
						cdsSequenceString2=cdsSequenceString2.replaceAll("\\s", "");
						cdsSequenceString2=cdsSequenceString2.replaceAll("[a-z]+[a-z\\.]+[a-z]+", "");
						cdsSequenceString2=cdsSequenceString2.replaceAll("\\*", "");					
						
						if (cdsSequenceString2.length() < 3) {
							metaInformation2 += "_exonlengthLessThan3";
						} else {
							metaInformation2 += "_exonlengthMoreThan3";
							if (annotationReadIncludeOrfLostService.ifLengthCouldbeDivedBYThree(cdsSequenceString2)) {
								metaInformation2 += "_exonlengthIsMultipleOf3";
								if (annotationReadIncludeOrfLostService.ifNewStopCOde(cdsSequenceString2)) {
									metaInformation2 += "_premutureStopCoden";
								} else {
									metaInformation2 += "_noPremutureStopCoden";
									if (annotationReadIncludeOrfLostService.ifEndWithStopCode(cdsSequenceString2)) {
										metaInformation2 += "_endWithStopCoden";
										if (annotationReadIncludeOrfLostService.ifStartWithStartCode(cdsSequenceString2)) {
											metaInformation2 += "_startWithStartCoden_ConservedFunction";
											ifOrfLost2 = false;
										} else {
											metaInformation2 += "_notWithStartCoden";
										}
									} else {
										metaInformation2 += "_notEndWithStopCoden";
									}
								}
							} else {
								metaInformation2 += "_exonlengthIsNotMultipleOf3";
							}
						}
					}
					
					boolean ifOrfLost3=true;
					if( annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().containsKey(name) ){
						
					} else {
						System.out.println("could not find " + name);
					}
					String metaInformation3 = annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().get(name).getMetaInformation();
					
					
					if( annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().containsKey(name) ){
						if( annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().get(name).getMetaInformation().contains("ConservedFunction") ){
							ifOrfLost3=false; // ORF not shifted
						}else{
							 // ORF shift
						}
					}
					String cdsSequenceString3 = annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().get(name).getSequence();
					
					if( ifOrfLost && ifOrfLost2 && ifOrfLost3  ){ // three methods indicate this gene is loss of function
						outPut.print("  2 2"); // ORF shifted
						outPut2.println(">"+name+metaInformation+metaInformation2+metaInformation3+"_n1n2n31");
						outPut2.println(cdsSequenceString3);  // if all the methods indicate this is a loss of function gene. Output the liftover sequence
					}else if(!ifOrfLost){ // the protein alignment results on are preferred
						outPut.print("  1 1"); // ORF not shifted
						outPut2.println(">"+name+"_"+metaInformation+"_1");
						outPut2.println(cdsSequenceString);
					}else if(!ifOrfLost2){ // the CDS alignment results on are secondly preferred
						outPut.print("  1 1"); // ORF not shifted
						outPut2.println(">"+name+"_"+metaInformation2+"_2");
						outPut2.println(cdsSequenceString2);
					}else if(!ifOrfLost3){ // output of lift-over method
						outPut.print("  1 1"); // ORF not shifted
						outPut2.println(">"+name+"_"+metaInformation3+"_3");
						outPut2.println(cdsSequenceString3);
					}else{
						outPut.print("  0 0"); // ORF unknown
						outPut2.println(">"+name+"_"+metaInformation3+"_4");
						outPut2.println(cdsSequenceString3);
					}
					
					outPut1.println(chrs.get(name) + "\t" + name + "\t0\t" + positions.get(name));
					
					outPut2p.println(">"+name+"_"+metaInformation);
					outPut2p.println(cdsSequenceString);
					
					outPut2c.println(">"+name+"_"+metaInformation2);
					outPut2c.println(cdsSequenceString2);
					
					outPut2l.println(">"+name+"_"+metaInformation3);
					outPut2l.println(cdsSequenceString3);
										
					total++;
					if( !ifOrfLost && !ifOrfLost2 && !ifOrfLost3  ){
						n123++; // three methods indicate this is a functional allele
					}
					if( !ifOrfLost && ifOrfLost2 && ifOrfLost3  ){
						n1++; // only protein alignment indicates this is a functional allele
					}
					if( ifOrfLost && !ifOrfLost2 && ifOrfLost3  ){
						n2++; // only CDS alignment indicates this is a functional allele
					}
					if( ifOrfLost && ifOrfLost2 && !ifOrfLost3  ){
						n3++; // only sequence lift-over indicates this is a functional allele
					}
					if( !ifOrfLost && !ifOrfLost2 && ifOrfLost3  ){
						n12++; // protein and CDS alignment indicate this is a functional allele
					}
					if( !ifOrfLost && ifOrfLost2 && !ifOrfLost3  ){
						n13++; // protein alignment and lift-over indicate this is a functional allele
					}
					if( ifOrfLost && !ifOrfLost2 && !ifOrfLost3  ){
						n23++; // CDS alignment and lift-over indicate this is a functional allele
					}
				}
			}
			
			outPut3.println("n1 " + n1);
			outPut3.println("n2 " + n2);
			outPut3.println("n3 " + n3);
			outPut3.println("n12 " + n12);
			outPut3.println("n13 " + n13);
			outPut3.println("n23 " + n23);
			outPut3.println("n123 " + n123);
			outPut3.println("total " + total);
			outPut.close();
			outPut1.close();
			outPut2.close();
			outPut3.close();
			outPut2p.close();
			outPut2c.close();
			outPut2l.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	class Position{
		String chr;
		int position;
		public String getChr() {
			return chr;
		}
		public void setChr(String chr) {
			this.chr = chr;
		}
		public int getPosition() {
			return position;
		}
		public void setPosition(int position) {
			this.position = position;
		}
		public Position(String chr, int position) {
			super();
			this.chr = chr;
			this.position = position;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((chr == null) ? 0 : chr.hashCode());
			result = prime * result + position;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (chr == null) {
				if (other.chr != null)
					return false;
			} else if (!chr.equals(other.chr))
				return false;
			if (position != other.position)
				return false;
			return true;
		}
		private ExonerateProteinAndEstAndSdiToLofPed getOuterType() {
			return ExonerateProteinAndEstAndSdiToLofPed.this;
		}
	}
}

