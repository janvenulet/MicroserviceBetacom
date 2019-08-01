package com.venul.betacom.microservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class MainVerticle extends AbstractVerticle
{
	private MongoClient mongoClient;
	private FreeMarkerTemplateEngine templateEngine;
	static private JWTAuth provider;
	private User user;
	private String token; 
	private String errorMessage = "";
	private String accessTokenResponseContent = "\"token_type\":\"jwt\",";
			//+ "\"expires_in\":3600,"
		    //+ "\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\","
		    //+ "\"example_parameter\":\"example_value\"";
	
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

	private Future<Void> setupHttpServer() {
		Future <Void> future = Future.future();
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.get("/").handler(this::indexHandler); //error possibility
		router.get("/signup").handler(this::signupHandler);
		router.get("/logout").handler(this::logoutHandler);
		router.get("/user/:username").handler(this::userHandler);
		router.post().handler(BodyHandler.create());
		router.post("/register").handler(this::registerHandler);
		router.post("/login").handler(this::loginHandler);
		router.post("/delete").handler(this::deleteItemHandler);
		router.post("/save").handler(this::addItemHandler);
		templateEngine = FreeMarkerTemplateEngine.create(vertx);
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
	
	private Future<Void> setupDatabase() {
		config().remove("db_name");
		mongoClient = MongoClient.createShared(vertx, config().put("db_name","microservices")); //creates pool on the first call
		Future<Void> future = Future.future();
		future.complete();
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
				    JsonObject json = result.result().get(0);
				    errorMessage = "";
					String token = provider.generateToken(new JsonObject().put("sub", login), new JWTOptions());
					System.out.println(token);
					provider.authenticate(new JsonObject().put("jwt", token), resultHandler -> {
						if (resultHandler.succeeded()) {
							user = resultHandler.result();
//							JsonObject body = new JsonObject().put("access_token", token).put("token_type", "Bearer");
							context.response().putHeader("Location", "/user/" + json.getString("username"));
							context.response().setStatusCode(301);
//						  	context.setUser(user);
//						  	context.response().putHeader("Content-Type", "application/json;charset=UTF-8");
//						  	context.response().putHeader("Cache-Control", "no-store"); //required by rfc6749 OAuth2.0 Protocol
//						  	context.response().putHeader("Pragma", "no-cache"); //required by rfc6749 OAuth2.0 Protocol
//							context.response().putHeader("WWW-Authenticate", "Bearer realm=\"Access to user data\"");
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
	
	private void userHandler(RoutingContext context) {
		String login = context.request().getParam("username");
		JsonObject query = new JsonObject().put(("username"), login);
		mongoClient.find("users", query, result -> {
			if (result.succeeded()) {
				context.put("title", "My Account");
				context.put("user", login);
				String id;
				if (!(result.result().get(0).getValue("_id") instanceof String)) 
					id = result.result().get(0).getString("_id").toString(); //here it fails for some reason
				else
					id = result.result().get(0).getString("_id");
				mongoClient.find("items", new JsonObject().put("owner", id), resultHandler -> {
					List<String> items = new ArrayList<String>();
					if (resultHandler.succeeded()) {
						if (!resultHandler.result().isEmpty()) {
							items = resultHandler.result().stream().map(json -> json.getString("name")).sorted().collect(Collectors.toList());
							context.put("items", items); 
							websiteRender(context, "templates/page.ftl");
						} else {
							context.put("items", items); //no items assigned to particular account	
							websiteRender(context, "templates/page.ftl");
						}
					} else {
						context.fail(resultHandler.cause());
					}
				});
			} else {
				context.fail(result.cause());
			}
		});
	}
	
	
	private void websiteRender(RoutingContext context, String filename) {
        templateEngine.render(context.data(), filename, ar -> {
            if (ar.succeeded()) {
              context.response().putHeader("Content-Type", "text/html");
              context.response().end(ar.result());
            } else {
              context.fail(ar.cause());
            }
          });
		}
	
	private void deleteItemHandler(RoutingContext context) {
		String owner = context.request().getParam("owner");
	    String name = context.request().getParam("name");
	    JsonObject query = new JsonObject().put(("username"), owner);
		mongoClient.find("users", query, result -> {
			if (result.succeeded()) {
				String id;
				if (!(result.result().get(0).getValue("_id") instanceof String)) 
					id = result.result().get(0).getString("_id").toString(); //here it fails for some reason
				else
					id = result.result().get(0).getString("_id");
			    JsonObject query2 = new JsonObject().put("owner", id).put("name", name);
			    mongoClient.removeDocument("items", query2, resultHandler -> {
			    	if(resultHandler.succeeded()) {
						context.response().putHeader("Location", "/user/" + owner);
						context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
						context.response().end();
			    	} else {
			    		 context.fail(resultHandler.cause());
			    	}
			    });
			} else {
				 context.fail(result.cause());
			}
		});
	}
	
	private void addItemHandler(RoutingContext context) {
	    String owner = context.request().getParam("owner"); 
	    String name = context.request().getParam("name");
	    JsonObject query = new JsonObject().put(("username"), owner);
		mongoClient.find("users", query, result -> {
			if (result.succeeded()) {
				String id;
				if (!(result.result().get(0).getValue("_id") instanceof String)) 
					id = result.result().get(0).getString("_id").toString(); //here it fails for some reason
				else
					id = result.result().get(0).getString("_id");
			    JsonObject query2 = new JsonObject().put("owner", id).put("name", name);
			    mongoClient.save("items", query2, resultHandler -> {
			    	if(resultHandler.succeeded()) {
						context.response().putHeader("Location", "/user/" + owner);
						context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
						context.response().end();
			    	} else {
			    		 context.fail(resultHandler.cause());
			    	}
			    });
			} else {
				context.fail(result.cause());
			}
		});
	}
	
	private void indexHandler(RoutingContext context) {
		context.put("title", "Log in");
		context.put("errorMessage", errorMessage); //sets "Wrong login or password! Please try again" above input form
		websiteRender(context, "templates/index.ftl");
	}
	
	private void logoutHandler(RoutingContext context) {
		user = null;
		context.response().putHeader("Location", "/");
		context.response().setStatusCode(301); //Redirection http response(3xx) - 301 moved permanently
		context.response().end();
	}

	private void signupHandler(RoutingContext context) {
		user = null;	
		context.put("title", "Sign up");
		if (errorMessage.equals("Wrong login or password! Please try again.")) errorMessage = "";
		context.put("errorMessage", errorMessage); //sets "Wrong login or password! Please try again" above input form
		websiteRender(context, "templates/index.ftl");
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
 
}
