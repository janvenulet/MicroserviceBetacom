<#include "header.ftl">

<div class="row">

  <div class="col-md-12 mt-1">
      <span class="float-right">
        <button class="btn btn-outline-warning" type="button" data-toggle="collapse"
                data-target="#editor" aria-expanded="false" aria-controls="editor">Add item</button>
        <a class="btn btn-outline-primary" href="/" role="button" aria-pressed="true">Log out</a>
      </span>
    <h1 class="display-4">
      <span class="text-muted">{</span>
    ${title}
      <span class="text-muted">}</span>
    </h1>
  </div>

  <div class="col-md-12 collapsable collapse clearfix" id="editor">
    <form action="/save" method="post">
      <div class="form-group">
        <input type="hidden" name="id" value="${id}">
        <input type="text" name="title" value="${title}">
        <input type="hidden" name="newPage" value="${newPage}">
      </div>
      <button type="submit" class="btn btn-primary">Save</button>
    <#if id != -1>
      <button type="submit" formaction="/delete" class="btn btn-danger float-right">Delete</button>
    </#if>
    </form>
  </div>

  <div class="col-md-12 mt-1">
  <#list items>
    <h2>Inventory:</h2>
    <ul>
      <#items as page>
        <li>${name}</li>
      </#items>
    </ul>
  <#else>
    <p>You don't have any items in your inventor.</p>
  </#list>
  </div>

</div>

<#include "footer.ftl">
