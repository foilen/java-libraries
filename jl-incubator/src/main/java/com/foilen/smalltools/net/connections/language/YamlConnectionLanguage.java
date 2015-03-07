/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.language;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionLanguage;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * Communicate via Yaml messages.
 */
public class YamlConnectionLanguage implements ConnectionLanguage {

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> receiveMessage(Connection connection) {
        String ymlMsg = StreamsTools.readString(connection.getInputStream());
        Yaml yaml = new Yaml();
        return yaml.loadAs(ymlMsg, Map.class);
    }

    @Override
    public void sendMessage(Connection connection, Map<String, Object> message) {
        Yaml yaml = new Yaml();
        String ymlMsg = yaml.dump(message);
        StreamsTools.write(connection.getOutputStream(), ymlMsg);
    }

}
