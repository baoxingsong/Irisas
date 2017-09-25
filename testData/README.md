# Introduction

We provide example data to help go throw the pipeline. You need to modify some commands or scripts to run your own data.

## Preparation
Download and uncompress the test data:
````
wget http://chi.mpipz.mpg.de/download/ioa/input.zip
unzip input.zip
cd input
````
Generate the genome sequence for each accession
````
mcmerge getgenome -o PA9996.fa Col.fa PA9996.sdi
mcmerge getgenome -o PA9997.fa Col.fa PA9997.sdi
mcmerge getgenome -o PA9998.fa Col.fa PA9998.sdi
mcmerge getgenome -o PA9999.fa Col.fa PA9999.sdi
````
Create a bigwig (reads coverage) file for each accession/line
````
samtools mpileup PA9996.bam | perl -ne 'BEGIN{print "track type=wiggle_0 name=PA9996 description=PA9996\n"};($c, $start, undef, $depth) = split; if ($c ne $lastC) { print "variableStep chrom=$c\n"; };$lastC=$c; print "$start\t$depth\n";'  > PA9996.wig
wigToBigWig PA9996.wig chrSize PA9996.bw
rm PA9996.wig

samtools mpileup PA9997.bam | perl -ne 'BEGIN{print "track type=wiggle_0 name=PA9997 description=PA9997\n"};($c, $start, undef, $depth) = split; if ($c ne $lastC) { print "variableStep chrom=$c\n"; };$lastC=$c; print "$start\t$depth\n";'  > PA9997.wig
wigToBigWig PA9997.wig chrSize PA9997.bw
rm PA9997.wig

samtools mpileup PA9998.bam | perl -ne 'BEGIN{print "track type=wiggle_0 name=PA9998 description=PA9998\n"};($c, $start, undef, $depth) = split; if ($c ne $lastC) { print "variableStep chrom=$c\n"; };$lastC=$c; print "$start\t$depth\n";'  > PA9998.wig
wigToBigWig PA9998.wig chrSize PA9998.bw
rm PA9998.wig

samtools mpileup PA9999.bam | perl -ne 'BEGIN{print "track type=wiggle_0 name=PA9999 description=PA9999\n"};($c, $start, undef, $depth) = split; if ($c ne $lastC) { print "variableStep chrom=$c\n"; };$lastC=$c; print "$start\t$depth\n";'  > PA9999.wig
wigToBigWig PA9999.wig chrSize PA9999.bw
rm PA9999.wig
````
(Optional) For storage saving purpose, you could delete bam files
````
rm *bam
````
Create a empty variants file for reference accession/line
````
touch Col.sdi
````
Get accession/line list
````
ls *fa | sed '~s/.fa//g' > lineList
````
# Run the pipeline with single command
````
java -Xmx50g -jar ./disk/Irisas.jar EasyRun  -r Col -l ./input/lineList -g input -a ./input/TAIR10_GFF3_genes_no_UM.gff
````
## Output
You will find orf.bed orf.bim orf.fam, which are the PLINK files of ORF-states.\
indel.ped and indel.map are PLINK files of INDEL. indel_own.map is the INDEL length information of the INDELs in PLINK files.\
snp.ped and snp.map are PLINK files of SNP.

# Run the pipeline step by step
Run the following commands to get the chromosome name list
````
grep ">" Col.fa | sed '~s/>//g' > chrList
````
## Cut the whole genome sequence into fragments
````
java -jar disk/Irisas.jar CutTheWholeGenomeWithWindow -o originalSeq -r Col -l input/lineList -g ./input/
````
## Perform MSA on each fragments
````
cd originalSeq
perl ../script/mafftSubmit.pl > command
````
You could run command line by line with any way you like.
For example you could run it with `xjobs` in parallel
````
xjobs -j 10 -s command
````
*For one command style, we freezed in the code, you could only use MAFFT for multiple sequence alignment.\
*For step-by-step style, you could use any multiple-sequence-alignment software for this step, but you should name the MSA result with "original name" + ".mafft", and put them into the folders named by chromosome names.

## Create new sdi files
Create a new sdi file for each accession and each chromosome. Then, merge the sdi files for several chromosomes from the same accession.
````
cd ../
mkdir result
cd result
java -jar ../disk/Irisas.jar NewSdiFromMsa -i ../originalSeq -l ../input/lineList -r Col -c ../input/chrList -o ./ -g ../input/
perl ../script/mergeSdi.pl
````

