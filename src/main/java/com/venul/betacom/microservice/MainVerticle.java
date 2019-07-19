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
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class MainVerticle extends AbstractVerticle
{
	private MongoClient mongoClient;
	private FreeMarkerTemplateEngine templateEngine;
	private boolean firstTry = true;
	
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
		if (firstTry) 
			context.put("firstTry", "yes");
		else 
			context.put("firstTry", "no"); //sets "Wrong login or password! Try again" above input form
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
		String login = context.request().getParam("login");
		String password = context.request().getParam("password");
		System.out.println(login);
		System.out.println(password);
		if (false){
			context.response().setStatusCode(200);
		} else {
			firstTry = false;
			context.response().putHeader("Location", "/");
			context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
			context.response().end();
		}
	}

	private void signupHandler(RoutingContext context) {
		context.put("title", "Sign up");
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
		String login = context.request().getParam("login");
		String password = context.request().getParam("password");
		JsonObject user = new JsonObject().put("login", login).put("password", password);
		templateEngine.render(context.data(), "templates/index.ftl" , asyncResult -> {
			if (asyncResult.succeeded()) {
				context.response().putHeader("Content-Type", "text/html");
				context.response().end(asyncResult.result());
			} else {
				context.fail(asyncResult.cause());
			}
		});
	}
	
	private Future<Void> setupDatabase() {
		mongoClient = MongoClient.createShared(vertx, config()); //creates pool on the first call
		Future<Void> future = Future.future();
		future.complete();
		return future;
	}
	
}
