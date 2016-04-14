package com.inbravo.cad.rest.service.writer.utils;

import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ConvertTimeZone {
  public static void main(String[] args) throws ParseException {

    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();

    DateTime dateTimeHere = parser.parseDateTime("2012-10-31T17:25:54.478-07:00");

    DateTime secondDateTime = dateTimeHere.withZone(DateTimeZone.forID("PST8PDT"));

    System.out.println("second: " + secondDateTime);
  }
}
