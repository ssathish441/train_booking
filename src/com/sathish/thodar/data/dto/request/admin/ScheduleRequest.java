package com.sathish.thodar.data.dto.request.admin;

import com.sathish.thodar.data.dto.enums.ScheduleStatus;
import java.util.HashMap;
import java.util.Map;

public class ScheduleRequest {
    private static long idCounter = 1;
    private Long id;
    private Long trainId;
    private long journeyDateEpoch;
    private ScheduleStatus status;
    private Map<String, String> timetable = new HashMap<>();
    private String rescheduleReason;

    public ScheduleRequest() { this.id = idCounter++; }

    public Long getId() { 
    	return id; 
    }
    public Long getTrainId() { 
    	return trainId; 
    }
    public void setTrainId(Long trainId) { 
    	this.trainId = trainId; 
    }
    public long getJourneyDateEpoch() { 
    	return journeyDateEpoch; 
    }
    public void setJourneyDateEpoch(long journeyDateEpoch) { 
    	this.journeyDateEpoch = journeyDateEpoch; 
    }
    public ScheduleStatus getStatus() { 
    	return status; 
    }
    public void setStatus(ScheduleStatus status) { 
    	this.status = status; 
    }
    public Map<String, String> getTimetable() { 
    	return timetable; 
    }
    public void setTimetable(Map<String, String> timetable) { 
    	this.timetable = timetable; 
    }
    public String getRescheduleReason() {
        return rescheduleReason;
    }
    public void setRescheduleReason(String rescheduleReason) {
        this.rescheduleReason = rescheduleReason;
    }
}