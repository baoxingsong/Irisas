package OtherFunctions.ORFgwasTestingUsedForNatureSubmission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.service.AnnotationReadIncludeOrfLostService;

public class ExonerateEstToLofPed {
	public static void main( String[] argv ){
		new ExonerateEstToLofPed(argv[0] );
	}
	public ExonerateEstToLofPed(String tempPath){
		HashMap<String, String> positions = new HashMap<String, String>();
		HashMap<String, String> chrs = new HashMap<String, String>();
		
		File file1 = new File("/biodata/dep_tsiantis/grp_gan/song/gennepredication/TAIR10annotationclassification/unmodifyed/TAIR10_GFF3_genes_no_UM.gff");
        try {
        	BufferedReader reader = null;
        	reader = new BufferedReader(new FileReader(file1));
            String tempString = null;
            Pattern p = Pattern.compile("^(\\S*)\t(.*)\tmRNA\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\t(\\S*)\tID=(.*);Parent(.*?)");
			while ((tempString = reader.readLine()) != null) {
				Matcher m = p.matcher(tempString);
				if ( m.find() ){
					positions.put(m.group(8), m.group(3));
					chrs.put(m.group(8), m.group(1).replaceAll("Chr", ""));
				}
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File folder2 = new File(tempPath + File.separator + "cdsExonerateEst");
        folder2.mkdirs();
        
		File folder = new File(tempPath + File.separator + "alignment");
		try {
			File[] listOfFiles = folder.listFiles();
			PrintWriter outPut = new PrintWriter(tempPath + File.separator+"exonerateEst.ped");
			PrintWriter outPut1 = new PrintWriter(tempPath + File.separator+"exonerateEst.map");
			PrintWriter outPut2 = new PrintWriter(tempPath + File.separator+"cdsExonerateEst"+File.separator+"exonerateEstSequences.fa");
			for( File f : listOfFiles ){
				if(  f.isFile() ){
					File file = new File(tempPath + File.separator + "alignment" + File.separator + f.getName());
					BufferedReader reader = null;
					reader = new BufferedReader(new FileReader(file));
					String tempString = null;
					Pattern p = Pattern.compile("\\d\\s*:\\s*([\\w\\.\\s\\>-]+)\\s*:\\s*\\d");
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
					String cdsSequenceString = sequenceBuffer.toString();
					System.out.println(cdsSequenceString);
					cdsSequenceString=cdsSequenceString.replaceAll("\\s", "");
					cdsSequenceString=cdsSequenceString.replaceAll("[a-z]+\\.+[a-z]+", "");
					AnnotationReadIncludeOrfLostService annotationReadIncludeOrfLostService = new AnnotationReadIncludeOrfLostService();
					boolean ifOrfLost = true;
					String metaInformation="";
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
					File folder3 = new File(tempPath);
					if( ifOrfLost  ){
						outPut.println(folder3.getName() + "  " + folder3.getName() + "  0 0  1  1  2 2"); // ORF shifted
					}else{
						outPut.println(folder3.getName() + "  " + folder3.getName() + "  0 0  1  1  1 1"); // ORF not shifted
					}
					outPut1.println(chrs.get(f.getName()) + "\t" + f.getName() + "\t0\t" + positions.get(f.getName()));
					outPut2.println(">"+f.getName()+"_"+metaInformation);
					outPut2.println(cdsSequenceString);
				}
			}
			outPut.close();
			outPut1.close();
			outPut2.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
