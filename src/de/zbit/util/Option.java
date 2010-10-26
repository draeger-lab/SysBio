/**
 * 
 */
package de.zbit.util;

import argparser.BooleanHolder;
import argparser.CharHolder;
import argparser.DoubleHolder;
import argparser.FloatHolder;
import argparser.IntHolder;
import argparser.LongHolder;
import argparser.ObjectHolder;
import argparser.StringHolder;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-24
 */
public class Option {

	/**
	 * A short description what the purpose of this option is.
	 */
	private String description;
	/**
	 * 
	 */
	private short numLeadingMinus;
	/**
	 * The name of this option.
	 */
	private String optionName;
	/**
	 * 
	 */
	private String rangeSpec;
	/**
	 * The data type that is expected as an associated value in a key-value pair
	 * of this {@link Option}'s name and a value. For instance, Boolean,
	 * Integer, String etc.
	 */
	private Class<?> requiredType;

	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 */
	public Option(String optionName, Class<?> requiredType, String description) {
		this(optionName, requiredType, description, null);
	}

	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param rangeSpec
	 */
	public Option(String optionName, Class<?> requiredType, String description,
			String rangeSpec) {
		this(optionName, requiredType, description, rangeSpec, (short) 2);
	}

	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param rangeSpec
	 * @param numLeadingMinus
	 *            the number of leading '-' symbols of the command-line argument
	 *            corresponding to this option.
	 */
	public Option(String optionName, Class<?> requiredType, String description,
			String rangeSpec, short numLeadingMinus) {
		this.optionName = optionName;
		this.requiredType = requiredType;
		this.description = description;
		this.rangeSpec = rangeSpec;
		if (numLeadingMinus < 0) {
			throw new IllegalArgumentException(
					"numLeadingMinus must be a positive number");
		}
		this.numLeadingMinus = numLeadingMinus;
	}

	/**
	 * 
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
			return new ObjectHolder();
		}
	}

	/**
	 * 
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
			return new ObjectHolder(defaultValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * @return this {@link Option}'s name
	 */
	public String getOptionName() {
		return optionName;
	}

	/**
	 * 
	 * @return
	 */
	public String getRangeSpecifiaction() {
		return rangeSpec;
	}

	/**
	 * @return the type
	 */
	public Class<?> getRequiredType() {
		return requiredType;
	}

	/**
	 * 
	 * @return
	 */
	public String getSpecification() {
		StringBuilder sb = new StringBuilder();
		String cmd = toCommandLineOptionKey();
		String separators[] = { "=", " ",  ""};
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
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getOptionName().hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetRangeSpecification() {
		return rangeSpec != null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return optionName;
	}
}
