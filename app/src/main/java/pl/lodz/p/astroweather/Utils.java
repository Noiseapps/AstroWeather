package pl.lodz.p.astroweather;

import com.astrocalculator.AstroDateTime;

import java.util.Locale;

public class Utils {

    public static String formatAstroDateToString(AstroDateTime dateTime) {
        return String.format(Locale.getDefault(), "%4d-%2d-%2d %2d:%2d",
                dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDay(),
                dateTime.getHour(),
                dateTime.getMinute());
    }
}
