package com.employees.employees_be.routes.api.employees;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class index {

    @Autowired
    private create createHandler;

    @Autowired
    private update updateHandler;
    
    @Autowired
    private getEmployees getEmployeesHandler;

    @Autowired
    private getAll getAllHandler;

    @Autowired
    private delete deleteHandler;
    
    @Autowired
    private GetNthLevelManagerHandler getNthLevelManagerHandler;
    
    @Autowired
    private GetTopLevelHandler getTopLevelHandler;
    
    @Autowired
    private EditTopLevelEmployeeProfileHandler editTopLevelEmployeeProfileHandler;
    
    @Autowired
    private getHierarchyTree getHierarchyTreeHandler;
    
    @Autowired
    private clearDBHandler clearDBHandler;
    
    // Endpoint to create a new employee
    @PostMapping("/create")
    public Map<String, Object> createEmployee(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        return createHandler.handle(body);
    }

    // Endpoint to update an existing employee's credentials
    @PutMapping("/updateProfile")
    public Map<String, Object> updateEmployee(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        return updateHandler.handle(body);
    }
    
    // Endpoint to get all employees with pagination and sorting
    @GetMapping("/getEmployees")
    public Map<String, Object> getEmployees(HttpServletRequest req) {
        return getEmployeesHandler.handle(req);
    }

    // Endpoint to get all employees directly from MongoDB
    @GetMapping("/getAll")
    public Map<String, Object> getAllEmployees() {
        return getAllHandler.handle();
    }

    @DeleteMapping("/delete")
    public Map<String, Object> deleteEmployee(@RequestBody Map<String, Object> body) {
        return deleteHandler.handle(body);
    }

    @GetMapping("/getNthLevelManager")
    public Map<String, Object> getNthLevelManager(
            @RequestParam String e_id,
            @RequestParam int n
    ) {
        return getNthLevelManagerHandler.handle(e_id, n);
    }

    // Endpoint to get the top-level employee profile
    @GetMapping("/getTopLevelEmployee")
    public Map<String, Object> getTopLevelEmployee() {
        return getTopLevelHandler.handle();
    }

    // Endpoint to edit the top-level employee profile
    @PutMapping("/editTopLevelProfile")
    public Map<String, Object> editTopLevelEmployeeProfile(@RequestBody Map<String, Object> body) {
        return editTopLevelEmployeeProfileHandler.handle(body);
    }

    @GetMapping("/getHierarchyTree")
    public Map<String, Object> getHierarchyTree() {
        return getHierarchyTreeHandler.handle();
    }

    @PostMapping("/clearDB")
    public Map<String, Object> clearDatabase(@RequestBody Map<String, Object> body) {
        return clearDBHandler.handle(body);
    }

}
