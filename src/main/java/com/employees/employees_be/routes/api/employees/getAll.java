package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.bson.Document;

import java.util.List;
import java.util.Map;

@Component
public class getAll {

    @Autowired
    private EmployeesCollection employeesCollection;

    // This function will directly fetch all employee records from the MongoDB collection
    public Map<String, Object> handle() {
        try {
            List<Document> allEmployees = employeesCollection.getAll(); // No filter, get all
            return Map.of(
                "status", "success",
                "data", allEmployees,
                "count" , allEmployees.size()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch employees", e);
        }
    }
}