## Reformat sdi files into PLINK files.
````
java -jar ../disk/Irisas.jar SdiToSnpPlink -l ../input/lineList -r Col -g ../input -s ./
java -jar ../disk/Irisas.jar SdiToIndelPlink -l ../input/lineList -s ./
````

# ORF
## Get the CDS and protein sequence of reference accession/line
````
cd ..
mkdir ORF
cd ORF
mkdir reference
cd reference
java -jar ../../disk/Irisas.jar ExtractCdsSequenceAndCheckORF -r ../../input/Col.fa -a ../../input/TAIR10_GFF3_genes_no_UM.gff -o cds -p protein
````
## Align genome sequence of other accessions to reference accessions
Prepare sequence
````
cd ../
java -jar ../disk/Irisas.jar ExtractGenomeSequce -m ../input/PA9996.fa -s ../input/PA9996.sdi -a ../input/TAIR10_GFF3_genes_no_UM.gff -o PA9996
java -jar ../disk/Irisas.jar ExtractGenomeSequce -m ../input/PA9997.fa -s ../input/PA9997.sdi -a ../input/TAIR10_GFF3_genes_no_UM.gff -o PA9997
java -jar ../disk/Irisas.jar ExtractGenomeSequce -m ../input/PA9998.fa -s ../input/PA9998.sdi -a ../input/TAIR10_GFF3_genes_no_UM.gff -o PA9998
java -jar ../disk/Irisas.jar ExtractGenomeSequce -m ../input/PA9999.fa -s ../input/PA9999.sdi -a ../input/TAIR10_GFF3_genes_no_UM.gff -o PA9999
````
Perform alignment with [Exonerate](http://www.ebi.ac.uk/about/vertebrate-genomics/software/exonerate)
````
cd PA9996
perl ../../script/exonerateSubmit.pl > command
````
Each line in file 'command' is a independent command, you could run it with
````
xjobs -j 10 -s command
````
or any way you want
````
sh ./command
````

Run this step for each accession:
````
cd ../PA9997
perl ../../script/exonerateSubmit.pl > command
xjobs -j 10 -s command

cd ../PA9998
perl ../../script/exonerateSubmit.pl > command
xjobs -j 10 -s command

cd ../PA9999
perl ../../script/exonerateSubmit.pl > command
xjobs -j 10 -s command

cd ../reference
perl ../../script/exonerateSubmit.pl > command
xjobs -j 10 -s command
````
## Generate integrating effects for each accession
````
cd ../
java -jar ../disk/Irisas.jar GenerateLofPed -a ../input/TAIR10_GFF3_genes_no_UM.gff -f PA9996 -s ../input/PA9996.sdi -m ../input/PA9996.fa -r ../input/Col.fa
java -jar ../disk/Irisas.jar GenerateLofPed -a ../input/TAIR10_GFF3_genes_no_UM.gff -f PA9997 -s ../input/PA9997.sdi -m ../input/PA9997.fa -r ../input/Col.fa
java -jar ../disk/Irisas.jar GenerateLofPed -a ../input/TAIR10_GFF3_genes_no_UM.gff -f PA9998 -s ../input/PA9998.sdi -m ../input/PA9998.fa -r ../input/Col.fa
java -jar ../disk/Irisas.jar GenerateLofPed -a ../input/TAIR10_GFF3_genes_no_UM.gff -f PA9999 -s ../input/PA9999.sdi -m ../input/PA9999.fa -r ../input/Col.fa
java -jar ../disk/Irisas.jar GenerateLofPed -a ../input/TAIR10_GFF3_genes_no_UM.gff -f reference -s ../input/Col.sdi -m ../input/Col.fa -r ../input/Col.fa
````
## Merge ORF-states of each accession together
create a list file with the following content:
````
PA9996/orf.ped PA9996/orf.map
PA9997/orf.ped PA9997/orf.map
PA9998/orf.ped PA9998/orf.map
PA9999/orf.ped PA9999/orf.map
reference/orf.ped   reference/orf.map
````
And run command:
````
plink --merge-list listForMerge --out orf
````
You will get integrating effect file in PLINK format.
