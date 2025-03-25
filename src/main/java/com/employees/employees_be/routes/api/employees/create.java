package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import com.employees.employees_be.services.EmailService;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
public class create {

    @Autowired
    private EmployeesCollection employeesCollection;

    @Autowired
    private EmailService emailService;

    // Define required fields including 'reportsTo'
    private static final List<String> REQUIRED_FIELDS = Arrays.asList("name", "phoneNumber", "email", "reportsTo");

    public Map<String, Object> handle(Map<String, Object> body) {
        // Middleware chain: sequentially call each function
        validateRequiredFields(body);
        checkPhoneNumber(body);
        checkManager(body);
        String emailStatus = sendEmailToManager(body);
        String id = insertEmployee(body);
        return buildResponse(id , emailStatus);
    }

    // Middleware 1: Validate required fields
    private void validateRequiredFields(Map<String, Object> body) {
        List<String> missingFields = new ArrayList<>();
        for (String field : REQUIRED_FIELDS) {
            Object value = body.get(field);
            if (value == null || value.toString().trim().isEmpty()) {
                missingFields.add(field);
            }
        }
        if (!missingFields.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing required fields: " + String.join(", ", missingFields)
            );
        }
    }

    // Middleware 2: Check if phone number already exists
    private void checkPhoneNumber(Map<String, Object> body) {
        String phone = body.get("phoneNumber").toString();
        if (employeesCollection.doesPhoneNumberExist(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee with this phone number already exists");
        }
    }

    // Middleware 3: Check manager or top-level existence
    private void checkManager(Map<String, Object> body) {
        String managerEid = body.get("reportsTo").toString();
        if (managerEid.equals("-1")) {
            // For top-level, ensure no other top-level employee exists
            if (employeesCollection.doesTopLevelExist()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Top-level employee already exists");
            }
        } else {
            // For non top-level, check if manager exists
            if (!employeesCollection.doesEidExist(managerEid)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Employee with e_id " + managerEid + " not found.");
            }
        }
    }

    // Insert the employee into MongoDB
    private String insertEmployee(Map<String, Object> body) {
        Document doc = new Document(body);
        // EmployeesCollection.insert() generates and sets the e_id internally
        return employeesCollection.insert(doc);
    }

    // Middleware 4: Send email notification to the manager
    private String sendEmailToManager(Map<String, Object> body) {
        String managerEid = body.get("reportsTo").toString();
        
        if (managerEid.equals("-1")) {
            return "Cant Send to the top level employee";
        }

        Optional<Document> managerDoc = employeesCollection.findById(managerEid);
        if (managerDoc.isEmpty()) {
            return "manager doesnt exist" ;
        }

        Document manager = managerDoc.get();
        String managerEmail = manager.getString("email");

        String employeeName = body.get("name").toString();
        String phoneNumber = body.get("phoneNumber").toString();
        String email = body.get("email").toString();

        String subject = "New Employee Assigned Under You";
        String bodyText = employeeName + " will now work under you. Mobile number is " + phoneNumber + " and email is " + email + ".";

        String status = emailService.sendEmail(managerEmail, subject, bodyText);
        return status;
    }


    // Build a success response
    private Map<String, Object> buildResponse(String id , String status) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("employeeId", id);
        res.put("emailStatus",status);
        return res;
    }
}
