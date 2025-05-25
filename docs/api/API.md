# SheetCell API Documentation

This document describes the REST API endpoints for the SheetCell server application.

## Base URL
```
http://localhost:8080/sheetcell/api
```

## Authentication
All API endpoints require user authentication. Include the username in the request headers:
```
X-Username: your-username
```

## Endpoints

### User Management

#### Login
```http
POST /users/login
Content-Type: application/json

{
  "username": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "sessionId": "string"
}
```

### Sheet Management

#### Get All Sheets
```http
GET /sheets
```

**Response:**
```json
{
  "sheets": [
    {
      "id": "string",
      "name": "string",
      "owner": "string",
      "dimensions": {
        "rows": 50,
        "columns": 20
      },
      "permission": "OWNER|WRITER|READER",
      "lastModified": "2024-01-01T12:00:00Z"
    }
  ]
}
```

#### Upload Sheet
```http
POST /sheets/upload
Content-Type: multipart/form-data

file: [XML file]
```

#### Get Sheet Data
```http
GET /sheets/{sheetId}
```

#### Update Cell
```http
PUT /sheets/{sheetId}/cells/{cellId}
Content-Type: application/json

{
  "value": "string",
  "version": 1
}
```

### Permissions

#### Request Permission
```http
POST /sheets/{sheetId}/permissions/request
Content-Type: application/json

{
  "permission": "WRITER|READER"
}
```

#### Manage Permissions (Owner only)
```http
PUT /sheets/{sheetId}/permissions/{username}
Content-Type: application/json

{
  "permission": "WRITER|READER",
  "approved": true
}
```

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Data Models

### Sheet
```json
{
  "id": "string",
  "name": "string",
  "owner": "string",
  "dimensions": {
    "rows": "number",
    "columns": "number"
  },
  "cells": {
    "A1": {
      "originalValue": "string",
      "effectiveValue": "string",
      "lastModifiedBy": "string",
      "version": "number"
    }
  },
  "version": "number"
}
```

### Cell
```json
{
  "id": "string",
  "originalValue": "string",
  "effectiveValue": "string",
  "lastModifiedBy": "string",
  "version": "number",
  "dependencies": ["string"],
  "dependents": ["string"]
}
```

### Permission
```json
{
  "username": "string",
  "permission": "OWNER|WRITER|READER",
  "approved": "boolean",
  "requestedAt": "string",
  "approvedAt": "string"
}
``` 