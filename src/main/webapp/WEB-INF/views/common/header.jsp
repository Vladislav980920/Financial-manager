<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Family Finance Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">Family Finance Manager</a>
        <div class="navbar-collapse">
            <ul class="navbar-nav me-auto">
                <c:if test="${not empty sessionScope.user}">
                    <li class="nav-item">
                        <a class="nav-link" href="/views/dashboard">Dashboard</a>
                    </li>
                </c:if>
            </ul>
            <ul class="navbar-nav">
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <li class="nav-item">
                            <span class="nav-link">Welcome, ${sessionScope.user.username}</span>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/users/logout">Logout</a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link" href="/views/auth/login">Login</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/views/auth/register">Register</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
<div class="container mt-4">