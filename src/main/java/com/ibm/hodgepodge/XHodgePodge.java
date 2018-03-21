package com.ibm.hodgepodge;

import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.DateTime;
import lotus.domino.International;

/**
 * Implements conversion functions for Domino DateTime objects.
 */
public class XHodgePodge {
  
 /**
  * Builds a Java 8 DateTimeFormatter string to parse dates and times 
  * in current Notes format, as per the International settings.
  * 
  * Generally you'll just want to call buildFormatter.
  */
  public static String buildFormatterString(International i18n) {
    String tsep = i18n.getTimeSep();
    String dsep = i18n.getDateSep();
    StringBuilder pat = new StringBuilder();
    // Date
    if (i18n.isDateYMD()) {
      pat.append("y").append(dsep).append("M").append(dsep).append("d");
    }
    if (i18n.isDateDMY()) {
      pat.append("d").append(dsep).append("M").append(dsep).append("y");
    }
    if (i18n.isDateMDY()) {
      pat.append("M").append(dsep).append("d").append(dsep).append("y");
    }
    pat.append(" ");
    // Time
    pat.append(i18n.isTime24Hour() ? "H" : "h");
    pat.append(tsep);
    pat.append("mm");
    pat.append(tsep);
    pat.append("ss");
    // AM or PM
    if (!i18n.isTime24Hour()) {
      pat.append(" a");
    }
    pat.append(" z");
    return pat.toString();
  }
  
  /**
   * Builds a DateTimeFormatter to parse DateTime.zoneDateTime() values,
   * given the current lotus.domino.International settings.
   * 
   * @param i18n the current International settings retrieved from session.getInternational();
   * @return a DateTimeFormatter
   */
  public static DateTimeFormatter buildFormatter(International i18n) {
    String fmt = buildFormatterString(i18n);
    return DateTimeFormatter.ofPattern(fmt);
  }
 
}
