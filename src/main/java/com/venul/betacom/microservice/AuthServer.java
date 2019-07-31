package com.venul.betacom.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class AuthServer extends AbstractVerticle {
	
	static private JWTAuth provider;
	private MongoClient mongoClient;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Future<Void> future = setupDatabase().compose(v -> setupHttpServer()).compose(v -> setupAuth());
		future.setHandler(asyncResult -> {
			if(asyncResult.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(asyncResult.cause().toString());
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
	
	private Future<Void> setupHttpServer() {
		Future <Void> future = Future.future();
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.post("/auth").handler(this::verifyUser);
		server.requestHandler(router).listen(8094, asyncResult -> {
			if (asyncResult.succeeded()) {
				System.out.print("Authorization Http server running on port 8094");
				future.complete();
			} else {
				System.out.print("Could not start a HTTP server\n" + asyncResult.cause().toString());
				future.fail(asyncResult.cause());
			}
		});
		return future;
	}
	
	private Future<Void> setupAuth(){
		JWTAuthOptions config = new JWTAuthOptions()
				.addPubSecKey(new PubSecKeyOptions()
						.setAlgorithm("HS256")
						.setPublicKey("keyboard cat")
						.setSymmetric(true));	
		provider = JWTAuth.create(vertx, config);
		Future<Void> future = Future.future();
		future.complete();
		return future;
	}
	
	private void verifyUser(RoutingContext context) {
		String login = context.request().getParam("loginId");
		String password = context.request().getParam("passwordId");
		JsonObject query = new JsonObject().put(("username"), login).put("password", password);
		mongoClient.find("users", query, result -> {
			if(result.succeeded()) {
				if (result.result().isEmpty()) {
					context.response().putHeader("Location", "/");
					context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
					context.response().end();
				} else {
				    JsonObject json = result.result().get(0);
					String token = provider.generateToken(new JsonObject().put("sub", login), new JWTOptions());
					System.out.println(token);
					provider.authenticate(new JsonObject().put("jwt", token), resultHandler -> {
						if (resultHandler.succeeded()) {
							JsonObject body = new JsonObject().put("access_token", token).put("token_type", "Bearer");
//						  	context.response().putHeader("Content-Type", "application/json;charset=UTF-8");
//						  	context.response().putHeader("Cache-Control", "no-store"); //required by rfc6749 OAuth2.0 Protocol
//						  	context.response().putHeader("Pragma", "no-cache"); //required by rfc6749 OAuth2.0 Protocol
							context.response().putHeader("Location", "/user/" + json.getString("username") + "#access_token=" + token);
							context.response().setStatusCode(301);
//							context.response().putHeader("token", "Bearer " + token);
//							context.response().putHeader("Content-Length", Integer.toString(body.toString().length()));
//							System.out.println(body.toString());
//							context.response().write(body.toString()); //Redirection http response(3xx) - 301 moved permanently
							context.response().end();
						} else {
							  resultHandler.cause().printStackTrace();
						}
					});
				}
			} else {
				result.cause().printStackTrace();
			}
		});
	}
	
}
