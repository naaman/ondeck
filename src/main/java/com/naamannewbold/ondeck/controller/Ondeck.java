package com.naamannewbold.ondeck.controller;

import com.sun.jersey.api.core.PackagesResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
@ApplicationPath("")
public class Ondeck extends PackagesResourceConfig {
    public Ondeck() {
        super("com.naamannewbold.ondeck.controller");
    }
}
