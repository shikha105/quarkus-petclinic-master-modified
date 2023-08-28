package org.quarkus.samples.petclinic.system;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.net.URI;
import java.util.Optional;

//This class intercepts all HTTP requests and checks its cookies
//to make sure the request is valid
public class Filters {

    @ServerRequestFilter
    public Optional<RestResponse<Void>> getFilter(ContainerRequestContext ctx) {

        //Redirects all the web pages(except login and register) to login page if authCookie is not present
        if (!ctx.getCookies().containsKey("authCookie") && ctx.getMethod().equals(HttpMethod.GET) && (!ctx.getUriInfo().getPath().equals("/")
                && !ctx.getUriInfo().getPath().equals("/register/new"))) {
            RestResponse response = RestResponse.temporaryRedirect(URI.create(ctx.getUriInfo().getBaseUri().getPath()));
            return Optional.of(response);
        }

        //Redirects to welcome page if logged in user goes to login or registration page
        if (ctx.getCookies().containsKey("authCookie") && (ctx.getUriInfo().getPath().equals("/")
                || ctx.getUriInfo().getPath().equals("/register/new"))) {
            RestResponse response = RestResponse.temporaryRedirect(URI.create(ctx.getUriInfo().getBaseUri().getPath() + "welcome"));
            return Optional.of(response);
        }
        //Deletes all cookies when we logout and redirects us to the login page
        else if (ctx.getCookies().containsKey("authCookie") && ctx.getUriInfo().getPath().equals("/logout")) {
            Cookie cookie = ctx.getCookies().get("authCookie");
            NewCookie deletedCookie = new NewCookie(cookie.getName(), "", "/", cookie.getDomain(),
                    cookie.getVersion(), "", 0, null, false, false);
            ctx.getHeaders().add("Set-Cookie", deletedCookie.toString());
            RestResponse response = RestResponse.temporaryRedirect(URI.create(ctx.getUriInfo().getBaseUri().getPath()));
            response.getHeaders().add("Set-Cookie", deletedCookie.toString());
            return Optional.of(response);
        }
        return Optional.empty();
    }
}
