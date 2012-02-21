/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
* Convenience methods for escaping special characters related to HTML, XML, 
* and regular expressions.
* 
* <P>To keep you safe by default, WEB4J goes to some effort to escape 
* characters in your data when appropriate, such that you <em>usually</em>
* don't need to think too much about escaping special characters. Thus, you
*  shouldn't need to <em>directly</em> use the services of this class very often. 
* 
* <P><span class='highlight'>For Model Objects containing free form user input, 
* it is highly recommended that you use {@link SafeText}, not <tt>String</tt></span>.
* Free form user input is open to malicious use, such as
* <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>Cross Site Scripting</a>
* attacks. 
* Using <tt>SafeText</tt> will protect you from such attacks, by always escaping 
* special characters automatically in its <tt>toString()</tt> method.   
* 
* <P>The following WEB4J classes will automatically escape special characters 
* for you, when needed : 
* <ul>
* <li>the {@link SafeText} class, used as a building block class for your 
* application's Model Objects, for modeling all free form user input
* <li>the {@link Populate} tag used with forms
* <li>the {@link Report} class used for creating quick reports
* <li>the {@link Text}, {@link TextFlow}, and {@link Tooltips} custom tags used 
* for translation
* </ul> 
* 
* @author modified by Wrzodek
* @version $Rev$
* @since 1.0
*  1. ä = \u00e4
   2. Ä = \u00c4
   3. ö = \u00f6
   4. Ö = \u00d6
   5. ü = \u00fc
   6. Ü = \u00dc
   7. ß = \u00df

*/
public final class EscapeChars {

  /**
   * Escape characters for text appearing in HTML markup.
   * 
   * <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
   * The idea is to neutralize control characters commonly used by scripts, such that
   * they will not be executed by the browser. This is done by replacing the control
   * characters with their escaped equivalents.  
   * See {@link hirondelle.web4j.security.SafeText} as well.
   * 
   * <P>The following characters are replaced with corresponding 
   * HTML character entities :
   * <table border='1' cellpadding='3' cellspacing='0'>
   * <tr><th> Character </th><th>Replacement</th></tr>
   * <tr><td> < </td><td> &lt; </td></tr>
   * <tr><td> > </td><td> &gt; </td></tr>
   * <tr><td> & </td><td> &amp; </td></tr>
   * <tr><td> " </td><td> &quot;</td></tr>
   * <tr><td> \t </td><td> &#009;</td></tr>
   * <tr><td> ! </td><td> &#033;</td></tr>
   * <tr><td> # </td><td> &#035;</td></tr>
   * <tr><td> $ </td><td> &#036;</td></tr>
   * <tr><td> % </td><td> &#037;</td></tr>
   * <tr><td> ' </td><td> &#039;</td></tr>
   * <tr><td> ( </td><td> &#040;</td></tr> 
   * <tr><td> ) </td><td> &#041;</td></tr>
   * <tr><td> * </td><td> &#042;</td></tr>
   * <tr><td> + </td><td> &#043; </td></tr>
   * <tr><td> , </td><td> &#044; </td></tr>
   * <tr><td> - </td><td> &#045; </td></tr>
   * <tr><td> . </td><td> &#046; </td></tr>
   * <tr><td> / </td><td> &#047; </td></tr>
   * <tr><td> : </td><td> &#058;</td></tr>
   * <tr><td> ; </td><td> &#059;</td></tr>
   * <tr><td> = </td><td> &#061;</td></tr>
   * <tr><td> ? </td><td> &#063;</td></tr>
   * <tr><td> @ </td><td> &#064;</td></tr>
   * <tr><td> [ </td><td> &#091;</td></tr>
   * <tr><td> \ </td><td> &#092;</td></tr>
   * <tr><td> ] </td><td> &#093;</td></tr>
   * <tr><td> ^ </td><td> &#094;</td></tr>
   * <tr><td> _ </td><td> &#095;</td></tr>
   * <tr><td> ` </td><td> &#096;</td></tr>
   * <tr><td> { </td><td> &#123;</td></tr>
   * <tr><td> | </td><td> &#124;</td></tr>
   * <tr><td> } </td><td> &#125;</td></tr>
   * <tr><td> ~ </td><td> &#126;</td></tr>
   * </table>
   * 
   * <P>Note that JSTL's {@code <c:out>} escapes <em>only the first 
   * five</em> of the above characters.
   */
  public static final String forHTML(String string){
    return forHTML(string,false);
  }
  
