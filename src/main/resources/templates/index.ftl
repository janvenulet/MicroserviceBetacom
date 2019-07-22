<#include "header.ftl">

      <h1 class="display-4">${title}</h1>
        <#if title = "Log in">
        <div class="input-group">
          <form class="form-group" action="/login" method="post">
            <div class="form-group">
              <small class="form-text text-danger">${errorMessage}</small>
              <label for="usernameId">Username: </label>
              <input type="text" class="form-control" id="loginId" placeholder="Username" name="loginId">
            </div>
            <div class="form-group">
              <label for="passwordId">Password: </label>
              <input type="password" class="form-control" id="passwordId" placeholder="Password" aria-label="Password" name="passwordId">
            </div>
            <button type="submit" class="btn btn-primary">Log in</button>
          </form>
        </div>
	  <form action="/signup" method="get">
        <button type="submit" class="btn btn-primary" id="register-btn">Sign up</button>
      </form>
      </#if>
      <#if title = "Sign up">
        <div class="input-group">
          <form class="form-group" action="/register" method="post">
            <div class="form-group">
            <small class="form-text text-danger">${errorMessage}</small>
              <label for="usernameId">Username: </label>
              <input type="text" minlength="5" class="form-control" id="loginId" placeholder="Username" aria-label="Username" name="loginId" required>
            </div>
            <div class="form-group">
              <label for="passwordId">Password: </label>
              <input type="password" minlength="5" class="form-control" id="passwordId" placeholder="Password" aria-label="Password" name="passwordId" required>
              <small class="form-text text-muted">Password needs to be at least 5 characters.</small>	
            </div>
            <button type="submit" class="btn btn-primary">Sign up</button>
          </form>
        </div>
       	<form action="/" method="get">
        	<button type="submit" class="btn btn-primary" id="register-btn">
        	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>Go back
        	</button>
      	</form>
      </#if>
<#include "footer.ftl">
