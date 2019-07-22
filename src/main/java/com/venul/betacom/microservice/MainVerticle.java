package com.venul.betacom.microservice;

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
					context.response().putHeader("Location", "/" + json.getString("username"));
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
					System.out.println(query.toString());
					mongoClient.save("users", query, res -> {
						if(res.succeeded()) {
							System.out.println("Saved user");
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
