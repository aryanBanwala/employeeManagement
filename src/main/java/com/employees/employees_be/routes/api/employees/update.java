package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
public class update {

    @Autowired
    private EmployeesCollection employeesCollection;

    private static final List<String> ALLOWED_FIELDS = Arrays.asList("name", "email", "phoneNumber" , "reportsTo" , "profileImage");

    public Map<String, Object> handle(Map<String, Object> body) {
        
        String e_id = validateAndExtractEid(body);

        disallowTopLevelUpdate(e_id);

        Map<String, Object> updateFields = buildUpdateFields(body);
        
        checkPhoneNumberDuplication(e_id, updateFields);
        
        validateAndBuildReportsTo(body, updateFields, e_id);

        performUpdate(e_id, updateFields);
        
        return fetchUpdatedEmployee(e_id);

    }

    private String validateAndExtractEid(Map<String, Object> body) {
        if (!body.containsKey("e_id") || body.get("e_id") == null || body.get("e_id").toString().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: e_id");
        }
        return body.get("e_id").toString();
    }

    private void disallowTopLevelUpdate(String e_id) {
        Optional<String> topEidOpt = employeesCollection.getTopLevelEid();
        if (topEidOpt.isPresent() && topEidOpt.get().equals(e_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Cannot update main boss");
        }
    }
    
    private void validateAndBuildReportsTo(Map<String, Object> body, Map<String, Object> updateFields, String currentEmployeeEid) {
        if (body.containsKey("reportsTo")) {
            String newReportsTo = body.get("reportsTo").toString();
            if (newReportsTo.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reportsTo value");
            }
            // Validate the provided manager's existence and cyclic dependency.
            validateReportsTo(currentEmployeeEid, newReportsTo);
            // If valid, add the reportsTo field to updateFields.
            updateFields.put("reportsTo", newReportsTo);
        }
    }
    

    private void validateReportsTo(String currentEmployeeEid, String newReportsTo) {
        // If the value is "-1", it means top-level (no manager); that's allowed.
        if (newReportsTo.equals("-1")) {
            return;
        }
        
        // Check that the new manager exists.
        Optional<Document> managerDocOpt = employeesCollection.findById(newReportsTo);
        if (!managerDocOpt.isPresent()) {
            // Throw error instead of breaking
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Manager with e_id " + newReportsTo + " not found in DB"
            );
        }
        
        // Now check for cyclic dependency:
        Set<String> visited = new HashSet<>();
        String managerEid = newReportsTo;
        while (!managerEid.equals("-1")) { // "-1" indicates top-level boss.
            if (managerEid.equals(currentEmployeeEid)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Cyclic dependency detected: employee cannot report to themselves or a subordinate"
                );
            }
            if (visited.contains(managerEid)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Cyclic dependency detected in the manager chain"
                );
            }
            visited.add(managerEid);
            
            Optional<Document> docOpt = employeesCollection.findById(managerEid);
            if (!docOpt.isPresent()) {
                // Manager not found while traversing means data inconsistency: throw error.
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Manager with e_id " + managerEid + " not found in DB during chain validation"
                );
            }
            Document doc = docOpt.get();
            Object rep = doc.get("reportsTo");
            if (rep == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Manager record for e_id " + managerEid + " is missing reportsTo field"
                );
            }
            managerEid = rep.toString();
        }
    }
    

    private Map<String, Object> buildUpdateFields(Map<String, Object> body) {
        Map<String, Object> updateFields = new HashMap<>();
        for (String field : ALLOWED_FIELDS) {
            if (body.containsKey(field)) {
                Object value = body.get(field);
                if (value != null && !value.toString().trim().isEmpty()) {
                    updateFields.put(field, value);
                }
            }
        }
        if (updateFields.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid fields provided for update");
        }
        return updateFields;
    }

    private void checkPhoneNumberDuplication(String e_id, Map<String, Object> updateFields) {
        if (updateFields.containsKey("phoneNumber")) {
            String newPhone = updateFields.get("phoneNumber").toString();
            if (employeesCollection.doesPhoneNumberExistExcept(newPhone, e_id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Another employee with this phone number already exists");
            }
        }
    }

    private void performUpdate(String e_id, Map<String, Object> updateFields) {
        boolean updated = employeesCollection.updateById(e_id, new Document(updateFields));
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee with e_id " + e_id + " not found");
        }
    }

    private Map<String, Object> fetchUpdatedEmployee(String e_id) {
        Optional<Document> updatedDoc = employeesCollection.findById(e_id);
        if (updatedDoc.isPresent()) {
            return updatedDoc.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee with e_id " + e_id + " not found after update");
        }
    }
}
