/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Joiner;

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

    public String getDatabase() {
        return database;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getPassword() {
        return password;
    }

    public String getSchema() {
        return schema;
    }

    public String getServer() {
        return server;
    }

    public String getUsername() {
        return username;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String toUri() {
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:").append(schema).append("://");

        if (username != null) {
            try {
                sb.append(URLEncoder.encode(username, CharsetTools.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new SmallToolsException(e);
            }
            if (password != null) {
                sb.append(":");
                try {
                    sb.append(URLEncoder.encode(password, CharsetTools.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    throw new SmallToolsException(e);
                }
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
