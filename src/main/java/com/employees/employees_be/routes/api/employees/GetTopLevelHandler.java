package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class GetTopLevelHandler {

    @Autowired
    private EmployeesCollection employeesCollection;

    public Map<String, Object> handle() {
        Optional<String> topLevelEid = employeesCollection.getTopLevelEid();

        if (topLevelEid.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No top-level employee found");
        }

        Optional<Document> topLevelEmployee = employeesCollection.findById(topLevelEid.get());

        if (topLevelEmployee.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Top-level employee record not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("topLevelEmployee", topLevelEmployee.get());

        return response;
    }
}
