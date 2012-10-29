#!/usr/bin/perl

$ac = "none";
$id = "none";
$geneName = "none";
$geneAC = "none";
$bindingfactor = "";
$organism = "";
$annotation = "";

if (length(@ARGV) < 2) {
	print "usage: perl extract_sites.pl [site.dat] [bindingSites-file]\n";
}
open(IN, "<$ARGV[0]");
open(OUT, ">$ARGV[1]");

print OUT "regulation_ac\tregulation_id\torganism\tregulated_gene_ac\tregulated_gene_name\tbinding_factors\tannotation\n";

while (<IN>){
	if (/\/\//) { #new entry
		if (!($ac =~/none/) && !($geneAC =~/none/)) {
			chop($bindingfactor);
			if (length($bindingfactor) <5) {
				$bindingfactor = "none";
			}
			chop($annotation);
			if (length($annotation) < 5) {
				$annotation = "none";
			}

			print OUT "$ac\t$id\t$organism\t$geneAC\t$geneName\t$bindingfactor\t$annotation\n";
		}

		$ac = "none";
		$id = "none";
		$geneName = "none";
		$geneAC = "none";
		$bindingfactor = "";
		$organism = "";
		$annotation = "";

		print "\n"; 
	}
	elsif (/^AC  (R\d{5})/) {
		print "$1\t";
		$ac = $1;
	}
	elsif (/^ID  (.*)/) {
		$id = $1;
	}
	elsif (/^OS  ([A-Za-z\(\)]+),.+$/) {
		$organism = $1;
		print $organism;

	}
	elsif (/^DE  ([-\_ A-Za-z\d]+).*; Gene: (G\d{6}).$/) {
		$geneName = $1;
		$geneAC = $2;
	}
	elsif (/^DE  Gene: (G\d{6}).$/) {
		$geneName = "none";
		$geneAC = $1;
	}
	elsif (/^BF  (T\d{5});.+/) {
		$bf = $1;
		$bindingfactor .= $bf.";";
	}
	elsif (/^DR  ([A-Za-z0-9: ]+);.+/) {
		$dr = $1;
		$annotation .= $dr.";";
	}

}


close(IN);
close(OUT);
