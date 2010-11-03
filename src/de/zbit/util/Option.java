/**
 * 
 */
package de.zbit.util;

import java.util.LinkedList;
import java.util.List;

import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.CharHolder;
import argparser.DoubleHolder;
import argparser.FloatHolder;
import argparser.IntHolder;
import argparser.LongHolder;
import argparser.StringHolder;
import de.zbit.io.CSVReader;

/**
 * An {@link Option} defines a key in a key-provider class and can also be used
 * to specify command-line options for a program.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-24
 */
public class Option<Type> {
	
	/**
	 * A range of any type. Consisting of lower and upper bound and
	 * the information, if the lower/upper bound itself is included
	 * or not. 
	 * @author wrzodek
	 */
	class Range {
		public Type lBound;
		public Type uBound;
		public boolean excludingLBound=false;
		public boolean excludingUBound=false;
		
		public Range() {
			super();
		}
		
		public Range (Type value) {
			this (value, value);
		}
		
		public Range (Type lowerBound, Type upperBound) {
			this (lowerBound, upperBound, false, false);
		}
		
		@SuppressWarnings("unchecked")
		public Range (Type lowerBound, Type upperBound, boolean excludingLBound, boolean excludingUBound) {
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
				System.err.println("Cannot compare class " + value.getClass().getName() + ". Please implement the Comparable interface.");
				return false;
			}
		}
		
		public List<Type> getAllAcceptableValues() {
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
	  	if ((end-start)>10) return null;
	  	if (!excludingLBound) addNumberOrCharacter(lBound.getClass(), r, start);
	  	// Collect all possible choices
	  	for (start+=1; start<end; start++) {
	  		addNumberOrCharacter(lBound.getClass(), r, start);
	  	}
	  	if (!excludingUBound) addNumberOrCharacter(lBound.getClass(), r, end);
	  	
	  	return r;
		}
		
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
	
	/**
	 * A collection of ranges with a few convenient methods to work with them.
	 * 
	 * @author wrzodek
	 */
	public class rangeCollection {
		private List<Range> ranges = new LinkedList<Range>();
		
		public void addRange(Range range) {
			ranges.add(range);
		}
		
		/**
		 * If there are less or equal than 10 acceptable values, all those
		 * are returned. Else: null is returned.
		 * @return
		 */
		public List<Type> getAllAcceptableValues() {
			List<Type> ret = new LinkedList<Type>();
			for (Range r : ranges) {
				List<Type> newItems = r.getAllAcceptableValues();
				// Too many elements, or invalid Type
				if (newItems==null) return null;
				for (Type type : newItems) {
					if (!ret.contains(type)) ret.add(type);
				}
				if (ret.size()>10) return null;
			}
			return ret;
		}
		
		/**
		 * Checks, if the given value is in range of all ranges.
		 * @param value
		 * @return
		 */
		public boolean isInRange(Type value) {
			for (Range r : ranges) {
				if (!r.isInRange(value)) return false;
			}
			return true;
		}
		
	}
	

    /**
     * A short description what the purpose of this option is.
     */
    private final String description;
    /**
     * Gives the number of leading '-' symbols when converting this
     * {@link Option} instance's name into a command line key.
     */
    private final short numLeadingMinus;
    /**
     * The name of this option.
     */
    private final String optionName;
    /**
     * An optional range specification string to limit the allowable values when
     * using this {@link Option} on the command line.
     * See {@link #getRangeSpecifiaction()} for more information.
     */
    private final String rangeSpec;
    /**
     * More convenient format of {@link #rangeSpec}
     */
    private final rangeCollection range;
		/**
     * The data type that is expected as an associated value in a key-value pair
     * of this {@link Option}'s name and a value. For instance, Boolean,
     * Integer, String etc.
     */
    private final Class<Type> requiredType;
    
    /**
     * The default value for this option. May be null, if it is going to be read from
     * the XML-file later.
     */
    private final Type defaultValue;
    
    /**
     * A shorter name for the command line, for instance, in addition to --file
     * one might want the option -f.
     */
    private final String shortCmdName;

    /**
     * @param optionName
     * @param requiredType
     * @param description
     */
    public Option(String optionName, Class<Type> requiredType, String description) {
	this(optionName, requiredType, description, null,null);
    }

