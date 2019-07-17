package com.venul.betacom.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class MainVerticle extends AbstractVerticle
{

	private FreeMarkerTemplateEngine templateEngine;
	
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
		
		router.get("/").handler(indexHandler()); //error possibility
		
		templateEngine = FreeMarkerTemplateEngine.create(vertx); //??? 
		server.requestHandler(router).listen(8092, asyncResult -> {
		if (asyncResult.succeeded()) {
			System.out.print("HTTP server running on port 8080");
			future.complete();
		} else {
			System.out.print("Could not start a HTTP server\n" + asyncResult.cause().toString());
			future.fail(asyncResult.cause());
		}
		});
		return future;
	}

	private Handler<RoutingContext> indexHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	private Future<Void> setupDatabase() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
