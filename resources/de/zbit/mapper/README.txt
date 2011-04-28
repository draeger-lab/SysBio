Exports from Biomart containing
- Ensembl Identifier (Gene/Transcript/Protein)
- EntrezGene Identifier
- Associated Gene Names (Gene Symbols).

Every file in the "FILELIST.txt"-file is parsed into the mapper. So updating can be done by simply deleting the old files, adding new ones and changing the FILELIST.txt accordingly.

As of 2011-04-28, all files are from the Ensembl v62 release.
New files can be created using, e.g., the following URL:
http://www.ensembl.org/biomart/martview/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360/6288fc4dc1a057064ed55bf953222360?VIRTUALSCHEMANAME=default&ATTRIBUTES=rnorvegicus_gene_ensembl.default.feature_page.ensembl_gene_id|rnorvegicus_gene_ensembl.default.feature_page.ensembl_transcript_id|rnorvegicus_gene_ensembl.default.feature_page.external_gene_id|rnorvegicus_gene_ensembl.default.feature_page.entrezgene&FILTERS=&VISIBLEPANEL=resultspanel
