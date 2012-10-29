#!/usr/bin/perl

$human = 0;
$ac = "none";
$name = "none";
$type = "none";
$geneAC = "none";
$geneName = "none";
$bindingsites = "";
$complexes = "";
$interaction = "";
$precurser = "";
$superfamily = "none";
$organism = "";
$annotation = "";

if (length(@ARGV) < 3) {
	print "usage: perl extract_factors.pl [factor.dat] [bindingFactor-file] [organism-file]\n";
}


open(IN, "<$ARGV[0]");
open(OUT, ">$ARGV[1]");
open(ORG, ">$ARGV[2]");

print OUT "TF_AC\tTF_name\torganism\tTF_type\tbindingsites\tencoding_gene_ac\tencoding_gene_name\tcomplexes\tinteracting_factors\tprecurser\tsuperfamily\tbindingsite_annotation\n";
print ORG "TF_AC\tTFname\torganism\n";

while (<IN>){
	if (/\/\//) { #new entry
		if (!($ac =~/none/)) {
			chop($bindingsites);
			if (length($bindingsites) < 6) {
				$bindingsites = "none";
			}
			chop($complexes);
			if (length($complexes) < 5) {
				$complexes = "none";
			}
			chop($interaction);
			if (length($interaction) < 5) {
				$interaction = "none";
			}
			chop($precurser);
			if (length($precurser) < 5) {
				$precurser = "none";
			}
			chop($annotation);
			if (length($annotation) < 5) {
				$annotation = "none";
			}

			print OUT "$ac\t$name\t$organism\t$type\t$bindingsites\t$geneAC\t$geneName\t$complexes\t$interaction\t$precurser\t$superfamily\t$annotation\n";
		}

		if (length($organism) < 2) {
			$organism = "none";
		}
	
		print ORG "$ac\t$name\t$organism\n";


		#reset

		$ac = "none";
		$name = "none";
		$type = "none";
		$geneAC = "none";
		$geneName = "none";
		$bindingsites = "";
		$complexes = "";
		$interaction = "";
		$precurser = "";
		$superfamily = "none";
		$organism = "";
		$annotation = "";

		print "\n"; 
	}
	elsif (/^AC  (T\d{5})/) {
		print "$1\t";
		$ac = $1;
	}
	elsif (/^FA  (.*)/) {
		$name = $1;
	}
	elsif (/^OS  ([A-Za-z\(\) ]+),.+$/) {
		$organism = $1;
		print $organism;
	}
	elsif (/^TY  (\w+)./) {
		$type = $1;
	}
	elsif (/^BS  (R\d{5}); (.+)/) {
		$bs = $1;
		$bindingsites .= $bs.";";
	}
	elsif (/^GE  (G\d{6}); ([-\_ A-Za-z\d]+)[;.:].*/) {
		$geneAC = $1;
		$geneName = $2;
	}
	elsif (/^CX  (T\d{5}); .+/) {
		$c = $1;
		$complexes .= $c.";";
	}
	elsif (/^IN  (T\d{5}); (.+)$/) {
		$in = $1;
		$interaction .= $in.";";
	}
	elsif (/^ST  (T\d{5});.+/) {
		$pre = $1;
		$precurser .= $pre.";";
	}
	elsif (/^HP  (T\d{5});.+/) {
		$superfamily = $1;
	}
	elsif (/^DR  ([A-Za-z0-9: ]+);.+/) {
		$dr = $1;
		$annotation .= $dr.";";
	}

}


close(IN);
close(OUT);
close(ORG);