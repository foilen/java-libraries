/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Joiner;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * To help manipulating jdbc uri.
 */
public class JdbcUriTools {

    private String schema;
    private String username;
    private String password;
    private String server;
    private String database;
    private Map<String, String> options = new TreeMap<>();

    /**
     * Create a new instance from a uri.
     *
     * @param uriText the uri
     */
    public JdbcUriTools(String uriText) {

        if (!uriText.startsWith("jdbc:")) {
            throw new SmallToolsException("JDBC URI must start with 'jdbc:'");
        }

        uriText = uriText.substring(5);

        try {
            URI uri = new URI(uriText);

            schema = uri.getScheme();

            String uriAuthority = uri.getAuthority();
            String[] authServer = uriAuthority.split("@");
            if (authServer.length == 1) {
                server = authServer[0];
            } else {
                int serverPos = uriAuthority.lastIndexOf("@");
                String userAndPass = uriAuthority.substring(0, serverPos);
                String[] userAndPassParts = userAndPass.split("\\:");
                username = userAndPassParts[0];
                if (userAndPassParts.length > 1) {
                    password = userAndPassParts[1];
                }
                server = uriAuthority.substring(serverPos + 1);

            }

            String uriPath = uri.getPath();
            if (uriPath.length() > 1) {
                database = uriPath.substring(1);
            }

            String query = uri.getQuery();
            if (query != null) {
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(Joiner.on('\n').join(query.split("\\&")).getBytes()));
                properties.forEach((key, value) -> options.put(key.toString(), value.toString()));
            }

        } catch (Exception e) {
            throw new SmallToolsException("Bad uri", e);
        }
    }

    /**
     * Get the database name.
     *
     * @return the database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Get the options.
     *
     * @return the options
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Get the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the schema.
     *
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Get the server.
     *
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * Get the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the database name.
     *
     * @param database the database name
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Set the options.
     *
     * @param options the options
     */
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    /**
     * Set the password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the schema.
     *
     * @param schema the schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Set the server.
     *
     * @param server the server
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Set the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the uri.
     *
     * @return the uri
     */
    public String toUri() {
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:").append(schema).append("://");

        if (username != null) {
            sb.append(URLEncoder.encode(username, StandardCharsets.UTF_8));
            if (password != null) {
                sb.append(":");
                sb.append(URLEncoder.encode(password, StandardCharsets.UTF_8));
            }
            sb.append("@");
        }

        sb.append(server);

        if (database != null || !options.isEmpty()) {
            sb.append("/");
        }

        if (database != null) {
            sb.append(database);
        }

        if (!options.isEmpty()) {
            sb.append("?");
            AtomicBoolean first = new AtomicBoolean(true);
            options.forEach((name, value) -> {
                if (first.get()) {
                    first.set(false);
                } else {
                    sb.append("&");
                }
                sb.append(name).append("=").append(value);
            });
        }

        return sb.toString();
    }

}
