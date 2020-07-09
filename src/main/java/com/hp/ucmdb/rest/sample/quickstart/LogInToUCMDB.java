/*
 * MicroFocus.com Inc.
 * Copyright(c) 2020 All Rights Reserved.
 */
package com.hp.ucmdb.rest.sample.quickstart;

import com.hp.ucmdb.rest.sample.utils.RestApiConnectionUtils;

import java.io.IOException;

/**
 * @author wusui
 * @version $Id: LoginUCMDB.java, 2020-07-07 3:58 PM wusui Exp $
 */
public class LogInToUCMDB {

    public static void main(String[] args) {
        if(args.length < 3){
            System.out.println("Parameters: hostname username password");
            System.exit(0);
        }

        String hostname = args[0];
        String username = args[1];
        String password = args[2];

        String rootURL = RestApiConnectionUtils.buildRootUrl(hostname, "8443",  false);

        // authenticate
        String token = null;
        try {
            token = RestApiConnectionUtils.loginServer(rootURL, username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(token == null || token.length() == 0){
            System.out.println("Can not log in to the UCMDB server. Check your serverIp, userName or password!");
            System.exit(0);
        }
        System.out.println("Login successful! You have got the token to access UCMDB: " + token);

    }

}
