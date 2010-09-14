package de.zbit.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import de.zbit.util.InfoManagement;

public class UniProtParserTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("llmmm");
    UniProtParser uw = new UniProtParser();
    ArrayList<String>[] blocks = uw.getGeneBlock(new String[]{"1433B_MOUSE", "SER1_DROME"});
    System.out.println("blocks.length " + blocks.length);
    for (int i = 0; i < blocks.length; i++) {
      if(i==0){
        System.out.println("1433B_Mouse:");
      }
      else if (i==1)
        System.out.println("SER1_DROME");
      ArrayList<String> block = blocks[i];
      for (String geneEntry : block) {
        System.out.println("----");
        System.out.println(geneEntry);
      }
    }
    
    if(true)return;
    
    
//    List<SortedArrayList<String>[]> test = uw
//              .getGene(new String[]{"A1A4B1_MOUSE", "1433B_MOUSE","ZC4H2_HUMAN"});
////              ,"ZCCHV_HUMAN","ZCH10_HUMAN","ZCH11_HUMAN","ZCH12_HUMAN","ZCH13_HUMAN","ZCH14_HUMAN","ZCH16_HUMAN","ZCH18_HUMAN","ZCHC2_HUMAN","ZCHC3_HUMAN","ZCHC4_HUMAN","ZCHC5_HUMAN","ZCHC6_HUMAN","ZCHC7_HUMAN","ZCHC8_HUMAN","ZCHC9_HUMAN","ZCPW1_HUMAN","ZCPW2_HUMAN","ZCRB1_HUMAN","ZDBF2_HUMAN","ZDC8P_HUMAN","ZDH11_HUMAN","ZDH12_HUMAN","ZDH13_HUMAN","ZDH14_HUMAN","ZDH15_HUMAN","ZDH16_HUMAN","ZDH17_HUMAN","ZDH18_HUMAN","ZDH19_HUMAN","ZDH20_HUMAN","ZDH21_HUMAN","ZDH22_HUMAN","ZDH23_HUMAN","ZDH24_HUMAN","ZDHC1_HUMAN","ZDHC2_HUMAN","ZDHC3_HUMAN","ZDHC4_HUMAN","ZDHC5_HUMAN","ZDHC6_HUMAN","ZDHC7_HUMAN","ZDHC8_HUMAN","ZDHC9_HUMAN","ZEB1_HUMAN","ZEP1_HUMAN","ZEP2_HUMAN","ZEP3_HUMAN","ZER1_HUMAN","ZF106_HUMAN","ZF112_HUMAN","ZF161_HUMAN","ZFAN1_HUMAN","ZFAN3_HUMAN","ZFAN5_HUMAN","ZFAN6_HUMAN","ZFAT_HUMAN","ZFHX2_HUMAN","ZFHX3_HUMAN","ZFHX4_HUMAN","ZFN2A_HUMAN","ZFN2B_HUMAN","ZFP14_HUMAN","ZFP1_HUMAN","ZFP28_HUMAN","ZFP2_HUMAN","ZFP30_HUMAN","ZFP37_HUMAN","ZFP3_HUMAN","ZFP41_HUMAN","ZFP57_HUMAN","ZFP62_HUMAN","ZFP82_HUMAN","ZFP90_HUMAN","ZFP92_HUMAN","ZFPL1_HUMAN","ZFR2_HUMAN","ZFR_HUMAN","ZFX_HUMAN","ZFY16_HUMAN","ZFY19_HUMAN","ZFY21_HUMAN","ZFY26_HUMAN","ZFY27_HUMAN","ZFYV1_HUMAN","ZFYV9_HUMAN","ZFY_HUMAN","ZG16_HUMAN","ZGPAT_HUMAN","ZH11B_HUMAN","ZHANG_HUMAN","ZHX1_HUMAN","ZHX2_HUMAN","ZHX3_HUMAN","ZIC1_HUMAN","ZIC2_HUMAN","ZIC3_HUMAN","ZIC4_HUMAN","ZIC5_HUMAN","ZIK1_HUMAN","ZIM2_HUMAN","ZIM3_HUMAN","ZKSC1_HUMAN","ZKSC2_HUMAN","ZKSC3_HUMAN","ZKSC4_HUMAN","ZKSC5_HUMAN","ZMAT2_HUMAN","ZMAT3_HUMAN","ZMAT4_HUMAN","ZMAT5_HUMAN","ZMIZ1_HUMAN","ZMIZ2_HUMAN","ZMY10_HUMAN","ZMY11_HUMAN","ZMY12_HUMAN","ZMY15_HUMAN","ZMY17_HUMAN","ZMY19_HUMAN","ZMYM1_HUMAN","ZMYM2_HUMAN","ZMYM3_HUMAN","ZMYM4_HUMAN","ZMYM5_HUMAN","ZMYM6_HUMAN","ZN100_HUMAN","ZN101_HUMAN","ZN107_HUMAN","ZN114_HUMAN","ZN117_HUMAN","ZN121_HUMAN","ZN124_HUMAN","ZN131_HUMAN","ZN132_HUMAN","ZN133_HUMAN","ZN134_HUMAN","ZN135_HUMAN","ZN136_HUMAN","ZN137_HUMAN","ZN138_HUMAN","ZN140_HUMAN","ZN141_HUMAN","ZN142_HUMAN","ZN143_HUMAN","ZN148_HUMAN","ZN154_HUMAN","ZN155_HUMAN","ZN157_HUMAN","ZN160_HUMAN","ZN165_HUMAN","ZN167_HUMAN","ZN169_HUMAN","ZN174_HUMAN","ZN175_HUMAN","ZN177_HUMAN","ZN180_HUMAN","ZN181_HUMAN","ZN182_HUMAN","ZN184_HUMAN","ZN185_HUMAN","ZN187_HUMAN","ZN189_HUMAN","ZN192_HUMAN","ZN193_HUMAN","ZN195_HUMAN","ZN197_HUMAN","ZN200_HUMAN","ZN202_HUMAN","ZN205_HUMAN","ZN207_HUMAN","ZN208_HUMAN","ZN211_HUMAN","ZN212_HUMAN","ZN213_HUMAN","ZN214_HUMAN","ZN215_HUMAN","ZN217_HUMAN","ZN219_HUMAN","ZN221_HUMAN","ZN222_HUMAN","ZN223_HUMAN","ZN224_HUMAN","ZN225_HUMAN","ZN226_HUMAN","ZN227_HUMAN","ZN229_HUMAN","ZN230_HUMAN","ZN232_HUMAN","ZN233_HUMAN","ZN234_HUMAN","ZN235_HUMAN","ZN236_HUMAN","ZN238_HUMAN","ZN239_HUMAN","ZN248_HUMAN","ZN250_HUMAN","ZN251_HUMAN","ZN252_HUMAN","ZN253_HUMAN","ZN254_HUMAN","ZN256_HUMAN","ZN257_HUMAN","ZN260_HUMAN","ZN263_HUMAN","ZN264_HUMAN","ZN266_HUMAN","ZN267_HUMAN","ZN268_HUMAN","ZN271_HUMAN","ZN273_HUMAN","ZN274_HUMAN","ZN275_HUMAN","ZN276_HUMAN","ZN277_HUMAN","ZN282_HUMAN","ZN283_HUMAN","ZN284_HUMAN","ZN287_HUMAN","ZN295_HUMAN","ZN296_HUMAN","ZN300_HUMAN","ZN302_HUMAN","ZN304_HUMAN","ZN311_HUMAN","ZN316_HUMAN","ZN317_HUMAN","ZN318_HUMAN","ZN319_HUMAN","ZN320_HUMAN","ZN321_HUMAN","ZN323_HUMAN","ZN326_HUMAN","ZN329_HUMAN","ZN330_HUMAN","ZN331_HUMAN","ZN333_HUMAN","ZN334_HUMAN","ZN337_HUMAN","ZN33A_HUMAN","ZN33B_HUMAN","ZN341_HUMAN","ZN343_HUMAN","ZN345_HUMAN","ZN347_HUMAN","ZN350_HUMAN","ZN358_HUMAN","ZN362_HUMAN","ZN363_HUMAN","ZN365_HUMAN","ZN366_HUMAN","ZN37A_HUMAN","ZN382_HUMAN","ZN383_HUMAN","ZN384_HUMAN","ZN391_HUMAN","ZN394_HUMAN","ZN396_HUMAN","ZN397_HUMAN","ZN398_HUMAN","ZN404_HUMAN","ZN407_HUMAN","ZN409_HUMAN","ZN410_HUMAN","ZN415_HUMAN","ZN416_HUMAN","ZN417_HUMAN","ZN418_HUMAN","ZN419_HUMAN","ZN420_HUMAN","ZN423_HUMAN","ZN425_HUMAN","ZN426_HUMAN","ZN428_HUMAN","ZN429_HUMAN","ZN430_HUMAN","ZN431_HUMAN","ZN432_HUMAN","ZN433_HUMAN","ZN434_HUMAN","ZN436_HUMAN","ZN438_HUMAN","ZN439_HUMAN","ZN440_HUMAN","ZN441_HUMAN","ZN442_HUMAN","ZN443_HUMAN","ZN444_HUMAN","ZN445_HUMAN","ZN446_HUMAN","ZN449_HUMAN","ZN454_HUMAN","ZN460_HUMAN","ZN461_HUMAN","ZN467_HUMAN","ZN468_HUMAN","ZN469_HUMAN","ZN470_HUMAN","ZN471_HUMAN","ZN473_HUMAN","ZN479_HUMAN","ZN480_HUMAN","ZN483_HUMAN","ZN484_HUMAN","ZN485_HUMAN","ZN486_HUMAN","ZN487_HUMAN","ZN490_HUMAN","ZN491_HUMAN","ZN492_HUMAN","ZN493_HUMAN","ZN496_HUMAN","ZN497_HUMAN","ZN498_HUMAN","ZN500_HUMAN","ZN501_HUMAN","ZN502_HUMAN","ZN503_HUMAN","ZN506_HUMAN","ZN507_HUMAN","ZN509_HUMAN","ZN510_HUMAN","ZN511_HUMAN","ZN514_HUMAN","ZN516_HUMAN","ZN517_HUMAN","ZN519_HUMAN","ZN521_HUMAN","ZN524_HUMAN","ZN525_HUMAN","ZN526_HUMAN","ZN527_HUMAN","ZN528_HUMAN","ZN529_HUMAN","ZN530_HUMAN","ZN536_HUMAN","ZN540_HUMAN","ZN541_HUMAN","ZN542_HUMAN","ZN543_HUMAN","ZN544_HUMAN","ZN546_HUMAN","ZN547_HUMAN","ZN548_HUMAN","ZN549_HUMAN","ZN550_HUMAN","ZN551_HUMAN","ZN552_HUMAN","ZN554_HUMAN","ZN555_HUMAN","ZN556_HUMAN","ZN557_HUMAN","ZN558_HUMAN","ZN559_HUMAN","ZN560_HUMAN","ZN561_HUMAN","ZN562_HUMAN","ZN563_HUMAN","ZN564_HUMAN","ZN565_HUMAN","ZN566_HUMAN","ZN567_HUMAN","ZN568_HUMAN","ZN569_HUMAN","ZN570_HUMAN","ZN571_HUMAN","ZN572_HUMAN","ZN573_HUMAN","ZN574_HUMAN","ZN575_HUMAN","ZN576_HUMAN","ZN577_HUMAN","ZN578_HUMAN","ZN579_HUMAN","ZN580_HUMAN","ZN581_HUMAN","ZN582_HUMAN","ZN583_HUMAN","ZN584_HUMAN","ZN586_HUMAN","ZN587_HUMAN","ZN589_HUMAN","ZN593_HUMAN","ZN594_HUMAN","ZN595_HUMAN","ZN596_HUMAN","ZN597_HUMAN","ZN598_HUMAN","ZN599_HUMAN","ZN600_HUMAN","ZN605_HUMAN","ZN606_HUMAN","ZN607_HUMAN","ZN608_HUMAN","ZN609_HUMAN","ZN610_HUMAN","ZN611_HUMAN","ZN613_HUMAN","ZN614_HUMAN","ZN615_HUMAN","ZN616_HUMAN","ZN619_HUMAN","ZN620_HUMAN","ZN621_HUMAN","ZN622_HUMAN","ZN623_HUMAN","ZN624_HUMAN","ZN625_HUMAN","ZN626_HUMAN","ZN627_HUMAN","ZN628_HUMAN","ZN629_HUMAN","ZN630_HUMAN","ZN638_HUMAN","ZN639_HUMAN","ZN641_HUMAN","ZN642_HUMAN","ZN643_HUMAN","ZN645_HUMAN","ZN646_HUMAN","ZN648_HUMAN","ZN649_HUMAN","ZN652_HUMAN","ZN653_HUMAN","ZN655_HUMAN","ZN658_HUMAN","ZN660_HUMAN","ZN662_HUMAN","ZN664_HUMAN","ZN665_HUMAN","ZN667_HUMAN","ZN668_HUMAN","ZN669_HUMAN","ZN670_HUMAN","ZN671_HUMAN","ZN672_HUMAN","ZN673_HUMAN","ZN674_HUMAN","ZN675_HUMAN","ZN676_HUMAN","ZN677_HUMAN","ZN678_HUMAN","ZN679_HUMAN","ZN680_HUMAN","ZN681_HUMAN","ZN682_HUMAN","ZN683_HUMAN","ZN684_HUMAN","ZN687_HUMAN","ZN688_HUMAN","ZN689_HUMAN","ZN691_HUMAN","ZN692_HUMAN","ZN695_HUMAN","ZN696_HUMAN","ZN697_HUMAN","ZN699_HUMAN","ZN700_HUMAN","ZN701_HUMAN","ZN702_HUMAN","ZN703_HUMAN","ZN706_HUMAN","ZN707_HUMAN","ZN708_HUMAN","ZN709_HUMAN","ZN710_HUMAN","ZN711_HUMAN","ZN713_HUMAN","ZN714_HUMAN","ZN716_HUMAN","ZN717_HUMAN","ZN718_HUMAN","ZN720_HUMAN","ZN721_HUMAN","ZN726_HUMAN","ZN730_HUMAN","ZN732_HUMAN","ZN733_HUMAN","ZN734_HUMAN","ZN735_HUMAN","ZN737_HUMAN","ZN738_HUMAN","ZN740_HUMAN","ZN746_HUMAN","ZN747_HUMAN","ZN749_HUMAN","ZN750_HUMAN","ZN75A_HUMAN","ZN75C_HUMAN","ZN75D_HUMAN","ZN761_HUMAN","ZN763_HUMAN","ZN764_HUMAN","ZN765_HUMAN","ZN766_HUMAN","ZN767_HUMAN","ZN768_HUMAN","ZN770_HUMAN","ZN771_HUMAN","ZN772_HUMAN","ZN773_HUMAN","ZN774_HUMAN","ZN775_HUMAN","ZN776_HUMAN","ZN777_HUMAN","ZN778_HUMAN","ZN781_HUMAN","ZN782_HUMAN","ZN783_HUMAN","ZN784_HUMAN","ZN785_HUMAN","ZN786_HUMAN","ZN787_HUMAN","ZN788_HUMAN","ZN789_HUMAN","ZN790_HUMAN","ZN791_HUMAN","ZN792_HUMAN","ZN793_HUMAN","ZN799_HUMAN","ZN800_HUMAN","ZN805_HUMAN","ZN806_HUMAN","ZN808_HUMAN","ZN812_HUMAN","ZN813_HUMAN","ZN818_HUMAN","ZN823_HUMAN","ZN827_HUMAN","ZN828_HUMAN","ZN829_HUMAN","ZN830_HUMAN","ZN831_HUMAN","ZN833_HUMAN","ZN834_HUMAN","ZN835_HUMAN","ZN836_HUMAN","ZN837_HUMAN","ZN839_HUMAN","ZN840_HUMAN","ZN841_HUMAN","ZN844_HUMAN","ZN845_HUMAN","ZN846_HUMAN","ZN860_HUMAN","ZN862_HUMAN","ZNF10_HUMAN","ZNF12_HUMAN","ZNF14_HUMAN","ZNF16_HUMAN","ZNF17_HUMAN","ZNF18_HUMAN","ZNF19_HUMAN","ZNF20_HUMAN","ZNF22_HUMAN","ZNF23_HUMAN","ZNF24_HUMAN","ZNF25_HUMAN","ZNF26_HUMAN","ZNF28_HUMAN","ZNF2_HUMAN","ZNF30_HUMAN","ZNF32_HUMAN","ZNF34_HUMAN","ZNF35_HUMAN","ZNF3_HUMAN","ZNF41_HUMAN","ZNF43_HUMAN","ZNF44_HUMAN","ZNF45_HUMAN","ZNF48_HUMAN","ZNF56_HUMAN","ZNF57_HUMAN","ZNF66_HUMAN","ZNF67_HUMAN","ZNF69_HUMAN","ZNF70_HUMAN","ZNF71_HUMAN","ZNF73_HUMAN","ZNF74_HUMAN","ZNF76_HUMAN","ZNF77_HUMAN","ZNF79_HUMAN","ZNF7_HUMAN","ZNF80_HUMAN","ZNF81_HUMAN","ZNF83_HUMAN","ZNF84_HUMAN","ZNF85_HUMAN","ZNF8_HUMAN","ZNF90_HUMAN","ZNF91_HUMAN","ZNF92_HUMAN","ZNF93_HUMAN","ZNF98_HUMAN","ZNF99_HUMAN","ZNFX1_HUMAN","ZNHI1_HUMAN","ZNHI2_HUMAN","ZNHI3_HUMAN","ZNHI6_HUMAN","ZNP12_HUMAN","ZNRF1_HUMAN","ZNRF2_HUMAN","ZNRF3_HUMAN","ZNRF4_HUMAN","ZNT10_HUMAN","ZNT1_HUMAN","ZNT2_HUMAN","ZNT3_HUMAN","ZNT4_HUMAN","ZNT5_HUMAN","ZNT6_HUMAN","ZNT7_HUMAN","ZNT8_HUMAN","ZNT9_HUMAN","ZO1_HUMAN","ZO2_HUMAN","ZO3_HUMAN","ZP1_HUMAN","ZP2_HUMAN","ZP3_HUMAN","ZP4_HUMAN","ZPBP1_HUMAN","ZPBP2_HUMAN","ZPI_HUMAN","ZPLD1_HUMAN","ZPR1_HUMAN","ZRAB2_HUMAN","ZRAB3_HUMAN","ZRAN1_HUMAN","ZSA5A_HUMAN","ZSA5B_HUMAN","ZSA5C_HUMAN","ZSC10_HUMAN","ZSC12_HUMAN","ZSC16_HUMAN","ZSC18_HUMAN","ZSC20_HUMAN","ZSC21_HUMAN","ZSC22_HUMAN","ZSC23_HUMAN","ZSC29_HUMAN","ZSCA1_HUMAN","ZSCA2_HUMAN","ZSCA4_HUMAN","ZSWM1_HUMAN","ZSWM2_HUMAN","ZSWM3_HUMAN","ZSWM4_HUMAN","ZSWM5_HUMAN","ZSWM6_HUMAN","ZSWM7_HUMAN","ZUFSP_HUMAN","ZW10_HUMAN","ZWILC_HUMAN","ZXDA_HUMAN","ZXDB_HUMAN","ZXDC_HUMAN","ZY11A_HUMAN","ZY11B_HUMAN","ZYX_HUMAN","ZZEF1_HUMAN","ZZZ3_HUMAN"});
////    InfoManagement.saveToFilesystem("uniprot.dat", uw.getUniprotManager());    
//    for (int j = 0; j < test.size(); j++) {
//      System.out.println("j: " + j);
//      SortedArrayList<String>[] asl = test.get(j);
//      
//      for (int i = 0; i < asl.length; i++) {
//        SortedArrayList<String> list = asl[i];
//        if(i==0)
//          System.out.println("protein id:");
//        else if(i==1)
//          System.out.println("names:");
//        else if(i==2)
//          System.out.println("synonyms:");
//        else if(i==3)
//          System.out.println("orderedLocusNames:");
//        else if(i==4)
//          System.out.println("orfNames");
//        for (String l : list) {
//          System.out.println(l);
//        }
//      }
//    }
//    // uw.getGene("YWHAB");


