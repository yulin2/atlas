package org.atlasapi.remotesite.itv.whatson;

import com.google.common.base.Objects;


public class ItvWhatsOnEntryDuration {
    private long Ticks;
    private int Days;
    private int Hours;
    private int Milliseconds;
    private int Minutes;
    private int Seconds;
    private double TotalDays;
    private double TotalHours;
    private int TotalMilliseconds;
    private int TotalMinutes;
    private int TotalSeconds;
    
    public long getTicks() {
        return Ticks;
    }
    
    public int getDays() {
        return Days;
    }
    
    public int getHours() {
        return Hours;
    }
    
    public int getMilliseconds() {
        return Milliseconds;
    }
    
    public int getMinutes() {
        return Minutes;
    }
    
    public int getSeconds() {
        return Seconds;
    }
    
    public double getTotalDays() {
        return TotalDays;
    }
    
    public double getTotalHours() {
        return TotalHours;
    }
    
    public int getTotalMilliseconds() {
        return TotalMilliseconds;
    }
    
    public int getTotalMinutes() {
        return TotalMinutes;
    }
    
    public int getTotalSeconds() {
        return TotalSeconds;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(getClass())
                .add("Ticks", getTicks())
                .add("Days", getDays())
                .add("Hours", getHours())
                .add("Milliseconds", getMilliseconds())
                .add("Minutes", getMinutes())
                .add("Seconds", getSeconds())
                .add("TotalDays", getTotalDays())
                .add("TotalHours", getTotalHours())
                .add("TotalMilliseconds", getTotalMilliseconds())
                .add("TotalMinutes", getTotalMinutes())
                .add("TotalSeconds", getTotalSeconds())
                .toString();
    }
}
