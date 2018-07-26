package OtherFunctions.ORFgwasTestingUsedForNatureSubmission;

import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.CDS.Cds;
import me.songbx.model.Strand;
import me.songbx.model.Transcript.Transcript;
import me.songbx.service.AnnotationReadIncludeOrfLostService;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.util.StandardGeneticCode;
import me.songbx.util.exception.codingNotFound;
import me.songbx.util.exception.codingNotThree;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

// to do, check which does not fellow the gt..ag rule
public class ExtractCdsSequenceAndCheckORF {
	
	private String referenceGenome;
	private String gffFile;
	private String cdsOutPutFolder;
	private String proteinOutPutFolder;
	public ExtractCdsSequenceAndCheckORF( ){

	}
	public synchronized void setReferenceGenome(String referenceGenome) {
		this.referenceGenome = referenceGenome;
	}
	public synchronized void setGffFile(String gffFile) {
		this.gffFile = gffFile;
	}
	public synchronized void setCdsOutPutFolder(String cdsOutPutFolder) {
		this.cdsOutPutFolder = cdsOutPutFolder;
	}
	public synchronized void setProteinOutPutFolder(String proteinOutPutFolder) {
		this.proteinOutPutFolder = proteinOutPutFolder;
	}

	public ExtractCdsSequenceAndCheckORF(String [] argv ){
		StringBuffer helpMessage=new StringBuffer("Integrating effect pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -r   reference genome sequence in fasta format\n");
        helpMessage.append("  -a   reference gene structure annotation in GFF/GTF format\n");
        helpMessage.append("  -o   output CDS sequence for each transcript\n");
        helpMessage.append("  -p   output protein sequence for each transcript\n");
        
		Options options = new Options();
        options.addOption("r",true,"referenceGenome");
        options.addOption("a",true,"gffFile");
        options.addOption("o",true,"cdsOutPutFile");
        options.addOption("p",true,"proteinOutPutFile");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        
        if(cmd.hasOption("r")){
        	referenceGenome = cmd.getOptionValue("r");
        }else{
        	System.err.println("-r is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("a")){
        	gffFile = cmd.getOptionValue("a");
        }else{
        	System.err.println("-a is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("o")){
        	cdsOutPutFolder = cmd.getOptionValue("o");
        }else{
        	System.err.println("-o is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("p")){
        	proteinOutPutFolder = cmd.getOptionValue("p");
        }else{
        	System.err.println("-p is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        
        File folder = new File(cdsOutPutFolder);
		if( folder.exists() ){
			if(!folder.isDirectory() ){
				System.err.println(cdsOutPutFolder + " is not a floder");
        	}
		}else{
			folder.mkdirs();
		}
		folder = new File(proteinOutPutFolder);
		if( folder.exists() ){
			if(!folder.isDirectory() ){
				System.err.println(proteinOutPutFolder + " is not a floder");
        	}
		}else{
			folder.mkdirs();
		}
		doit();
	}
	
	public void doit(){
		try {
			StandardGeneticCode standardGeneticCode = new StandardGeneticCode();
			ChromoSomeReadService chromoSomeReadService = new ChromoSomeReadService(referenceGenome);
			AnnotationReadImpl annotationReadImpl = new AnnotationReadImpl(gffFile);
			HashMap<String, HashSet<Transcript>> sstranscriptHashSet = annotationReadImpl
					.getTranscriptHashSet();
			Iterator<String> chrNamesIt = sstranscriptHashSet.keySet().iterator();
			while (chrNamesIt.hasNext()) {
				String key = chrNamesIt.next();
				for (Transcript transcript : sstranscriptHashSet.get(key)) {
					ArrayList<Cds> cdsList = new ArrayList<Cds>();
					for (Cds cds : transcript.getCdsHashSet()) {
						cdsList.add(cds);
					}
					Collections.sort(cdsList);
					if( transcript.getStrand() == Strand.POSITIVE ){
						for( int i=1; i<cdsList.size(); i++ ){
							String seq = chromoSomeReadService.getSubSequence(key, cdsList.get(i-1).getEnd()+1, cdsList.get(i-1).getEnd()+2, transcript.getStrand());
							if( seq.equalsIgnoreCase("gt") ){
								
							}else if( seq.equalsIgnoreCase("gc") ){
								
							}else{
								System.out.println( transcript.getName() + " Strange Splice Sites " + seq );
							}
							seq = chromoSomeReadService.getSubSequence(key, cdsList.get(i).getStart()-2, cdsList.get(i).getStart()-1, transcript.getStrand());
							if( seq.equalsIgnoreCase("ag") ){
								
							}else{
								System.out.println( transcript.getName() + " Strange Splice Sites1 " + seq );
							}
						}
					}else{
						for( int i=1; i<cdsList.size(); i++ ){
							String seq = chromoSomeReadService.getSubSequence(key, cdsList.get(i-1).getStart()-2, cdsList.get(i-1).getStart()-1, transcript.getStrand());
							if( seq.equalsIgnoreCase("gt") ){
								 
							}else if( seq.equalsIgnoreCase("gc") ){
								
							}else{
								System.out.println( transcript.getName() + " Strange Splice Sites " + seq );
							}
							seq = chromoSomeReadService.getSubSequence(key, cdsList.get(i).getEnd()+1, cdsList.get(i).getEnd()+2, transcript.getStrand());
							if( seq.equalsIgnoreCase("ag") ){
								
							}else{
								System.out.println( transcript.getName() + " Strange Splice Sites1 " + seq );
							}
						}
					}
					StringBuffer sb = new StringBuffer();
					for (Cds cds : cdsList ) {
						String thisCdsSequence = chromoSomeReadService.getSubSequence(key,
								cds.getStart(), cds.getEnd(),
								cds.getTranscript().getStrand());
						sb.append(thisCdsSequence);
					}
					String metaInformation = "";
					String cdsSequenceString = sb.toString();
					AnnotationReadIncludeOrfLostService annotationReadIncludeOrfLostService = new AnnotationReadIncludeOrfLostService();
					
					boolean ifOrfLost = true;
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
					if(ifOrfLost){
						System.out.println( transcript.getName() + " " + metaInformation );
						PrintWriter outPut = new PrintWriter(cdsOutPutFolder + File.separator + transcript.getName());
						outPut.println(">" + transcript.getName() + " orfshift");
						outPut.println(cdsSequenceString);
						outPut.close();
					}else{
						PrintWriter outPut = new PrintWriter(cdsOutPutFolder + File.separator +transcript.getName());
						outPut.println(">" + transcript.getName());
						outPut.println(cdsSequenceString);
						outPut.close();
						
						StringBuffer ssSb = new StringBuffer();
						for (int jj = 0; jj <= cdsSequenceString.length() - 3; jj += 3) {
							try {
								ssSb.append(standardGeneticCode.getGeneticCode(cdsSequenceString.substring(jj, jj + 3), (0 == jj)));
							} catch (codingNotThree | codingNotFound e) {
								
							}
						}
						PrintWriter outPut2 = new PrintWriter(proteinOutPutFolder + File.separator +transcript.getName());
						outPut2.println(">" + transcript.getName());
						outPut2.println(ssSb);
						outPut2.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
