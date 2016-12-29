package net.brainified.http.users;

import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import net.brainified.db.Dao;
import net.brainified.db.User;
import net.brainified.http.HandlerConfiguration;

@HandlerConfiguration(path = "/api/users", method = HttpMethod.POST)
final class AddUserHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddUserHandler.class);

  private static final String INVALID_JSON_IN_BODY = "Invalid JSON in body";

  private final Dao<User> dao;

  @Inject
  public AddUserHandler(final Dao<User> dao) {
    this.dao = dao;
  }

  @Override
  public void handle(final RoutingContext routingContext) {
    final String body = routingContext.getBodyAsString();

    AddUserRequest addUserRequest = null;
    try {
      addUserRequest = Json.decodeValue(body, AddUserRequest.class);
    } catch (final DecodeException e) {
      routingContext.response().setStatusCode(400).end(INVALID_JSON_IN_BODY);
      return;
    }

    final User user = new User();
    user.setUsername(addUserRequest.getUsername());
    user.setPasswordHash(Hashing.sha1().hashString(addUserRequest.getPassword(), Charsets.UTF_8).toString());
    user.setRole(addUserRequest.getRole());

    dao.add(user).subscribe(savedUser -> {
      routingContext
        .response()
        .setStatusCode(201)
        .putHeader("Content-Type", "application/json; charset=utf-8")
        .putHeader("Location", routingContext.request().absoluteURI() + "/" + savedUser.get_id())
        .end(Json.encodePrettily(savedUser));
    }, error -> {
      LOGGER.error(error.getMessage());
      routingContext.response().setStatusCode(500).end();
    });

  }

}