  /**
   * 
   * @param string
   * @param skipExistingHTMLencodings
   * @return
   */
  public static final String forHTML(String string, boolean skipExistingHTMLencodings){
    final StringBuilder result = new StringBuilder();
    for (char character : string.toCharArray()) {
      if      (character == '\t')result.append("&#009;");
      else if (character == '!') result.append("&#033;");
      else if (character == '#') result.append("&#035;");
      else if (character == '$') result.append("&#036;");
      else if (character == '%') result.append("&#037;");
      else if (character == '\'')result.append("&#039;");
      else if (character == '(') result.append("&#040;");
      else if (character == ')') result.append("&#041;");
      else if (character == '*') result.append("&#042;");
      else if (character == '+') result.append("&#043;");
      else if (character == ',') result.append("&#044;");
      else if (character == '-') result.append("&#045;");
      else if (character == '.') result.append("&#046;");
      else if (character == '/') result.append("&#047;");
      else if (character == ':') result.append("&#058;");
      else if (character == ';') result.append("&#059;");
      else if (character == '=') result.append("&#061;");
      else if (character == '?') result.append("&#063;");
      else if (character == '@') result.append("&#064;");
      else if (character == '[') result.append("&#091;");
      else if (character == '\\')result.append("&#092;");
      else if (character == ']') result.append("&#093;");
      else if (character == '^') result.append("&#094;");
      else if (character == '_') result.append("&#095;");
      else if (character == '`') result.append("&#096;");
      else if (character == '{') result.append("&#123;");
      else if (character == '|') result.append("&#124;");
      else if (character == '}') result.append("&#125;");
      else if (character == '~') result.append("&#126;");
      else if (!skipExistingHTMLencodings && character == '<') result.append("&#60;"); 
      else if (!skipExistingHTMLencodings && character == '>') result.append("&#62;"); 
      else if (!skipExistingHTMLencodings && character == '&') result.append("&#38;"); 
      else if (character == '"') result.append("&quot;"); 
      else if (character == '\u00e0') result.append("&#224;");
      else if (character == '\u00c0') result.append("&#192;");
      else if (character == '\u00e2') result.append("&#226;");
      else if (character == '\u00c2') result.append("&#194;");
      else if (character == '\u00e4') result.append("&#228;");
      else if (character == '\u00c4') result.append("&#196;");
      else if (character == '\u00e5') result.append("&#229;");
      else if (character == '\u00c5') result.append("&#00c5;");
      else if (character == '\u00e6') result.append("&#230;");
      else if (character == '\u00c6') result.append("&#198;");
      else if (character == '\u00e7') result.append("&#231;");
      else if (character == '\u00c7') result.append("&#199;");
      else if (character == '\u00e9') result.append("&#233;");
      else if (character == '\u00c9') result.append("&#201;");
      else if (character == '\u00e8') result.append("&#232;");
      else if (character == '\u00c8') result.append("&#200;");
      else if (character == '\u00ea') result.append("&#234;");
      else if (character == '\u00ca') result.append("&#202;");
      else if (character == '\u00eb') result.append("&#235;");
      else if (character == '\u00cb') result.append("&#203;");
      else if (character == '\u00ef') result.append("&#239;");
      else if (character == '\u00cf') result.append("&#207;");
      else if (character == '\u00f4') result.append("&#244;");
      else if (character == '\u00d4') result.append("&#212;");
      else if (character == '\u00f6') result.append("&#246;");
      else if (character == '\u00d6') result.append("&#214;");
      else if (character == '\u00f8') result.append("&#248;");
      else if (character == '\u00d8') result.append("&#216;");
      else if (character == '\u00df') result.append("&#223;");
      else if (character == '\u00f9') result.append("&#249;");
      else if (character == '\u00d9') result.append("&#217;");         
      else if (character == '\u00fb') result.append("&#251;");         
      else if (character == '\u00db') result.append("&#219;");
      else if (character == '\u00fc') result.append("&#252;");
      else if (character == '\u00dc') result.append("&#220;");
      else if (character == '\u00ae') result.append("&#174;");
      else if (character == '\u00a9') result.append("&#169;");
      else if (character == '\u20ac') result.append("&#8364;");
      else if (!skipExistingHTMLencodings && character == '\n')result.append("<br/>");          // Handle Newline
      
      else result.append(character); // simple char, which must not be escaped.
    }
    return result.toString();
  }
  

