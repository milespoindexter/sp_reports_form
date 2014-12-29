package com.cn.dsa.silverpop.servlet;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.velocity.VelocityContext;

import com.cn.dsa.silverpop.db.OrgMgr;
import com.cn.dsa.silverpop.common.Org;
import com.cn.dsa.silverpop.ReportsRequester;
import com.cn.dsa.silverpop.ReportRequest;

import com.cn.dsa.common.VelocityMgr;

public class ReportFormServlet extends HttpServlet {

    public static String REPORT_TYPE = "GetAggregateTrackingForOrg";
    public static String APP_NAME = "SilverPop Report Request";
    
    public static String NL = System.getProperty("line.separator");
    
    private static String SP_DATE_FORMAT = "MM/dd/yyyy";
    
    private static final String REPORT_FORM = "spReportRequestForm.vm";
    private OrgMgr orgMgr = OrgMgr.getInstance();
    
    private static Logger logMgr = Logger.getLogger(ReportFormServlet.class.getName());
    private static VelocityMgr velocityMgr = new VelocityMgr("silverpop");
    
    public void init(final ServletConfig config) {
        //contextPath = config.getServletContext().getRealPath("/");
    }

    //this is null unless form gets submitted
    private List<ReportRequest> requestedReports = null;
    
    public void setRequestedReports(List<ReportRequest> rr) {
        requestedReports = rr;
    }
    
    public List<ReportRequest> getRequestedReports() {
        return requestedReports;
    }
    
    private boolean formSubmitted = false;
    
    private void setFormSubmitted(boolean fs) {
        formSubmitted = fs;
    }
    
    public boolean isFormSubmitted() {
        return formSubmitted;
    }
    
    //valid email address needed to proceed with requests
    private boolean emailValid = false;
    
    private void setEmailValid(boolean ev) {
        emailValid = ev;
    }
    
    public boolean isEmailValid() {
        return emailValid;
    }

    //valid file name needed to proceed with requests
    private boolean fileNameValid = false;
    
    private void setFileNameValid(boolean fn) {
        fileNameValid = fn;
    }
    
    public boolean isFileNameValid() {
        return fileNameValid;
    }
    
    //valid start/end dates needed to proceed with requests
    private boolean datesValid = false;
    
    private void setDatesValid(boolean dv) {
        datesValid = dv;
    }
    
    public boolean areDatesValid() {
        return datesValid;
    }

    //store bad date fields so they can be flagged later on form
    private ArrayList<String> badDates = new ArrayList<String>();
    
    private void addBadDate(String badDate) {
        //logMgr.info("adding bad date: "+badDate);
        badDates.add(badDate);
    }
    
    public List<String> getBadDates() {
        return badDates;
    }
    
    //code strings of requested orgs
    private ArrayList<String> requestCodes = new ArrayList<String>();
    
    private void addRequestCode(String rc) {
        requestCodes.add(rc);
    }
    
    public List<String> getRequestCodes() {
        return requestCodes;
    }
    
    private void reset() {
        requestedReports = null;
        formSubmitted = false;
        emailValid = false;
        datesValid = false;
        fileNameValid = false;
        badDates = new ArrayList<String>();
        requestCodes = new ArrayList<String>();
    }
    
    //FOR TESTING
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        reset();
        
        //logMgr.info("doPost called . . .");
        String email = null;
        String fileName = null;
        String startDate = generateStartDate();
        String endDate = generateEndDate();
        
        String[] reportTypes = request.getParameterValues("reportType");
        
