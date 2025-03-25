# Employee Management System - Backend (Spring Boot & MongoDB) 🚀

## 📌 Overview

This is a backend service for an employee management system built using **Spring Boot** and **MongoDB**. It provides APIs to manage employees, their hierarchy, and related operations.

## 🔧 Prerequisites

Ensure you have the following installed:

- 🖥️ **Java 21**
- ⚙️ **Maven**
- 📦 **MongoDB**

## ⚙️ Installation & Setup

1. **Clone the repository:**

   ```sh
   git clone https://github.com/your-repo/employees_be.git
   cd employees_be
   ```

2. **Configure MongoDB:**

   - Ensure MongoDB is running locally or update the connection details in `application.properties`.

3. **Build and Run:**

   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

   🚀 The server will start on **port 8081** by default.

---

## 🔹 Initial Setup (Important!)

Before using the system, you **must** create the top-level employee manually in MongoDB. This is the root user, representing the highest-level employee in the hierarchy.

Run the following query in your MongoDB shell:

```json
{
  "e_id": "root",
  "name": "CEO",
  "phoneNumber": "1234567890",
  "email": "ceo@example.com",
  "reportsTo": "-1",
  "createdAt": ISODate("2024-03-26T00:00:00Z"),
  "updatedAt": ISODate("2024-03-26T00:00:00Z")
}
```

Without this root employee, hierarchy-based operations (like fetching managers) will not work.

---

## 📌 API Endpoints

### 🔐 Authentication Middleware

All API requests must include a header:

```sh
secret: fally
```

Otherwise, the request will be rejected with `403 Forbidden`.

---

### 📂 Employee Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| 🟢 `GET` | `/api/employees/getEmployees` | Get paginated employees |
| 🟢 `GET` | `/api/employees/getAll` | Get all employees from MongoDB |
| 🟢 `POST` | `/api/employees/create` | Add a new employee |
| 🟡 `PUT` | `/api/employees/updateProfile` | Update an employee’s profile |
| 🔴 `DELETE` | `/api/employees/delete` | Delete an employee |

---

### 📊 Hierarchy Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| 🟢 `GET` | `/api/employees/getNthLevelManager?e_id=xyz&n=2` | Get Nth level manager |
| 🟢 `GET` | `/api/employees/getTopLevelEmployee` | Get the top-level employee |
| 🟡 `PUT` | `/api/employees/editTopLevelProfile` | Edit the top-level employee profile |
| 🟢 `GET` | `/api/employees/getHierarchyTree` | Get the full hierarchy tree |

---

## 🛠️ Technologies Used

- 🖥️ **Spring Boot 3.4.4**
- 📦 **MongoDB**
- ✨ **Lombok**
- 🌐 **Spring Web**
- ⚙️ **Maven**

---

## 📜 License

This project is for **internal use only**. No external licensing applies.

👨‍💻 **Thank you for using this project!** If you have any questions or suggestions, feel free to contribute or reach out. 🚀