   public static String stringToHTMLString(String string) {
     StringBuffer sb = new StringBuffer(string.length());
     // true if last char was blank
     boolean lastWasBlankChar = false;
     int len = string.length();
     char c;

     for (int i = 0; i < len; i++)
         {
         c = string.charAt(i);
         if (c == ' ') {
             // blank gets extra work,
             // this solves the problem you get if you replace all
             // blanks with &nbsp;, if you do that you loss 
             // word breaking
             if (lastWasBlankChar) {
                 lastWasBlankChar = false;
                 sb.append("&nbsp;");
                 }
             else {
                 lastWasBlankChar = true;
                 sb.append(' ');
                 }
             }
         else {
             lastWasBlankChar = false;
             //
             // HTML Special Chars
             if (c == '"')
                 sb.append("&quot;");
             else if (c == '&')
                 sb.append("&amp;");
             else if (c == '<')
                 sb.append("&lt;");
             else if (c == '>')
                 sb.append("&gt;");
             else if (c == '\n')
                 // Handle Newline
                 sb.append("&lt;br/&gt;");
             else {
                 int ci = 0xffff & c;
                 if (ci < 160 )
                     // nothing special only 7 Bit
                     sb.append(c);
                 else {
                     // Not 7 Bit use the unicode system
                     sb.append("&#");
                     sb.append(new Integer(ci).toString());
                     sb.append(';');
                     }
                 }
             }
         }
     return sb.toString();
 }
   
  /**
  * Escape all ampersand characters in a URL. 
  *  
  * <P>Replaces all <tt>'&'</tt> characters with <tt>'&amp;'</tt>.
  * 
  *<P>An ampersand character may appear in the query string of a URL.
  * The ampersand character is indeed valid in a URL.
  * <em>However, URLs usually appear as an <tt>HREF</tt> attribute, and 
  * such attributes have the additional constraint that ampersands 
  * must be escaped.</em>
  * 
  * <P>The JSTL <c:url> tag does indeed perform proper URL encoding of 
  * query parameters. But it does not, in general, produce text which 
  * is valid as an <tt>HREF</tt> attribute, simply because it does 
  * not escape the ampersand character. This is a nuisance when 
  * multiple query parameters appear in the URL, since it requires a little 
  * extra work.
  */
  public static String forHrefAmpersand(String aURL){
    return aURL.replace("&", "&amp;");
  }
   
  /**
   * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
   *
   * <P>Used to ensure that HTTP query strings are in proper form, by escaping
   * special characters such as spaces.
   *
   * <P>It is important to note that if a query string appears in an <tt>HREF</tt>
   * attribute, then there are two issues - ensuring the query string is valid HTTP
   * (it is URL-encoded), and ensuring it is valid HTML (ensuring the 
   * ampersand is escaped).
   */
   public static String forURL(String aURLFragment){
     String result = null;
     try {
       result = URLEncoder.encode(aURLFragment, "UTF-8");
     }
     catch (UnsupportedEncodingException ex){
       throw new RuntimeException("UTF-8 not supported", ex);
     }
     return result;
   }

