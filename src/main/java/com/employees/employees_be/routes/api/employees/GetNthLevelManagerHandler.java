package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class GetNthLevelManagerHandler {

    @Autowired
    private EmployeesCollection employeesCollection;

    public Map<String, Object> handle(String e_id, int n) {
        // Validate inputs
        validateEid(e_id);
        validateLevel(n);

        // Fetch nth level manager from EmployeesCollection
        Map<String, Object> result = employeesCollection.getNthLevelManager(e_id, n);

        // Handle errors from EmployeesCollection
        if (result.containsKey("error")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, result.get("error").toString());
        }

        return result;
    }

    private void validateEid(String e_id) {
        if (e_id == null || e_id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid 'e_id'");
        }
    }

    private void validateLevel(int n) {
        if (n <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid 'n' value: Must be a positive integer");
        }
    }
}
