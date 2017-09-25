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

public class ExonerateProteinAndEstToLofPed {
	public static void main( String[] argv ){
		new ExonerateProteinAndEstToLofPed(argv[0] );
	}
	public ExonerateProteinAndEstToLofPed(String tempPath){
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
        File folder2 = new File(tempPath + File.separator + "cdsExonerateProtein");
        folder2.mkdirs();
        
		File folder = new File(tempPath + File.separator + "proalignment");
		try {
			File[] listOfFiles = folder.listFiles();
			PrintWriter outPut = new PrintWriter(tempPath + File.separator+"exonerateProtein.ped");
			PrintWriter outPut1 = new PrintWriter(tempPath + File.separator+"exonerateProtein.map");
			PrintWriter outPut2 = new PrintWriter(tempPath + File.separator+"cdsExonerateProtein"+File.separator+"exonerateProteinSequences.fa");
			for( File f : listOfFiles ){
				if(  f.isFile() ){
					File file = new File(tempPath + File.separator + "proalignment" + File.separator + f.getName());
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String tempString = null;
					Pattern p = Pattern.compile("^\\s*\\d+\\s*:\\s*([\\w\\.\\s\\>\\-\\{\\}\\*]+)\\s*:\\s*\\d+\\s*$");
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
					//System.out.println(cdsSequenceString);
					cdsSequenceString=cdsSequenceString.replaceAll("\\{", "");
					cdsSequenceString=cdsSequenceString.replaceAll("\\}", "");
					cdsSequenceString=cdsSequenceString.replaceAll("\\s", "");
					cdsSequenceString=cdsSequenceString.replaceAll("[a-z]+\\.+[a-z]+", "");
					cdsSequenceString=cdsSequenceString.replaceAll("\\*", "");
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
					
					File file2 = new File(tempPath + File.separator + "alignment" + File.separator + f.getName());
					BufferedReader reader2 = new BufferedReader(new FileReader(file2));
					String tempString2 = null;
					Pattern p2 = Pattern.compile("^\\s*\\d+\\s*:\\s*([\\w\\.\\s\\>\\-\\{\\}\\*]+)\\s*:\\s*\\d+\\s*$");
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
					
					String cdsSequenceString2 = sequenceBuffer2.toString();
					//System.out.println(cdsSequenceString);
					cdsSequenceString2=cdsSequenceString2.replaceAll("\\{", "");
					cdsSequenceString2=cdsSequenceString2.replaceAll("\\}", "");
					cdsSequenceString2=cdsSequenceString2.replaceAll("\\s", "");
					cdsSequenceString2=cdsSequenceString2.replaceAll("[a-z]+\\.+[a-z]+", "");
					cdsSequenceString2=cdsSequenceString2.replaceAll("\\*", "");					
					boolean ifOrfLost2 = true;
					String metaInformation2="";
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
					File folder3 = new File(tempPath);
					if( ifOrfLost && ifOrfLost2  ){
						outPut.println(folder3.getName() + "  " + folder3.getName() + "  0 0  1  1  2 2"); // ORF shifted
						outPut1.println(chrs.get(f.getName()) + "\t" + f.getName() + "\t0\t" + positions.get(f.getName()));
						outPut2.println(">"+f.getName()+"_"+metaInformation);
						outPut2.println(cdsSequenceString);
					}else if(ifOrfLost){
						outPut.println(folder3.getName() + "  " + folder3.getName() + "  0 0  1  1  1 1"); // ORF not shifted
						outPut1.println(chrs.get(f.getName()) + "\t" + f.getName() + "\t0\t" + positions.get(f.getName()));
						outPut2.println(">"+f.getName()+"_"+metaInformation2);
						outPut2.println(cdsSequenceString2);
					}else{
						outPut.println(folder3.getName() + "  " + folder3.getName() + "  0 0  1  1  1 1"); // ORF not shifted
						outPut1.println(chrs.get(f.getName()) + "\t" + f.getName() + "\t0\t" + positions.get(f.getName()));
						outPut2.println(">"+f.getName()+"_"+metaInformation);
						outPut2.println(cdsSequenceString);
					}
					
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
