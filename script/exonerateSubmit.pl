#!perl -w
use strict;

#===============================================================================
#
#         FILE:  exonerateSubmit.pl
#
#        USAGE:  ./exonerateSubmit.pl
#
#  DESCRIPTION:  This is a part of WMSA pipeline. It is designed to generate exonerate commands.
#
#      OPTIONS:  ---
# REQUIREMENTS:  ---
#         BUGS:  ---
#        NOTES:  ---
#       AUTHOR:  Baoxing Song (songbx.me), song@mpipz.mpg.de
#      COMPANY:  MPIPZ
#      VERSION:  1.0
#      CREATED:  09/17/2017 08:25:24 AM
#     REVISION:  ---
#===============================================================================


system("mkdir alignment");
my $dir_name2 = "../reference/cds/";
opendir(DIR, $dir_name2) || die "Can't open directory $dir_name2";
my @dots = readdir(DIR);
foreach my $file (@dots){
		if($file=~/\w+/){
			my $command = "exonerate --maxintron 30000 --model est2genome -i -10 --score 10 --bestn 1 --minintron 10 ../reference/cds/$file ./genomeSequence/$file >./alignment/$file";
			print "$command\n";
		}
}
close DIR;

system("mkdir proalignment");
$dir_name2 = "../reference/protein";
opendir(DIR, $dir_name2) || die "Can't open directory $dir_name2";
@dots = readdir(DIR);
foreach my $file (@dots){
		if($file=~/\w+/){
			my $command="exonerate --bestn 1 --maxintron 30000 --intronpenalty -10 --model protein2genome --percent 10 --score 10 --minintron 10 ../reference/protein/$file ./genomeSequence/$file >./proalignment/$file";
			print "$command\n";
		}
}
close DIR;
