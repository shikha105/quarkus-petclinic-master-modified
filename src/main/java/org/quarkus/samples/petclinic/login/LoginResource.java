package org.quarkus.samples.petclinic.login;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.netty.util.internal.StringUtil;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.quarkus.samples.petclinic.system.Constants;
import org.quarkus.samples.petclinic.system.TemplatesLocale;
import io.quarkus.security.jpa.Password;


@Path("/")
public class LoginResource {

    @Inject
    TemplatesLocale templates;

    @Inject
    Validator validator;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return templates.login("", new HashMap<>());
    }

    @POST
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    /**
     * Renders the login.html
     *
     * @return
     */
    public TemplateInstance processCreationForm(@BeanParam Login login, RoutingContext routingContext) {

        final Set<ConstraintViolation<Login>> violations = validator.validate(login);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {

            for (ConstraintViolation<Login> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return templates.login(Constants.LOGIN_RESOURCE_LOGIN_FAILED, errors);

        } else {

            Login user = Login.findByEmail(login.email);
            if (StringUtil.isNullOrEmpty(user.email) || !BcryptUtil.matches(login.password, user.password)) {
                errors.put("violation.getPropertyPath().toString()", "violation.getMessage()");
                return templates.login(Constants.LOGIN_RESOURCE_LOGIN_FAILED, errors);
            }
            Cookie cookie = Cookie.cookie("authCookie", UUID.randomUUID() + "::" + login.email);
            cookie.setMaxAge(3600);
            cookie.setHttpOnly(true); // Make the cookie accessible only via HTTP
            routingContext.addCookie(cookie);
            return templates.welcome();
        }
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance logout() {
        return templates.login("", new HashMap<>());
    }


}
