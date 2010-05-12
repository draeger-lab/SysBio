package de.zbit.dbfetch;

/**
 * 
 * @author unknown
 * 
 */
public class IPIFetcher extends DBFetcher {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1921530776666710988L;
	/**
	 * 
	 */
	private String format;

	/**
	 * 
	 */
	public IPIFetcher() {
		this(1000);
	}

	/**
	 * 
	 * @param cacheSize
	 */
	public IPIFetcher(int cacheSize) {
		super(cacheSize);
		setFormat("annot");
		setStyle(Style.RAW);
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.dbfetch.DBFetcher#getCheckStrFromInfo(java.lang.String)
	 */
	@Override
	public String getCheckStrFromInfo(String info) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.dbfetch.DBFetcher#getDbName()
	 */
	@Override
	public String getDbName() {
		return "ipi";
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.dbfetch.DBFetcher#getFormat()
	 */
	@Override
	public String getFormat() {
		return format;
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.dbfetch.DBFetcher#setFormat(java.lang.String)
	 */
	@Override
	public void setFormat(String format) {
		this.format = format;
	}

}