        try {
            //See if form has been submitted
            String[] submits = request.getParameterValues("getReports");
            if(submits != null && submits.length > 0) {  //form submitted
                setFormSubmitted(true);
                logMgr.info("form submitted: ");
                //check incoming params
                
                //startDate
                startDate = extractDate(ReportRequest.START_DATE, request);
                //end Date
                endDate = extractDate(ReportRequest.END_DATE, request);
                
                //get requester email
                email = extractRequesterEmail(request);
                
                //file name to write out reports to
                fileName = extractFileName(request);
                
                List<ReportRequest> reportRequests = extractReportRequests(request);
                
                //after all dates have been checked, see if there are any bad dates found
                List<String> badDates = getBadDates();
                logMgr.info("bad date count: "+badDates.size());
                if(badDates.size() == 0) {
                    setDatesValid(true);
                }
                
                if(reportRequests != null && reportRequests.size() > 0) {
                    setRequestedReports(reportRequests);
                    //if OK, submit requests to service
                    //logMgr.info("isEmailValid: "+isEmailValid()+", areDatesValid: "+areDatesValid());
                    if(isEmailValid() && areDatesValid() && isFileNameValid()) {
                        for (String reportType: reportTypes) {
                            logMgr.info("sending "+reportType+" report request");
                            ReportsRequester requester = new ReportsRequester(reportType);
                            //append to file name for this report . . .
                            String reportFileName = fileName + "_"+reportType;
                            String serviceResponse = requester.requestReports(reportRequests, email, reportFileName, startDate, endDate);
                            if(serviceResponse != null) {
                                logMgr.info("requester response: "+NL+serviceResponse);
                            }
                        }
                    }
                }
            }
            
            PrintWriter out = response.getWriter();
            logMgr.info("creating form . . .");
            out.println( createForm(request, email, fileName, startDate, endDate, reportTypes) );
            
            out.close();
        }
        catch(Exception e) {
            logMgr.warning("bombed processing post request: "+e);
        }
    }
    
    /**
     * generates a date string 30 days previous to current date
     */
    private String generateStartDate() {
        Date today = new Date();

        // Use the Calendar class to subtract 30 days
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_YEAR, -30);

        // Use the date formatter to produce a formatted date string
        Date startDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(SP_DATE_FORMAT);
        return sdf.format(startDate);
        
    }
    
    private String generateEndDate() {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(SP_DATE_FORMAT);
        return sdf.format(today);
        
    }
    
    
    private List<ReportRequest> extractReportRequests(HttpServletRequest request) {
        List<ReportRequest> reportRequests = new ArrayList<ReportRequest>();
        String[] orgCodes = request.getParameterValues("getReport");
        
        if(orgCodes != null && orgCodes.length > 0) {
            for(int i = 0; i < orgCodes.length; i++) {
                ReportRequest reportRequest = null;

                String orgCode = orgCodes[i];
                addRequestCode(orgCode);
                //logMgr.info("creating report request for: "+orgCode);
                
                Org org = orgMgr.getOrg(orgCode);
                if(org != null) {
                    reportRequest = new ReportRequest(org);
                    //get start and end dates
                    try {
                        String startDate = request.getParameter(ReportRequest.START_DATE+"_"+orgCode);
                        //check if they have added a custom date to over-ride the default date
                        if(startDate != null && startDate.length() > 0) {
                            //date value there, so over-ride the default start date
                            reportRequest.setStartDate(startDate);
                            logMgr.info("checking startDate for "+orgCode+": "+startDate);
                            if(!isValidDate(startDate)) { //not valid
                                //valid date needed
                                //logMgr.info("adding bad startDate: "+startDate);
                                addBadDate(ReportRequest.START_DATE+"_"+orgCode);
                            }
                        }
                        
                        
                        String endDate = request.getParameter(ReportRequest.END_DATE+"_"+orgCode);
                        //check if they have added a custom date to over-ride the default date
                        if(endDate != null && endDate.length() > 0) {
                            //date value there, so over-ride the default end date
                            reportRequest.setEndDate(endDate);
                            
                            logMgr.info("checking endDate for "+orgCode+": "+endDate);
                            if(!isValidDate(endDate)) { //not valid
                                //valid date needed
                                addBadDate(ReportRequest.END_DATE+"_"+orgCode);
                            }
                        }
                        
                    }
                    catch(Exception e) {
                        logMgr.warning("using default start/end dates for report: "+orgCode);
                    }

                    reportRequests.add(reportRequest);
                
                }
                else {
                    logMgr.warning("no Org found for "+orgCode);
                }

                
            }//end for loop
            
        }
        return reportRequests;
    }
    
    private String extractRequesterEmail(HttpServletRequest request) {
        String email = request.getParameter("email");
        //check that email is valid, else trigger error msg
        if(isValidEmailAddress(email)) {  //valid email needed
            setEmailValid(true);
        }
        return email;
    }

    private String extractFileName(HttpServletRequest request) {
        String fileName = request.getParameter("fileName");
        //check that fileName is valid, else trigger error msg
        if(isValidFileName(fileName)) {
            setFileNameValid(true);
        }
        return fileName;
    }

    private String extractDate(String dateKey, HttpServletRequest request) {
        String date = request.getParameter(dateKey);
        //check that date is valid, else trigger error msg
        if(!isValidDate(date)) {
            addBadDate(dateKey);
        }
        return date;
    }
    
    private static boolean isValidFileName(String fileName) {
       boolean result = false;
       if(fileName != null && fileName.length() > 0) {
           try {
                String expression = "[_a-zA-Z0-9\\-\\.]+";  //only allow legal file name chars
                CharSequence inputStr = fileName;
                Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(inputStr);
                result = matcher.matches();
    
           } catch (Exception ex) {
               logMgr.warning("could not validate fileName: "+fileName);
               logMgr.warning(ex.getMessage());
           }
       }
       //logMgr.info("fileName validity for "+fileName+" = "+result);
       return result;
    }
    
    private static boolean isValidEmailAddress(String email) {
        boolean result = false;
        if(email != null && email.length() > 0) {
            String[] emails = email.split(",");
            for(String e : emails) {
                e = e.trim();
                try {
                    String expression = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
                    CharSequence inputStr = e;
                    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(inputStr);
                    result = matcher.matches();
                    
               } catch (Exception ex) {
                   logMgr.warning("could not validate email: "+e);
                   logMgr.warning(ex.getMessage());
               }
           }
       }
       //logMgr.info("email validity for "+email+" = "+result);
       return result;
    }

    private static boolean isValidDate(String dateStr) {
        //example: <SentDateTime>01/25/2011</SentDateTime>
        SimpleDateFormat sdf = new SimpleDateFormat(SP_DATE_FORMAT);
        boolean result = false;
        Date testDate = null;
        if(dateStr != null && dateStr.length() > 0) {
            try {
                testDate = sdf.parse(dateStr);
            } catch (ParseException ex) {
               //result = false;
            }
            if(testDate != null) {
                String testDateStr = sdf.format(testDate);
                logMgr.info("testing: "+testDateStr+" = "+dateStr);
                if(testDateStr.equals(dateStr) && testDateStr.length() == 10) {
                    result = true;
                }
            }
        }
        logMgr.info("date: "+dateStr+" valid="+result);
        return result;
    }
    
    private String createForm(HttpServletRequest request, String email, String fileName, String startDate, String endDate, String[] reportTypes) throws Exception {
        
        String form = "";
        
        List<ReportRequest> reportRequests = getRequestedReports();
        
        List<Org> orgs = orgMgr.getOrgs();
        logMgr.info(orgs.size()+" orgs to present");
        
        if(reportRequests != null) {
            logMgr.info(reportRequests.size()+" requested reports");
            //get dates from form if there for each org request
            for(ReportRequest reportRequest : reportRequests) {
                
                String orgCode = reportRequest.getOrgCode();
                
                //start date
                String formStartDate = request.getParameter(ReportRequest.START_DATE+"_"+orgCode);
                if(formStartDate != null && formStartDate.length() > 0) {
                    if(!formStartDate.equals( reportRequest.getStartDate() ) ){ //date has been modified on form
                        reportRequest.setStartDate(formStartDate);
                    }
                }
                //end date
                String formEndDate = request.getParameter(ReportRequest.END_DATE+"_"+orgCode);
                if(formEndDate != null && formEndDate.length() > 0) {  
                    if(!formEndDate.equals( reportRequest.getEndDate() ) ){ //date has been modified on form
                        reportRequest.setEndDate(formEndDate);
                    }
                }
            }
        }
            
        try {
            VelocityContext context = new VelocityContext();
            
            //put needed incoming params from request object here
            context.put("reports", orgs);
            if(isFormSubmitted()) {
                context.put("requests", reportRequests);
                if(!isEmailValid() || !areDatesValid() || !isFileNameValid() || reportRequests == null || reportRequests.size() == 0) {
                    context.put("success","false");
                    
                    if(reportRequests == null || reportRequests.size() == 0) {
                        context.put("requestListEmpty","true");
                    }
                    if(!isEmailValid()) {
                        context.put("emailNotValid","true");
                    }
                    if(!areDatesValid()) {
                        context.put("datesNotValid","true");
                    }
                    if(!isFileNameValid()) {
                        context.put("fileNameNotValid","true");
                    }
                }
                else {
                    context.put("success","true");
                }
                context.put("requestCodes",getRequestCodes());
            }
            context.put("applicationName",APP_NAME);
            context.put("today",new Date());
            context.put("email",email); //blank string if form not submitted
            context.put("fileName",fileName); //blank string if form not submitted
            context.put("startDate",startDate); //30 days previous if form not submitted
            context.put("endDate",endDate); //current date if form not submitted
            //show report types chosen
            context.put("reportTypes",reportTypes);

            form = velocityMgr.merge(REPORT_FORM, context);
            //logMgr.info("form : " + form );
        }
        catch(Exception e) {
            logMgr.warning("bombed creating report request form: "+e.getMessage());
        }
        
        return form;
    
    }

    public static void main(String[] args) throws IOException {
        try {
            //String dateStr = "06/26/2012 23:59:dd";
            String dateStr = "06/26/13";
            //ReportFormServlet rfs = new ReportFormServlet();
            boolean valid = ReportFormServlet.isValidDate(dateStr);
            valid = ReportFormServlet.isValidDate("06/26/13854");
            valid = ReportFormServlet.isValidDate("06/26/2013");
            //logMgr.info("date: "+dateStr+" valid="+valid);
            
        }
        catch(Exception e) {
            logMgr.warning("bombed: "+e);
        }
        
    }
    
    
}