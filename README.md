SilverPop Reports Form

2012-08-06<br>
Miles Poindexter<br>
selfpropelledcity@gmail.com<br>

Provides web form to submit request for SilverPop reports

Talks to Oracle DB to get list of available reports.

URL: ______________________________________________________

http://localhost:8081/SilverPopReportRequest/form

App Server:
TomEE

MVN: _____________________________________________________________________<br>
mvn exec:java -Dexec.mainClass="com.cn.dsa.silverpop.ReportRequester"<br>
mvn exec:java -Dexec.mainClass="com.cn.dsa.silverpop.servlet.ReportFormServlet"
