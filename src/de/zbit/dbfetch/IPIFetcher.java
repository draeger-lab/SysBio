package de.zbit.dbfetch;

public class IPIFetcher extends DBFetcher {

  private static final long serialVersionUID = -1921530776666710988L;

  private String format;
  
  public IPIFetcher() {
    this(1000);
  }
  
  public IPIFetcher(int cacheSize) {
    super(cacheSize);
    setFormat("annot");
    setStyle(Style.RAW);
  }
  
  @Override
  public String getCheckStrFromInfo(String info) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDbName() {
    return "ipi";
  }

  @Override
  public String getFormat() {
    return format;
  }

  @Override
  public void setFormat(String format) {
    this.format = format;
  }

}
