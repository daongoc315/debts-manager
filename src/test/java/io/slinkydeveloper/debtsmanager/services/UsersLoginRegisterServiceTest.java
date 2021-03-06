package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.*;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.persistence.UserDao;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.slinkydeveloper.debtsmanager.services.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UsersService Test
 */
@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersLoginRegisterServiceTest extends BaseServicesTest {

  private PgPool pgClient;
  private UserDao userDao;
  private JWTAuth auth;

  @Override
  @BeforeAll
  public void beforeAll(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.beforeAll(vertx, testContext);
    pgClient = PgClient.pool(vertx,
      new PgPoolOptions()
        .setPort(environment.getServicePort("postgres", POSTGRESQL_PORT))
        .setHost(environment.getServiceHost("postgres", POSTGRESQL_PORT))
        .setDatabase("debts-manager")
        .setUser("postgres")
        .setPassword("postgres")
    );
    userDao = UserDao.create(pgClient);
    vertx.fileSystem().readFile("jwk.json", testContext.succeeding(buf -> {
      auth = JWTAuth.create(vertx, new JWTAuthOptions().addJwk(buf.toJsonObject()));
      usersService = UsersService.create(userDao, auth);
      testContext.completeNow();
    }));
}

  @BeforeEach
  public void before(VertxTestContext testContext) {
    wipeDb(pgClient).setHandler(testContext.succeeding(v -> testContext.completeNow()));
  }

  @Test
  public void registerTest(VertxTestContext test) {
    AuthCredentials credentials = new AuthCredentials("francesco", "francesco");
    AuthCredentials credentialsHashed = new AuthCredentials(credentials).hashPassword();

    usersService.register(credentials, new OperationRequest(), test.succeeding(operationResponse -> {
      test.verify(() -> {
        assertSuccessResponse("text/plain", operationResponse);
        assertNotNull(operationResponse.getPayload().toString());
      });
      pgClient.preparedQuery("SELECT \"password\" FROM \"user\" WHERE username=$1", Tuple.of(credentials.getUsername()), test.succeeding(rows -> {
        Row row = rows.iterator().next();
        test.verify(() -> {
          assertEquals(1, rows.rowCount());
          assertEquals(credentialsHashed.getPassword(), row.getString("password"));
        });
        test.completeNow();
      }));
    }));
  }

  @Test
  public void registerAlreadyExistingUserTest(VertxTestContext test) {
    registerBeforeTestLogin(new AuthCredentials("francesco", "francesco"), test)
      .setHandler(v -> {
        usersService.register(new AuthCredentials("francesco", "francesco"), new OperationRequest(), test.succeeding(operationResponse -> {
          test.verify(() -> {
            assertTextResponse(400, "Bad Request", "User francesco already exists", operationResponse);
          });
          test.completeNow();
        }));
      });
  }

  @Test
  public void loginTest(VertxTestContext test) {
    AuthCredentials credentials = new AuthCredentials("francesco", "francesco");
    registerBeforeTestLogin(credentials, test)
      .setHandler(v -> {
        usersService.login(credentials, new OperationRequest(), test.succeeding(operationResponse -> {
          test.verify(() -> {
            assertSuccessResponse("text/plain", operationResponse);
            assertNotNull(operationResponse.getPayload().toString());
          });
          test.completeNow();
        }));
      });
  }

  @Test
  public void loginTestWrongUsername(VertxTestContext test) {
    AuthCredentials credentials = new AuthCredentials("francesco", "francesco");
    registerBeforeTestLogin(credentials, test)
      .setHandler(v -> {
        usersService.login(credentials.setUsername("wrongUsername"), new OperationRequest(), test.succeeding(operationResponse -> {
          test.verify(() -> {
            assertTextResponse(400, "Bad Request", "Wrong username or password", operationResponse);
          });
          test.completeNow();
        }));
      });
  }

  @Test
  public void loginTestWrongPassword(VertxTestContext test) {
    AuthCredentials credentials = new AuthCredentials("francesco", "francesco");
    registerBeforeTestLogin(credentials, test)
      .setHandler(v -> {
        usersService.login(credentials.setPassword("wrongPassword"), new OperationRequest(), test.succeeding(operationResponse -> {
          test.verify(() -> {
            assertTextResponse(400, "Bad Request", "Wrong username or password", operationResponse);
          });
          test.completeNow();
        }));
      });
  }

}
