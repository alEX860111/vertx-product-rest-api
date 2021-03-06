package net.brainified.http;

import javax.inject.Inject;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.Router;

public final class HttpServerVerticle extends AbstractVerticle {

  private static final int DEFAULT_HTTP_PORT = 8080;

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private final Vertx vertx;

  private final Router router;

  @Inject
  public HttpServerVerticle(final Vertx vertx, final Router router) {
    this.vertx = vertx;
    this.router = router;
  }

  @Override
  public void start(final Future<Void> fut) {
    vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", DEFAULT_HTTP_PORT), result -> {
      if (result.succeeded()) {
        LOGGER.info(this.getClass() + " initialized and listening on port " + result.result().actualPort() + ".");
        fut.complete();
      } else {
        LOGGER.error(result.cause().getMessage());
        fut.fail(result.cause());
      }
    });
  }

}