package com.employees.employees_be.routes.api.employees;

import com.employees.employees_be.db.EmployeesCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class getEmployees {

    @Autowired
    private EmployeesCollection employeesCollection;

    public Map<String, Object> handle(HttpServletRequest request) {

        Map<String, Object> params = validateAndExtractParameters(request);
        
        List<Document> employees = retrieveEmployees(params);
        
        // Get actual total count of employees (ignoring query filters)
        long total = employeesCollection.countEmployees(new Document());
        
        return buildResponse(params, employees, total);

    }

    private Map<String, Object> validateAndExtractParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        // --- Pagination parameters ---
        String pageParam = request.getParameter("page");
        String limitParam = request.getParameter("limit");
        int page = 1;
        int limit = 10;
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 1");
                }
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
            }
        }
        if (limitParam != null) {
            try {
                limit = Integer.parseInt(limitParam);
                if (limit < 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be >= 1");
                }
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid limit number");
            }
        }
        params.put("page", page);
        params.put("limit", limit);
        params.put("skip", (page - 1) * limit);

        // --- Sorting parameters ---
        String sortBy = request.getParameter("sortBy");
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "name";
        } else if (!sortBy.equals("name") && !sortBy.equals("email") && !sortBy.equals("phone")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorting allowed only by 'name', 'email' or 'phone'");
        }
        params.put("sortBy", sortBy);

        String orderParam = request.getParameter("order");
        int sortOrder = 1; // Default ascending
        if (orderParam != null && !orderParam.trim().isEmpty()) {
            if (orderParam.equalsIgnoreCase("desc")) {
                sortOrder = -1;
            } else if (!orderParam.equalsIgnoreCase("asc")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be either 'asc' or 'desc'");
            }
        }
        params.put("order", (orderParam == null || orderParam.trim().isEmpty()) ? "asc" : orderParam.toLowerCase());
        params.put("sortOrder", sortOrder);

        Document sortDoc = new Document(sortBy, sortOrder);
        params.put("sortDoc", sortDoc);

        return params;
    }

    private List<Document> retrieveEmployees(Map<String, Object> params) {

        int skip = (Integer) params.get("skip");
        int limit = (Integer) params.get("limit");
        Document sortDoc = (Document) params.get("sortDoc");
        
        // Use an empty query for now; add filters if needed.
        Document query = new Document();
        return employeesCollection.getEmployees(query, skip, limit, sortDoc);
    }

    // Middleware 3: Build and return the final response map with metadata and data
    private Map<String, Object> buildResponse(Map<String, Object> params, List<Document> employees , long total) {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "success");
        response.put("page", params.get("page"));
        response.put("limit", params.get("limit"));
        response.put("sortBy", params.get("sortBy"));
        response.put("order", params.get("order"));
        response.put("total", total); 
        response.put("data", employees);

        return response;
    }
}
