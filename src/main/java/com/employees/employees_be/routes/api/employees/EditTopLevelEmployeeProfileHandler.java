package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
public class EditTopLevelEmployeeProfileHandler {

    @Autowired
    private EmployeesCollection employeesCollection;

    // Allowed fields for update
    private static final List<String> ALLOWED_FIELDS = Arrays.asList("name", "email", "phoneNumber", "profileImage");

    public Map<String, Object> handle(Map<String, Object> body) {
        String e_id = getTopLevelEid();

        Map<String, Object> updateFields = buildUpdateFields(body);

        checkPhoneNumberDuplication(e_id, updateFields);

        performUpdate(e_id, updateFields);

        return fetchUpdatedEmployee(e_id);
    }

    private String getTopLevelEid() {
        Optional<String> topEidOpt = employeesCollection.getTopLevelEid();
        if (topEidOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No top-level employee found");
        }
        return topEidOpt.get();
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
    
        for (String key : body.keySet()) {
            if (!ALLOWED_FIELDS.contains(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid field: " + key);
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update top-level employee profile");
        }
    }

    private Map<String, Object> fetchUpdatedEmployee(String e_id) {
        Optional<Document> updatedDoc = employeesCollection.findById(e_id);
        if (updatedDoc.isPresent()) {
            return updatedDoc.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Top-level employee not found after update");
        }
    }
}
