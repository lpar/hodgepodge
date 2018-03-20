package com.ibm.hodgepodge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HodgePodgeTest {

  private static final TimeZone UTC = TimeZone.getTimeZone("Z");
  static ArrayList<ZoneId> zoneids = new ArrayList<>();
  
  static final Logger LOG = Logger.getLogger(HodgePodgeTest.class.getName());

  @BeforeAll
  static void init () {
    Set<String> zoneset = ZoneId.getAvailableZoneIds();
    for (String zone : zoneset) {
      ZoneId zid = ZoneId.of(zone);
      zoneids.add(zid);
    }
  }

  Random rand = new Random();

  private Calendar randomCalendar () {
    int y = this.rand.nextInt(80) + 1980;
    int m = this.rand.nextInt(12);
    int d = this.rand.nextInt(27) + 1;
    Calendar cal = Calendar.getInstance();
    cal.set(y, m, d);
    return cal;
  }

  private LocalDate randomLocalDate () {
    int y = this.rand.nextInt(80) + 1980;
    int m = this.rand.nextInt(12) + 1;
    int d = this.rand.nextInt(27) + 1;
    return LocalDate.of(y, m, d);
  }

  private LocalTime randomLocalTime () {
    int hh = this.rand.nextInt(24);
    int mm = this.rand.nextInt(60);
    int ss = this.rand.nextInt(60);
    return LocalTime.of(hh, mm, ss);
  }

  private Date randomDate () {
    Date result = new Date();
    result.setTime(this.rand.nextLong() % 10000000L);
    return result;
  }

  private Instant randomInstant () {
    return Instant.ofEpochSecond(this.rand.nextLong() % 10000000L);
  }

  private ZoneId randomZoneId () {
    int n = this.rand.nextInt(zoneids.size());
    return zoneids.get(n);
  }

  private ZonedDateTime randomZonedDateTime () {
    int y = this.rand.nextInt(80) + 1980;
    int m = this.rand.nextInt(12) + 1;
    int d = this.rand.nextInt(27) + 1;
    int hh = this.rand.nextInt(24);
    int mm = this.rand.nextInt(60);
    int ss = this.rand.nextInt(60);
    ZoneId z = this.randomZoneId();
    return ZonedDateTime.of(y, m, d, hh, mm, ss, 0, z);
  }

  private boolean checkDateTime (final Object x, final Object y) {
    String sx = TimeFormats.toDateTime(x);
    String sy = TimeFormats.toDateTime(y);
    boolean same = false;
    // We allow no timezone as the same as Z
    if (sx.equals(sy) || sx.equals(sy + "Z") || sy.equals(sx + "Z")) {
      LOG.fine(String.format("%s %s == %s %s\n", x.getClass().getName(), sx, y.getClass().getName(), sy));
      same = true;
    } else {
      LOG.severe(String.format("%s %s != %s %s\n", x.getClass().getName(), sx, y.getClass().getName(), sy));
    }
    return same;
  }

  private boolean checkDate (final Object x, final Object y) {
    String sx = TimeFormats.toDate(x);
    String sy = TimeFormats.toDate(y);
    boolean same = false;
    if (sx.equals(sy)) {
      LOG.fine(String.format("%s %s == %s %s\n", x.getClass().getName(), sx, y.getClass().getName(), sy));
      same = true;
    } else {
      LOG.severe(String.format("%s %s != %s %s\n", x.getClass().getName(), sx, y.getClass().getName(), sy));
    }
    return same;
  }

  private boolean checkTime (final Object x, final Object y) {
    String sx = TimeFormats.toTime(x);
    String sy = TimeFormats.toTime(y);
    boolean same = false;
    if (sx.equals(sy)) {
      LOG.fine(String.format("%s %s == %s %s\n", x.getClass().getName(), sx, y.getClass().getName(), sy));
      same = true;
    } else {
      LOG.severe(String.format("%s %s != %s %s\n", x.getClass().getName(), sx, y.getClass().getName(), sy));
    }
    return same;
  }

  @Test
  void testDateAndCalendar () {
    for (int i = 0; i < 10; i++) {
      Calendar x = this.randomCalendar();
      x.setTimeZone(UTC); // since Dates are UTC
      Date y = HodgePodge.toDate(x);
      Assertions.assertTrue(this.checkDateTime(x, y));
      Calendar x2 = HodgePodge.toCalendar(y);
      Assertions.assertTrue(this.checkDateTime(x2, y));
    }
  }
  
  @Test
  void testDateAndInstant () {
    for (int i = 0; i < 10; i++) {
      Instant x = this.randomInstant();
      Date y = HodgePodge.toDate(x);
      Assertions.assertTrue(this.checkDateTime(x, y));
      Instant x2 = HodgePodge.toInstant(y);
      Assertions.assertTrue(this.checkDateTime(x2, y));
    }
  }
  
  @Test
  void testDateAndZonedDateTime () {
    for (int i = 0; i < 10; i++) {
      ZonedDateTime x = this.randomZonedDateTime();
      // This happens during conversion anyway so do it here so the string match works
      x = x.withZoneSameInstant(ZoneOffset.UTC);
      Date y = HodgePodge.toDate(x);
      Assertions.assertTrue(this.checkDateTime(x, y));
      ZonedDateTime x2 = HodgePodge.toZonedDateTime(y);
      Assertions.assertTrue(this.checkDateTime(x2, y));
    }
    // Test date toZonedDateTimeLocal
    for (int i = 0; i < 10; i++) {
     Date x = this.randomDate();
     ZonedDateTime y = HodgePodge.toZonedDateTimeLocal(x);
      Date x2 = HodgePodge.toDate(y);
      Assertions.assertEquals(x, x2);
    }
  }

  @Test
  void testDateAndLocalDate () {
    for (int i = 0; i < 10; i++) {
      LocalDate x = this.randomZonedDateTime().toLocalDate();
      Date y = HodgePodge.toDate(x);
      Assertions.assertTrue(this.checkDate(x, y));
      LocalDate x2 = HodgePodge.toLocalDate(y);
      Assertions.assertTrue(this.checkDate(x2, y));
    }
  }

  @Test
  void testDateAndLocalTime () {
    for (int i = 0; i < 10; i++) {
      LocalTime x = this.randomZonedDateTime().toLocalTime();
      Date y = HodgePodge.toDate(x);
      Assertions.assertTrue(this.checkTime(x, y));
      LocalTime x2 = HodgePodge.toLocalTime(y);
      Assertions.assertTrue(this.checkTime(x2, y));
    }
  }

  @Test
  void testDateAndLocalDateTime () {
    for (int i = 0; i < 10; i++) {
      LocalDateTime x = this.randomZonedDateTime().toLocalDateTime();
      Date y = HodgePodge.toDate(x);
      Assertions.assertTrue(this.checkDateTime(x, y));
      LocalDateTime x2 = HodgePodge.toLocalDateTime(y);
      Assertions.assertTrue(this.checkDateTime(x2, y));
    }
  }

  @Test
  void testCalendarAndInstant () {
    for (int i = 0; i < 10; i++) {
      Instant x = this.randomInstant();
      Calendar y = HodgePodge.toCalendar(x);
      Assertions.assertTrue(this.checkDateTime(x, y));
      Instant x2 = HodgePodge.toInstant(y);
      Assertions.assertTrue(this.checkDateTime(x2, y));
    }
  }

  @Test
  void testCalendarAndZonedDateTime () {
    for (int i = 0; i < 10; i++) {
      ZonedDateTime x = this.randomZonedDateTime();
      Calendar y = HodgePodge.toCalendar(x);
      Assertions.assertTrue(this.checkDateTime(x, y));
      ZonedDateTime x2 = HodgePodge.toZonedDateTime(y);
      Assertions.assertTrue(this.checkDateTime(x2, y));
    }
  }

  @Test
  void testCalendarAndLocalDate () {
    for (int i = 0; i < 10; i++) {
      LocalDate x = this.randomLocalDate();
      Calendar y = HodgePodge.toCalendar(x);
      Assertions.assertTrue(this.checkDate(x, y));
      LocalDate x2 = HodgePodge.toLocalDate(y);
      Assertions.assertTrue(this.checkDate(x2, y));
    }
  }

  @Test
  void testCalendarAndLocalTime () {
    for (int i = 0; i < 10; i++) {
      LocalTime x = this.randomLocalTime();
      Calendar y = HodgePodge.toCalendar(x);
      Assertions.assertTrue(this.checkTime(x, y));
      LocalTime x2 = HodgePodge.toLocalTime(y);
      Assertions.assertTrue(this.checkTime(x2, y));
    }
  }

  @Test
  void testCalendarAndLocalDateTime () {
    for (int i = 0; i < 10; i++) {
      LocalDateTime x = this.randomZonedDateTime().toLocalDateTime();
      Calendar y = HodgePodge.toCalendar(x);
      Assertions.assertTrue(this.checkTime(x, y));
      LocalDateTime x2 = HodgePodge.toLocalDateTime(y);
      Assertions.assertTrue(this.checkTime(x2, y));
    }
  }

  @Test
  void testInstantAndZonedDateTime () {
    // Test date toZonedDateTimeLocal
    for (int i = 0; i < 10; i++) {
     Instant x = this.randomInstant();
     ZonedDateTime y = HodgePodge.toZonedDateTime(x);
      Instant x2 = HodgePodge.toInstant(y);
      Assertions.assertEquals(x, x2);
    } 
  }

}
