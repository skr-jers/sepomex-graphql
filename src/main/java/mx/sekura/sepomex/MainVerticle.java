package mx.sekura.sepomex;

import graphql.GraphQL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx(); // (1)
    vertx.deployVerticle(new MainVerticle()); // (2)
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    System.out.println("Se inicio el verticle");
    GraphQL graphQL = new GraphQLVerticle(vertx).setupGraphQL();
    GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route("/graphql").handler(graphQLHandler);

    vertx.createHttpServer()
            .requestHandler(router)
            .listen(8666);
  }
}
