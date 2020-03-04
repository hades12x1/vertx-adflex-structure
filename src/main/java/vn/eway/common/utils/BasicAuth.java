package vn.eway.common.utils;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;

import java.io.File;

public class BasicAuth {

    public static AuthHandler createAuthShiro(Vertx vertx, JsonObject config) {
        JsonObject authConfigJsonObject = new JsonObject();
        authConfigJsonObject.put("properties_path", (new File(config.getString("locate.auth.config"))).toURI().toString());

        ShiroAuthOptions shiroAuthOptions = new ShiroAuthOptions();
        shiroAuthOptions.setType(ShiroAuthRealmType.PROPERTIES);
        shiroAuthOptions.setConfig(authConfigJsonObject);
        ShiroAuth shiroAuth = ShiroAuth.create(vertx, shiroAuthOptions);
        return BasicAuthHandler.create(shiroAuth);
    }
}
