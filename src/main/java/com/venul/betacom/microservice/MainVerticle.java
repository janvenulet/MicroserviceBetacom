package com.venul.betacom.microservice;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
//import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class MainVerticle extends AbstractVerticle
{
	private MongoClient mongoClient;
	private FreeMarkerTemplateEngine templateEngine;
	private String errorMessage = "";
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Future<Void> future = setupDatabase().compose(v -> setupHttpServer());
		future.setHandler(asyncResult -> {
			if(asyncResult.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(asyncResult.cause().toString());
			}
		});
	}

	private Future<Void> setupHttpServer() {
		Future <Void> future = Future.future();
		HttpServer server = vertx.createHttpServer();
		
		Router router = Router.router(vertx);
		
		router.get("/").handler(this::indexHandler); //error possibility
		router.get("/signup").handler(this::signupHandler);
		router.post().handler(BodyHandler.create());
		router.post("/register").handler(this::registerHandler);
		router.post("/login").handler(this::loginHandler);
		router.get("/user/:username").handler(this::userHandler);
		templateEngine = FreeMarkerTemplateEngine.create(vertx); //??? 
		server.requestHandler(router).listen(8092, asyncResult -> {
		if (asyncResult.succeeded()) {
			System.out.print("HTTP server running on port 8092");
			future.complete();
		} else {
			System.out.print("Could not start a HTTP server\n" + asyncResult.cause().toString());
			future.fail(asyncResult.cause());
		}
		});
		return future;
	}

	private void userHandler(RoutingContext context) {
		String login = context.request().getParam("username");
		JsonObject query = new JsonObject().put(("username"), login);
		mongoClient.find("users", query, result -> {
			if (result.succeeded()) {
				context.put("title", login);
				System.out.println(login);
				String id;
				if (!(result.result().get(0).getValue("_id") instanceof String)) 
					id = result.result().get(0).getString("_id").toString(); //here it fails for some reason
				else
					id = result.result().get(0).getString("_id");
				mongoClient.find("items", new JsonObject().put("owner", id), resultHandler -> {
					if (resultHandler.succeeded()) {
						if (!result.result().isEmpty()) {
							System.out.println(resultHandler.result().toString());
							ArrayList<String> items = new ArrayList<String>();
							for (JsonObject jsonObject : resultHandler.result()) {
								items.add(jsonObject.getString("name"));
							}
							System.out.println(items.toString());
							context.put("items", items.toString());
						} else {
							System.out.println("empty");
							context.put("items", "-1"); //no items assigned to particular account
						}
					} else {
						context.fail(resultHandler.cause());
					}
				});
			} else {
				context.fail(result.cause());
			}
            templateEngine.render(context.data(), "templates/page.ftl", ar -> {   // <3>
                if (ar.succeeded()) {
                  context.response().putHeader("Content-Type", "text/html");
                  context.response().end(ar.result());  // <4>
                } else {
                  context.fail(ar.cause());
                }
              });
			
		});
		
	}
	private void indexHandler(RoutingContext context) {
		context.put("title", "Log in");
		context.put("errorMessage", errorMessage); //sets "Wrong login or password! Please try again" above input form
		templateEngine.render(context.data(), "templates/index.ftl" , asyncResult -> {
			if (asyncResult.succeeded()) {
				context.response().putHeader("Content-Type", "text/html");
				context.response().end(asyncResult.result());
			} else {
				context.fail(asyncResult.cause());
			}
		});
	}
	
	
	private void loginHandler(RoutingContext context) {
		String login = context.request().getParam("loginId");
		String password = context.request().getParam("passwordId");
		JsonObject query = new JsonObject().put(("username"), login).put("password", password);
		mongoClient.find("users", query, result -> {
			if(result.succeeded()) {
				if (result.result().isEmpty()) {
					errorMessage = "Wrong login or password! Please try again.";
					context.response().putHeader("Location", "/");
					context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
					context.response().end();
				} else {
					System.out.println("Singing in");
				    JsonObject json = result.result().get(0);
				    errorMessage = "";
					context.response().putHeader("Location", "/user/" + json.getString("username"));
					context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
					context.response().end();
					return;
				}

			} else {
				result.cause().printStackTrace();
			}
		});
	}

	private void signupHandler(RoutingContext context) {
		context.put("title", "Sign up");
		if (errorMessage.equals("Wrong login or password! Please try again.")) errorMessage = "";
		context.put("errorMessage", errorMessage); //sets "Wrong login or password! Please try again" above input form
		templateEngine.render(context.data(), "templates/index.ftl" , asyncResult -> {
			if (asyncResult.succeeded()) {
				context.response().putHeader("Content-Type", "text/html");
				context.response().end(asyncResult.result());
			} else {
				context.fail(asyncResult.cause());
			}
		});
	}

	private void registerHandler(RoutingContext context) {
		String login = context.request().getParam("loginId");
		String password = context.request().getParam("passwordId");
		JsonObject query = new JsonObject().put("username", login);
		System.out.println(query.toString());
		mongoClient.find("users", query, result -> {
			if(result.succeeded()) {
				errorMessage = "";
				if (result.result().isEmpty()) {
					query.put("password", password);
					mongoClient.save("users", query, res -> {
						if(res.succeeded()) {
							context.response().putHeader("Location", "/" + login);
							context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
							context.response().end();
						} else {
							res.cause().printStackTrace();
						}
					});
				} else {
					errorMessage = "Account with given username already exist! Please try again. ";
					context.response().putHeader("Location", "/signup");
					context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
					context.response().end();
				}
			} else {
				result.cause().printStackTrace();
			}
			
		});
	}
	
	private Future<Void> setupDatabase() {
		config().remove("db_name");
		mongoClient = MongoClient.createShared(vertx, config().put("db_name","microservices")); //creates pool on the first call
		Future<Void> future = Future.future();
		future.complete();
		return future;
	}
	
}
