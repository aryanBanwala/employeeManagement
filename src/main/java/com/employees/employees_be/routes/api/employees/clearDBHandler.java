package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class clearDBHandler {

    @Autowired
    private EmployeesCollection employeesCollection;

    public Map<String, Object> handle(Map<String, Object> body) {
        if (!body.containsKey("confirm") || !Boolean.TRUE.equals(body.get("confirm"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmation required to clear the database");
        }

        long deletedCount = employeesCollection.clearDatabase();

        return Map.of(
            "status", "success",
            "message", "All employee records deleted",
            "deleted_count", deletedCount
        );
    }
}
