package com.ibm.hodgepodge;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import lotus.domino.DateTime;
import lotus.domino.cso.Session;

/**
 * The Hodge/Podge date/time converter for Java provides convenience functions
 * for converting pre-Java 1.8 classes (Date and Calendar) to and from post-Java
 * 1.8 classes (java.time.*).
 * 
 * Regarding conversions to and from Date objects, it's important to note the 
 * following from the Java SE documentation: "the Date class is intended to
 * reflect coordinated universal time (UTC)".  Often Java makes it look
 * otherwise, because SimpleDateFormatter objects perform time zone conversion
 * for you by default. In this code, all Date values passed in are assumed to
 * be UTC, and all Date values returned are UTC.
 * 
 * Regarding Calendar objects, these routines will happily pass you incomplete
 * Calendars -- for example, with just a date or just a time. Other code may
 * assume that Calendar objects always have all fields, so be careful.
 */
public class HodgePodge {

  private static TimeZone UTC = TimeZone.getTimeZone("UTC");

  /**
   * Converts a Calendar to a Date.
   * @param cal the Calendar to convert
   * @return
   */
  public final static Date toDate (final Calendar cal) {
    return cal.getTime();
  }

  /**
   * Converts an Instant to a Date.
   * @param ins the Instant to convert
   * @return
   */
  public final static Date toDate (final Instant ins) {
    return Date.from(ins);
  }

  /**
   * Converts a ZonedDateTime to a Date.
   * @param zdt the ZonedDateTime to convert
   * @return
   */
  public final static Date toDate (final ZonedDateTime zdt) {
    ZonedDateTime zdtutc = zdt.withZoneSameInstant(ZoneOffset.UTC);
    return Date.from(zdtutc.toInstant());
  }

  /**
   * Converts a LocalDate to a Date with the same calendar date,
   * and the time set to the start of that day (in UTC).
   * @param ld the LocalDate to convert
   * @return
   */
  public final static Date toDate (final LocalDate ld) {
    return Date.from(ld.atStartOfDay(ZoneOffset.UTC).toInstant());
  }

  /**
   * Converts a LocalTime to a Date with the date portion set to the specified day.
   * Or to put it another way, merges a LocalTime and LocalDate into a Date.
   * @param lt the LocalTime to convert
   * @param ondate the LocalDate to merge it with
   * @return
   */
  public final static Date toDate (final LocalTime lt, final LocalDate ondate) {
    LocalDateTime ldt = lt.atDate(ondate);
    return Date.from(ldt.atZone(ZoneOffset.UTC).toInstant());
  }

  /**
   * Converts a LocalTime to a Date, with the time part of the Date being set to the current time.
   * @param lt the LocalTime to convert
   * @return
   */
  public final static Date toDate (final LocalTime lt) {
    return HodgePodge.toDate(lt, LocalDate.now());
  }

  /**
   * Converts a LocalDateTime to a Date.
   * @param ldt the LocalDateTime to convert, which is assumed to be UTC
   * @return
   */
  public final static Date toDate (final LocalDateTime ldt) {
    return Date.from(ldt.atZone(ZoneOffset.UTC).toInstant());
  }

  /**
   * Converts a Date to a Calendar.
   * @param d
   * @return
   */
  public final static Calendar toCalendar (final Date d) {
    return HodgePodge.toCalendar(d, UTC);
  }

  /**
   * Converts a Date to a Calendar, with a specified TimeZone applied to the Date.
   * @param d the Date to convert
   * @param z the TimeZone to assume the Date is in
   * @return
   */
  public final static Calendar toCalendar (final Date d, final TimeZone z) {
    GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(z);
    cal.setTime(d);
    return cal;
  }

  /**
   * Converts an Instant to a Calendar.
   * @param ins the Instant to convert
   * @return
   */
  public final static Calendar toCalendar (final Instant ins) {
    GregorianCalendar cal = new GregorianCalendar(UTC);
    cal.setTimeInMillis(ins.toEpochMilli());
    return cal;
  }

  /**
   * Converts a ZonedDateTime to a Calendar.
   * @param zdt the ZonedDateTime to convert
   * @return
   */
  public final static Calendar toCalendar (final ZonedDateTime zdt) {
    return GregorianCalendar.from(zdt);
  }

  /**
   * Converts a LocalDate to a Calendar (which will be incomplete until you
   * add the missing fields).
   * @param ld the LocalDate to convert
   * @return
   */
  public final static Calendar toCalendar (final LocalDate ld) {
    Calendar cal = new GregorianCalendar();
    cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
    return cal;
  }

