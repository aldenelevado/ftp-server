package com.ajavac.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ftp服务器
 * Created by wyp0596 on 17/04/2017.
 */
@Component
public class MyFtpServer {

    private static final Logger logger = LoggerFactory.getLogger(MyFtpServer.class);

    private FtpServer ftpServer;

    @Value("${ftp.port:2121}")
    private int port;
    @Value("${ftp.username:admin}")
    private String username;
    @Value("${ftp.password:admin}")
    private String password;
    @Value("${ftp.home-dir:home}")
    private String homeDir;

    @PostConstruct
    public void start() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        // set the port of the listener
        factory.setPort(port);
        // replace the default listener
        serverFactory.addListener("default", factory.createListener());


        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager um = userManagerFactory.createUserManager();
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(800, 200));
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);
        user.setHomeDirectory(new File(homeDir).getAbsolutePath());
        user.setAuthorities(authorities);
        try {
            um.save(user);
        } catch (FtpException e) {
            logger.warn("ftp启动异常", e);
        }

        serverFactory.setUserManager(um);
        // start the ftpServer
        ftpServer = serverFactory.createServer();
        try {
            ftpServer.start();
        } catch (FtpException e) {
            logger.warn("ftp启动异常", e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("ftp启动成功,端口号:" + port);
        }
    }

    @PreDestroy
    public void stop() {
        if (ftpServer != null) {
            ftpServer.stop();
        }
    }

    public FtpServer getFtpServer() {
        return ftpServer;
    }
}