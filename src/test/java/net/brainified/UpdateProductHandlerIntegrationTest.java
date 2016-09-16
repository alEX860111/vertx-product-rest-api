package net.brainified;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class UpdateProductHandlerIntegrationTest {

  private Vertx vertx;

  private ProductService serviceMock;

  @Before
  public void setUp(TestContext context) {
    serviceMock = Mockito.mock(ProductService.class);
    final Injector injector = Guice.createInjector(Modules.override(new ApplicationModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProductService.class).toInstance(serviceMock);
      }
    }));
    vertx = injector.getInstance(Vertx.class);
    vertx.deployVerticle(injector.getInstance(HttpServerVerticle.class), context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testUpdateProduct(TestContext context) {
    final int id = 1;

    final JsonObject json = new JsonObject();
    json.put("name", "myProduct");
    json.put("price", 100);

    final Product product = new Product();
    product.setId(id);
    final ProductData data = new ProductData();
    data.setName("myProduct");
    data.setPrice(100);
    product.setData(data);
    when(serviceMock.updateProduct(eq(id), any(ProductData.class))).thenReturn(Future.succeededFuture(product));

    final Async async = context.async();

    vertx.createHttpClient().put(8080, "localhost", "/api/products/" + id, response -> {
      context.assertEquals(200, response.statusCode());
      response.handler(body -> {
        final Product resultProduct = Json.decodeValue(body.toString(), Product.class);
        context.assertEquals(product.getId(), resultProduct.getId());
        context.assertEquals(json.getString("name"), resultProduct.getData().getName());
        context.assertEquals(json.getInteger("price"), resultProduct.getData().getPrice());
        async.complete();
      });
    }).end(json.encode());
  }

  @Test
  public void testUpdateProduct_sendInvalidProductId(TestContext context) {
    final JsonObject json = new JsonObject();
    json.put("name", "myProduct");
    json.put("price", 100);

    final Async async = context.async();

    vertx.createHttpClient().put(8080, "localhost", "/api/products/x", response -> {
      context.assertEquals(400, response.statusCode());
      response.handler(body -> {
        context.assertEquals("Invalid id", body.toString());
        async.complete();
      });
    }).end(json.encode());
  }

  @Test
  public void testUpdateProduct_sendInvalidBody(TestContext context) {
    final Async async = context.async();

    vertx.createHttpClient().put(8080, "localhost", "/api/products/1", response -> {
      context.assertEquals(400, response.statusCode());
      response.handler(body -> {
        context.assertEquals("Invalid JSON in body", body.toString());
        async.complete();
      });
    }).end();
  }

  @Test
  public void testUpdateProduct_notFound(TestContext context) {
    final int id = 1;

    final JsonObject json = new JsonObject();
    json.put("name", "myProduct");
    json.put("price", 100);

    when(serviceMock.updateProduct(eq(id), any(ProductData.class))).thenReturn(Future.failedFuture("not found"));

    final Async async = context.async();

    vertx.createHttpClient().put(8080, "localhost", "/api/products/" + id, response -> {
      context.assertEquals(404, response.statusCode());
      async.complete();
    }).end(json.encode());
  }

}