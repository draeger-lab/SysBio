#!/usr/bin/perl

$ac = "none";
$geneName = "none";
$organism = "";
$annotation = "";

if (length(@ARGV) < 2) {
	print "usage: perl extract_sites.pl [gene.dat] [genesAnnotation-file]\n";
}
open(IN, "<$ARGV[0]");
open(OUT, ">$ARGV[1]");

print OUT "gene_ac\tgene_name\torganism\tannotation\n";

while (<IN>){
	if (/\/\//) { #new entry
		if (!($ac =~/none/) && !($geneAC =~/none/)) {
			chop($annotation);
			if (length($annotation) < 5) {
				$annotation = "none";
			}

			print OUT "$ac\t$geneName\t$organism\t$annotation\n";
		}

		$ac = "none";
		$geneName = "none";
		$organism = "";
		$annotation = "";

		print "\n"; 
	}
	elsif (/^AC  (G\d{6})/) {
		print "$1\t";
		$ac = $1;
	}
	elsif (/^SD  (.+)$/) {
		$geneName = $1;
	}
	elsif (/^OS  ([A-Za-z\(\)]+),.+$/) {
		$organism = $1;
	}
	elsif (/^DR  ([A-Za-z0-9: ]+);.+/) {
		$dr = $1;
		$annotation .= $dr.";";
	}

}


close(IN);
close(OUT);
