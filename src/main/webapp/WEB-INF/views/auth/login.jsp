<%@ include file="../common/header.jsp" %>
<h2>Login</h2>
<c:if test="${not empty error}">
    <div class="alert alert-danger">${error}</div>
</c:if>
<form action="/users/login" method="post">
    <div class="mb-3">
        <label for="username" class="form-label">Username</label>
        <input type="text" class="form-control" id="username" name="username" required>
    </div>
    <div class="mb-3">
        <label for="password" class="form-label">Password</label>
        <input type="password" class="form-control" id="password" name="password" required>
    </div>
    <button type="submit" class="btn btn-primary">Login</button>
</form>
<%@ include file="../common/footer.jsp" %>