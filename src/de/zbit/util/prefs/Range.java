package de.zbit.util.prefs;

import java.io.File;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import de.zbit.io.CSVReader;
import de.zbit.io.GeneralFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;

/**
 * A collection of ranges with a few convenient methods to work with them.
 * @author wrzodek
 * @author Andreas Dr&auml;ger
 */
public class Range<Type> {
	
	/**
	 * If {@link #getAllAcceptableValues()} is called, if there are more than
	 * this much acceptable values, null is returned. E.g. between {@link Double} 0.0
	 * and 1.0 there are infinite many values, but between integer 0 and 10, there
	 * only a finite number of values.
	 */
	private final static short defaultMaxAcceptableValuesToReturn = 50;
	
	/**
	 * A range of any type. Consisting of lower and upper bound and
	 * the information, if the lower/upper bound itself is included
	 * or not. 
	 * @author wrzodek
	 */
	class SubRange {
		private Type lBound;
		private Type uBound;
		private boolean excludingLBound=false;
		private boolean excludingUBound=false;
		
		/**
		 * 
		 */
		private SubRange() {
			super();
		}
		
		/**
		 * 
		 * @param value
		 */
		public SubRange (Type value) {
			this (value, value);
		}
		
		/**
		 * 
		 * @param lowerBound
		 * @param upperBound
		 */
		public SubRange (Type lowerBound, Type upperBound) {
			this (lowerBound, upperBound, false, false);
		}
		
		/**
		 * 
		 * @param lowerBound
		 * @param upperBound
		 * @param excludingLBound
		 * @param excludingUBound
		 */
		@SuppressWarnings("unchecked")
		public SubRange (Type lowerBound, Type upperBound, boolean excludingLBound, boolean excludingUBound) {
			super();
			lBound = lowerBound;
			uBound = upperBound;
			
			this.excludingLBound = excludingLBound;
			this.excludingUBound = excludingUBound;
			
			// Ensure, that lBound is always smaller than uBound
			if (lBound instanceof Comparable) {
				if (((Comparable)lBound).compareTo(((Comparable)uBound)) >0) {
					// SWAP
					Type temp = lBound;
					lBound=uBound;
					uBound=temp;
					
					boolean temp2 = this.excludingLBound;
					this.excludingLBound = this.excludingUBound;
					this.excludingUBound = temp2;
				}
			}
		}
	  

		/**
		 * Returns true, if and only if the given value is in the
		 * Range defined by this SubRange.
		 * @param value
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public boolean isInRange(Type value) {
			if (value instanceof Comparable) {
				// Check lower bound
				int r = ((Comparable)value).compareTo(((Comparable)lBound));
				if (r<0 || (r==0 && excludingLBound) ) return false;
				
				// Check upper bound
				r = ((Comparable)value).compareTo(((Comparable)uBound));
				if (r>0 || (r==0 && excludingUBound) ) return false;
				
				return true;
			} else if (lBound.equals(uBound)) {
				// Check absolute value
				if (!excludingLBound && !excludingUBound) {
				  if (value.equals(lBound)) return true;
				  else return false;
				} else {
					return false;
				}
			} else {
				// TODO: Logging
				System.err.printf(ResourceManager.getBundle("de.zbit.locales.Warnings")
						.getString("CLASS_NOT_COMPARABLE"), value.getClass().getName());
				return false;
			}
		}
		
		/**
		 * If there are less or equal than 'maximumToReturn'
		 * acceptable values, all those are returned. Else: null is returned. 
		 * @param maximumToReturn
		 * @return
		 */
		public List<Type> getAllAcceptableValues(int maximumToReturn) {
			List<Type> r = new LinkedList<Type>();
			
			// Check absolute value
		  if (lBound.equals(uBound)) {
		  	if (!excludingLBound && !excludingUBound)
		  	  r.add(lBound);
		  	return r;
		  }
		  
		  // Check if ranges have a finite length
		  double start=0;double end=0;
		  if (Utils.isInteger(lBound.getClass())) {
		  	start = ((Number)lBound).doubleValue();
		  	end = ((Number)uBound).doubleValue();
		  	
		  } else if (lBound instanceof Character) {
		  	start = (int)(Character)lBound;
		  	end = (int)(Character)uBound;
		  	
		  } else {
		  	// E.g. Strings
		  	return null;
		  }
		  
		  // Exit if too much choices and handle bounds
	  	if ((end-start)>maximumToReturn) return null;
	  	if (!excludingLBound) addNumberOrCharacter(lBound.getClass(), r, start);
	  	// Collect all possible choices
	  	for (start+=1; start<end; start++) {
	  		addNumberOrCharacter(lBound.getClass(), r, start);
	  	}
	  	if (!excludingUBound) addNumberOrCharacter(lBound.getClass(), r, end);
	  	
	  	return r;
		}
		
