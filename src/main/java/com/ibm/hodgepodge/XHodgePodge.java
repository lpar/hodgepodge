package com.ibm.hodgepodge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.DateTime;
import lotus.domino.International;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Implements conversion functions for Domino DateTime objects 
 * to and from other Java date and time classes.
 */
public class XHodgePodge {
 
  private static DateTimeFormatter dominoFormat;
  
 /**
  * Builds a Java 8 DateTimeFormatter string to parse dates and times 
  * in current Notes format, as per the International settings.
  * 
  * Generally you'll just want to call buildFormatter, or let XHodgePodge
  * build the formatter for you the first time you convert from a DateTime.
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
 
  /**
   * Gets a formatter to handle Domino DateTime parsing.
   * @param ndt a NotesDateTime, used to obtain a session if needed
   * @return
   * @throws NotesException
   */
  private static DateTimeFormatter getFormatter(DateTime ndt) throws NotesException {
    if (XHodgePodge.dominoFormat == null) {
      Session session = ndt.getParent();
      International i18n = session.getInternational();
      XHodgePodge.dominoFormat = buildFormatter(i18n);
    }
    return XHodgePodge.dominoFormat;
  }
 
  /**
   * Converts a DateTime to a ZonedDateTime, preserving the time zone stored in the 
   * original DateTime object.
   * 
   * If you don't need to preserve the time zone, it's probably a little faster and
   * more robust to use toZonedDateTimeUTC.
   * 
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static ZonedDateTime toZonedDateTime(DateTime ndt) throws NotesException {
    DateTimeFormatter dtf = getFormatter(ndt);
    return ZonedDateTime.from(dtf.parse(ndt.getZoneTime()));
  }

  /**
   * Converts a DateTime to a ZonedDateTime, at the cost of converting the time zone
   * to UTC. Avoids parsing textual date/time information. If you need to keep the
   * original time zone information, use toZonedDateTime.
   * 
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static ZonedDateTime toZonedDateTimeUTC(DateTime ndt) throws NotesException {
    Date d = ndt.toJavaDate();
    return ZonedDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
  }

  /**
   * Converts a Domino DateTime to a Date.
   * 
   * @param dt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public Date toDate(final DateTime dt) throws NotesException {
    return dt.toJavaDate();
  } 
 
  /**
   * Converts a Domino DateTime to a Calendar via toZonedDateTime.
   * 
   * @param dt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public Calendar toCalendar(final DateTime ndt) throws NotesException {
    DateTimeFormatter dtf = getFormatter(ndt);
    ZonedDateTime zdt = ZonedDateTime.from(dtf.parse(ndt.getZoneTime()));
    return GregorianCalendar.from(zdt);
  }
 
  /**
   * Converts a DateTime to a LocalDateTime via toZonedDateTime.
   * 
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static LocalDateTime toLocalDateTime(DateTime ndt) throws NotesException {
    ZonedDateTime zdt = toZonedDateTime(ndt);
    return zdt.toLocalDateTime();
  }

  /**
   * Converts a NotesDateTime to a LocalDate via toZonedDateTime.
   * 
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static LocalDate toLocalDate(DateTime ndt) throws NotesException {
    ZonedDateTime zdt = toZonedDateTime(ndt);
    return zdt.toLocalDate();
  }
  
  /**
   * Converts a NotesDateTime to a LocalTime via toZonedDateTime.
   * 
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static LocalTime toLocalTime(DateTime ndt) throws NotesException {
    ZonedDateTime zdt = toZonedDateTime(ndt);
    return zdt.toLocalTime();
  }

  /**
   * Converts a DateTime to an Instant.
   * 
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static Instant toInstant(DateTime ndt) throws NotesException {
    return ndt.toJavaDate().toInstant();
  }
  
 
  /**
   * Converts a Date to a Domino DateTime.
   * 
   * @param session a Session to use to create the DateTime object
   * @param d the Date to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final Date d) throws NotesException {
    return session.createDateTime(d);
  }

  /**
   * Converts a Calendar to a Domino DateTime.
   * 
   * @param session a Session to use to create the DateTime object
   * @param d the Calendar to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final Calendar cal) throws NotesException {
    return session.createDateTime(cal);
  }

  /**
   * Converts an Instant to a Domino DateTime.
   * 
   * @param session a Session to use to create the DateTime object
   * @param ins the Instant to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final Instant ins) throws NotesException {
    return session.createDateTime(Date.from(ins));
  }

  /**
   * Converts a ZonedDateTime to a Domino DateTime.
   * 
   * @param session a Session to use to create the DateTime object
   * @param zdt the ZonedDateTime to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final ZonedDateTime zdt) throws NotesException {
    return session.createDateTime(GregorianCalendar.from(zdt));
  }

  /**
   * Converts a LocalDateTime to a Domino DateTime, which ends up in the current system time zone.
   * 
   * @param session a Session to use to create the DateTime object
   * @param ldt the LocalDateTime to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final LocalDateTime ldt) throws NotesException {
    return session.createDateTime(GregorianCalendar.from(ZonedDateTime.of(ldt, ZoneId.systemDefault())));
  }

  /**
   * Converts a LocalDate to a Domino DateTime with wildcarded time.
   * 
   * @param session a Session to use to create the DateTime object
   * @param ld the LocalDate to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final LocalDate ld) throws NotesException {
    Calendar cal = new GregorianCalendar();
    cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
    DateTime ndt = session.createDateTime(cal);
    ndt.setAnyTime();
    return ndt;
  }

  /**
   * Converts a LocalTime to a Domino DateTime with wildcarded date.
   * 
   * @param session a Session to use to create the DateTime object
   * @param lt the LocalTime to convert
   * @return
   * @throws NotesException
   */
  public DateTime toDateTime(final Session session, final LocalTime lt) throws NotesException {
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.HOUR_OF_DAY, lt.getHour());
    cal.set(Calendar.MINUTE, lt.getMinute());
    cal.set(Calendar.SECOND, lt.getSecond());
    DateTime ndt = session.createDateTime(cal);
    ndt.setAnyDate();
    return ndt;
  }

 
}
