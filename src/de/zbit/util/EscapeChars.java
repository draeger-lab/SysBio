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
* 
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
      else if (!skipExistingHTMLencodings && character == '<') result.append("&lt;"); 
      else if (!skipExistingHTMLencodings && character == '>') result.append("&gt;"); 
      else if (!skipExistingHTMLencodings && character == '&') result.append("&amp;"); 
      else if (character == '"') result.append("&quot;"); 
      else if (character == 'à') result.append("&agrave;");
      else if (character == 'À') result.append("&Agrave;");
      else if (character == 'â') result.append("&acirc;");
      else if (character == 'Â') result.append("&Acirc;");
      else if (character == 'ä') result.append("&auml;");
      else if (character == 'Ä') result.append("&Auml;");
      else if (character == 'å') result.append("&aring;");
      else if (character == 'Å') result.append("&Aring;");
      else if (character == 'æ') result.append("&aelig;");
      else if (character == 'Æ') result.append("&AElig;");
      else if (character == 'ç') result.append("&ccedil;");
      else if (character == 'Ç') result.append("&Ccedil;");
      else if (character == 'é') result.append("&eacute;");
      else if (character == 'É') result.append("&Eacute;");
      else if (character == 'è') result.append("&egrave;");
      else if (character == 'È') result.append("&Egrave;");
      else if (character == 'ê') result.append("&ecirc;");
      else if (character == 'Ê') result.append("&Ecirc;");
      else if (character == 'ë') result.append("&euml;");
      else if (character == 'Ë') result.append("&Euml;");
      else if (character == 'ï') result.append("&iuml;");
      else if (character == 'Ï') result.append("&Iuml;");
      else if (character == 'ô') result.append("&ocirc;");
      else if (character == 'Ô') result.append("&Ocirc;");
      else if (character == 'ö') result.append("&ouml;");
      else if (character == 'Ö') result.append("&Ouml;");
      else if (character == 'ø') result.append("&oslash;");
      else if (character == 'Ø') result.append("&Oslash;");
      else if (character == 'ß') result.append("&szlig;");
      else if (character == 'ù') result.append("&ugrave;");
      else if (character == 'Ù') result.append("&Ugrave;");         
      else if (character == 'û') result.append("&ucirc;");         
      else if (character == 'Û') result.append("&Ucirc;");
      else if (character == 'ü') result.append("&uuml;");
      else if (character == 'Ü') result.append("&Uuml;");
      else if (character == '®') result.append("&reg;");
      else if (character == '©') result.append("&copy;");
      else if (character == '€') result.append("&euro;");
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
 
