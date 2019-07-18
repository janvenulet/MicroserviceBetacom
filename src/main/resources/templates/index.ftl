<#include "header.ftl">

      <h1 class="display-4">Log in</h1>
        <div class="input-group">
          <form class="form-group" action="/login" method="post">
            <div class="form-group">
              <label for="usernameId">Username: </label>
              <input type="text" class="form-control" id="usernameId" placeholder="Username" aria-label="Username">
            </div>
            <div class="form-group">
              <label for="passwordId">Password: </label>
              <input type="password" class="form-control" id="passwordId" placeholder="Password" aria-label="Password">
            </div>
            <button type="submit" class="btn btn-primary">Log in</button>
          </form>
        </div>

<#include "footer.ftl">