		/**
		 * Add a value to a list.
		 * @param Type - the Type of 'val'
		 * @param list - the list to add the value 'val'.
		 * @param val - value, actually if type Type. It will be converted into type
		 * and added to the list.
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private boolean addNumberOrCharacter(Class<?> Type, List<Type> list, double val) {
			//Integer.decode(nm)
			//Object re = Reflect.invokeIfContains(Type, "decode", String.class, Double.toString(val));
			
			// Make an Integer value of the double
			String strVal = Double.toString(val);
			int pos = strVal.indexOf('.');
			if (pos>0) strVal = strVal.substring(0, pos);
			
			// Parse it into Type and add it to the list.
			Object re = Reflect.invokeParser(Type, strVal);
			if (re==null) {
				// Character
				if (Type.equals(Character.class)) {
					((List<Character>)list).add(( (char)val) );
					return true;
				} else {
					try {
					  list.add( (Type)Type.cast(val) );
					} catch(Throwable t) {return false;}
				}
			} else {
				try {
				  list.add( (Type)(re) );
				} catch(Throwable t) {return false;}
				return true;
			}
			return false;
		}
		
	 }
	
	/*
	 * END OF SUB-CLASSES
	 */
	
	/**
	 * The list of all subRanges in this Range.
	 * (e.g. {1,2,3} has subRanges "1","2" and "3".
	 */
	private List<SubRange> ranges = new LinkedList<SubRange>();
	
	/**
	 * The original RangeString.
	 */
	private String rangeString;
	
	/**
	 * The class object of the Type.
	 */
	private Class<Type> typee;
	
	/**
   * <p><var>rangeSpec</var> is an optional range specification,
   * placed inside curly braces, consisting of a
   * comma-separated list of range items each specifying
   * permissible values for the option. A range item may be an
   * individual value, or it may itself be a subrange,
   * consisting of two individual values, separated by a comma,
   * and enclosed in square or round brackets. Square and round
   * brackets denote closed and open endpoints of a subrange, indicating
   * that the associated endpoint value is included or excluded
   * from the subrange.
   * The values specified in the range spec need to be
   * consistent with the type of value expected by the option.
   *
   * <p><b>Examples:</b>
   *
   * <p>A range spec of <code>{2,4,8,16}</code> for an integer
   * value will allow the integers 2, 4, 8, or 16.
   *
   * <p>A range spec of <code>{[-1.0,1.0]}</code> for a floating
   * point value will allow any floating point number in the
   * range -1.0 to 1.0.
   * 
   * <p>A range spec of <code>{(-88,100],1000}</code> for an integer
   * value will allow values > -88 and <= 100, as well as 1000.
   *
   * <p>A range spec of <code>{"foo", "bar", ["aaa","zzz")} </code> for a
   * string value will allow strings equal to <code>"foo"</code> or
   * <code>"bar"</code>, plus any string lexically greater than or equal
   * to <code>"aaa"</code> but less then <code>"zzz"</code>.
   *
   * @param requiredType - The class object of the Type.
   * @param rangeSpec - as defined above.
	 */
	public Range(Class<Type> requiredType, String rangeSpec) {
		this(requiredType);
		this.rangeString = rangeSpec;
		try {
			parseRangeSpec(rangeSpec);
		} catch (ParseException exc) {
			/*
			 * We cannot throw this exception because in interfaces it is impossible
			 * to catch these.
			 */
			throw new IllegalArgumentException(rangeSpec, exc);
		}
	}
	
