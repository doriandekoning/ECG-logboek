package com.kingdorian.android.ecg_logboek;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by dorian on 9-5-16.
 */
public class HourEntry {

    private Date startTime;
    private Date endTime;
    private String description;

    public void HourEntry(Date startTime, Date endTime, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("start", startTime.toString());
            jsonObject.put("end", endTime.toString());
            jsonObject.put("description", description);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}