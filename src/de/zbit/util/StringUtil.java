package de.zbit.util;

import java.util.Arrays;
import java.util.List;

public class StringUtil {

    /**
     * Returns the concatenated strings of the array separated with the given
     * delimiter.
     * 
     * @param ary
     * @param delim
     * @return
     */
    public static String implode(String[] ary, String delim) {
        String out = "";
        for(int i=0; i<ary.length; i++) {
            if(i!=0) { out += delim; }
            out += ary[i];
        }
        return out;
    }

    /**
     * Returns the concatenated strings of the array separated with the given
     * delimiter.
     * 
     * @param ary
     * @param delim
     * @return
     */
    public static String implode(List<String> list, String delim) {
      String[] ary = new String[list.size()];
      list.toArray(ary);
      return implode(ary, delim);
    }

    public static String fill(String input, int len, char fill, boolean prepend) {
        if( input == null ) {
            input = "";
        }
        if( len <= input.length() ) {
            return input;
        }

        char[] cs = new char[len - input.length()];
        Arrays.fill(cs, fill);
        
        return prepend ? (new String(cs) + input) : input + (new String(cs));
    }
}
