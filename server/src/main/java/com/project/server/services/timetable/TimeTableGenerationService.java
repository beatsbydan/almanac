package com.project.server.services.timetable;

import com.project.server.db.models.*;
import com.project.server.db.services.courseCodes.CourseCodeService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.enums.Role;
import com.project.server.exceptions.TimeTableGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

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
        String fileName;

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            // FORMAT THE BORDERS.

            XWPFRun titleRun = createTextRun(document);
            titleRun.setText(String.format("BOWEN UNIVERSITY %s session %s SEMESTER TIMETABLE", timeTable.getSession(), timeTable.getSemester()));
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily(String.valueOf(FontFamily.ROMAN));

            fileName = generateFileName(isStudent, timeTable, user);

            if (isStudent) addStudentHeadings(document, user);
            else addLecturerHeadings(document, user);

            populateTable(document, schedules, isStudent, user);

            document.write(out);
            bytesArr = out.toByteArray();

        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new TimeTableGenerationException("Failed to generate timetable");
        }

        return new DocumentResponse(bytesArr, fileName);
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

    private String generateFileName(boolean isStudent, TimeTable timeTable, AppUser user){
        String fileNature = isStudent ? user.getDepartment().getName() + user.getLevel().getLevel() : user.getUserId();
        return String.format("BUI_%s_%s_semester_%s_timetable", timeTable.getSession(), timeTable.getSemester(), fileNature);
    }
    private XWPFRun createTextRun(XWPFDocument document){
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        return paragraph.createRun();
    }
    private void populateTable(XWPFDocument document, List<Schedule> schedules, boolean isStudent, AppUser user){
        int tableRows = schedules.size() + 1;
        XWPFTable table = document.createTable(tableRows, isStudent ? 8 : 6);

        table.setTableAlignment(TableRowAlign.CENTER);
        table.setWidth(800);
        table.setWidthType(TableWidthType.PCT);

        table.getRow(0).getCell(0).setText("S/N");
        table.getRow(0).getCell(1).setText("COURSE TITLE");
        table.getRow(0).getCell(2).setText("COURSE CODE(S)");
        table.getRow(0).getCell(3).setText("DAY");
        table.getRow(0).getCell(4).setText("TIME");
        table.getRow(0).getCell(5).setText("VENUE(S)");

        if(isStudent){
            table.getRow(0).getCell(6).setText("LECTURER NAME");
            table.getRow(0).getCell(7).setText("LECTURER PHONE");
        }

        for(int i = 1; i < tableRows; i++){
            Schedule currentSchedule = schedules.get(i - 1);

            String courseCodesString = String.join(", ", courseCodeService.getCourseCodesByCourse(currentSchedule.getCourse()).stream().map(CourseCode::getCourseCode).toList());
            String classRoomsString = String.join(", ", currentSchedule.getClassRooms().stream().map(ClassRoom::getName).toList());

            table.getRow(i).getCell(0).setText(String.valueOf(i));
            table.getRow(i).getCell(1).setText(currentSchedule.getCourse().getCourseTitle());
            table.getRow(i).getCell(2).setText(courseCodesString);
            table.getRow(i).getCell(3).setText(currentSchedule.getDay());
            table.getRow(i).getCell(4).setText(currentSchedule.getTimePeriod());
            table.getRow(i).getCell(5).setText(classRoomsString);

            if(isStudent){
                table.getRow(i).getCell(6).setText(currentSchedule.getAppUser().getName());
                table.getRow(i).getCell(7).setText(currentSchedule.getAppUser().getPhone());
            }
        }
    }
}