package com.project.server.cron.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeTableGenerationCronService {
    /*
     * GENERAL TIMETABLE GENERATION
     * STEPS:
     * 1. UWC go first so all courses that don't have a specific department attached should be formatted.
     * 2. Group by colleges next so all schedules with classrooms within colleges would be formatted.
     * 3. Lastly for each college, if there are courses that use classrooms outside the college, we add them last
     * LOGIC (CRON: 5 minutes active, 3 minutes cooldown):
     * - In the current active timetable,
     * - Attend to each case one at a time (Start with general courses).
     * - Batch 50 schedules for each case (UWC / College)
     * - Group by day first, then time, then classroom
     * - Insert into the document.
     * - Save the file on the server and keep track of current case on timetable.
     * - End.
     * - Upon every cron entry, a case must be completed.
     * - If it's not, save the current case again and re-iterate.
     * - Only proceed to next case (UWC / College) if current case is complete.
     */

    /*
     * GROUPING AND FORMATTING
     * 1. All Schedules should be grouped according to Days first
     * 2. Next would be according to time
     */

    /*
     * REQUIREMENTS:
     * 1. Endpoint to generate
     * 2. Endpoint to download
     * 3. Endpoint to get generation info.
     */

    /*
     * TABLE FORMAT
     * GENERAL:
     *   - Day outline comes first before the table.
     *   - Time slots in ranges of 1hr per column
     *   - First column from second row down contains all classrooms within colleges after which classrooms outside the college containing schedules would be added
     */
}
