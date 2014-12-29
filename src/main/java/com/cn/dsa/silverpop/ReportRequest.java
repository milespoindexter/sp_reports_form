package com.cn.dsa.silverpop;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import com.cn.dsa.silverpop.common.Org;

/**
 * This class holds information about a Silverpop Report Request
 */
@XmlRootElement(name="reportRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ReportRequest extends Org {
    
    public static final String LOAD = "loadToMDW";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String EMAIL = "email";
    public static final String FILE_NAME = "fileName";
    public static final String REPORT_TYPE = "reportType";
    public static final String EVENT_START_DATE = "eventStartDate";
    public static final String EVENT_END_DATE = "eventEndDate";
    public static final String EXPORT_FORMAT = "exportFormat";

    private Org org;
    public void setOrg(Org org) {
        this.org = org;
        setOrgProps(org);
    }
    public Org getOrg() {
        return org;
    }

    //constructor
    public ReportRequest( Org org) {
        setOrg(org);
    }

    private void setOrgProps(Org org) {
        setOrgCode(org.getOrgCode());
        setLogin(org.getLogin());
        setPwd(org.getPwd());
        setFtpServer(org.getFtpServer());
        setApiUrl(org.getApiUrl());
    }


    //constructor
    public ReportRequest(String orgCode) {
        setOrgCode(orgCode);
    }

   

    //constructor
    public ReportRequest() {
        setOrgCode("CN");
    }

    

    //needed for report requests
    String startDate = null;
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getStartDate() {
        return startDate;
    }

    String endDate = null;
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getEndDate() {
        return endDate;
    }
    
    String email = null;
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    String fileName = null;
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    String reportType = null;
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    public String getReportType() {
        return reportType;
    }

    String eventStartDate = null;
    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }
    public String getEventStartDate() {
        return eventStartDate;
    }

    String eventEndDate = null;
    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }
    public String getEventEndDate() {
        return eventEndDate;
    }

    String exportFormat = null;
    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
    public String getExportFormat() {
        return exportFormat;
    }

    
    
    public static void main(String[] args) {
        try {
            String testOrg = "ALL_EP";
            
        }
        catch(Exception e) {
            System.err.println("bombed: "+e);
        }
        
    }
    
    
}
