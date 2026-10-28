package com.project.server.db.services.tenantCalendar;

import com.project.server.db.models.BlackoutWindow;
import com.project.server.db.models.TenantCalendar;
import com.project.server.db.repositories.TenantCalendarRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantCalendarServiceImpl implements TenantCalendarService {
    private final TenantCalendarRepository repository;

    @Override
    public TenantCalendar getByTenantId(Long tenantId) throws NotFoundException {
        return repository.findByTenant_Id(tenantId).orElseThrow(() -> new NotFoundException("Tenant calendar not found."));
    }

    @Override
    public List<String> getDaysInUse(Long tenantId) throws NotFoundException {
        return List.of(getByTenantId(tenantId).getDaysInUse().split(","));
    }

    @Override
    public List<String> getPeriodTimes(Long tenantId) throws NotFoundException {
        return List.of(getByTenantId(tenantId).getPeriodTimes().split(","));
    }

    @Override
    public List<String> getDurations(Long tenantId) throws NotFoundException {
        return List.of(getByTenantId(tenantId).getDurations().split(","));
    }

    @Override
    public int getMaxEndHour(Long tenantId) throws NotFoundException {
        return getByTenantId(tenantId).getMaxEndHour();
    }

    @Override
    public boolean hasBlackoutConflict(Long tenantId, String day, int startTime, int endTime) throws NotFoundException {
        TenantCalendar calendar = getByTenantId(tenantId);
        if (calendar.getBlackoutWindows() == null) return false;

        for (BlackoutWindow window : calendar.getBlackoutWindows()) {
            if (window.getDay().equals(day) && startTime >= window.getStartTime() && startTime <= window.getEndTime()) {
                return true;
            }
        }
        return false;
    }
}
