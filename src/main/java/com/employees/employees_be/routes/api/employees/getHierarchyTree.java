package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class getHierarchyTree {

    @Autowired
    private EmployeesCollection employeesCollection;

    public Map<String, Object> handle() {
        // Step 1: Get top-level employee
        Document topEmployee = fetchTopLevelEmployee();

        // Step 2: Build hierarchical tree
        Map<String, Object> hierarchyTree = buildHierarchy(topEmployee);

        // Step 3: Format final response
        return buildResponse(hierarchyTree);
    }

    private Document fetchTopLevelEmployee() {
        return employeesCollection.getTopLevelEid()
                .flatMap(employeesCollection::findById)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Top-level employee not found"));
    }

    private Map<String, Object> buildHierarchy(Document employee) {
        String e_id = employee.getString("e_id");

        // Fetch direct reports (subordinates)
        List<Document> subordinates = employeesCollection.findByReportsTo(e_id);

        // Structure the employee node
        Map<String, Object> employeeNode = new HashMap<>();
        employeeNode.put("e_id", e_id);
        employeeNode.put("name", employee.getString("name"));
        employeeNode.put("email", employee.getString("email"));
        employeeNode.put("profileImage", employee.getString("profileImage"));

        // Recursively build children
        List<Map<String, Object>> children = subordinates.stream()
                .map(this::buildHierarchy)
                .collect(Collectors.toList());

        employeeNode.put("children", children);
        return employeeNode;
    }

    private Map<String, Object> buildResponse(Map<String, Object> hierarchyTree) {
        // This function ensures response is clean and well-structured
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", hierarchyTree);
        return response;
    }
}
