package org.atlasapi.remotesite.seesaw.selftest;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SeesawSelfTestController {

    @RequestMapping("/system/selftest/seesaw")
    public String testSeesawAdapter(HttpServletResponse response) throws IOException {
        
        TestSuite testSuite = new TestSuite(SeesawAtoZBrandsAdapterTest.class);
        TestResult result = new TestResult(); 
        testSuite.run(result);
        
        String bgColor;
        if (result.wasSuccessful()) {
            response.setStatus(HttpServletResponse.SC_OK);
            bgColor = "#00FF00";
        }
        else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            bgColor = "#FF0000";
        }
        
        ServletOutputStream out = response.getOutputStream();
        out.print("<html><head><title>Test Results</title></head><body style='background-color: " + bgColor + "'>" +
        		"<h1>Results of test of SeesawAllBrandsAdapter</h1>" +
        		"Tests run: " + result.runCount() + "<br />" + 
        		"Failures: " + result.failureCount() + "<br />" + 
        		"Errors: " + result.errorCount() + "<br />");
        		
        @SuppressWarnings("unchecked")
        Enumeration<TestFailure> failures = result.failures();
        if (failures.hasMoreElements()) {
            out.print("Failure Details: <br />");
        }
        printFailures(failures, out);
        
        @SuppressWarnings("unchecked")
        Enumeration<TestFailure> errors = result.errors();
        if (errors.hasMoreElements()) {
            out.print("Error Details: <br />");
        }
        printFailures(errors, out);
        
        out.print("</body></html");
        
        return null;
    }
    
    private void printFailures(Enumeration<TestFailure> failures, ServletOutputStream out) throws IOException {
        while (failures.hasMoreElements()) {
            TestFailure failure = (TestFailure) failures.nextElement();
            out.print(failure.failedTest().getClass().toString() + ": " + failure.exceptionMessage() + "<br />");
        }
    }
}