  /**
  * Escape characters for text appearing as XML data, between tags.
  * 
  * <P>The following characters are replaced with corresponding character entities :
  * <table border='1' cellpadding='3' cellspacing='0'>
  * <tr><th> Character </th><th> Encoding </th></tr>
  * <tr><td> < </td><td> &lt; </td></tr>
  * <tr><td> > </td><td> &gt; </td></tr>
  * <tr><td> & </td><td> &amp; </td></tr>
  * <tr><td> " </td><td> &quot;</td></tr>
  * <tr><td> ' </td><td> &#039;</td></tr>
  * </table>
  * 
  * <P>Note that JSTL's {@code <c:out>} escapes the exact same set of 
  * characters as this method. <span class='highlight'>That is, {@code <c:out>}
  *  is good for escaping to produce valid XML, but not for producing safe 
  *  HTML.</span>
  */
  public static String forXML(String aText){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else if (character == '\"') {
        result.append("&quot;");
      }
      else if (character == '\'') {
        result.append("&#039;");
      }
      else if (character == '&') {
         result.append("&amp;");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }

  /**
  * Return <tt>aText</tt> with all <tt>'<'</tt> and <tt>'>'</tt> characters
  * replaced by their escaped equivalents.
  */
  public static String toDisableTags(String aText){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  

  /**
  * Replace characters having special meaning in regular expressions
  * with their escaped equivalents, preceded by a '\' character.
  *
  * <P>The escaped characters include :
  *<ul>
  *<li>.
  *<li>\
  *<li>?, * , and +
  *<li>&
  *<li>:
  *<li>{ and }
  *<li>[ and ]
  *<li>( and )
  *<li>^ and $
  *</ul>
  */
  public static String forRegex(String aRegexFragment){
    final StringBuilder result = new StringBuilder();

    final StringCharacterIterator iterator = 
      new StringCharacterIterator(aRegexFragment)
    ;
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      /*
      * All literals need to have backslashes doubled.
      */
      if (character == '.') {
        result.append("\\.");
      }
      else if (character == '\\') {
        result.append("\\\\");
      }
      else if (character == '?') {
        result.append("\\?");
      }
      else if (character == '*') {
        result.append("\\*");
      }
      else if (character == '+') {
        result.append("\\+");
      }
      else if (character == '&') {
        result.append("\\&");
      }
      else if (character == ':') {
        result.append("\\:");
      }
      else if (character == '{') {
        result.append("\\{");
      }
      else if (character == '}') {
        result.append("\\}");
      }
      else if (character == '[') {
        result.append("\\[");
      }
      else if (character == ']') {
        result.append("\\]");
      }
      else if (character == '(') {
        result.append("\\(");
      }
      else if (character == ')') {
        result.append("\\)");
      }
      else if (character == '^') {
        result.append("\\^");
      }
      else if (character == '$') {
        result.append("\\$");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  
  /**
  * Escape <tt>'$'</tt> and <tt>'\'</tt> characters in replacement strings.
  * 
  * <P>Synonym for <tt>Matcher.quoteReplacement(String)</tt>.
  * 
  * <P>The following methods use replacement strings which treat 
  * <tt>'$'</tt> and <tt>'\'</tt> as special characters:
  * <ul>
  * <li><tt>String.replaceAll(String, String)</tt>
  * <li><tt>String.replaceFirst(String, String)</tt>
  * <li><tt>Matcher.appendReplacement(StringBuffer, String)</tt>
  * </ul>
  * 
  * <P>If replacement text can contain arbitrary characters, then you 
  * will usually need to escape that text, to ensure special characters 
  * are interpreted literally.
  */
  public static String forReplacementString(String aInput){
    return Matcher.quoteReplacement(aInput);
  }
  
  /**
  * Disable all <tt><SCRIPT></tt> tags in <tt>aText</tt>.
  * 
  * <P>Insensitive to case.
  */  
  public static String forScriptTagsOnly(String aText){
    String result = null;
    Matcher matcher = SCRIPT.matcher(aText);
    result = matcher.replaceAll("&lt;SCRIPT>");
    matcher = SCRIPT_END.matcher(result);
    result = matcher.replaceAll("&lt;/SCRIPT>");
    return result;
  }
  
  // PRIVATE //
  
  private EscapeChars(){
    //empty - prevent construction
  }
  
  private static final Pattern SCRIPT = Pattern.compile(
    "<SCRIPT>", Pattern.CASE_INSENSITIVE
   );
  private static final Pattern SCRIPT_END = Pattern.compile(
    "</SCRIPT>", Pattern.CASE_INSENSITIVE
  );
  
  /**
   * 
   * @param aIdx
   * @param aBuilder
   */
  private static void addCharEntity(Integer aIdx, StringBuilder aBuilder){
    String padding = "";
    if( aIdx <= 9 ){
       padding = "00";
    }
    else if( aIdx <= 99 ){
      padding = "0";
    }
    else {
      //no prefix
    }
    String number = padding + aIdx.toString();
    aBuilder.append("&#" + number + ";");
  }
}
 
