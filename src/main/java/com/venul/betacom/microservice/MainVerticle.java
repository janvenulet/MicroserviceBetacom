package com.venul.betacom.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle
{

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Future<Void> steps = setupDatabase().compose(v -> setupHttpServer());
		steps.setHandler(asyncResult -> {
			if(asyncResult.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(asyncResult.cause().toString());
			}
		});
	}

	private Future<Void> setupHttpServer() {
		// TODO Auto-generated method stub
		return null;
	}

	private Future<Void> setupDatabase() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
