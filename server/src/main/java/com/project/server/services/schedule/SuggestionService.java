package com.project.server.services.schedule;

import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.TimeTable;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.scheduleCombination.ScheduleCombinationService;
import com.project.server.db.services.tenantCalendar.TenantCalendarService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.memory.Suggestion;
import com.project.server.dtos.response.ClassroomResponse;
import com.project.server.dtos.response.SuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Slf4j
public class SuggestionService {
    private final ClassRoomService classRoomService;
    private final TimeTableService timeTableService;
    private final ScheduleCombinationService scheduleCombinationService;
    private final TenantCalendarService tenantCalendarService;

    @SneakyThrows
    public List<SuggestionResponse> getSuggestionsByFilter(String day, String startTime, String duration, String college, String classSize){
        return toSuggestionResponse(generateSuggestions(day, startTime, duration, college, classSize));
    }

    @SneakyThrows
    private List<Suggestion> generateSuggestions(String day, String startTime, String duration, String college, String classSize){
        List<Suggestion> suggestions = new ArrayList<>();

        TimeTable timeTable = timeTableService.getActiveTimeTableOptional().orElse(null);
        if(timeTable == null) return new ArrayList<>();

        List<ClassRoom> rooms = (college != null) ? classRoomService.findAllByCollege(college) : classRoomService.getClassroomsWithoutCollege();

        if(classSize != null) {
            int size = Integer.parseInt(Arrays.stream(classSize.split("< ")).toList().getLast());
            rooms.removeIf(room -> room.getCapacity() < size);
        }

        Long tenantId = TenantContext.get();
        List<String> daysInUse = tenantCalendarService.getDaysInUse(tenantId);
        List<String> periodTimes = tenantCalendarService.getPeriodTimes(tenantId);
        List<String> durations = tenantCalendarService.getDurations(tenantId);
        int maxEndHour = tenantCalendarService.getMaxEndHour(tenantId);

        Random random = new Random();
        long generationStartTime = System.currentTimeMillis();
        long generationPeriod = 10_000;

        while (suggestions.size() < 10 && ((System.currentTimeMillis() - generationStartTime) < generationPeriod)){
            String randomDay = (day != null) ? day : daysInUse.get(random.nextInt(daysInUse.size()));
            String randomStartTime = (startTime != null) ? startTime : periodTimes.get(random.nextInt(periodTimes.size()));
            String randomDuration = (duration != null) ? duration : durations.get(random.nextInt(durations.size()));

            int startTimeInt = Integer.parseInt(Arrays.stream(randomStartTime.split(":00")).toList().getFirst());
            int endTimeInt = Integer.parseInt(Arrays.stream(randomDuration.split(" ")).toList().getFirst()) + startTimeInt;

            ClassRoom randomClassRoom = rooms.get(random.nextInt(rooms.size()));

            Suggestion suggestion = toSuggestion(randomDay, randomStartTime, randomDuration, randomClassRoom, maxEndHour);

            if((!scheduleCombinationService.isExistingCombination(
                    randomDay,
                    startTimeInt,
                    endTimeInt,
                    randomClassRoom.getName(),
                    timeTable.getCode()
            )) && !suggestions.contains(suggestion)) suggestions.add(suggestion);
        }

        return suggestions;
    }

    private Suggestion toSuggestion(String day, String startTime, String duration, ClassRoom classRoom, int maxEndHour){
        String splitRegex = ":00";
        List<String> timeSplit = Arrays.stream(startTime.split(splitRegex)).toList();
        String startTimeString = timeSplit.getFirst();
        String startTimeStringSuffix = timeSplit.getLast();

        int startTimeInt = Integer.parseInt(startTimeString);
        int endTime = startTimeInt + Integer.parseInt(Arrays.stream(duration.split(" ")).toList().getFirst());

        if(endTime > maxEndHour) endTime = maxEndHour;

        String endTimeString = String.valueOf(endTime);
        String endTimeStringSuffix = endTime >= 12 ? "pm" : startTimeStringSuffix;

        String time =  String.format("%s - %s%s%s", startTime, endTimeString, splitRegex, endTimeStringSuffix);

        return Suggestion.builder()
                .day(day)
                .startTime(startTime)
                .duration(duration)
                .time(time)
                .classRoom(classRoom)
                .build();
    }

    private List<SuggestionResponse> toSuggestionResponse(List<Suggestion> suggestions){
        List<SuggestionResponse> response = new ArrayList<>();

        for(Suggestion suggestion : suggestions){
            ClassRoom room = suggestion.getClassRoom();
            String collegeName = room.getOrgUnit() != null ? room.getOrgUnit().getShortName() : null;
            response.add(SuggestionResponse.builder()
                            .classRoom(ClassroomResponse.builder()
                                    .className(room.getName())
                                    .code(room.getCode())
                                    .classSize(room.getCapacity())
                                    .college(collegeName)
                                    .build())
                            .duration(suggestion.getDuration())
                            .time(suggestion.getTime())
                            .startTime(suggestion.getStartTime())
                            .day(suggestion.getDay())
                    .build());
        }

        return response;
    }
}
