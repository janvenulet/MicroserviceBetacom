<#include "header.ftl">

<div class="row">

  <div class="col-md-12 mt-1">
      <span class="float-right">
        <button class="btn btn-outline-warning" type="button" data-toggle="collapse"
                data-target="#editor" aria-expanded="false" aria-controls="editor">Add item</button>
        <a class="btn btn-outline-primary" href="/" role="button" aria-pressed="true">Log out</a>
      </span>
    <h1 class="display-4">
      <span class="text-muted">Welcome back </span>
    ${user}
      <span class="text-muted">!</span>
    </h1>
  </div>

  <div class="col-md-12 mt-1">
  <h2>Inventory:</h2>
  <#list items>
    <ul>
      <#items as name>
        <li>${name}</li>
      </#items>
    </ul>
  <#else>
    <p>You don't have any items in your inventor.</p>
  </#list>
  </div>


 <div class="col-md-12 collapsable collapse clearfix" id="editor">
    <form action="/save" method="post">
      <div class="form-group">
        <input type="hidden" name="owner" value="${user}">
        <input type="text" name="name" placeholder="New item">
      </div>
      <button type="submit" class="btn btn-primary">Add item</button>

    </form>
  </div>


</div>

<#include "footer.ftl">