    /**
     * @param optionName
     * @param requiredType
     * @param description
     * @param numLeadingMinus
     * @param shortCmdName
     */
    public Option(String optionName, Class<Type> requiredType, String description,
	short numLeadingMinus, String shortCmdName) {
	this(optionName, requiredType, description, null, numLeadingMinus,
	    shortCmdName);
    }

    /**
     * @param optionName
     * @param requiredType
     * @param description
     * @param rangeSpec - See {@link #getRangeSpecifiaction()}.
     */
    public Option(String optionName, Class<Type> requiredType, String description,
	String rangeSpec) {
	this(optionName, requiredType, description, rangeSpec, (short) 2);
    }

    /**
     * @param optionName
     * @param requiredType
     * @param description
     * @param rangeSpec - See {@link #getRangeSpecifiaction()}.
     * @param numLeadingMinus
     *        the number of leading '-' symbols of the command-line argument
     *        corresponding to this option.
     */
    public Option(String optionName, Class<Type> requiredType, String description,
	String rangeSpec, short numLeadingMinus) {
	this(optionName, requiredType, description, rangeSpec, numLeadingMinus,
	    null,null);
    }

    /**
     * 
     * @param optionName
     * @param requiredType
     * @param description
     * @param rangeSpec - See {@link #getRangeSpecifiaction()}.
     * @param numLeadingMinus
     * @param shortCmdName
     */
    public Option(String optionName, Class<Type> requiredType, String description,
        String rangeSpec, short numLeadingMinus, String shortCmdName) {
      this(optionName, requiredType, description, rangeSpec, numLeadingMinus,
          null,null);
    }
    
    
    
    
    /**
     * 
     * @param optionName
     * @param requiredType
     * @param description
     * @param defaultValue
     */
    public Option(String optionName, Class<Type> requiredType, String description,
        Type defaultValue) {
      this(optionName, requiredType, description, null, defaultValue);
    }
    
    /**
     * 
     * @param optionName
     * @param requiredType
     * @param description
     * @param numLeadingMinus
     * @param shortCmdName
     * @param defaultValue
     */
    public Option(String optionName, Class<Type> requiredType, String description,
        short numLeadingMinus, String shortCmdName, Type defaultValue) {
      this(optionName, requiredType, description, null, numLeadingMinus,
          shortCmdName, defaultValue);
    }
    
    /**
     * 
     * @param optionName
     * @param requiredType
     * @param description
     * @param rangeSpec - See {@link #getRangeSpecifiaction()}.
     * @param defaultValue
     */
    public Option(String optionName, Class<Type> requiredType, String description,
        String rangeSpec, Type defaultValue) {
      this(optionName, requiredType, description, rangeSpec, (short) 2, defaultValue);
    }
    
    /**
     * 
     * @param optionName
     * @param requiredType
     * @param description
     * @param rangeSpec - See {@link #getRangeSpecifiaction()}.
     * @param numLeadingMinus
     *        the number of leading '-' symbols of the command-line argument
     *        corresponding to this option.
     * @param defaultValue
     */
    public Option(String optionName, Class<Type> requiredType, String description,
        String rangeSpec, short numLeadingMinus, Type defaultValue) {
      this(optionName, requiredType, description, rangeSpec, numLeadingMinus,
          null, defaultValue);
    }
    
    /**
     * 
     * @param optionName
     * @param requiredType
     * @param description
     * @param rangeSpec - See {@link #getRangeSpecifiaction()}.
     * @param numLeadingMinus
     * @param shortCmdName
     * @param defaultValue
     */
    public Option(String optionName, Class<Type> requiredType, String description,
	String rangeSpec, short numLeadingMinus, String shortCmdName, Type defaultValue) {
	this.optionName = optionName;
	this.requiredType = requiredType;
	this.description = description;
	this.rangeSpec = rangeSpec;
	if (rangeSpec!=null && rangeSpec.length()>0) {
	  this.range = parseRangeSpec(rangeSpec);
	} else {
		this.range=null;
	}
	this.shortCmdName = shortCmdName;
	this.defaultValue = defaultValue;
	if (numLeadingMinus < 0) {
	    throw new IllegalArgumentException(
		"numLeadingMinus must be a positive number");
	}
	this.numLeadingMinus = numLeadingMinus;
    }

