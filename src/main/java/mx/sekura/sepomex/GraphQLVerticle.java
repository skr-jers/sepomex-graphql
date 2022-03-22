package mx.sekura.sepomex;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.*;
import mx.sekura.sepomex.models.Address;
import mx.sekura.sepomex.models.ZipCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GraphQLVerticle {

    private final Vertx vertx;

    public GraphQLVerticle(Vertx vertx) {
        this.vertx = vertx;
    }

    public GraphQL setupGraphQL() {
        System.out.println("Iniciando graphQL");
        //Leer el schema
        String schema = vertx.fileSystem().readFileBlocking("addresses.graphql").toString();
        System.out.println("el schema es: " + schema);
        //Definiciones de types
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        DataFetcher<CompletionStage<ZipCode>> allAddresses = env ->{
            System.out.println("Entre");
            CompletableFuture<ZipCode> completableFuture = new CompletableFuture<>();


            String code = env.getArgument("code");
            System.out.println("El codigo postal a buscar es: " + code);
            //TODO: Hacer conexión a BD aquí?
            this.queryAddresses( completableFuture, code);



            return completableFuture;
        };

        //RuntimeWiring indica como resolver los types y fetch data
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("zipcode", allAddresses))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private void queryAddresses(CompletableFuture<ZipCode> completableFuture, String code) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("sepomex")
                .setUser("flex")
                .setPassword("password");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        Pool client = Pool.pool(vertx, connectOptions, poolOptions);

        client
                .preparedQuery("SELECT codigo, asentamiento, municipio, estado FROM sepomex WHERE codigo=?;")
                .execute( Tuple.of(code), ar -> {
                    if (ar.succeeded()) {
                        RowSet<Row> result = ar.result();
                        System.out.println("Got " + result.size() + " rows ");
                        ZipCode zipCode = new ZipCode(code);
                        List<Address> list = new ArrayList<>();
                        for (Row row: result) {
                            Address address = new Address(
                                    row.getInteger("codigo").toString(),
                                    row.getString("asentamiento"),
                                    row.getString("municipio"),
                                    row.getString("estado")
                            );
                            list.add(address);
                        }
                        zipCode.setAddresses(list);

                        completableFuture.complete(zipCode);
                    } else {
                        System.out.println("Failure: " + ar.cause().getMessage());
                        completableFuture.completeExceptionally(ar.cause());
                    }

                    // Now close the pool
                    client.close();
                });
    }

}
