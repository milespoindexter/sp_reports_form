package com.cn.dsa.silverpop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.xml.sax.InputSource;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.velocity.VelocityContext;

import com.cn.dsa.common.VelocityMgr;


public class ReportsRequester {
    private static final String REQUEST_TEMPLATE = "spReportsRequest.vm";
    
    private static String rrApiUrl = "http://localhost:8181/spr";  //Test API
    private static VelocityMgr velocityMgr = new VelocityMgr("silverpop");
    
    //this can't be null, so this value is set in constructor
    private String reportType = null;

    private static Logger logMgr = Logger.getLogger(ReportsRequester.class.getName());

    //constructor
    public ReportsRequester(String reportType) {
        this.reportType = reportType;
        
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public String requestReports(List<ReportRequest> requests, String email, String fileName, String startDate, String endDate) {
        String serviceResponse = "";
        
        //modify dates to make them compatible with SilverPop XML API
        addTimeString(requests);
        if(startDate != null && startDate.trim().length() == 10) {
            startDate = startDate.trim() + " 00:00:00";
        }
        if(endDate != null && endDate.trim().length() == 10) {
            endDate = endDate.trim() + " 23:59:59";
        }
            
        String request = createReportsRequest(requests, email, fileName, startDate, endDate);
        System.out.println("reports request: ");
        System.out.println(request);
        
        try {
            serviceResponse = doPostRequest(rrApiUrl,request);
        }
        catch(Exception e) {
            System.err.println("bombed creating multi-report request from form: "+e);
        }
        
        return serviceResponse;
    }

    /**
     * for each start date, add 00:00:00
     * for each end date, add 23:59:59
     */
    private void addTimeString(List<ReportRequest> requests) {
        
        for(ReportRequest request : requests) {
            String startDate = request.getStartDate();
            
            if(startDate != null && startDate.length() == 10) {
                startDate = startDate.trim() + " 00:00:00";
                request.setStartDate(startDate);
            }
            String endDate = request.getEndDate();
            if(endDate != null && endDate.length() == 10) {
                endDate = endDate.trim() + " 23:59:59";
                request.setEndDate(endDate);
            }
        }
    }
    
    private String doPostRequest(String urlstr, String postBody) throws MalformedURLException, java.io.IOException, ProtocolException {
        StringBuffer response = new StringBuffer();
        URL url = new URL(urlstr);
        
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("POST");
        httpCon.setRequestProperty("Content-Type","text/xml");
        
        OutputStreamWriter wr = new OutputStreamWriter(httpCon.getOutputStream());
        wr.write(postBody);
        
        wr.flush ();
        wr.close ();

        //System.out.println("http response code: "+httpCon.getResponseCode());
        //System.out.println("http response msg: "+httpCon.getResponseMessage());
        
        //Get Response
        InputStream is = httpCon.getInputStream();
        //return is;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        is.close();
        
        return response.toString();
    }
    
    

    
    /**
     * uses apache velocityMgr to create xml request body
     * template: spReportsRequest.vm
     */
    private String createReportsRequest(List<ReportRequest> requests, String email, String fileName, String startDate, String endDate) {
        String reportsRequest = "";
    
        try {
            VelocityContext context = new VelocityContext();
            context.put("reportType",getReportType());
            context.put("reportRequests", requests);
            context.put("email", email);
            context.put("fileName", fileName);
            context.put("startDate", startDate);
            context.put("endDate", endDate);
            
            reportsRequest = velocityMgr.merge(REQUEST_TEMPLATE, context);
        }
        catch(Exception e) {
            System.err.println("bombed sending reports request: "+e);
        }
        
        return reportsRequest;
        
    }
    
    public static void main(String[] args) throws IOException {
        try {
            ReportsRequester rr = new ReportsRequester("GetAggregateTrackingForOrg");
            
        }
        catch(Exception e) {
            System.err.println("bombed: "+e);
        }
        
    }
    
    
}
