package OtherFunctions.ReSdiWithWindowsedMsa;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndelSnpPlinkFromMsaAAA {
	private int threadNumber = 5;
    private int numberOfAccessions = 50;

    private String msaFolder;
    private String accessionListFile;
    private String refName;
    private ArrayList<String> chrs = new  ArrayList<String>();
    private String outPutPath;
    private String genomeFolder;

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }
    public void setNumberOfAccessions(int numberOfAccessions) {
        this.numberOfAccessions = numberOfAccessions;
    }
    public void setMsaFolder(String msaFolder) {
        this.msaFolder = msaFolder;
    }
    public void setAccessionListFile(String accessionListFile) {
        this.accessionListFile = accessionListFile;
    }
    public void setRefName(String refName) {
        this.refName = refName;
    }
    public void setChrs(ArrayList<String> chrs) {
        this.chrs = chrs;
    }
    public void setOutPutPath(String outPutPath) {
        this.outPutPath = outPutPath;
    }
    public void setGenomeFolder(String genomeFolder) {
        this.genomeFolder = genomeFolder;
    }
    public IndelSnpPlinkFromMsaAAA(){

    }

	public IndelSnpPlinkFromMsaAAA(String[] argv ){
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization pipeline\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number. (Default 5)\n");
        helpMessage.append("  -n   [integer] number of accession to process for each batch, should be larger than thread number (Default 50)\n");
        helpMessage.append("  -i   input folder where could find MSA result\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -c   list of chromosome names\n");
        helpMessage.append("  -o   output folder\n");
        helpMessage.append("  -g   the folder where the genome sequences and sdi files are located\n");
        
		Options options = new Options();
        options.addOption("t",true,"threadnumber");
        options.addOption("n",true,"numberOfAccessions");
        options.addOption("i",true,"msaFolder");
        options.addOption("l",true,"accessionListFile");
        options.addOption("r",true,"refName");
        options.addOption("c",true,"chrlist");
        options.addOption("o",true,"outPutPath");
        options.addOption("g",true,"genomeFolder");
        
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        if(cmd.hasOption("t")){
        	threadNumber = Integer.parseInt(cmd.getOptionValue("t"));
        }
        if(cmd.hasOption("n")){
        	numberOfAccessions = Integer.parseInt(cmd.getOptionValue("n"));
        }
        if(cmd.hasOption("i")){
        	msaFolder = cmd.getOptionValue("i");
        }else{
        	System.err.println("-i is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("l")){
        	accessionListFile = cmd.getOptionValue("l");
        }else{
        	System.err.println("-l is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("r")){
        	refName = cmd.getOptionValue("r");
        }else{
        	System.err.println("-r is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        String chrlist="";
        if(cmd.hasOption("c")){
        	chrlist = cmd.getOptionValue("c");
        }else{
        	System.err.println("-c is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("o")){
        	outPutPath = cmd.getOptionValue("o");
        }else{
        	System.err.println("-o is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }
        if(cmd.hasOption("g")){
        	genomeFolder = cmd.getOptionValue("g");
        }else{
        	System.err.println("-g is missing. Please, check the parameters.");
        	System.err.println(helpMessage);
            System.exit(1);
        }


        File file2 = new File(chrlist);
        BufferedReader reader2 = null;
        try {
            reader2 = new BufferedReader(new FileReader(file2));
            String tempString = null;
            while ((tempString = reader2.readLine()) != null) {
                chrs.add(tempString);
            }
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e1) {
                    e1.getStackTrace();
                }
            }
        }

        doit();
	}
	
	
	public void doit(){
        // prepare accession List begin
		ArrayList<String> names = new  ArrayList<String>();
		File file = new File(accessionListFile);
		BufferedReader reader = null;
        try {
        	reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	names.add(tempString);
            }
            names.add(refName);
        }catch (IOException e) {
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
        // prepare accession List end
        
        // prepare folder for output begin
        File outPutPathFolder = new File(outPutPath);
		if( outPutPathFolder.exists() ){
			
		}else{
			outPutPathFolder.mkdirs();
		}
        // prepare folder for output end

        for(String ss1 : chrs){
        	if( (new File(msaFolder + File.separator + ss1)).isDirectory() ){
        	    // get msa file list begin
        		HashMap<String, ArrayList<MsaFile>> msaFileLocationsHashmap = new HashMap<String, ArrayList<MsaFile>>();
				msaFileLocationsHashmap.put(ss1, new ArrayList<MsaFile>());
				File f = new File(msaFolder + File.separator + ss1 + File.separator);
				String s[]=f.list();
				Pattern p = Pattern.compile("^(\\d+)_(\\d+)\\.mafft$");
				for(String ss : s){
					Matcher m=p.matcher(ss);
					if(m.find()){
                        MsaFile msaFile = new MsaFile(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), msaFolder + File.separator + ss1 + File.separator + ss);
						msaFileLocationsHashmap.get(ss1).add(msaFile);
					}
				}
                // get msa file list end

                // prepare folder for output begin
				File outputFolder = new File(outPutPath + File.separator + ss1+"/");
				if( outputFolder.exists() ){
					
				}else{
					outputFolder.mkdirs();
				}// prepare folder for output end

                new IndelSnpPlinkFromMsaAction(names, msaFileLocationsHashmap, threadNumber, outPutPath + File.separator+ss1+File.separator, refName, genomeFolder);
			}
		}
	}
}

