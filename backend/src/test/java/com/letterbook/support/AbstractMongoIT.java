package com.letterbook.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AbstractMongoIT.Init.class)
public abstract class AbstractMongoIT {

    static final MongoDBContainer MONGO =
        new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    static { MONGO.start(); }

    public static class Init implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                "spring.data.mongodb.uri=" + MONGO.getReplicaSetUrl("letterbook_test"),
                "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-32bytes",
                "app.jwt.expiration-minutes=60",
                "app.cep.base-url=http://localhost:0"
            ).applyTo(ctx.getEnvironment());
        }
    }
}
