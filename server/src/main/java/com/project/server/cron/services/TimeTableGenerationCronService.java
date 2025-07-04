package com.project.server.cron.services;

import com.project.server.db.models.*;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.college.CollegeService;
import com.project.server.db.services.courseCodes.CourseCodeService;
import com.project.server.db.services.generationTracker.GenerationTrackerService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.enums.GenerationStage;
import com.project.server.enums.Status;
import com.project.server.exceptions.TimeTableGenerationException;
import com.project.server.util.VariableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.*;

import static com.project.server.util.HelperUtil.createTextRun;
import static com.project.server.util.HelperUtil.generateTimeSlots;
import static com.project.server.util.VariableUtil.TIME_TABLE_DIRECTORY;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeTableGenerationCronService {
    private final TimeTableService timeTableService;
    private final GenerationTrackerService trackerService;
    private final ScheduleService scheduleService;
    private final ClassRoomService classRoomService;
    private final CollegeService collegeService;
    private final CourseCodeService courseCodeService;

    public void generate() throws TimeTableGenerationException {
        Pageable pageable = PageRequest.of(0, 100);
        List<GenerationStage> generationStages = Arrays.asList(GenerationStage.values());

        try{
            TimeTable timeTable;
            Optional<TimeTable> timeTableOpt = timeTableService.getActiveTimeTableOptional();
            if(timeTableOpt.isPresent()) timeTable = timeTableOpt.get();
            else throw new TimeTableGenerationException("No active Timetable available for generation.");

            Status status = timeTable.getStatus();

            if(timeTable.getCreationStatus().equals(Status.ENABLED)){
                log.info("TimeTable Generation cannot begin at this time.");
                return;
            }
            else if(status == Status.FAILED_TO_GENERATE) throw new TimeTableGenerationException("TimeTable Generation was unsuccessful.");
            else if(status.equals(Status.DONE_WITH_GENERATION)) {
                log.info("TimeTable Generation has been completed and is available for download.");
                return;
            }

            GenerationTracker tracker = timeTable.getGenerationTracker();
            GenerationStage stage = tracker.getGenerationStage();

            log.info("Continuing generation procedure for STAGE: {}", stage.name());

            Page<Schedule> schedules;
            List<String> classRooms;
            College college = null;

            if(stage.equals(GenerationStage.UWC)) {
                classRooms = classRoomService.getClassroomsWithoutCollege().stream().map(ClassRoom::getName).toList();
                schedules = scheduleService.getUnprocessedUniversityWideSchedules(pageable);
            }
            else {
                college = collegeService.getByName(stage.name());
                classRooms = classRoomService.findAllByCollege(stage.name()).stream().map(ClassRoom::getName).toList();
                schedules = scheduleService.getUnprocessedSchedulesByCollege(stage.name(), pageable);
            }

            if(!schedules.hasContent()){
                int nextIndex = generationStages.indexOf(stage) + 1;
                if(nextIndex < generationStages.size()) {
                    GenerationStage nextStage = generationStages.get(nextIndex);
                    tracker.setGenerationStage(nextStage);
                    trackerService.save(tracker);
                    log.info("No Schedules found for stage {}, continuing on next cron entry for next stage: {}", stage.name(), nextStage.name());
                }
                else{
                    timeTable.setStatus(Status.DONE_WITH_GENERATION);
                    timeTableService.save(timeTable);
                }

                return;
            }

            log.info("Successfully fetched {} schedules for stage: {}", schedules.getTotalElements(), stage.name());

            XWPFDocument timetable;

            File timetableDir = new File(TIME_TABLE_DIRECTORY);
            if(!timetableDir.exists()) timetableDir.mkdirs();

            log.info("Successfully upserted root directory: {}", TIME_TABLE_DIRECTORY);

            File file = new File(TIME_TABLE_DIRECTORY, timeTable.getFileName());

            if(file.exists()) {
                log.info("Fetching timetable document from {}", file.getPath());
                FileInputStream inputStream = new FileInputStream(file);
                timetable = new XWPFDocument(inputStream);
            }
            else {
                log.info("No timetable document found, creating a new file");
                timetable = new XWPFDocument();
            }

            populateTable(timeTable, timetable, college, schedules.getContent(), classRooms);

            FileOutputStream outputStream = new FileOutputStream(file);
            timetable.write(outputStream);

            timeTable.setStatus(Status.DONE_WITH_GENERATION);
            timeTableService.save(timeTable);
            log.info("Successfully generated timetable document and saved at path: {}", file.getPath());
        }
        catch(Exception e){
            throw new TimeTableGenerationException(e.getMessage());
        }
    }

    private void populateTable(TimeTable timeTable, XWPFDocument timetableDocument, College college, List<Schedule> schedules, List<String> classRooms){
        Map<String, Map<String, List<Schedule>>> groupedSchedules = groupSchedules(schedules);

        CTSectPr sectPr = timetableDocument.getDocument().getBody().getSectPr();
        if(sectPr == null) sectPr = timetableDocument.getDocument().getBody().addNewSectPr();
        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setOrient(STPageOrientation.LANDSCAPE);

        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(1066));
        pageMar.setBottom(BigInteger.valueOf(1066));
        pageMar.setLeft(BigInteger.valueOf(1066));
        pageMar.setRight(BigInteger.valueOf(1066));

        pageMar.setHeader(BigInteger.valueOf(1066));
        pageMar.setFooter(BigInteger.valueOf(1066));

        XWPFRun titleRun = createTextRun(timetableDocument);
        titleRun.setText(String.format("BOWEN UNIVERSITY %s session %s SEMESTER TIMETABLE", timeTable.getSession(), timeTable.getSemester()));
        titleRun.setBold(true);
        titleRun.setFontSize(20);
        titleRun.addBreak();

        XWPFRun collegeRun = createTextRun(timetableDocument);
        collegeRun.setCapitalized(true);
        collegeRun.setBold(true);
        collegeRun.setFontSize(14);
        collegeRun.setText(college.getName());
        collegeRun.addBreak();

        for(String day : groupedSchedules.keySet()){
            log.info("Inserting Schedules for day: {}", day);
            insertSchedules(day, groupedSchedules.get(day), classRooms, timetableDocument);
        }

        completeScheduleProcessing(schedules);
    }

    private void insertSchedules(String day, Map<String, List<Schedule>> classRoomSchedules, List<String> classRooms, XWPFDocument timetable){
        XWPFRun dayRun = createTextRun(timetable);
        dayRun.setCapitalized(true);
        dayRun.setBold(true);
        dayRun.setText(day);

        XWPFTable table = timetable.createTable(classRooms.size() + 1, 11);

        CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
        width.setType(STTblWidth.PCT);
        width.setW(BigInteger.valueOf(5000));

        table.getRow(0).getCell(0).setText("TIMINGS / VENUES");
        addTimings(table);
        addClassRooms(table, classRooms);

        for(int i = 1; i <= classRooms.size(); i++){
            List<Schedule> currentClassSchedules = classRoomSchedules.get(classRooms.get(i - 1));
            if(currentClassSchedules != null) {
                for (Schedule schedule : currentClassSchedules) {
                    int startTime = schedule.getStartTime();
                    int endTime = schedule.getEndTime();
                    int duration = endTime - startTime;

                    int scheduleCell = getCellByStartTime(startTime);
                    String courseCodes = String.join(" / ", courseCodeService.getCourseCodesByCourse(schedule.getCourse()).stream().map(CourseCode::getCourseCode).toList());

                    table.getRow(i).getCell(scheduleCell).setText(courseCodes);
                    if (duration == 2) table.getRow(i).getCell(scheduleCell + 1).setText(courseCodes);
                }
            }
        }

        XWPFRun spacerRun = createTextRun(timetable);
        spacerRun.addBreak();
    }

    private void addTimings(XWPFTable table){
        List<String> timings = generateTimeSlots();
        for(int i = 1; i < 11; i++){
            table.getRow(0).getCell(i).setText(timings.get(i - 1));
        }
    }

    private void addClassRooms(XWPFTable table, List<String> classRooms){
        for(int i = 1; i <= classRooms.size(); i++){
            table.getRow(i).getCell(0).setText(classRooms.get(i - 1));
        }
    }

    private int getCellByStartTime(int startTime){
        int cell = 0;
        switch (startTime){
            case 8 -> {
                cell = 1;
            }
            case 9 -> {
                cell = 2;
            }
            case 10 -> {
                cell = 3;
            }
            case 11 -> {
                cell = 4;
            }
            case 12 -> {
                cell = 5;
            }
            case 13 -> {
                cell = 6;
            }
            case 14 -> {
                cell = 7;
            }
            case 15 -> {
                cell = 8;
            }
            case 16 -> {
                cell = 9;
            }
            case 17 -> {
                cell = 10;
            }
        }

        return cell;
    }

    private void completeScheduleProcessing(List<Schedule> schedules){
        for(Schedule schedule : schedules){
            schedule.setProcessedForGeneration(true);
        }
        scheduleService.saveAll(schedules);
    }

    private Map<String, Map<String, List<Schedule>>> groupSchedules(List<Schedule> schedules){
        Map<String, Map<String, List<Schedule>>> finalSortedSchedules = new LinkedHashMap<>();

        Map<String, List<Schedule>> groupedByDay = new HashMap<>();
        for(Schedule schedule : schedules) {
            groupedByDay.computeIfAbsent(schedule.getDay(), k -> new ArrayList<>()).add(schedule);
        }

        for(String day : VariableUtil.DAYS){
            List<Schedule> dailySchedules = groupedByDay.get(day);
            if(dailySchedules == null || dailySchedules.isEmpty()) continue;

            Map<String, List<Schedule>> groupedByClassRoom = new HashMap<>();
            for(Schedule schedule: dailySchedules){
                for(ClassRoom room : schedule.getClassRooms()){
                    groupedByClassRoom.computeIfAbsent(room.getName(), k -> new ArrayList<>()).add(schedule);
                }
            }
            for(List<Schedule> classRoomSchedules : groupedByClassRoom.values()){
                classRoomSchedules.sort(Comparator.comparing(Schedule::getStartTime));
            }

            finalSortedSchedules.put(day, groupedByClassRoom);
        }

        return finalSortedSchedules;
    }
}