	/**
	 * 
	 * @param requiredType
	 */
	private Range(Class<Type> requiredType) {
		super();
		this.typee = requiredType;
		this.rangeString = "";
		this.constraints = null;
	}
	
	/**
	 * Additional constraints to restrict an input.
	 */
	private Object constraints;
	
	/**
	 * @return the constraints
	 */
	public Object getConstraints() {
		return constraints;
	}

	/**
	 * 
	 * @param requiredType
	 *        must be an instance of {@link File}!
	 * @param filter
	 */
	public Range(Class<Type> requiredType, GeneralFileFilter filter) {
		this(requiredType);
		if (!requiredType.isAssignableFrom(File.class)) { 
      throw new IllegalArgumentException(
			  String.format(ResourceManager.getBundle("de.zbit.locales.Warnings")
				  	.getString("REQUIRED_TYPE_FOR_X_MUST_BE"), GeneralFileFilter.class
					  .getName(), File.class.getName()));
		}
		constraints = filter;
	}
	
	/**
	 * This is a convenient constructors that builds a range string from
	 * a list of all acceptable object automatically.
	 * 
	 * See {@link #Range(Class, String)} for more information.
	 * 
   * @param requiredType
   * @param acceptedObjects
   */
  public Range(Class<Type> requiredType, List<Type> acceptedObjects) {
    this(requiredType, Range.toRangeString(acceptedObjects));
  }
  
	/**
	 * Checks whether additional side constraints have been set.
	 * @return
	 */
	public boolean isSetConstraints() {
		return constraints != null;
	}
		
	/**
	 * The source String that has been used to build this class.
	 * @return
	 */
	public String getRangeSpecString() {
		return rangeString;
	}
	
	/**
	 * Add a SubRange to this collection of ranges.
	 * @param range
	 */
	private void addRange(SubRange range) {
		ranges.add(range);
	}
	
	/**
	 * If there are less or equal than {@link #defaultMaxAcceptableValuesToReturn}
	 * acceptable values, all those are returned. Else: null is returned.
	 * @return
	 */
	public List<Type> getAllAcceptableValues() {
		return getAllAcceptableValues(defaultMaxAcceptableValuesToReturn);
	}
	
	/**
	 * If there are less or equal than 'maximumToReturn'
	 * acceptable values, all those are returned. Else: null is returned. 
	 * @param maximumToReturn
	 * @return
	 */
	public List<Type> getAllAcceptableValues(int maximumToReturn) {
		List<Type> ret = new LinkedList<Type>();
		for (SubRange r : ranges) {
			List<Type> newItems = r.getAllAcceptableValues(maximumToReturn);
			// Too many elements, or invalid Type
			if (newItems==null) return null;
			for (Type type : newItems) {
				if (!ret.contains(type)) ret.add(type);
			}
			if (ret.size()>maximumToReturn) return null;
		}
		return ret;
	}
	
	/**
	 * Checks, if the given value is in range of all ranges. See also
	 * {@link #castAndCheckIsInRange(Object)}.
	 * 
	 * @param value
	 * @return
	 */
	public boolean isInRange(Type value) {
		if (isSetConstraints()) {
			if (constraints instanceof GeneralFileFilter) {
				// in this case, Type must be a String or a File.
				File file;
				if (value instanceof String) {
					file = new File(value.toString());
				} else {
					file = (File) value;
				}
				return ((GeneralFileFilter) constraints).accept(file);
			}
		} else {
			for (SubRange r : ranges) {
				if (r.isInRange(value)) { return true; }
			}
		}
		return false;
	}
	
