package com.naamannewbold.ondeck.auth;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
public class AuthFilter implements ContainerRequestFilter {

    private final ConsumerManager manager;
    // right now, only supporting google auth
    private final String userSuppliedIdentifier = "https://www.google.com/accounts/o8/id";

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    public AuthFilter() throws ConsumerException {
        manager = new ConsumerManager();
    }

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        // check for openid as being present in the user's session. if it's not,
        // go through the authentication dance.
        if (!authenticated()) {
            try {
                // the user may have already authorized this app. attempt to verify their account. if it
                // fails, catch the exception and authenticate them.
                verify();
            } catch (OpenIDException e) {
                // authenicate the user since we are unable to verify the account.
                authenticate(containerRequest);
            }

            throw new UnsupportedOperationException("The user is not authenticated. " +
                    "Both verification and authentication with the Open ID provider has somehow failed, which shouldn't be possible.");
        }
        // user is already authenticated. pass them through.
        return containerRequest;
    }

    private boolean authenticated() {
        return (request.getSession().getAttribute("openid") != null);
    }

    private void authenticate(ContainerRequest containerRequest) {
        String returnURL = containerRequest.getRequestUri().toASCIIString();

        try {
            @SuppressWarnings("unchecked")
            List<DiscoveryInformation> discoveries = manager.discover(userSuppliedIdentifier);
            DiscoveryInformation discovered = manager.associate(discoveries);
            request.getSession().setAttribute("openid-disc", discovered);
            AuthRequest authRequest = manager.authenticate(discovered, returnURL);
            FetchRequest fetchRequest = FetchRequest.createFetchRequest();
            fetchRequest.addAttribute("email", "http://schema.openid.net/contact/email", true);
            authRequest.addExtension(fetchRequest);
            response.sendRedirect(authRequest.getDestinationUrl(true));
        } catch (OpenIDException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void verify() throws OpenIDException {
        try {
            ParameterList responseParams = new ParameterList(request.getParameterMap());

            DiscoveryInformation discovered = (DiscoveryInformation)request.getSession().getAttribute("openid-disc");

            StringBuffer receivingURL = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(request.getQueryString());

            VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    responseParams, discovered);

            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

                String openIdIdentifier = verified.getIdentifier();
                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

                    List emails = fetchResp.getAttributeValues("email");

                    if (emails.size() > 0)
                        request.getSession().setAttribute("email", (String)emails.get(0));
                }
                request.getSession().setAttribute("openid", openIdIdentifier);
                response.sendRedirect(authSuccess.getReturnTo());
            } else {
                throw new OpenIDException("Account not verified.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
