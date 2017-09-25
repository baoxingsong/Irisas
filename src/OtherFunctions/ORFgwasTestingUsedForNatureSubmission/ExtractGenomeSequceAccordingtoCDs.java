package OtherFunctions.ORFgwasTestingUsedForNatureSubmission;

import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.CDS.Cds;
import me.songbx.model.Transcript.Transcript;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.service.MapFileService;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExtractGenomeSequceAccordingtoCDs {
	private String targetgenomeSequence;
	private String sdiFile;
	private String gffFile;
	private String outPutFolder;
	private int interval = 1000;

	public void setTargetgenomeSequence(String targetgenomeSequence) {
		this.targetgenomeSequence = targetgenomeSequence;
	}
	public void setSdiFile(String sdiFile) {
		this.sdiFile = sdiFile;
	}
	public void setGffFile(String gffFile) {
		this.gffFile = gffFile;
	}
	public void setOutPutFolder(String outPutFolder) {
		this.outPutFolder = outPutFolder;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public ExtractGenomeSequceAccordingtoCDs(){

	}
	private ArrayList<String> outPutFiles = new ArrayList<String>();
	public ArrayList<String> getOutPutFiles(){
		return outPutFiles;
	}
	public ExtractGenomeSequceAccordingtoCDs( String [] argv ){
		StringBuffer helpMessage=new StringBuffer("Integrating effect pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -m   genome sequence of target accession/line in fasta format\n");
        helpMessage.append("  -s   sdi file of target accession/line\n");
        helpMessage.append("  -a   reference gene structure annotation in GFF/GTF format\n");
        helpMessage.append("  -o   output folder for the current accession/line. (it is called exonerate working folder)\n");
        helpMessage.append("  -i   [integer] extend interval (Default 1000)\n");
        
		Options options = new Options();
        options.addOption("m",true,"targetgenomeSequence");
        options.addOption("s",true,"sdiFile");
        options.addOption("a",true,"gffFile");
        options.addOption("o",true,"cdsOutPutFile");
        options.addOption("i",true,"interval");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        
        if(cmd.hasOption("m")){
        	targetgenomeSequence = cmd.getOptionValue("m");
        }else{
        	System.err.println("-m is missing. Please, check the parameters.");
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
        if(cmd.hasOption("a")){
        	gffFile = cmd.getOptionValue("a");
        }else{
        	System.err.println("-a is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("o")){
        	outPutFolder = cmd.getOptionValue("o");
        }else{
        	System.err.println("-o is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("i")){
        	interval = Integer.parseInt(cmd.getOptionValue("i"));
        }
        File folder = new File(outPutFolder);
		if( folder.exists() ){
			if(folder.isDirectory() ){
				System.err.println(outPutFolder + " is not a floder");
        	}
		}else{
			folder.mkdirs();
		}
		doIt();
	}
	
	public void doIt(){
		File dir = new File(outPutFolder);
		dir.mkdirs();
		dir = new File(outPutFolder+File.separator+"genomeSequence");
		dir.mkdirs();
		
		try {
			AnnotationReadImpl annotationReadImpl = new AnnotationReadImpl(gffFile);
			ChromoSomeReadService chromoSomeReadService = new ChromoSomeReadService(targetgenomeSequence);
			MapFileService mapfile=new MapFileService(sdiFile);
			
			HashMap<String, HashSet<Transcript>> sstranscriptHashSet = annotationReadImpl.getTranscriptHashSet();
			for (String chName : sstranscriptHashSet.keySet() ) {
				for (Transcript transcript : sstranscriptHashSet.get(chName)) {
					int cdsStart = Integer.MAX_VALUE;
					int cdsEnd = 0;
					for (Cds cds : transcript.getCdsHashSet()) {
						if( cds.getStart()<cdsStart ){
							cdsStart = cds.getStart(); //for the implementation of AnnotationReadImpl, start is always less than end
						}
						if( cds.getEnd()>cdsEnd ){
							cdsEnd = cds.getEnd();
						}
					}
					int start = mapfile.getChangedFromBasement(chName,cdsStart) - interval;
					if( start <=0 ){
						start=1;
					}
					int end = mapfile.getChangedFromBasement(chName,cdsEnd) + interval;
					if( end > chromoSomeReadService.getChromoSomeById(chName).getSequence().length() ){
						end = chromoSomeReadService.getChromoSomeById(chName).getSequence().length();
					}
					String seqs = chromoSomeReadService.getSubSequence(chName, start, end, transcript.getStrand());
					String outputFile = outPutFolder+File.separator+"genomeSequence"+File.separator+transcript.getName();
					outPutFiles.add(outputFile);
					PrintWriter outPut = new PrintWriter(outputFile);
					outPut.println(">"+transcript.getName()+"_genome");
					outPut.println(seqs);
					outPut.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
