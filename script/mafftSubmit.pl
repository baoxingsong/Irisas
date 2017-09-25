#!perl -w
use strict;

#===============================================================================
#
#         FILE:  mafftSubmit.pl
#
#        USAGE:  ./mafftSubmit.pl
#
#  DESCRIPTION:  This is a part of WMSA pipeline. It is designed to generate mafft commands.
#
#      OPTIONS:  ---
# REQUIREMENTS:  ---
#         BUGS:  ---
#        NOTES:  ---
#       AUTHOR:  Baoxing Song (songbx.me), song@mpipz.mpg.de
#      COMPANY:  MPIPZ
#      VERSION:  1.0
#      CREATED:  09/17/2017 06:15:20 AM
#     REVISION:  ---
#===============================================================================

my $count=0;
my $command;
opendir(DIR1, "./") || die "Can't open directory ../";
my @dots1 = readdir(DIR1);
foreach my $file1 (@dots1){
	my $chrName = $file1;
	if( ($chrName=~/\w/) && ( $chrName ne "mafft" ) && ( $chrName ne "command") ){
		my $c = "";
		system("mkdir $chrName");
		my $dir_name = "./" . $chrName . "/";
		my $dir_name2 = "./" . $chrName . "/";
		opendir(DIR, $dir_name2) || die "Can't open directory $dir_name";
		my @dots = readdir(DIR);
		foreach my $file (@dots){
			if($file=~/^\d+_\d+$/){
				my $inputfile=$dir_name2 . $file;
				my $outfile=$dir_name . $file . ".mafft";
				my $iftoberun = 1;

				if(-e $outfile){
					my @args = stat ($outfile);
					my $size = $args[7];
					if( $size > 1 ){
						$iftoberun = 0;
					}
				}
				
				if(1== $iftoberun){
					print "mafft --auto $inputfile > $outfile\n";
				}
			}
		}
		close DIR;
	}
}
