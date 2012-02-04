package com.naamannewbold.ondeck.controller;

import com.naamannewbold.ondeck.config.DB;
import com.naamannewbold.ondeck.model.User;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.List;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
@Path("/circle")
public class Circle {
    @Context
    HttpServletRequest request;

    @GET
    @Produces("text/plain")
    public String getMain() {
        return String.valueOf(request.getSession().getAttribute("openid") + "\n" + request.getSession().getAttribute("email"));
    }

}
