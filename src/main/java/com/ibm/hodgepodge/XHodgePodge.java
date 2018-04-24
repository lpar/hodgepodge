package com.ibm.hodgepodge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import lotus.domino.DateTime;
import lotus.domino.International;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Implements conversion functions for Domino DateTime objects
 * to and from other Java date and time classes.
 */
public class XHodgePodge {

  private static DateTimeFormatter localFormat;

  /**
   * Builds a Java 8 DateTimeFormatter string to parse dates and times
   * in current Notes format, as per the International settings.
   * Note that the formatter doesn't parse time zones, because those can't be
   * parsed in Java 8, as per
   * https://bugs.openjdk.java.net/browse/JDK-8066806
   *
   * Generally you'll just want to call buildFormatter, or let XHodgePodge
   * build the formatter for you the first time you convert from a DateTime.
   * @throws NotesException
   */
  public static String buildFormatterString(final International i18n) throws NotesException {
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
    return pat.toString();
  }

  /**
   * Builds a DateTimeFormatter to parse DateTime.zoneDateTime() values, given
   * the current lotus.domino.International settings.
   *
   * @param i18n the current International settings retrieved from session.getInternational();
   * @return a DateTimeFormatter
   * @throws NotesException
   */
  public static DateTimeFormatter buildFormatter(final International i18n) throws NotesException {
    String fmt = buildFormatterString(i18n);
    return DateTimeFormatter.ofPattern(fmt);
  }

  /**
   * Gets a formatter to handle Domino DateTime.zoneDateTime() parsing to a LocalDateTime.
   * @param ndt a NotesDateTime, used to obtain a session if needed
   * @return
   * @throws NotesException
   */
  private static DateTimeFormatter getFormatter(final DateTime ndt) throws NotesException {
    if (XHodgePodge.localFormat == null) {
      Session session = ndt.getParent();
      International i18n = session.getInternational();
      XHodgePodge.localFormat = buildFormatter(i18n);
    }
    return XHodgePodge.localFormat;
  }