    /**
     * @return
     * @see #createArgumentHolder(Object)
     */
    public Object createArgumentHolder() {
	if (requiredType.equals(Float.class)) {
	    return new FloatHolder();
	} else if (requiredType.equals(Double.class)) {
	    return new DoubleHolder();
	} else if (requiredType.equals(Short.class)) {
	    return new IntHolder();
	} else if (requiredType.equals(Integer.class)) {
	    return new IntHolder();
	} else if (requiredType.equals(Long.class)) {
	    return new LongHolder();
	} else if (requiredType.equals(Boolean.class)) {
	    return new BooleanHolder();
	} else if (requiredType.equals(Character.class)) {
	    return new CharHolder();
	} else if (requiredType.equals(String.class)) {
	    return new StringHolder();
	} else {
	    return new StringHolder();
	}
    }

    /**
     * @param defaultValue
     * @return
     */
    public Object createArgumentHolder(Object defaultValue) {
	String value = defaultValue.toString();
	if (requiredType.equals(Float.class)) {
	    return new FloatHolder(Float.parseFloat(value));
	} else if (requiredType.equals(Double.class)) {
	    return new DoubleHolder(Double.parseDouble(value));
	} else if (requiredType.equals(Short.class)) {
	    return new IntHolder(Short.parseShort(value));
	} else if (requiredType.equals(Integer.class)) {
	    return new IntHolder(Integer.parseInt(value));
	} else if (requiredType.equals(Long.class)) {
	    return new LongHolder(Long.parseLong(value));
	} else if (requiredType.equals(Boolean.class)) {
	    return new BooleanHolder(Boolean.parseBoolean(value));
	} else if (requiredType.equals(Character.class)) {
	    if (value.length() != 1) {
		throw new IllegalArgumentException("Invalid char symbol "
			+ value);
	    }
	    return new CharHolder(value.charAt(0));
	} else if (requiredType.equals(String.class)) {
	    return new StringHolder(value);
	} else {
	    return new StringHolder(defaultValue.toString());
	}
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	return o.toString().equals(toString());
    }

    /**
     * @return the description
     */
    public String getDescription() {
	return description;
    }

    /**
     * @return the numLeadingMinus
     */
    public short getNumLeadingMinus() {
	return numLeadingMinus;
    }
    
    /**
     * @return the range
     */
    public rangeCollection getRange() {
    	return range;
    }
    
    /**
     * @return this {@link Option}'s name
     */
    public String getOptionName() {
	return optionName;
    }
    
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
     * @return the rangeSpec
     */
    public String getRangeSpecifiaction() {
	return rangeSpec;
    }

    /**
     * @return the type
     */
    public Class<Type> getRequiredType() {
	return requiredType;
    }
    
    /**
     * The default value for this option. If it is null,
     * the cfg packet tries to read it from an config.xml.
     * 
     * If this fails, an exception is thrown.
     * @return
     */
    public Type getDefaultValue() {
      return defaultValue;
    }
    
    /**
     * Returns the value for this Option, which must be contained
     * in the given SBPreferences. 
     * @param parentPreferences
     * @return
     */
    public Type getValue(SBPreferences parentPreferences) {
    	// Returns a string.
    	Object ret = parentPreferences.get(this.toString());
    	
    	return parseOrCast(ret);
    }
    
    /**
     * Returns the value for this Option, which must be contained
     * in the given SBProperties. 
     * @param parentPreferences
     * @return
     */
		public Type getValue(SBProperties parentProperties) {
    	// Returns a string.
    	Object ret = parentProperties.getProperty(this.toString());
    	
    	return parseOrCast(ret);
    }

    /**
     * Convert 'ret' to {@link #requiredType} by parsing it
     * (e.g. Integer.parseInt), or casting it to the desired type.
     * @param ret
     * @return
     */
		@SuppressWarnings("unchecked")
		private Type parseOrCast(Object ret) {
			if (ret == null)
    		return null;
    	if (Reflect.containsParser(requiredType))
    		ret = Reflect.invokeParser(requiredType, ret);
    	
    	if (requiredType.equals(Character.class)) {
    		ret = ((Character)ret.toString().charAt(0));
    	}
    	
    	return (Type) ret;
		}
    

