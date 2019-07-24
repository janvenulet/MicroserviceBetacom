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

  <div class="col-md-12 mt-1">
  <#list items>
    <h2>Inventory:</h2>
    <ul>
      <#items as name>
        <li>${name}</li>
      </#items>
    </ul>
  <#else>
    <p>You don't have any items in your inventor.</p>
  </#list>
  </div>

</div>

<#include "footer.ftl">
