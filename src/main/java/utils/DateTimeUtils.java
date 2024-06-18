package utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import manager.AdminUserManager.DashboardFilter;

public class DateTimeUtils {

	public enum DateTimeZone {
		IST("IST"), GMT("GMT");

		public final String value;

		DateTimeZone(String value) {
			this.value = value;
		}

	}

	public enum DateTimeFormat {

		yyyy_MM_dd_HH_mm_ss_SSS_GMT_z("yyyy-MM-dd HH:mm:ss.SSS z"),
		yyyy_MM_dd_T_HH_mm_ss_SSSZ("yyyy-MM-dd'T'HH:mm:ss.SSSZ"), yyyy_MM_dd("yyyy-MM-dd"), yyyy_MM("yyyy-MM"),
		MMM_dd_yyyy("MMM dd, yyyy"), E("E"), hh_mm("hh:mm"), HH_mm("HH:mm"), MMM("MMM"), MM("MM"), dd("dd"),
		yyyy("yyyy"), EEE_MMM_d("EEE, MMM d"), d_MMM("d MMM"), MMM_d("MMM d"), MMM_d_hh_mm_a("MMM d, hh:mm a"),
		MMM_d_yyyy("MMM d, yyyy"), MMM_yyyy("MMM-yyyy"), hh_mm_a("hh:mm a"), MMM_dd_yyyy_h_mm_a("MMM dd, yyyy h:mm a"),
		yyyy_MM_dd_HH_mm("yyyy-MM-dd HH:mm"), h_mm_a("h:mm a"), dd_MM_yyyy("dd-MM-yyyy"),
		dd_MM_yyyy_slash("dd/MM/yyyy"), dd_M_yyyy_slash("dd/M/yyyy"), yyyy_MM_dd_HH_mm_ss("yyyy-MM-dd HH:mm:ss"),
		dd_MM_yyyy_HH_mm_ss("dd-MM-yyyy HH:mm:ss"), dd_MM_yyyy_hh_mm_a("dd-MM-yyyy hh:mm a"), d_EEE_yyyy("d MMM, yyyy");

		public final String value;

		DateTimeFormat(String value) {
			this.value = value;
		}
	}

