package io.slinkydeveloper.debtsmanager;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPoolOptions;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.slinkydeveloper.debtsmanager.services.UsersService;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public abstract class BaseTestWithEnvironment {

  public final static int POSTGRESQL_PORT = 5432;
  public final static int REDIS_PORT = 6379;

  public DockerComposeContainer environment =
    new DockerComposeContainer(new File("docker-compose-test-env.yml"))
      .withLocalCompose(true) // Using local compose that supports docker compose file 3
      .withExposedService("postgres", POSTGRESQL_PORT, Wait.forListeningPort())
      .withExposedService("redis", REDIS_PORT);

  @BeforeAll
  public void beforeAll(Vertx vertx, VertxTestContext testContext) throws Exception {
    environment.start();
  }

  @AfterAll
  public void afterAll() {
    environment.stop();
  }

}
