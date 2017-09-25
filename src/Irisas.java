import OtherFunctions.IndelGwas.SdiIndelToPedV2;
import OtherFunctions.IndelGwas.SNPgwas.SdiSnpToPed;
import OtherFunctions.ORFGwas.ExonerateProteinAndEstAndSdiToLofPed;
import OtherFunctions.ORFgwasTestingUsedForNatureSubmission.ExtractCdsSequenceAndCheckORF;
import OtherFunctions.ORFgwasTestingUsedForNatureSubmission.ExtractGenomeSequceAccordingtoCDs;
import OtherFunctions.ReSdiWithWindowsedMsa.CutTheWholeGenomeWithaWindowRamSaveVersion;
import OtherFunctions.ReSdiWithWindowsedMsa.ReSDIFromMsaAAAVLinkversion;
import me.songbx.action.EasyRun;

public class Irisas {
	public static void main(String[] argv) {
		StringBuffer helpMessage=new StringBuffer("INDEL synchronization and Integrating effect GWAS pipeline\nE-mail:song@mpipz.mpg.de\n");
		helpMessage.append("\nUsage:  java -jar iia.jar <command> [options]\n");
		helpMessage.append("\nCommands:\n");
		helpMessage.append("\n\n");

		helpMessage.append("  -- One command for all the steps \n");
		helpMessage.append("\n");
		helpMessage.append("     EasyRun                         run all the steps with single command\n");
		helpMessage.append("\n\n");

		helpMessage.append("  -- Functions for whole genome wide variants synchronization \n");
		helpMessage.append("\n");
		helpMessage.append("     CutTheWholeGenomeWithWindow     cut the whole genome sequence with a window\n");
		helpMessage.append("     NewSdiFromMsa                   generate sdi files from MSA results\n");
		helpMessage.append("     SdiToSnpPlink                   generate PLINK files of SNP\n");
		helpMessage.append("     SdiToIndelPlink                 generate PLINK files of INDEL\n");
		helpMessage.append("\n\n");

		helpMessage.append("  -- Functions for integrating effect\n");
		helpMessage.append("\n");
		helpMessage.append("     ExtractCdsSequenceAndCheckFunc  extract CDS&protein sequence\n");
		helpMessage.append("     ExtractGenomeSequce             extract the genome sequence of each transcript\n");
		helpMessage.append("     GenerateLofPed                  generate PLINK files of integrating effect\n");

		
		if( argv.length < 1 ){
			System.err.println(helpMessage);
		} else if ( argv[0].equalsIgnoreCase("CutTheWholeGenomeWithWindow") ){
			new CutTheWholeGenomeWithaWindowRamSaveVersion(argv);
		} else if ( argv[0].equalsIgnoreCase("NewSdiFromMsa") ){
			new ReSDIFromMsaAAAVLinkversion(argv);
		} else if ( argv[0].equalsIgnoreCase("SdiToSnpPlink") ){
			new SdiSnpToPed(argv);
		} else if ( argv[0].equalsIgnoreCase("SdiToIndelPlink") ){
			new SdiIndelToPedV2(argv);
		} 
		// ORF-states related functions
		else if ( argv[0].equalsIgnoreCase("ExtractCdsSequenceAndCheckFunc") ){
			new ExtractCdsSequenceAndCheckORF(argv);
		} else if ( argv[0].equalsIgnoreCase("ExtractGenomeSequce") ){
			new ExtractGenomeSequceAccordingtoCDs(argv);
		} else if ( argv[0].equalsIgnoreCase("GenerateLofPed") ){
			new ExonerateProteinAndEstAndSdiToLofPed(argv);
		}
		// run everything with one command
		else if ( argv[0].equalsIgnoreCase("EasyRun") ){
			new EasyRun(argv);
		} else {
			System.err.println(helpMessage);
		}
	}
}

//
//Manifest-Version: 1.0
//Rsrc-Class-Path: ./ BigWig.jar commons-cli-1.2.jar commons-lang3-3.4.j
// ar commons-math3-3.0.jar dom4j-1.6.1.jar hamcrest-core-1.1.0.jar jaco
// coant.jar java-genomics-io.jar junit.jar log4j-1.2.15.jar sam-1.67.ja
// r
//Class-Path: .
//Rsrc-Main-Class: Wmsa
//Main-Class: org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader
