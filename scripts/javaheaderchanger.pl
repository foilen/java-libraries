#!/usr/bin/perl

use strict;
use warnings;

if (scalar @ARGV != 1) {
	die ('Need the header file');
}

my $headerfile = shift @ARGV;

scandir('..');



	
sub scandir {
	my $dirname = shift @_;
	
	opendir(my $dh, $dirname) || die;
    while(my $filename = readdir $dh) {
		
		next if ( ($filename eq '.') || ($filename eq '..') );
		
		my $fullpath = "$dirname/$filename";
		scandir($fullpath) if (-d $fullpath);
		
		fixjava($fullpath) if ($fullpath =~ /\.java$/);
		fixjsp($fullpath) if ($fullpath =~ /\.jsp$/);
    }
    closedir $dh;
}

sub fixjava {
	my $filename = shift @_;
	
	print "$filename\n";
	
	open OUT, ">$filename.new" or die $!;
	open IN, "<$headerfile" or die $!;
	while (my $line = <IN>) {
		print OUT $line;
	}
	close IN;
	
	
	my $skip = 1;
	open IN, "<$filename" or die $!;
	while (my $line = <IN>) {
		$skip = 0 if ($line =~ /^package/);
		print OUT $line if (!$skip);
	}
	close IN;
	close OUT;
	
	rename "$filename.new", $filename;
}

sub fixjsp {
	my $filename = shift @_;
	
	print "$filename\n";
	
	open OUT, ">$filename.new" or die $!;
	open IN, "<$headerfile" or die $!;
	print OUT "<%\n";
	while (my $line = <IN>) {
		print OUT $line;
	}
	close IN;
	print OUT "%>\n";
	
	
	my $skip = 1;
	open IN, "<$filename" or die $!;
	while (my $line = <IN>) {
		$skip = 0 if ($line =~ /^<%@/);
		print OUT $line if (!$skip);
	}
	close IN;
	close OUT;
	
	rename "$filename.new", $filename;
}
