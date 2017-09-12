/*
 * Copyright 2017 Russian Post
 *
 * This source code is Russian Post Confidential Proprietary.
 * This software is protected by copyright. All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or reverse engineer the software.
 * Otherwise this violation would be treated by law and would be subject to legal prosecution.
 * Legal use of the software provides receipt of a license from the right holder only.
 */
package org.silentpom;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.security.SecureRandom;

/**
 * Created by agundilovich on 12.09.2017.
 */
public class CommandHelloWorld extends HystrixCommand<String> {
    private final String name;
    final SecureRandom random = new SecureRandom();

    public CommandHelloWorld(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    @Override
    protected String run() throws InterruptedException {
        System.out.println("Command start: " + name);
        Thread.sleep(random.nextInt(500));
        System.out.println("Command finished: " + name);
        return "Hello " + name + "!";
    }
}
