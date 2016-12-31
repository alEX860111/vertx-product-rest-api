package net.brainified.http.products;

import javax.inject.Inject;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import net.brainified.db.Dao;
import net.brainified.db.Product;
import net.brainified.http.HandlerConfiguration;

@HandlerConfiguration(path = "/products/:id", method = HttpMethod.DELETE, requiresAuthentication = true)
final class DeleteProductHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteProductHandler.class);

  private final Dao<Product> dao;

  @Inject
  public DeleteProductHandler(final Dao<Product> dao) {
    this.dao = dao;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");

    dao.delete(id).subscribe(deleted -> {
      final int statusCode = deleted ? 204 : 404;
      routingContext.response().setStatusCode(statusCode).end();
    }, error -> {
      LOGGER.error(error.getMessage(), error);
      routingContext.response().setStatusCode(500).end();
    });
  }

}
