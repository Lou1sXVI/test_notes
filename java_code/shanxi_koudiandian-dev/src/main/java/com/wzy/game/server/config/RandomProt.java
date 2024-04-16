package com.wzy.game.server.config;

import com.wzy.game.server.util.ServerPortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

//设置随机端口需要启动的类

public class RandomProt {
    private Logger logger = LoggerFactory.getLogger(RandomProt.class);
    public RandomProt(String args[]) {
        boolean isServerPort = false;
        String serverPort = "";
        if (null != args && 0 < args.length) {
            for (String arg : args) {
                if (StringUtils.hasText(arg) && arg.startsWith("server.port")) {
                    isServerPort = true;
                    serverPort = arg;
                    break;
                }
            }
        }
        int port = 0;
        //没指定端口随机生成
        if (!isServerPort) {
            port = new ServerPortUtil().getAvailblePort();
            logger.info("===========当前端口是:" + port);
            System.setProperty("server.port", String.valueOf(port));
        }
        else{
            logger.info("===========当前端口是:" + serverPort.split("=")[1]);
            System.setProperty("server.port", serverPort.split("=")[1]);
        }
    }
}