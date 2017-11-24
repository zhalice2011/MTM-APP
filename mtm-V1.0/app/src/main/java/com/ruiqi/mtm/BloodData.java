package com.ruiqi.mtm;

/**
 * Created by HongYuLiu on 2017/10/16.
 */

public class BloodData {

    // 采集时间，时间格式：2017-03-01 17:17:21
    private String time;
    // 舒张压（低压）
    private int sys;
    // 收缩压（高压）
    private int dia;
    // 心率 (bpm)
    private int bpm;
    //血糖值
    private String bloodsugar;
    // 血氧 (bpm)
    private int spo;
    // 脉率 (pr)
    private int pr;
    // 体重 (wt)
    private String wt;
    //数据来源(datatype)
    private String datatype;

    // 心电计---分析结果
    private String results;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getBpm() {
        return bpm;
    }

    public int getSpo() {
        return spo;
    }

    public int getPr() {
        return pr;
    }

    public String getWt() {
        return wt;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }
    public void setSpo(int spo) {
        this.spo = spo;
    }
    public void setPr(int pr) {
        this.pr = pr;
    }
    public void setWt(String wt) {
        this.wt = wt;
    }
    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
    //心电计
    public String getResults() {
        return results;
    }
    public void setResults(String results) {
        this.results = results;
    }

    //血糖
    public void setBloodSugar(String bloodsugar){
        this.bloodsugar=bloodsugar;
    }
    public String getBloodSugar(){
        return bloodsugar;
    }
}