    /**
     * @return the shortCmdName
     */
    public String getShortCmdName() {
	return shortCmdName;
    }

    /**
     * A {@link String} to be parsed by an {@link ArgParser} to specify the
     * command line option corresponding to this {@link Option}. If a short
     * version of this option is set, this will have to be used without any
     * separators between the short option name and the command line value. For
     * instance, if the short option's name is '-v', the {@link ArgParser} will
     * require key value pairs such as '-vMyValue'. For the long option name,
     * multiple versions are generated to separate the option's name from the
     * value.
     * 
     * @return
     */
    public String getSpecification() {
	StringBuilder sb = new StringBuilder();
	if (isSetShortCmdName()) {
	    sb.append(shortCmdName);
	    sb.append(',');
	}
	String cmd = toCommandLineOptionKey();
	String separators[] = { "=", " ", "" };
	for (int i = 0; i < separators.length; i++) {
	    sb.append(cmd);
	    sb.append(separators[i]);
	    if (i < separators.length - 1) {
		sb.append(',');
	    }
	}
	sb.append('%');
	if (requiredType.equals(Float.class)) {
	    sb.append('f');
	} else if (requiredType.equals(Double.class)) {
	    sb.append('f');
	} else if (requiredType.equals(Short.class)) {
	    sb.append('d');
	} else if (requiredType.equals(Integer.class)) {
	    sb.append('d');
	} else if (requiredType.equals(Long.class)) {
	    sb.append('d');
	} else if (requiredType.equals(Boolean.class)) {
	    sb.append('v');
	} else if (requiredType.equals(Character.class)) {
	    sb.append('c');
	} else if (requiredType.equals(String.class)) {
	    sb.append('s');
	} else {
	    // some other Object
	    sb.append('s');
	}
	if (isSetRangeSpecification() && (rangeSpec.length() > 0)) {
	    sb.append(' ');
	    sb.append(rangeSpec);
	}
	sb.append(" #");
	sb.append(getDescription());
	return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return getOptionName().hashCode();
    }

    /**
     * @return
     */
    public final boolean isSetDescription() {
	return description != null;
    }

    /**
     * @return
     */
    public boolean isSetRangeSpecification() {
	return rangeSpec != null;
    }

    /**
     * @return
     */
    public final boolean isSetRequiredType() {
	return requiredType != null;
    }

    /**
	 * 
	 */
    public final boolean isSetShortCmdName() {
	return shortCmdName != null;
    }

    /**
     * @return
     */
    public final boolean optionName() {
	return optionName != null;
    }

    /**
     * This creates a command-line argument name from this {@link Option}'s name
     * by adding {@link #numLeadingMinus} '-' symbols, converting the name to
     * lower case and by replacing all underscores by '-' symbols.
     * 
     * @return
     */
    public String toCommandLineOptionKey() {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < numLeadingMinus; i++) {
	    sb.append('-');
	}
	sb.append(optionName.toLowerCase().replace('_', '-'));
	return sb.toString();
    }

    /**
     * Parse the range specification string into a more convenient
     * data structute. See {@link #getRangeSpecifiaction()} for
     * more information on possible Strings.
     * @param range
     * @return rangeCollection<Type>
     */
    private rangeCollection parseRangeSpec(String range) {
    	rangeCollection ret = new rangeCollection();
    	
			try {
				range = range.substring(range.indexOf('{')+1, range.lastIndexOf('}'));
				// Be carefull with " and '
				//String[] items = range.split(Pattern.quote(","));
				List<Character> stringSep = new LinkedList<Character>();
				stringSep.add('\''); stringSep.add('\"');
				String[] items = CSVReader.getSplits(range, ',', true, true, stringSep);
				
				Range r = null;
				for (int i=0; i<items.length; i++) {
					String item = items[i].trim();
					String item2=item;
					r = new Range();
					
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
					
					r.lBound=parseOrCast(item);
					r.uBound=parseOrCast(item2);
					ret.addRange(r);
				}
				
			} catch (Exception e) {
				System.err.println("Range in wrong format: '" + range +"'.");
				e.printStackTrace();
				return null;
			}

    	return ret;
    }
    
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return optionName;
    }
}
