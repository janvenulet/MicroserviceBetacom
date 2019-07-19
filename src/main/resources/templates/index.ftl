<#include "header.ftl">

      <h1 class="display-4">${title}</h1>
        <#if title = "Log in">
        <div class="input-group">
          <form class="form-group" action="/login" method="post" enctype="text/plain">
            <div class="form-group">
              <#if firstTry=="no"> <small class="form-text text-danger">Wrong login or password! Try again.</small> </#if> 
              <label for="usernameId">Username: </label>
              <input type="text" class="form-control" id="usernameId" placeholder="Username" aria-label="Username" name="login">
            </div>
            <div class="form-group">
              <label for="passwordId">Password: </label>
              <input type="password" class="form-control" id="passwordId" placeholder="Password" aria-label="Password" name="password">
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
          <form class="form-group" action="/register" method="post" enctype="text/plain">
            <div class="form-group">
              <label for="usernameId">Username: </label>
              <input type="text" class="form-control" id="usernameId" placeholder="Username" aria-label="Username" name="login" required>
            </div>
            <div class="form-group">
              <label for="passwordId">Password: </label>
              <input type="password" minlength="5" class="form-control" id="passwordId" placeholder="Password" aria-label="Password" name="password" required>
              <small class="form-text text-muted">Password needs to be at least 5 characters.</small>	
            </div>
            <button type="submit" class="btn btn-primary">Sign up</button>
          </form>
        </div>
      </#if>
<#include "footer.ftl">
