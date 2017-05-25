package pl.lodz.p.astroweather;

import com.astrocalculator.AstroDateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String formatAstroDateToString(AstroDateTime dateTime) {
//        %02d - liczba całkowita, 2 miejsca, dodajemy 0 na początku, jeśli mniej niż 10
        return String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d:%02d",
                dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDay(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond());
    }

    public static String formatAstroDateToStringTimeOnly(AstroDateTime dateTime) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                dateTime.getHour(),
                dateTime.getMinute());
    }

    public static String formatAstroDateToStringDateOnly(AstroDateTime dateTime) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDay());
    }

    public static AstroDateTime getCurrentAstroDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        return new AstroDateTime(calendar.get(Calendar.YEAR),
//                +1 bo miesiące Java liczy od 0
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                calendar.getTimeZone().getRawOffset(),
                calendar.getTimeZone().useDaylightTime());
    }
}
