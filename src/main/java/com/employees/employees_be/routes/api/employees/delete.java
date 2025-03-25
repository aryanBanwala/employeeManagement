package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Component
public class delete {

    @Autowired
    private EmployeesCollection employeesCollection;

    public Map<String, Object> handle(@RequestBody Map<String, Object> body) {

        if (!body.containsKey("e_id") || body.get("e_id") == null || body.get("e_id").toString().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: e_id");
        }

        String e_id = body.get("e_id").toString();

        validateAndCheckExistence(e_id);
        disallowTopLevelDelete(e_id);
        long modifiedCount = reassignReportsTo(e_id);
        deleteEmployee(e_id);

        return buildResponse(modifiedCount);
    }

    private void validateAndCheckExistence(String e_id) {
        if (e_id == null || e_id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: e_id");
        }

        if (!employeesCollection.findById(e_id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee with e_id " + e_id + " not found");
        }
    }

    private void disallowTopLevelDelete(String e_id) {
        Optional<String> topEidOpt = employeesCollection.getTopLevelEid();
        if (topEidOpt.isPresent() && topEidOpt.get().equals(e_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Cannot delete main boss");
        }
    }

    private long reassignReportsTo(String e_id) {
        Optional<Document> employeeOpt = employeesCollection.findById(e_id);
        if (!employeeOpt.isPresent()) {
            return 0; // Employee doesn't exist, nothing to reassign
        }
    
        String newManagerId = employeeOpt.get().getString("reportsTo");
    
        if (newManagerId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Employee's reportsTo field is missing");
        }
    
        try {
            long modifiedCount = employeesCollection.reassignSubordinates(e_id, newManagerId);
            return modifiedCount;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reassign subordinates: " + e.getMessage());
        }
    }
    

    private void deleteEmployee(String e_id) {
        if (!employeesCollection.deleteById(e_id)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete employee with e_id " + e_id);
        }
    }

    private Map<String, Object> buildResponse(long modifiedCount) {
        return Map.of(
            "status", "success",
            "message", "Employee deleted successfully",
            "reassignedCount", modifiedCount,
            "reassignedMessage", modifiedCount > 0 
                ? modifiedCount + " employees reassigned to a new manager."
                : "No subordinates found to reassign."
        );
    }
    
}