if(true)return;
String[][] overAllArray = null;
String[] ipiArray = null, acArray = null;
ArrayList<String[]> acList = null, idList = null;
ArrayList<String> acList2 = new ArrayList<String>();
try {
  // Reading file
  BufferedReader snpIn = new BufferedReader(new FileReader("C:/Dokumente und Einstellungen/buechel/Desktop/protein.txt"));
  String line;
  int counter = 0;
  while((line=snpIn.readLine())!=null){
    counter++;
  }
  snpIn.close();

  overAllArray = new String[counter][3];
  snpIn = new BufferedReader(new FileReader("C:/Dokumente und Einstellungen/buechel/Desktop/protein.txt"));
  ipiArray = new String[counter];
  int i=0;
  while((line=snpIn.readLine())!=null){
    line = line.trim().replace("\"", "").toUpperCase();
    if(line != null){
      ipiArray[i] = line;  
      overAllArray[i][0] = line;
      i++;
    }
  }
  snpIn.close();
}
catch(Exception e){
  System.err.print("Error while reading protein File");
}

// fetching upAC
if(ipiArray != null){
  IPIParser ipiParser = new IPIParser();
  acList = ipiParser.getUniProtACs(ipiArray);
  int counter = 0;
  if (acList != null) {
    for (int i = 0; i<acList.size(); i++) {
      if(acList.get(i)[1] != null && !acList2.contains(acList.get(i)[1])){
        acList2.add(acList.get(i)[1]);
        overAllArray[i][1] = acList.get(i)[1];
        counter++;
      }
      else{
        //          log.debug("IPI " + acList.get(i)[0] + " has no uniprot ac number!");
        overAllArray[i][1] = acList.get(i)[1];
        System.out.println("IPI " + acList.get(i)[0] + " has no uniprot ac number!");
      }
    }

    // fetch UniProt ID
    acArray = new String[counter];
    int j = 0;
    for (int i = 0; i<acList2.size(); i++) {
      String ac = acList2.get(i);
      int index = ac.indexOf("-");
      if(index>=0)
        ac = ac.substring(0,index);
      acArray[j] = ac;
      j++;
    }

    j = 0;
    if(acArray != null){
      UniProtParser up = new UniProtParser();
      idList = up.getUniProtID(acArray);  
      // write proteins in protein vector
      for (int i=0; i<idList.size(); i++){
        String[] entry = idList.get(i);

        if(overAllArray[j][1]!= null && overAllArray[j][1].startsWith(entry[0])){
          overAllArray[j][2] = entry[1];
          j++;
        }
        else{
          j++;
          while(j<counter){
            if(overAllArray[j][1]!= null && overAllArray[j][1].startsWith(entry[0])){
              break;
            }
            else
              j++;
          }
          overAllArray[j][2] = entry[1];
        }
      }
    }

    //          for (String[] s : overAllArray) {
    //            System.out.println(s[0] + "\t" + s[1] + "\t\t" + s[2]);
    //          }
  }
  
  // Just make sure you don't loose infos next time.
  if (ipiParser.getIPIManagement().isCacheChangedSinceLastLoading()) {
    InfoManagement.saveToFilesystem("ipi.dat", ipiParser.getIPIManagement());
    System.out.println("Saved ipi.dat with size " + ipiParser.getIPIManagement().getNumberOfCachedIDs() + "(" + ipiParser.getIPIManagement().getNumberOfCachedInfos() + " infos).");
  }
}
}

}
