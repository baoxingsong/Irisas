package me.songbx.action;

import OtherFunctions.IndelGwas.SNPgwas.SdiSnpToPed;
import OtherFunctions.IndelGwas.SdiIndelToPedV2;
import OtherFunctions.ORFGwas.ExonerateProteinAndEstAndSdiToLofPed;
import OtherFunctions.ORFgwasTestingUsedForNatureSubmission.ExtractCdsSequenceAndCheckORF;
import OtherFunctions.ORFgwasTestingUsedForNatureSubmission.ExtractGenomeSequceAccordingtoCDs;
import OtherFunctions.PopulationStructure.Model.MarkerPostion;
import OtherFunctions.PopulationStructure.Model.MarkerPostionS;
import OtherFunctions.ReSdiWithWindowsedMsa.CutTheWholeGenomeWithaWindowRamSaveVersion;
import OtherFunctions.ReSdiWithWindowsedMsa.ReSDIFromMsaAAAVLinkversion;
import me.songbx.impl.RunSystemCommandList;
import me.songbx.impl.RunSystemCommands;
import me.songbx.util.MyThreadCount;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasyRun {
    private int threadNumber = 5;
    private int windowSize = 10000;
    private int overLapSize = 500;
    private String outPutPath = "originalSeq";
    private String refName;
    private String accessionListFile;
    private String genomeFolder;

    private int numberOfAccessions = 5;

    //INDEL TO PLINK
    private boolean ifcheckOverLap = true;

    private String gffFile;

    private int interval = 1000;

    public  EasyRun( String[] argv ){
        StringBuffer helpMessage=new StringBuffer("run the whole pipeline with one command\nE-mail:song@mpipz.mpg.de\nArguments:\n");
        helpMessage.append("  -t   [integer] thread number. (Default 5)\n");
        helpMessage.append("  -w   [integer] window size.  (Default 10000)\n");
        helpMessage.append("  -v   [integer] extend size between two neighbour windows (Default 500)\n");
        helpMessage.append("  -r   name of reference accession/line\n");
        helpMessage.append("  -l   list of accession names\n");
        helpMessage.append("  -g   the folder where the genome sequences and sdi files are located\n");

        helpMessage.append("  -n   [integer] number of accession to process for each batch, should be larger than thread number (Default 50)\n");

        helpMessage.append("  -a   reference gene structure annotation in GFF/GTF format\n");

        helpMessage.append("  -i   [integer] extend interval (Default 1000)\n");

        Options options = new Options();
        options.addOption("t",true,"threadnumber");
        options.addOption("w",true,"windowSize");
        options.addOption("v",true,"overLapSize");

        options.addOption("r",true,"referenceName");
        options.addOption("l",true,"accessionListFile");
        options.addOption("g",true,"genomeFolder");

        options.addOption("n",true,"numberOfAccessions");

        options.addOption("a",true,"gffFile");

        options.addOption("i",true,"interval");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd=null;
        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println("Please, check the parameters.");
            e.printStackTrace();
            System.exit(1);
        }
        // BEGIN CUT
        if(cmd.hasOption("t")){
            threadNumber = Integer.parseInt(cmd.getOptionValue("t"));
        }
        if(cmd.hasOption("w")){
            windowSize = Integer.parseInt(cmd.getOptionValue("w"));
        }
        if(cmd.hasOption("v")){
            overLapSize = Integer.parseInt(cmd.getOptionValue("v"));
        }
        if(cmd.hasOption("r")){
            refName = cmd.getOptionValue("r");
        }else{
            System.err.println("-r is missing. Please, check the parameters.");
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

        if(cmd.hasOption("g")){
            genomeFolder = cmd.getOptionValue("g");
        }else{
            System.err.println("-g is missing. Please, check the parameters.");
            System.err.println(helpMessage);
            System.exit(1);
        }
        // END CUT

        // begin NewSdiFromMsa
        if(cmd.hasOption("n")){
            numberOfAccessions = Integer.parseInt(cmd.getOptionValue("n"));
        }
        //end NewSdiFromMsa


        if(cmd.hasOption("a")){
            gffFile = cmd.getOptionValue("a");
        }else{
            System.err.println("-a is missing. Please, check the parameters.");
            System.err.println(helpMessage);
            System.exit(1);
        }

        if(cmd.hasOption("i")){
            interval = Integer.parseInt(cmd.getOptionValue("i"));
        }

        //check environment begin
        if( checkSoftwareInstallation() ){
            System.out.println("software installation checked");
        }else{
            System.exit(1);
        }
        //check environment end


        // cut genome begin
        CutTheWholeGenomeWithaWindowRamSaveVersion cutTheWholeGenomeWithaWindowRamSaveVersion = new CutTheWholeGenomeWithaWindowRamSaveVersion();
        cutTheWholeGenomeWithaWindowRamSaveVersion.setThreadNumber(threadNumber);
        cutTheWholeGenomeWithaWindowRamSaveVersion.setWindowSize(windowSize);
        cutTheWholeGenomeWithaWindowRamSaveVersion.setOverLapSize(overLapSize);
        cutTheWholeGenomeWithaWindowRamSaveVersion.setOutPutPath(outPutPath);
        cutTheWholeGenomeWithaWindowRamSaveVersion.setRefName(refName);
        cutTheWholeGenomeWithaWindowRamSaveVersion.setAccessionListFile(accessionListFile);
        cutTheWholeGenomeWithaWindowRamSaveVersion.setGenomeFolder(genomeFolder);
        cutTheWholeGenomeWithaWindowRamSaveVersion.runit();
        ArrayList<String> fastaFiles = cutTheWholeGenomeWithaWindowRamSaveVersion.getFastaFiles();
        // cut genome end
        System.err.println("cut the genome sequence into fragments done");

        // run mafft begin
        MyThreadCount threadCount = new MyThreadCount(0);
        for( String fastaFile : fastaFiles ){
            String outPutFastaFile = fastaFile + ".mafft";
            boolean isThisThreadUnrun=true;
            while(isThisThreadUnrun){
                if(threadCount.getCount() < threadNumber){
                    String command = "mafft --auto " + fastaFile + " > " + outPutFastaFile;
                    RunSystemCommands runSystemCommand = new RunSystemCommands(command, threadCount);
                    threadCount.plusOne();
                    runSystemCommand.start();
                    isThisThreadUnrun=false;
                    break;
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.gc();
        while(threadCount.hasNext()){// wait for all the thread
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // run mafft end
        System.err.println("fragments multiple sequence alignment done");

        String newsdiFileLocation = "newSdiFiles";
        // generate new sdi files begin
        ReSDIFromMsaAAAVLinkversion reSDIFromMsaAAAVLinkversion = new ReSDIFromMsaAAAVLinkversion();
        reSDIFromMsaAAAVLinkversion.setThreadNumber(threadNumber);
        reSDIFromMsaAAAVLinkversion.setThreadNumber(numberOfAccessions);
        reSDIFromMsaAAAVLinkversion.setMsaFolder(outPutPath);
        reSDIFromMsaAAAVLinkversion.setAccessionListFile(accessionListFile);
        reSDIFromMsaAAAVLinkversion.setRefName(refName);
        reSDIFromMsaAAAVLinkversion.setOutPutPath(newsdiFileLocation);
        reSDIFromMsaAAAVLinkversion.setGenomeFolder(genomeFolder);
        reSDIFromMsaAAAVLinkversion.setChrs(cutTheWholeGenomeWithaWindowRamSaveVersion.getChrsList());
        reSDIFromMsaAAAVLinkversion.doit();
        // generate new sdi files end

        // merge sdi files begin
        HashSet<String> names = new  HashSet<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                tempString=tempString.replaceAll("\\s", "");
                if( tempString.length() > 0 ){
                    names.add(tempString);
                }
            }
            names.add(refName);
            reader.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for( String accessionName : names ) {
            if( accessionName.equals(refName) ){
                try {
                    PrintWriter outPutIDMap = new PrintWriter("./newSdiFiles/" + accessionName+".sdi");
                    outPutIDMap.print("");
                    outPutIDMap.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    PrintWriter outPutIDMap = new PrintWriter("./newSdiFiles/" + accessionName +".sdi");
                    int lineNumber = 0;
                    for (String chromosomeName : cutTheWholeGenomeWithaWindowRamSaveVersion.getChrsList()) {
                        BufferedReader reader = new BufferedReader(new FileReader("./newSdiFiles"+ File.separator + chromosomeName + File.separator + accessionName + "_" + chromosomeName +".myv2.sdi"));
                        String tempString = null;
                        while ((tempString = reader.readLine()) != null) {
                            if( lineNumber > 0 ){
                                outPutIDMap.print("\n"+tempString);
                            }else{
                                outPutIDMap.print(tempString);
                            }
                            lineNumber++;
                        }
                    }
                    outPutIDMap.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        // merge sdi files end
        System.err.print("generating new sdi files done");
        // sdi to SNP plink begin
        SdiSnpToPed sdiSnpToPed = new SdiSnpToPed();
        sdiSnpToPed.setThreadNumber(threadNumber);
        sdiSnpToPed.setAccessionListFile( accessionListFile);
        sdiSnpToPed.setRefName( refName);
        sdiSnpToPed.setGenomeFolder( genomeFolder);
        sdiSnpToPed.setSdiLocation(newsdiFileLocation);
        sdiSnpToPed.doIt();
        // sdi to SNP plink end
        System.err.print("SNP PLINK files are ready");

        // sdi to INDEL plink begin
        SdiIndelToPedV2 SdiIndelToPedV2 = new SdiIndelToPedV2();
        SdiIndelToPedV2.setThreadNumber(threadNumber);
        SdiIndelToPedV2.setIfcheckOverLap(ifcheckOverLap);
        SdiIndelToPedV2.setAccessionListFile( accessionListFile);
        SdiIndelToPedV2.setSdiLocation( newsdiFileLocation);
        SdiIndelToPedV2.doit();
        // sdi to INDEL plink end
        System.err.print("INDEL PLINK files are ready");


        System.err.print("Integrating analysis begin");
        System.err.print("preparing reference gene structure and sequence begin");
        // preparing reference begin
        ExtractCdsSequenceAndCheckORF extractCdsSequenceAndCheckORF = new ExtractCdsSequenceAndCheckORF();
        String cdsOutPutFolder="reference/cds";
        String proteinOutPutFolder="reference/protein";
        File file1 = new File("reference");
        if( file1.exists() && file1.isDirectory() ){

        }else{
            file1.mkdir();
        }
        file1 = new File(cdsOutPutFolder);
        if( file1.exists() && file1.isDirectory() ){

        }else{
            file1.mkdir();
        }
        file1 = new File(proteinOutPutFolder);
        if( file1.exists() && file1.isDirectory() ){

        }else{
            file1.mkdir();
        }

        String referenceGenome = genomeFolder + File.separator + refName + ".fa";
        extractCdsSequenceAndCheckORF.setReferenceGenome(referenceGenome);
        extractCdsSequenceAndCheckORF.setGffFile(gffFile);
        extractCdsSequenceAndCheckORF.setCdsOutPutFolder(cdsOutPutFolder);
        extractCdsSequenceAndCheckORF.setProteinOutPutFolder(proteinOutPutFolder);
        extractCdsSequenceAndCheckORF.doit();
        // preparing reference end
        System.err.print("preparing reference gene structure and sequence end");

        //ExtractGenomeSequceAccordingtoCDs begin
        try {
            BufferedReader reader = new BufferedReader(new FileReader(accessionListFile));
            String tempString = null;
            ArrayList<String> accessionNames = new ArrayList<String>();
            while ((tempString = reader.readLine()) != null) {
                accessionNames.add(tempString);

//                if( tempString.equalsIgnoreCase(refName) ){
//
//                }else{
//                    accessionNames.add(tempString);
//                }
            }

            PrintWriter outPutIDForPlink = new PrintWriter("listForPlinkMerge");
            for( String accessionName : accessionNames) {
                System.err.print(accessionName + " gene structure and sequence is doing");
                outPutIDForPlink.println(accessionName+"/orf.ped "+accessionName+"/orf.map");
                ExtractGenomeSequceAccordingtoCDs extractGenomeSequceAccordingtoCDs = new ExtractGenomeSequceAccordingtoCDs();
                extractGenomeSequceAccordingtoCDs.setTargetgenomeSequence(genomeFolder+File.separator+accessionName+".fa");
                extractGenomeSequceAccordingtoCDs.setSdiFile(genomeFolder+File.separator+accessionName+".sdi");
                extractGenomeSequceAccordingtoCDs.setGffFile(gffFile);
                extractGenomeSequceAccordingtoCDs.setOutPutFolder(accessionName);
                extractGenomeSequceAccordingtoCDs.setInterval(interval);
                extractGenomeSequceAccordingtoCDs.doIt();

                file1 = new File(accessionName+"/alignment/");
                if( file1.exists() && file1.isDirectory() ){

                }else{
                    file1.mkdir();
                }
                MyThreadCount threadCount1 = new MyThreadCount(0);
                for( String outPutFile :  extractGenomeSequceAccordingtoCDs.getOutPutFiles() ){
                    boolean isThisThreadUnrun=true;
                    while(isThisThreadUnrun){
                        if(threadCount1.getCount() < threadNumber){
                            File file = new File(outPutFile);

                            String command = "exonerate --maxintron 30000 --model est2genome -i -10 --score 10 --bestn 1 --minintron 10 ./reference/cds/"+file.getName()+" ./"+accessionName+"/genomeSequence/"+file.getName() + " > ./"+accessionName+"/alignment/"+file.getName();
                            RunSystemCommands runSystemCommand = new RunSystemCommands(command, threadCount1);
                            threadCount1.plusOne();
                            runSystemCommand.start();
                            isThisThreadUnrun=false;
                            break;
                        }else{
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                System.gc();
                while(threadCount1.hasNext()){// wait for all the thread
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ExonerateProteinAndEstAndSdiToLofPed exonerateProteinAndEstAndSdiToLofPed = new ExonerateProteinAndEstAndSdiToLofPed();
                exonerateProteinAndEstAndSdiToLofPed.setGffFile(gffFile);
                exonerateProteinAndEstAndSdiToLofPed.setFolderOfThisAccession(accessionName);
                exonerateProteinAndEstAndSdiToLofPed.setSdiFile(genomeFolder+File.separator+accessionName+".sdi");
                exonerateProteinAndEstAndSdiToLofPed.setTargetgenomeSequence(genomeFolder+File.separator+accessionName+".fa");
                exonerateProteinAndEstAndSdiToLofPed.setReferenceGenome(referenceGenome);
                exonerateProteinAndEstAndSdiToLofPed.doIt();
            }
            outPutIDForPlink.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch ( IOException e) {
            e.printStackTrace();
        }//ExtractGenomeSequceAccordingtoCDs end

        System.err.print("begin to generate integrating (ORF) PLINK files");
        //generateLofPed begin
        try {
            Process p = Runtime.getRuntime().exec("plink --merge-list listForPlinkMerge --out orf");
            p.waitFor();
        }catch(Exception e1) {
            e1.getStackTrace();
        }//generateLofPed end
        System.err.print("done");
    }
    boolean checkSoftwareInstallation(){
        boolean mafft = false;
        boolean plink = false;
        boolean exonerate = false;

        System.out.println("checking plink");
        {// check plink
            try {
                Runtime r = Runtime.getRuntime();
                String[] cmd = {"sh", "-c", "plink", "--version"};
                Process p = r.exec(cmd);
                BufferedInputStream is = new BufferedInputStream(p.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                p.waitFor();

                String s = null;
                while ((s = reader.readLine()) != null) {
                    if (s.contains("PLINK")) {
                        plink = true;
                    }
                }
                p.destroy();
                reader.close();
                if (!plink) {
                    System.err.println("plink is not well installed, please install it (https://www.cog-genomics.org/plink2/) and put the executable file under your PATH environment variable. ");
                }
            } catch (Exception e1) {
                e1.getStackTrace();
            }
        }
        System.out.println("checking exonerate");
        {// check exonerate
            try {
                Runtime r = Runtime.getRuntime();
                String[] cmd = {"sh", "-c", "exonerate"};
                Process p = r.exec(cmd);
                BufferedInputStream is = new BufferedInputStream(p.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                p.waitFor();

                String s = null;
                while ((s = reader.readLine()) != null) {
                    if (s.contains("version")) {
                        exonerate = true;
                    }
                }
                p.destroy();
                reader.close();
                if (!exonerate) {
                    System.err.println("exonerate is not well installed, please install it (http://www.ebi.ac.uk/about/vertebrate-genomics/software/exonerate) and put the executable file under your PATH environment variable. ");
                }
            } catch (Exception e1) {
                e1.getStackTrace();
            }
        }
        System.out.println("checking mafft");
        {// check mafft
            try {
                Runtime r = Runtime.getRuntime();
                String[] cmd = {"sh", "-c","mafft --version"};
                Process p = r.exec(cmd);
                BufferedInputStream is = new BufferedInputStream(p.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                BufferedInputStream is2 = new BufferedInputStream(p.getErrorStream());
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(is2));
                p.waitFor();
                String s = null;
                while ((s = reader.readLine()) != null) {
                    if (s.contains("v")) {
                        mafft = true;
                    }
                }
                while ((s = reader2.readLine()) != null) {
                    if (s.contains("v")) {
                        mafft = true;
                    }
                }
                p.destroy();
                reader.close();
                reader2.close();
                if (!mafft) {
                    System.err.println("mafft is not well installed, please install it (http://mafft.cbrc.jp/alignment/software/) and put the executable file under your PATH environment variable. ");
                }
            } catch (Exception e1) {
                e1.getStackTrace();
            }
        }
        if( exonerate && mafft && plink ){
            return true;
        }else{
            return false;
        }
    }
}
