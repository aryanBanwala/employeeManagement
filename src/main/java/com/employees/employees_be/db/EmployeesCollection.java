package com.employees.employees_be.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;

@Service
public class EmployeesCollection {

    private final MongoCollection<Document> collection;

    public EmployeesCollection(MongoService mongoService) {
        this.collection = mongoService.getCollection("employees");
    }

    // ✅ Insert: Generate a UUID and set it as "e_id" in the document
    public String insert(Document data) {
        String uuid = UUID.randomUUID().toString();
        data.put("e_id", uuid);
        Date now = new Date();
        data.put("createdAt", now);
        data.put("updatedAt", now);
        collection.insertOne(data);
        return uuid;
    }
    
    
    //check for employee exists or not
    public boolean doesEidExist(String eId) {
        Document doc = collection.find(Filters.eq("e_id", eId)).first();
        return doc != null;
    }

    //to check whether some other employee with this phone exists
    public boolean doesPhoneNumberExistExcept(String phoneNumber, String eId) {
        Document doc = collection.find(
            Filters.and(
                Filters.eq("phoneNumber", phoneNumber),
                Filters.ne("e_id", eId)
            )
        ).first();
        return doc != null;
    }

    public Optional<String> getTopLevelEid() {
        Document doc = collection.find(Filters.eq("reportsTo", "-1")).first();
        if (doc != null) {
            String topEid = doc.getString("e_id");
            return Optional.ofNullable(topEid);
        }
        return Optional.empty();
    }

    public List<Document> getEmployees(Document query, int skip, int limit, Document sort) {
        return collection.find(query)
                         .sort(sort)
                         .skip(skip)
                         .limit(limit)
                         .into(new ArrayList<>());
     }

     // Reassign all subordinates to a new manager
    public long reassignSubordinates(String oldManagerId, String newManagerId) {
        return collection.updateMany(
            Filters.eq("reportsTo", oldManagerId),
            new Document("$set", new Document("reportsTo", newManagerId))
        ).getModifiedCount();
    }

    // ✅ Find employees who report to a specific manager
    public List<Document> findByReportsTo(String managerId) {
        return collection.find(Filters.eq("reportsTo", managerId)).into(new ArrayList<>());
    }

    // Fetch all employees without any filter
    public List<Document> getAll() {
        return collection.find(new Document()).into(new ArrayList<>());
    }

    //to check boss exists or not
    public boolean doesTopLevelExist() {
        Document doc = collection.find(Filters.eq("reportsTo", "-1")).first();
        return doc != null;
    }

    // EmployeesCollection.java
    public boolean doesPhoneNumberExist(String phoneNumber) {
        Document doc = collection.find(Filters.eq("phoneNumber", phoneNumber)).first();
        return doc != null;
    }


    // ✅ Find by e_id
    public Optional<Document> findById(String id) {
        Document doc = collection.find(Filters.eq("e_id", id)).first();
        return Optional.ofNullable(doc);
    }

    //nth level helper
    public Map<String, Object> getNthLevelManager(String e_id, int n) {
        Document employee = collection.find(Filters.eq("e_id", e_id)).first();
        
        if (employee == null) {
            return Map.of("error", "Employee not found", "e_id", e_id);
        }
    
        String currentManagerId = employee.getString("reportsTo");
        if (currentManagerId == null || currentManagerId.equals("-1")) {
            return Map.of(
                "error", "Employee does not have a manager",
                "e_id", e_id,
                "level_requested", n
            );
        }
    
        int level = 1;
        while (level < n && currentManagerId != null && !currentManagerId.equals("-1")) {
            Document manager = collection.find(Filters.eq("e_id", currentManagerId)).first();
    
            if (manager == null) {
                return Map.of(
                    "error", "Manager does not exist in hierarchy",
                    "missing_manager_id", currentManagerId,
                    "last_valid_manager", level - 1
                );
            }
    
            currentManagerId = manager.getString("reportsTo");
            level++;
        }
    
        // Final manager retrieval
        Document nthLevelManager = collection.find(Filters.eq("e_id", currentManagerId)).first();
        if (nthLevelManager == null) {
            return Map.of(
                "error", "Requested level exceeds hierarchy",
                "max_levels_available", level - 1,
                "requested_level", n
            );
        }
    
        return Map.of(
            "status", "success",
            "nth_level_manager", nthLevelManager,
            "level_found", level
        );
    }
    

    // ✅ Find many by query (unchanged)
    public List<Document> findMany(Document query) {
        return collection.find(query).into(new java.util.ArrayList<>());
    }

    // ✅ Update by e_id
    public boolean updateById(String id, Document updates) {
    updates.put("updatedAt", new Date());
    return collection.updateOne(
            Filters.eq("e_id", id),
            new Document("$set", updates)
    ).getModifiedCount() > 0;
}


    // ✅ Delete by e_id
    public boolean deleteById(String id) {
        return collection.deleteOne(Filters.eq("e_id", id)).getDeletedCount() > 0;
    }

    // ✅ Find one by any field
    public Optional<Document> findOneByField(String fieldName, Object value) {
        Document doc = collection.find(Filters.eq(fieldName, value)).first();
        return Optional.ofNullable(doc);
    }

    // ✅ Update many by query (unchanged)
    public long updateMany(Document query, Document updates) {
        return collection.updateMany(query, new Document("$set", updates)).getModifiedCount();
    }

    // ✅ Delete many by query (unchanged)
    public long deleteMany(Document query) {
        return collection.deleteMany(query).getDeletedCount();
    }
}