  /**
   * Converts a LocalTime to a Calendar (which will be incomplete until you
   * add the missing fields.)
   * @param lt the LocalTime to convert
   * @return
   */
  public final static Calendar toCalendar (final LocalTime lt) {
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.HOUR_OF_DAY, lt.getHour());
    cal.set(Calendar.MINUTE, lt.getMinute());
    cal.set(Calendar.SECOND, lt.getSecond());
    return cal;
  }

  /**
   * Converts a LocalDateTime to a Calendar. The Calendar ends up with the
   * system default time zone.
   * @param ldt the LocalDateTime to convert
   * @return
   */
  public final static Calendar toCalendar (final LocalDateTime ldt) {
    return GregorianCalendar.from(ZonedDateTime.of(ldt, ZoneId.systemDefault()));
  }

  /**
   * Converts a Date to an Instant
   * @param d the Date to convert
   * @return
   */
  public final static Instant toInstant (final Date d) {
    return d.toInstant();
  }

  /**
   * Converts a Calendar to an Instant.
   * @param cal the Calendar to convert
   * @return
   */
  public final static Instant toInstant (final Calendar cal) {
    return cal.toInstant();
  }

  /**
   * Converts a ZonedDateTime to an Instant
   * @param zdt the ZonedDateTime to convert
   * @return
   */
  public final static Instant toInstant (final ZonedDateTime zdt) {
    return zdt.toInstant();
  }

  /**
   * Converts a LocalDateTime in the specified time zone to an Instant.
   * @param ldt the LocalDateTime to convert
   * @param z the TimeZone it is in
   * @return
   */
  public final static Instant toInstant (final LocalDateTime ldt, final ZoneOffset z) {
    return ldt.toInstant(z);
  }

  /**
   * Converts a Date to a ZonedDateTime.
   * @param d the Date to convert
   * @return
   */
  public final static ZonedDateTime toZonedDateTime (final Date d) {
    return ZonedDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
  }

  /**
   * Converts the specified Date to a ZonedDateTime, in whatever
   * the local system default time zone is at the appropriate Instant.
   *
   * @param d the Date to convert
   * @return
   */
  public final static ZonedDateTime toZonedDateTimeLocal (final Date d) {
    Instant ins = d.toInstant();
    ZoneId systemZone = ZoneId.systemDefault();
    ZoneOffset offset = systemZone.getRules().getOffset(ins);
    return ZonedDateTime.ofInstant(ins, offset);
  }

  /**
   * Converts a Calander to a ZonedDateTime
   * @param cal the Calendar to convert
   * @return
   */
  public final static ZonedDateTime toZonedDateTime (final Calendar cal) {
    if (!(cal instanceof GregorianCalendar)) {
      throw new DateTimeException("Cannot convert non-Gregorian Calendars");
    }
    return ((GregorianCalendar) cal).toZonedDateTime();
  }

  // Convert Instant to ZonedDateTime
  /**
   * Converts an Instant to a ZonedDateTime with the appropriate TimeZone.
   * @param ins the Instant to convert
   * @param zone the time zone you want the answer in
   * @return
   */
  public final static ZonedDateTime toZonedDateTime (final Instant ins, final ZoneId zone) {
    return ins.atZone(zone);
  }

  /**
   * Converts an Instant to a ZonedDateTime in UTC.
   * @param ins the Instant to convert
   * @return
   */
  public final static ZonedDateTime toZonedDateTime (final Instant ins) {
    return ins.atZone(ZoneOffset.UTC);
  }

  /**
   * Converts a Date to a LocalDate.
   * @param d the Date to convert
   * @return
   */
  public final static LocalDate toLocalDate (final Date d) {
    return d.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
  }

  /**
   * Converts a Calendar to a LocalDate. Only the year, month and day of the calendar are examined.
   * @param cal the Calendar to convert
   * @return
   */
  public final static LocalDate toLocalDate (final Calendar cal) {
    return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Converts a Date to a LocalTime by converting it to a LocalDateTime and then discarding the date.
   * @param d the Date to convert
   * @return
   */
  public final static LocalTime toLocalTime (final Date d) {
    Instant ins = Instant.ofEpochMilli(d.getTime());
    return LocalDateTime.ofInstant(ins, ZoneOffset.UTC).toLocalTime();
  }

  /**
   * Converts a Calendar to a LocalTime. Only the hour, minute and second fields are examined.
   * Any time zone in the Calendar is ignored.
   * @param cal the Calendar to convert
   * @return
   */
  public final static LocalTime toLocalTime (final Calendar cal) {
    return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
  }

  /**
   * Converts a Date to a LocalDateTime.
   * @param d the Date to convert
   * @return
   */
  public final static LocalDateTime toLocalDateTime (final Date d) {
    return LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
  }

  /**
   * converts a Calendar to a LocalDateTime, adjusting it to the system default time zone
   * if necessary.
   * @param cal
   * @return
   */
  public final static LocalDateTime toLocalDateTime (final Calendar cal) {
    TimeZone tz = cal.getTimeZone();
    ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
    return LocalDateTime.ofInstant(cal.toInstant(), zid);
  }

}
