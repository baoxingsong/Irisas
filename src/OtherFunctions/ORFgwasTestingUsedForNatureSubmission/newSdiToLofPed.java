package OtherFunctions.ORFgwasTestingUsedForNatureSubmission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.songbx.action.parallel.model.AnnotationIncludeOrfLostReadReferences;
import me.songbx.impl.AnnotationReadImpl;
import me.songbx.model.Transcript.Transcript;
import me.songbx.service.AnnotationReadIncludeOrfLostService;
import me.songbx.service.ChromoSomeReadService;
import me.songbx.service.MapFileService;

public class newSdiToLofPed {
	public static void main( String[] argv ){
		new newSdiToLofPed(argv[0] );
	}
	public newSdiToLofPed(String tempPath){
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
		File folder3 = new File(tempPath);
		try {
			PrintWriter outPut2l = new PrintWriter(tempPath + File.separator+"ExonerateSequence"+File.separator+"newLiftover.fa");
			
			String mapfileLocation="/netscratch/dep_tsiantis/grp_gan/song/wholeGenomeMsaToSdi107phenotype/newSDIfILE/mergendnewsdis/"+folder3.getName() + ".sdi";
			String targetchromeSomeReadFileLocation = "/netscratch/dep_tsiantis/grp_gan/song/wholeGenomeMsaToSdi107phenotype/newSDIfILE/mergendnewsdis/" + folder3.getName() + ".fa";;
			ChromoSomeReadService referenceChromeSomeRead = new ChromoSomeReadService("/biodata/dep_tsiantis/grp_gan/song/rdINDELallHere/inputData/fullSdiFile/col_0.fa");
			AnnotationReadImpl annotationReadImpl = new AnnotationReadImpl(file1.getAbsolutePath());
			AnnotationIncludeOrfLostReadReferences annotationReadReferences = new AnnotationIncludeOrfLostReadReferences();
			MapFileService mapfile=new MapFileService(mapfileLocation);
			ChromoSomeReadService targetchromoSomeRead = new ChromoSomeReadService(targetchromeSomeReadFileLocation);
			AnnotationReadIncludeOrfLostService annotationRead = new AnnotationReadIncludeOrfLostService(targetchromoSomeRead, referenceChromeSomeRead, mapfile);
			annotationRead.setAnnotationReadImpl(annotationReadImpl);
			annotationRead.updateInformation(true, false, false, false, false, false);
			annotationReadReferences.put(folder3.getName(), annotationRead);
			
			for ( String key:  annotationReadImpl.getTranscriptHashSet().keySet() ){
				for(Transcript transcript : annotationReadImpl.getTranscriptHashSet().get(key)){
					String name = transcript.getName();
					
					String metaInformation3 = annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().get(name).getMetaInformation();
					
					String cdsSequenceString3 = annotationReadReferences.get(folder3.getName()).getTranscriptHashMap().get(name).getSequence();
					
					outPut2l.println(">"+name+"_"+metaInformation3);
					outPut2l.println(cdsSequenceString3);
				}
			}
			outPut2l.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
