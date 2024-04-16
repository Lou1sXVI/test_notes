package com.wzy.game.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerPortConfig {
    @Value("server.port")
    String portString;

    public String getPortString() {
        return portString;
    }

    public void setPortString(String portString) {
        this.portString = portString;
    }
}
