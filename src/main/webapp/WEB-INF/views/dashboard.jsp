<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="common/header.jsp" %>
<h1>Welcome to your Dashboard, ${sessionScope.user.username}!</h1>

<h2 class="mt-4">Your Families</h2>
<table class="table table-striped">
  <thead>
  <tr>
    <th>ID</th>
    <th>Name</th>
    <th>Members</th>
    <th>Actions</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach items="${families}" var="family">
    <tr>
      <td>${family.id}</td>
      <td>${family.name}</td>
      <td>${family.membersCount}</td>
      <td>
        <a href="/api/families/${family.id}" class="btn btn-sm btn-info">View</a>
      </td>
    </tr>
  </c:forEach>
  </tbody>
</table>

<%@ include file="common/footer.jsp" %>