  /**
   * Converts a DateTime to an OffsetDateTime, preserving the time zone stored in the
   * original DateTime object.
   *
   * If you don't need to preserve the time zone, it's faster and  more robust
   * to use toZonedDateTimeUTC. This code has to use the text  representations
   * of the DateTime and re-parses them. This is because there's no way to get
   * the actual hours-and-minutes time zone offset from a DateTime --
   * you can only get an integer number of hours, which isn't good enough for
   * (for example) Australia.
   *
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static OffsetDateTime toOffsetDateTime(final DateTime ndt) throws NotesException {
    DateTimeFormatter dtf = getFormatter(ndt);
    // First parse the date and time into a LocalDateTime in the original DateTime
    // object's time zone.
    String sorigtime = ndt.getZoneTime();
    // Remove the time zone
    int tzi = sorigtime.lastIndexOf(' ');
    String rawdatetime = sorigtime.substring(0, tzi);
    LocalDateTime origtime = LocalDateTime.from(dtf.parse(rawdatetime));
    // Then parse the GMT date and time into a LocalDateTime in UTC,
    // again removing the time zone.
    String sgmt = ndt.getGMTTime();
    sgmt = sgmt.substring(0, sgmt.lastIndexOf(' '));
    LocalDateTime gmttime = LocalDateTime.from(dtf.parse(sgmt));
    // The difference between the two will give us the full time zone offset of
    // the time zone of the original DateTime object, which we can't get any other
    // way. (DateTime.timeZone() returns an int, and there are plenty of common
    // countries with non-integer offsets from UTC, including Australia.)
    long minutes = ChronoUnit.MINUTES.between(gmttime, origtime);
    // Now we can convert that into a time zone offset
    int hh = (int) minutes/60;
    int mm = (int) minutes % 60;
    ZoneOffset zo = ZoneOffset.ofHoursMinutes(hh, mm);
    // Combine it with the original date time to create our answer
    OffsetDateTime odt = origtime.atOffset(zo);
    return odt;
  }

  /**
   * Converts a DateTime plus a Notes time zone field to a ZonedDateTime.
   * If you don't have a Notes time zone field, you'll need to use toOffsetDateTime.
   *
   * @param ndt
   * @param notesTimeZone
   * @return
   * @throws NotesException
   */
  public static ZonedDateTime toZonedDateTime(final DateTime ndt, final String notesTimeZone) throws NotesException {
    DateTimeFormatter dtf = getFormatter(ndt);
    String lst = ndt.getZoneTime();
    lst = lst.substring(0, lst.lastIndexOf(' '));
    ZoneId tzid = toZoneId(notesTimeZone);
    LocalDateTime ldt = LocalDateTime.from(dtf.parse(lst));
    ZonedDateTime zdt = ldt.atZone(tzid);
    return zdt;
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
  public static ZonedDateTime toZonedDateTimeUTC(final DateTime ndt) throws NotesException {
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
  public static Date toDate(final DateTime dt) throws NotesException {
    return dt.toJavaDate();
  }

  /**
   * Converts a Domino DateTime and time zone to a Calendar via toZonedDateTime.
   *
   * @param dt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static Calendar toCalendar(final DateTime ndt, final String notesTimeZone) throws NotesException {
    ZonedDateTime zdt = toZonedDateTime(ndt, notesTimeZone);
    return GregorianCalendar.from(zdt);
  }

  /**
   * Converts a DateTime to a LocalDateTime by discarding time zone information.
   *
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static LocalDateTime toLocalDateTime(final DateTime ndt) throws NotesException {
    DateTimeFormatter dtf = getFormatter(ndt);
    // Notice: getZoneTime, not getLocalTime. The latter would adjust the date/time
    // to our runtime local time zone, which is not what we want. We want the
    // unmodified original time and date from the DateTime object.
    String lst = ndt.getZoneTime();
    return LocalDateTime.from(dtf.parse(lst));
  }

  /**
   * Converts a NotesDateTime to a LocalDate by discarding time zone and time
   * information.
   *
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static LocalDate toLocalDate(final DateTime ndt) throws NotesException {
    LocalDateTime ldt = toLocalDateTime(ndt);
    return ldt.toLocalDate();
  }

  /**
   * Converts a NotesDateTime to a LocalTime by discarding date and time zone
   * information.
   *
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static LocalTime toLocalTime(final DateTime ndt) throws NotesException {
    LocalDateTime ldt = toLocalDateTime(ndt);
    return ldt.toLocalTime();
  }

  /**
   * Converts a DateTime to an Instant.
   *
   * @param ndt the Domino DateTime to convert
   * @return
   * @throws NotesException
   */
  public static Instant toInstant(final DateTime ndt) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final Date d) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final Calendar cal) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final Instant ins) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final ZonedDateTime zdt) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final LocalDateTime ldt) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final LocalDate ld) throws NotesException {
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
  public static DateTime toDateTime(final Session session, final LocalTime lt) throws NotesException {
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.HOUR_OF_DAY, lt.getHour());
    cal.set(Calendar.MINUTE, lt.getMinute());
    cal.set(Calendar.SECOND, lt.getSecond());
    DateTime ndt = session.createDateTime(cal);
    ndt.setAnyDate();
    return ndt;
  }

  /**
   * Converts a Notes time zone abbreviation to an offset, if possible.
   *
   * You should not use this except as a last resort, because time zone
   * abbreviations are ambiguous.
   *
   * @param tzname the TLA or EFLA for the time zone
   * @return a hh:mm time zone offset as a string
   */
  public static String decodeNotesShortZone(final String tzname) {
    switch (tzname) {
      case "ADT": return "-03:00";
      case "AST": return "-04:00";
      case "BST": return "-10:00";
      case "CDT": return "-05:00";
      case "CEDT": return "+02:00";
      case "CET": return "+01:00";
      case "CST": return "-06:00";
      case "EDT": return "-04:00";
      case "EST": return "-05:00";
      case "GDT": return "+01:00";
      case "MDT": return "-06:00";
      case "MST": return "-07:00";
      case "NDT": return "-02:30";
      case "NST": return "-03:30";
      case "PDT": return "-07:00";
      case "PST": return "-08:00";
      case "YDT": return "-08:00";
      case "YST": return "-09:00";
      case "YW1": return "-00:00";
      case "YW2": return "-01:00";
      case "YW3": return "-02:00";
      case "ZE10": return "+10:00";
      case "ZE11": return "+11:00";
      case "ZE12": return "+12:00";
      case "ZE13": return "+13:00";
      case "ZE2": return "+02:00";
      case "ZE3": return "+03:00";
      case "ZE3B": return "+03:30";
      case "ZE4": return "+04:00";
      case "ZE4B": return "+04:30";
      case "ZE5": return "+05:00";
      case "ZE5B": return "+05:30";
      case "ZE5C": return "+05:45";
      case "ZE6": return "+06:00";
      case "ZE6B": return "+06:30";
      case "ZE7": return "+07:00";
      case "ZE8": return "+08:00";
      case "ZE9": return "+09:00";
      case "ZE9B": return "+09:30";
      case "ZW1": return "-01:00";
      case "ZW12": return "-12:00";
      case "ZW2": return "-02:00";
      case "ZW3": return "-03:00";
    }
    return tzname;
  }

  /**
   * Returns the closest equivalent Java 8 TimeZone for the Notes time zone
   * field value. If no conversion is known to the code, returns null,
   * in which case you should probably check to see if there's an updated
   * version of this code.
   *
   * @param notesTimeZone
   * @return
   */
  public static ZoneId toZoneId(final String notesTimeZone) {
    String ctz = toJavaTimeZone(notesTimeZone);
    if (ctz == null) {
      return null;
    }
    return ZoneId.of(ctz);
  }

  /**
   * Converts a Notes time zone field value to a Java semantic time zone name,
   * preserving meaning as closely as possible. Returns null if no conversion
   * is possible, in which case you should probably check to see if there's
   * an updated version of this code.
   *
   * Note that in some cases, Notes time zones have the wrong value, so you
   * will get a different offset for the Java time zone than you would get from
   * LotusScript. An example is Samoa, which is still listed as UTC-13 in Notes,
   * but is UTC-11 in Java (the language, not the island). The Java value is
   * correct, as Samoa moved across the International date line on 2011-12-29.
   *
   * @param notesTimeZone
   * @return
   */
  public static String toJavaTimeZone(final String notesTimeZone) {
    int tzi = notesTimeZone.lastIndexOf("ZN=");
    if (tzi < 8) {
      return null;
    }
    String tz = notesTimeZone.substring(tzi + 3);
    switch(tz) {
      case "Line Islands": return "Etc/GMT-14";
      case "UTC+13": return "Etc/GMT-13";
      case "Tonga": return "Pacific/Tongatapu";
      case "Samoa": return "Pacific/Samoa";
      case "Chatham Islands": return "Pacific/Chatham";
      case "UTC+12": return "Etc/GMT-12";
      case "Russia Time Zone 11": return "Asia/Magadan";
      case "New Zealand": return "Pacific/Auckland";
      case "Kamchatka": return "Asia/Kamchatka";
      case "Fiji": return "Pacific/Fiji";
      case "Sakhalin": return "Asia/Sakhalin";
      case "Russia Time Zone 10": return "Asia/Srednekolymsk";
      case "Norfolk": return "Pacific/Norfolk";
      case "Magadan": return "Asia/Magadan";
      case "Central Pacific": return "Pacific/Efate";
      case "Bougainville": return "Pacific/Bougainville";
      case "Lord Howe": return "Australia/Lord_Howe";
      case "West Pacific": return "Pacific/Guam";
      case "Vladivostok": return "Asia/Vladivostok";
      case "Tasmania": return "Australia/Tasmania";
      case "E. Australia": return "Australia/Brisbane";
      case "AUS Eastern": return "Australia/Melbourne";
      case "Cen. Australia": return "Australia/Adelaide";
      case "AUS Central": return "Australia/Darwin";
      case "Yakutsk": return "Asia/Yakutsk";
      case "Transbaikal": return "Asia/Chita";
      case "Tokyo": return "Asia/Tokyo";
      case "Korea": return "Asia/Seoul";
      case "Aus Central W.": return "Australia/Eucla";
      case "North Korea": return "Asia/Pyongyang";
      case "W. Australia": return "Australia/Perth";
      case "Ulaanbaatar": return "Asia/Ulaanbaatar";
      case "Taipei": return "Asia/Taipei";
      case "Singapore": return "Asia/Singapore";
      case "North Asia East": return "Asia/Irkutsk";
      case "China": return "Asia/Chongqing";
      case "W. Mongolia": return "Asia/Hovd";
      case "Tomsk": return "Asia/Novosibirsk";
      case "SE Asia": return "Asia/Jakarta";
      case "North Asia": return "Asia/Krasnoyarsk";
      case "N. Central Asia": return "Asia/Novosibirsk";
      case "Altai": return "Asia/Hovd";
      case "Myanmar": return "Asia/Rangoon";
      case "Omsk": return "Asia/Omsk";
      case "Central Asia": return "Asia/Dacca";
      case "Bangladesh": return "Asia/Dacca";
      case "Nepal": return "Asia/Kathmandu";
      case "Sri Lanka": return "Asia/Colombo";
      case "India": return "Asia/Kolkata";
      case "West Asia": return "Asia/Tashkent";
      case "Pakistan": return "Asia/Karachi";
      case "Ekaterinburg": return "Asia/Yekaterinburg";
      case "Afghanistan": return "Asia/Kabul";
      case "Saratov": return "Europe/Volgograd";
      case "Russia Time Zone 3": return "Europe/Samara";
      case "Mauritius": return "Indian/Mauritius";
      case "Georgian": return "Asia/Tbilisi";
      case "Caucasus": return "Asia/Yerevan";
      case "Azerbaijan": return "Asia/Baku";
      case "Astrakhan": return "Europe/Samara";
      case "Arabian": return "Asia/Dubai";
      case "Iran": return "Asia/Tehran";
      case "Turkey": return "Europe/Istanbul";
      case "Russian": return "Europe/Moscow";
      case "E. Africa": return "Africa/Nairobi";
      case "Belarus": return "Europe/Minsk";
      case "Arabic": return "Asia/Baghdad";
      case "Arab": return "Asia/Kuwait";
      case "West Bank": return "Asia/Gaza";
      case "Syria": return "Asia/Damascus";
      case "Sudan": return "Africa/Khartoum";
      case "South Africa": return "Africa/Harare";
      case "Namibia": return "Africa/Windhoek";
      case "Middle East": return "Asia/Beirut";
      case "Libya": return "Africa/Tripoli";
      case "Kaliningrad": return "Europe/Kaliningrad";
      case "Jordan": return "Asia/Amman";
      case "Israel": return "Asia/Tel_Aviv";
      case "GTB": return "Europe/Istanbul";
      case "FLE": return "Europe/Riga";
      case "Egypt": return "Africa/Cairo";
      case "E. Europe": return "Europe/Minsk";
      case "W. Europe": return "Europe/Amsterdam";
      case "W. Central Africa": return "Africa/Luanda";
      case "Central European": return "Europe/Sarajevo";
      case "Romance": return "Europe/Brussels";
      case "Central Europe": return "Europe/Prague";
      case "UTC": return "UTC";
      case "Morocco": return "Africa/Casablanca";
      case "Greenwich": return "Africa/Monrovia";
      case "GMT": return "Europe/London";
      case "Cape Verde": return "Atlantic/Cape_Verde";
      case "Azores": return "Atlantic/Azores";
      case "UTC-02": return "Etc/GMT+2";
      case "Mid-Atlantic": return "Etc/GMT+2";
      case "Tocantins": return "America/Araguaina";
      case "SA Eastern": return "America/Cayenne";
      case "Saint Pierre": return "America/Miquelon";
      case "Montevideo": return "America/Montevideo";
      case "Magallanes": return "America/Santiago";
      case "Greenland": return "America/Danmarkshavn";
      case "E. South America": return "America/Sao_Paulo";
      case "Bahia": return "America/Bahia";
      case "Argentina": return "America/Buenos_Aires";
      case "Newfoundland": return "Canada/Newfoundland";
      case "Venezuela": return "America/Caracas";
      case "SA Western": return "America/La_Paz";
      case "Paraguay": return "America/Asuncion";
      case "Pacific SA": return "America/Santiago";
      case "Central Brazilian": return "America/Cuiaba";
      case "Atlantic": return "Canada/Atlantic";
      case "US Eastern": return "America/Indiana/Indianapolis";
      case "Turks And Caicos": return "America/Grand_Turk";
      case "SA Pacific": return "America/Lima";
      case "Haiti": return "America/Port-au-Prince";
      case "Eastern Standard Time (Mexico)": return "America/Cancun";
      case "Eastern": return "America/New_York";
      case "Cuba": return "America/Havana";
      case "Easter Island": return "Pacific/Easter";
      case "Central Standard Time (Mexico)": return "America/Mexico_City";
      case "Central": return "America/Chicago";
      case "Central America": return "America/Costa_Rica";
      case "Canada Central": return "America/Regina";
      case "US Mountain": return "US/Arizona";
      case "Mountain Standard Time (Mexico)": return "America/Chihuahua";
      case "Mountain": return "America/Denver";
      case "UTC-08": return "Etc/GMT+8";
      case "Pacific Standard Time (Mexico)": return "America/Tijuana";
      case "Pacific": return "America/Los_Angeles";
      case "UTC-09": return "Etc/GMT+9";
      case "Alaskan": return "US/Alaska";
      case "Marquesas": return "Pacific/Marquesas";
      case "Hawaiian": return "Pacific/Honolulu";
      case "Aleutian": return "US/Aleutian";
      case "UTC-11": return "Etc/GMT+11";
      case "Dateline": return "Etc/GMT+12";
    }
    return null;
  }

}
