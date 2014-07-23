package org.atlasapi.remotesite.bbc.audience;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;


public class AudienceDataRow {

    private String channel;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String genre;
    private BigDecimal audience;
    private BigDecimal share;
    private Integer ai;
    private Integer male;
    private Integer female;
    private String repeat;
    private Integer age4to9;
    private Integer age10to15;
    private Integer age16to24;
    private Integer age25to34;
    private Integer age35to44;
    private Integer age45to54;
    private Integer age55to64;
    private Integer age65plus;
    private Integer ab;
    private Integer c1;
    private Integer c2;
    private Integer de;
    
    public AudienceDataRow(String channel, String title, LocalDate date, 
            LocalTime startTime, LocalTime endTime, String genre, 
            BigDecimal audience, BigDecimal share, Integer ai, Integer male,
            Integer female, String repeat, Integer age4to9, Integer age10to15,
            Integer age16to24, Integer age25to34, Integer age35to44, 
            Integer age45to54, Integer age55to64, Integer age65plus,
            Integer ab, Integer c1, Integer c2, Integer de) {
        this.channel = channel;
        this.title = title;
        this.date = date;
        this.startTime = startTime; 
        this.endTime = endTime;
        this.genre = genre;
        this.audience = audience;
        this.share = share;
        this.ai = ai;
        this.male = male;
        this.female = female;
        this.repeat = repeat;
        this.age4to9 = age4to9;
        this.age10to15 = age10to15;
        this.age16to24 = age16to24;
        this.age25to34 = age25to34;
        this.age35to44 = age35to44;
        this.age45to54 = age45to54;
        this.age55to64 = age55to64;
        this.age65plus = age65plus;
        this.ab = ab;
        this.c1 = c1;
        this.c2 = c2;
        this.de = de;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public String getTitle() {
        return title;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public BigDecimal getAudience() {
        return audience;
    }
    
    public BigDecimal getShare() {
        return share;
    }
    
    public Integer getAi() {
        return ai;
    }
    
    public Integer getMale() {
        return male;
    }
    
    public Integer getFemale() {
        return female;
    }
    
    public String getRepeat() {
        return repeat;
    }
    
    public Integer getAge4to9() {
        return age4to9;
    }
    
    public Integer getAge10to15() {
        return age10to15;
    }
    
    public Integer getAge16to24() {
        return age16to24;
    }
    
    public Integer getAge25to34() {
        return age25to34;
    }
    
    public Integer getAge35to44() {
        return age35to44;
    }
    
    public Integer getAge45to54() {
        return age45to54;
    }
    
    public Integer getAge55to64() {
        return age55to64;
    }
    
    public Integer getAge65plus() {
        return age65plus;
    }
    
    public Integer getAb() {
        return ab;
    }
  
    public Integer getC1() {
        return c1;
    }

    public Integer getC2() {
        return c2;
    }
    
    public Integer getDe() {
        return de;
    }
    
    public void mergeWith(AudienceDataRow other) {
        
    }
            
}
