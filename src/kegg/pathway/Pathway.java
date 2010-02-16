package kegg.pathway;

import java.util.ArrayList;


public class Pathway {
	/* see http://www.genome.jp/kegg/xml/docs/ */
	
	String name=""; // keggid.type  	the KEGGID of this pathway map
	String org=""; // maporg.type  	ko/ec/[org prefix]
	int number=0; // mapnumber.type  	the map number of this pathway map  (5 digit integer)
	String title=""; // string.type  	the title of this pathway map
	String image=""; // url.type  	the resource location of the image file of this pathway map
	String link=""; // url.type  	the resource location of the information about this pathway map
	
	ArrayList<Entry> entries = new ArrayList<Entry>();
	ArrayList<Reaction> reactions = new ArrayList<Reaction>();
	ArrayList<Relation> relations = new ArrayList<Relation>();
	
	
	public Pathway(String name, String org, int number, String title, String image, String link) {
		this(name,org,number);
		setTitle(title);
		setImage(image);
		setLink(link);
	}
	public Pathway(String name, String org, int number) {
		this();
		setName(name);
		setOrg(org);
		setNumber(number);
	}
	private Pathway(){
		super();
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrg() {
		return org;
	}
	public void setOrg(String org) {
		this.org = org;
	}
	/**
	 * Remember: Pathway number is an 5-digit integer. So if size < 5 you need to make a prefix of 0's.
	 * You'd better use getNumberReal() instead. 
	 */
	public int getNumber() {
		return number;
	}
	public String getNumberReal() {
		String ret = Integer.toString(number);
		while (ret.length()<5)
			ret = "0"+ret;
		return ret;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
  public ArrayList<Entry> getEntries() {
    return entries;
  }
  public ArrayList<Reaction> getReactions() {
    return reactions;
  }
  public ArrayList<Relation> getRelations() {
    return relations;
  }
	
  public void addEntry(Entry e) {
    entries.add(e);
  }
  public void addReaction(Reaction r) {
    reactions.add(r);
  }
  public void addRelation(Relation r) {
    relations.add(r);
  }
	
	public Entry getEntryForId(int id) {
	  for (int i=0; i<entries.size(); i++)
	    if (entries.get(i).getId()==id) return entries.get(i);
	  return null;
	}

	
}