	/**
	 * A convenient wrapper method, that calls {@link Option#parseOrCast(Class, Object)}
	 * before calling {@link #isInRange(Object)}.
	 * @param value
	 * @return
	 */
	public boolean castAndCheckIsInRange(Object value) {
		Type value2 = Option.parseOrCast(typee, value);
		if (value2==null) { return false;}
		return isInRange(value2);
	}
	

  /**
   * Parse the range specification string into a more convenient
   * data structute. See {@link #getRangeSpecifiaction()} for
   * more information on possible Strings.
   * @param range
   * @return rangeCollection<Type>
   * @throws ParseException 
   */
  private void parseRangeSpec(String range) throws ParseException {
  	
  	int positionTracker=0;
		try {
			range = range.substring(range.indexOf('{')+1, range.lastIndexOf('}'));
			// Be carefull with " and '
			//String[] items = range.split(Pattern.quote(","));
			List<Character> stringSep = new LinkedList<Character>();
			stringSep.add('\''); stringSep.add('"');
			String[] items = CSVReader.getSplits(range, ',', true, true, stringSep);
			
			SubRange r = null;
			for (int i=0; i<items.length; i++) {
				positionTracker+=items[i].length()+1; // +1 for the ','
				String item = items[i].trim();
				String item2=item;
				r = new SubRange();
				
				// Check if we have a range
				char c = item.charAt(0);
				if (c=='(' || c=='[') {
					if (c=='(') r.excludingLBound=true;
					i++;
					item = item.substring(1);
					item2 = items[i].trim();
					
					c = item2.charAt(item2.length()-1);
					if (c!=')' && c!=']') throw new Exception();
					else if (c==')') r.excludingUBound=true;
					item2 = item2.substring(0, item2.length()-1);
				}
				
				// Trim the string indicators
				for (Character sep : stringSep) {
					if (item.length()>2 && item.charAt(0)==(sep) && item.charAt(item.length()-1)==(sep) ) {
						item = item.substring(1, item.length()-1);
					}
					if (item2.length()>2 && item2.charAt(0)==(sep) && item2.charAt(item2.length()-1)==(sep) ) {
						item2 = item2.substring(1, item2.length()-1);
					}
				}
				
				r.lBound=Option.parseOrCast(typee, item);
				r.uBound=Option.parseOrCast(typee, item2);
				
				addRange(r);
			}
			
		} catch (Exception e) {
			// Erase it, so that other methods can see that it is invalid.
			if (range.equals(this.rangeString)) this.rangeString=null;
			
			throw new ParseException(String.format(ResourceManager.getBundle(
				"de.zbit.locales.Warnings").getString("RANGE_IN_WRONG_FORMAT"),
				(range == null ? "null" : range)), positionTracker);
			//e.printStackTrace();
		}

  }
  
  /**
   * Returns a List of type String, that is parsable as Range by the
   * Range class.
   * @param <Type>
   * @param acceptedObjects - a simple list of all acceptable objects.
   * @return String
   */
  public static <Type> String toRangeString(List<Type> acceptedObjects) {
    String s = "{" + StringUtil.implode(
              acceptedObjects
//              StringUtil.addPrefixAndSuffix(acceptedObjects, "\"", "\"")
              , ",") + "}";
    System.out.println(s);
    return "{" + StringUtil.implode(
      StringUtil.addPrefixAndSuffix(acceptedObjects, "\"", "\"")
      , ",") + "}";
  }
  
	/**
	 * 
	 * @param <T>
	 * @param cazz
	 * @return
	 */
	public static <T extends Enum<?>> String toRangeString(Class<T> cazz) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		T constants[] = cazz.getEnumConstants();
		int i = 0;
		for (T element : constants) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(element.toString());
			i++;
		}
		sb.append('}');
		return sb.toString();
	}
  
	/**
	 * Returns a range specification for boolean values. This can be used to
	 * specifiy that a boolean option expects an argument that is either <code>true</code> or
	 * <code>false</code>.
	 * 
	 * @return a range specification for boolean values
	 */
	public static Range<Boolean> booleanRange() {
	  return new Range<Boolean>(Boolean.class, "{\"true\", \"false\"}");
	}
	
}
