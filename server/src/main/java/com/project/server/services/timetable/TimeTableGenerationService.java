package com.project.server.services.timetable;

import com.project.server.db.models.*;
import com.project.server.db.services.courseCode.CourseCodeService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.enums.Role;
import com.project.server.exceptions.TimeTableGenerationException;
import com.project.server.util.VariableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.*;

import static com.project.server.util.HelperUtil.createTextRun;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableGenerationService {
    private final ScheduleService scheduleService;
    private final CourseCodeService courseCodeService;

    public DocumentResponse generate(
            TimeTable timeTable,
            AppUser user
    ) throws TimeTableGenerationException{
        boolean isStudent = user.getRole() == Role.STUDENT;
        List<Schedule> schedules = isStudent ? scheduleService.getByCourseRepresentative(user): scheduleService.getByLecturer(user);

        if(schedules.isEmpty()) throw new TimeTableGenerationException("You have no schedules in the current timetable.");

        byte[] bytesArr;

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream())
        {

            XWPFRun titleRun = createTextRun(document);
            titleRun.setText(String.format("BOWEN UNIVERSITY %s session %s SEMESTER TIMETABLE", timeTable.getSession(), timeTable.getSemester()));
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily(String.valueOf(FontFamily.ROMAN));

            if (isStudent) addStudentHeadings(document, user);
            else addLecturerHeadings(document, user);

            Map<String, List<Schedule>> groupedAndSortedSchedules = groupAndSortSchedules(schedules);
            for(String day : groupedAndSortedSchedules.keySet()){
                insertScheduleInTable(document, day, groupedAndSortedSchedules.get(day), isStudent);
            }

            document.write(out);
            bytesArr = out.toByteArray();

        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new TimeTableGenerationException("Failed to generate timetable");
        }

        return new DocumentResponse(bytesArr, user.getTimeTableFileName());
    }

    private Map<String, List<Schedule>> groupAndSortSchedules(List<Schedule> schedules){
        Map<String, List<Schedule>> groupedSchedules = new HashMap<>();

        for(Schedule schedule : schedules) {
            groupedSchedules.computeIfAbsent(schedule.getDay(), k -> new ArrayList<>())
                    .add(schedule);
        }

        for(List<Schedule> dailySchedules : groupedSchedules.values()){
            dailySchedules.sort(Comparator.comparing(Schedule::getStartTime));
        }

        Map<String, List<Schedule>> groupedAndSortedSchedules = new LinkedHashMap<>();
        for(String day : VariableUtil.DAYS){
            List<Schedule> schedulesForDay = groupedSchedules.get(day);
            if(schedulesForDay != null && !schedulesForDay.isEmpty()){
                groupedAndSortedSchedules.put(day, groupedSchedules.get(day));
            }
        }

        return groupedAndSortedSchedules;
    }

    private void insertScheduleInTable(XWPFDocument document,String day, List<Schedule> schedules, boolean isStudent){
        XWPFRun dayRun = createTextRun(document);
        dayRun.setCapitalized(true);
        dayRun.setBold(true);
        dayRun.setText(day);

        int tableRows = schedules.size() + 1;
        int tableCols = isStudent ? 7 : 5;
        XWPFTable table = document.createTable(tableRows, tableCols);

        CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
        width.setType(STTblWidth.PCT);
        width.setW(BigInteger.valueOf(5000));

        table.setTableAlignment(TableRowAlign.CENTER);

        table.getRow(0).getCell(0).setText("S/N");
        table.getRow(0).getCell(1).setText("COURSE TITLE");
        table.getRow(0).getCell(2).setText("COURSE CODE(S)");
        table.getRow(0).getCell(3).setText("TIME");
        table.getRow(0).getCell(4).setText("VENUE(S)");

        if(isStudent){
            table.getRow(0).getCell(5).setText("LECTURER NAME");
            table.getRow(0).getCell(6).setText("LECTURER PHONE");
        }

        for(int i = 1; i < tableRows; i++){
            Schedule currentSchedule = schedules.get(i - 1);

            String courseCodesString = String.join(", ", courseCodeService.getCourseCodesByCourse(currentSchedule.getCourse()).stream().map(CourseCode::getCourseCode).toList());
            String classRoomsString = String.join(", ", currentSchedule.getClassRooms().stream().map(ClassRoom::getName).toList());

            table.getRow(i).getCell(0).setText(String.valueOf(i));
            table.getRow(i).getCell(1).setText(currentSchedule.getCourse().getCourseTitle());
            table.getRow(i).getCell(2).setText(courseCodesString);
            table.getRow(i).getCell(3).setText(currentSchedule.getTimePeriod());
            table.getRow(i).getCell(4).setText(classRoomsString);

            if(isStudent){
                table.getRow(i).getCell(5).setText(currentSchedule.getAppUser().getName());
                table.getRow(i).getCell(6).setText(currentSchedule.getAppUser().getPhone());
            }
        }
    }

    private void addLecturerHeadings(XWPFDocument document, AppUser lecturer){
        XWPFRun lecturerInfoRun = createTextRun(document);
        lecturerInfoRun.setText(String.format("LECTURER ID: %s.", lecturer.getUserId()));
        lecturerInfoRun.setItalic(true);
        lecturerInfoRun.setFontFamily(String.valueOf(FontFamily.ROMAN));

    }

    private void addStudentHeadings(XWPFDocument document, AppUser student){
        XWPFRun collegeRun = createTextRun(document);
        collegeRun.setText(String.format("%s", student.getDepartment().getCollege().getName()));
        collegeRun.setItalic(true);
        collegeRun.setFontFamily(String.valueOf(FontFamily.ROMAN));

        XWPFRun deptRun = createTextRun(document);
        deptRun.setText(String.format("Department Of %s", student.getDepartment().getName()));
        deptRun.setItalic(true);
        deptRun.setFontFamily(String.valueOf(FontFamily.ROMAN));

        XWPFRun levelRun = createTextRun(document);
        levelRun.setText(String.format("%s level", student.getLevel().getLevel()));
        levelRun.setItalic(true);
        levelRun.setFontFamily(String.valueOf(FontFamily.ROMAN));
    }
}