	public enum Month {
		JANUARY("january", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-01"),
		FEBRUARY("february", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-02"),
		MARCH("march", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-03"),
		APRIL("april", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-04"),
		MAY("may", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-05"),
		JUNE("june", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-06"),
		JULY("july", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-07"),
		AUGUST("august", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-08"),
		SEPTEMBER("september", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-09"),
		OCTUBER("octuber", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-10"),
		NOVEMBER("november", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-11"),
		DECEMBER("december", getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST) + "-12");

		public final String value;
		public final String dateFormat;

		Month(String value, String format) {
			this.value = value;
			this.dateFormat = format;
		}

		public static Month get(String value) {
			for (Month item : Month.values()) {
				if (item.value.equalsIgnoreCase(value))
					return item;
			}
			return null;
		}

	}

	public enum Time {
		JANUARY("january", 1, "01"), FEBRUARY("february", 2, "02"), MARCH("march", 3, "03"), APRIL("april", 4, "04"),
		MAY("may", 5, "05"), JUNE("june", 6, "06"), JULY("july", 7, "07"), AUGUST("august", 8, "08"),
		SEPTEMBER("september", 9, "09"), OCTUBER("octuber", 10, "10"), NOVEMBER("november", 11, "11"),
		DECEMBER("december", 12, "12");

		public final String value;
		public final int code;
		public final String codeString;

		Time(String value, int code, String format) {
			this.value = value;
			this.code = code;
			this.codeString = format;
		}

		public static Time getTimeByName(String value) {
			for (Time item : Time.values()) {
				if (item.value.equalsIgnoreCase(value))
					return item;
			}
			return null;
		}

		public static Time getTimeByCode(int code) {
			for (Time item : Time.values()) {
				if (item.code == code)
					return item;
			}
			return null;
		}

		public static Time getTimeByCodeString(String codeString) {
			for (Time item : Time.values()) {
				if (item.codeString.equalsIgnoreCase(codeString))
					return item;
			}
			return null;
		}

		public String getYearMonthFormat(String year) {
			return year + "-" + this.codeString;
		}

		public static ArrayList<Time> getYearMonthList(String month) {

			ArrayList<Time> timeList = new ArrayList<>();
			Time currentMonth = getTimeByCodeString(month);

			for (int i = 0; i < currentMonth.code; i++) {
				timeList.add(Time.values()[i]);
			}

			return timeList;

		}

	}

	public static String getTimeStamp() {
		return (new Timestamp(System.currentTimeMillis())).toString();
	}

	public static String getCurrentDateTimeInGMT() {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.GMT.value));
		return sdf.format(dt);
	}

	public static String getCurrentDateTimeInIST() {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
		return sdf.format(dt);
	}

	public static String getDateForSalesforce(String datetime) throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
		return sdf.format(date);
	}

	public static String getDateForMonthlyPaymentsDue(String datetime) throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(datetime);
		SimpleDateFormat sdf = new SimpleDateFormat("MMM-yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
		return sdf.format(date);
	}

	public static String getDateForDB(String datetime, String inputFormat) throws ParseException {
		try {
			if (datetime.equals(Constants.NA))
				return datetime;
			Date date = new SimpleDateFormat(inputFormat).parse(datetime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
			return sdf.format(date);
		} catch (ParseException e) {
			return datetime;
		}
	}

	public static String getDate(String datetime, String inputFormat, String outputFormat) throws ParseException {
		if (datetime.equals(Constants.NA))
			return datetime;
		Date date = new SimpleDateFormat(inputFormat).parse(datetime);
		SimpleDateFormat sdf = new SimpleDateFormat(outputFormat);
		//sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
		return sdf.format(date);
	}

	public static Date getDateFromString(String datetime, DateTimeFormat dFormat) throws Exception {
		if (datetime.equals(Constants.NA))
			throw new Exception("Invalid datetime string.");
		SimpleDateFormat sdf = new SimpleDateFormat(dFormat.value);
		//sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
		return sdf.parse(datetime);
	}

	public static String getDateTime(DateTimeFormat dFormat, DateTimeZone zone) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(dFormat.value);
		sdf.setTimeZone(TimeZone.getTimeZone(zone.value));
		return sdf.format(dt);
	}

	public static String getDateTime(int day, DateTimeFormat dFormat, DateTimeZone zone) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, day);
		Date dt = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(dFormat.value);
		sdf.setTimeZone(TimeZone.getTimeZone(zone.value));
		return sdf.format(dt);
	}

	public static String getDateTimeFromString(String datetime, DateTimeFormat inputFormat, DateTimeFormat outputFormat,
			DateTimeZone zone) throws ParseException {
		if (datetime.equals(Constants.NA))
			return datetime;
		Date date = new SimpleDateFormat(inputFormat.value).parse(datetime);
		SimpleDateFormat sdf = new SimpleDateFormat(outputFormat.value);
		//sdf.setTimeZone(TimeZone.getTimeZone(zone.value)); check this out later
		return sdf.format(date);
	}

	public static String getStartDate() {
		// return "2019-12-01 00:00:01";
		return getStartDateTime(DashboardFilter.LAST_28_DAYS);
	}

	public static String getEndDate() {
		// return "2019-12-15 23:59:59";
		return getEndDateTime();
	}

	private static String getEndDateTime() {
		return DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM_dd, DateTimeZone.IST) + " 23:59:59";
	}

	private static String getStartDateTime(DashboardFilter filter) {
		return DateTimeUtils.getDateTime(filter.dateCode, DateTimeFormat.yyyy_MM_dd, DateTimeZone.IST) + " 00:00:01";
	}

	public static String alterMinutes(int min, String datetime, DateTimeFormat dFormat)
			throws ParseException {

		if (datetime.equals(Constants.NA))
			return datetime;
		
		SimpleDateFormat sdf = new SimpleDateFormat(dFormat.value);
		
		Date date = sdf.parse(datetime);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, min);
	
		return sdf.format(calendar.getTime());
	}
	
	public static String getSFtoDBDateTime(String datetime) throws ParseException {
		
		if (datetime.contains("T") && datetime.contains(".") && datetime.contains("+")) {
			
			return DateTimeUtils.getDateTimeFromString(
					datetime, 
					DateTimeFormat.yyyy_MM_dd_T_HH_mm_ss_SSSZ,
					DateTimeFormat.yyyy_MM_dd_HH_mm,
					DateTimeZone.IST
			);
			
		} else return datetime;
		
	}
	
	public static String convertISTtoGMT(String dateTimeIST, DateTimeFormat format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format.value);
		sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.IST.value));
		Date dateIST = sdf.parse(dateTimeIST);
		sdf.setTimeZone(TimeZone.getTimeZone(DateTimeZone.GMT.value));
		return sdf.format(dateIST);		
	}
	
	public static Date getDateFromDateTimeString(
			String dateString, 
			DateTimeFormat format
	) throws ParseException {
		return new SimpleDateFormat(format.value).parse(dateString);
	}
	
	public static String getStringFromDateTimeString(
			String datetime, 
			DateTimeFormat inputFormat, 
			DateTimeFormat outputFormat
	) throws ParseException {
		if (datetime.equals(Constants.NA)) return datetime;
		Date date = new SimpleDateFormat(inputFormat.value).parse(datetime);  
		SimpleDateFormat sdf = new SimpleDateFormat(outputFormat.value);	
		return sdf.format(date);
	}
	
	public static String getDateTimeAddingHours(int hour, DateTimeFormat dFormat, DateTimeZone zone) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, hour);
		Date dt = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(dFormat.value);
		sdf.setTimeZone(TimeZone.getTimeZone(zone.value));
		return sdf.format(dt);
	}
	
	public static int getDateDifferenceInMinutes(String date) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat(DateTimeFormat.yyyy_MM_dd_HH_mm_ss.value);

		Date datetimeInQuestion = sdf.parse(date);
		Date currentDatetime = sdf.parse(getCurrentDateTimeInIST());
		long difference_In_Time = datetimeInQuestion.getTime() - currentDatetime.getTime();
		long diffInMinutes = difference_In_Time / 60000;
		return (int) diffInMinutes;

	}

}
