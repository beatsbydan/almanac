package com.project.server.db.services.tenantCalendar;

import com.project.server.db.models.TenantCalendar;
import com.project.server.exceptions.NotFoundException;

import java.util.List;

public interface TenantCalendarService {
    TenantCalendar getByTenantId(Long tenantId) throws NotFoundException;
    List<String> getDaysInUse(Long tenantId) throws NotFoundException;
    List<String> getPeriodTimes(Long tenantId) throws NotFoundException;
    List<String> getDurations(Long tenantId) throws NotFoundException;
    int getMaxEndHour(Long tenantId) throws NotFoundException;
    boolean hasBlackoutConflict(Long tenantId, String day, int startTime, int endTime) throws NotFoundException;
}
