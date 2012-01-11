package com.naamannewbold.ondeck.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
@Path("/circle")
public class Circle {
    @GET
    @Produces("text/plain")
    public String getMain() {
        return "On Deck Circles";
    }
}
