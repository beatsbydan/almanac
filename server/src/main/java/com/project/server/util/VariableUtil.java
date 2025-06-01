package com.project.server.util;

import java.text.SimpleDateFormat;
import java.util.List;

public class VariableUtil {
    public static final String
            TIME_TABLE_DIRECTORY = System.getProperty("user.dir") + "/timetables",
            RESET_TOKEN = "resetToken",
            REFRESH_TOKEN = "refreshToken";

    public static final List<String> DAYS = List.of("Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays");
    public static final List<String> TIMES = List.of("8:00am", "9:00am", "10:00am", "11:00am", "12:00pm", "13:00pm" ,"14:00pm", "15:00pm", "16:00pm", "17:00pm");
    public static final List<String> DURATIONS = List.of("1 hour", "2 hours");
}
