package net.brainified.http;

import javax.inject.Inject;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import net.brainified.db.ProductDao;
import net.brainified.db.ProductData;

@HandlerConfiguration(path = "/api/products", method = HttpMethod.POST)
final class AddProductHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddProductHandler.class);

  private static final String INVALID_JSON_IN_BODY = "Invalid JSON in body";

  private final ProductDao dao;

  @Inject
  public AddProductHandler(final ProductDao dao) {
    this.dao = dao;
  }

  @Override
  public void handle(final RoutingContext routingContext) {
    final String body = routingContext.getBodyAsString();

    ProductData data = null;
    try {
      data = Json.decodeValue(body, ProductData.class);
    } catch (final DecodeException e) {
      routingContext.response().setStatusCode(400).end(INVALID_JSON_IN_BODY);
      return;
    }

    dao.addProduct(data).subscribe(savedProduct -> {
      routingContext
        .response()
        .setStatusCode(201)
        .putHeader("Content-Type", "application/json; charset=utf-8")
        .putHeader("Location", routingContext.request().absoluteURI() + "/" + savedProduct.get_id())
        .end(Json.encodePrettily(savedProduct));
    }, error -> {
      LOGGER.error(error.getMessage());
      routingContext.response().setStatusCode(500).end();
    });

  }

}
