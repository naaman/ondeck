package com.naamannewbold.ondeck.config;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public enum DB {

    PG("DATABASE_URL", "postgresql");

    private final String env;
    private final String user;
    private final String password;
    private final boolean useSSL;
    private final String protocol;
    private final String host;
    private final String dbName;
    private final int port;

    DB(String env, String protocol) {
        this.env = env;
        this.protocol = protocol;

        URI dbURL;
        try {
            dbURL = new URI(env(env, null));
        } catch (Exception e) {
            throw new RuntimeException("Invalid configuration. Check " + env + " is set appropriately.", e);
        }
        
        String[] dbUserInfo = dbURL.getUserInfo().split(":", 2);
        user = (dbUserInfo.length > 0) ? dbUserInfo[0] : null;
        password = (dbUserInfo.length > 1) ? dbUserInfo[1] : null;
        host = dbURL.getHost();
        dbName = dbURL.getPath();
        port = dbURL.getPort();

        // if DATABASE_SSL is set to "true", then use ssl, otherwise, don't
        useSSL = env("DATABASE_SSL", "false").equalsIgnoreCase("true");
    }

    public String jdbcUrl() {
        // parse DATABASE_URL into a jdbc string
        String jdbcConfigURL = "jdbc:" + protocol + "://" + host +
                ((port != -1) ? ":" + port : "") +
                ((dbName != null) ? dbName : "/" ) +
                "?user=" + user +
                "&password=" + password;

        // only use SSL if the env calls for it
        if (useSSL)
            jdbcConfigURL += "&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

        return jdbcConfigURL;
    }

    public EntityManagerFactory entityManagerFactory(String pu) {
        Map<String, String> madProps = new HashMap<String, String>();
        madProps.put("javax.persistence.jdbc.url", jdbcUrl());
        if (user != null) madProps.put("javax.persistence.jdbc.user", user);
        if (password != null) madProps.put("javax.persistence.jdbc.password", password);
        return Persistence.createEntityManagerFactory(pu, madProps);
    }

    private String env(String e, String def) {
        return (System.getenv(e) != null) ? System.getenv(e) : def;
    }

}
