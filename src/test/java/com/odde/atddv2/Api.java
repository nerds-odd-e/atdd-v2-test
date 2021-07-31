package com.odde.atddv2;

import com.github.leeonky.jsonassert.PatternComparator;
import com.odde.atddv2.entity.User;
import com.odde.atddv2.repo.UserRepo;
import io.cucumber.java.Before;
import lombok.SneakyThrows;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class Api {
    public final static PatternComparator COMPARATOR = PatternComparator.defaultPatternComparator();
    private String response, token;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Before("@api-login")
    public void apiLogin() {
        User defaultUser = new User().setUserName("j").setPassword("j");
        userRepo.save(defaultUser);
        token = restTemplate.postForEntity(makeUri("/users/login"), defaultUser, User.class)
                .getHeaders().get("token").get(0);
    }

    public void get(String path) {
        response = restTemplate.exchange(RequestEntity.get(makeUri("/api/" + path))
                .header("Accept", "application/json").header("token", token)
                .build(), String.class).getBody();
    }

    @SneakyThrows
    public void responseShouldMatchJson(String json) {
//        JSONAssert.assertEquals(json, response, JSONCompareMode.NON_EXTENSIBLE);
        try {
            String responseBodyNoNewLine = json.replace('\n', ' ');
            JSONAssert.assertEquals("[" + responseBodyNoNewLine + "]", "[" + response + "]", COMPARATOR);
        } catch (Throwable t) {
            System.err.println("Expect:");
            System.err.println(json);
            System.err.println("Actual:");
            System.err.println(response);
            throw t;
        }
    }

    public void post(String path, Object body) {
        response = restTemplate.exchange(RequestEntity.post(makeUri("/api/" + path))
                .header("Accept", "application/json").header("token", token)
                .body(body), String.class).getBody();
    }

    @SneakyThrows
    private URI makeUri(String path) {
        return URI.create(String.format("http://127.0.0.1:%s%s", 10081, path));
    }
}