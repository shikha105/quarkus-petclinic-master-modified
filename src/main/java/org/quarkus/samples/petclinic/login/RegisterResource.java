package org.quarkus.samples.petclinic.login;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.elytron.security.common.BcryptUtil;

import io.quarkus.qute.TemplateInstance;
import org.quarkus.samples.petclinic.system.Constants;
import org.quarkus.samples.petclinic.system.TemplatesLocale;

@Path("/register")
public class RegisterResource {

    @Inject
    TemplatesLocale templates;
    @Inject
    Validator validator;

    @GET
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return templates.register(Constants.REGISTER_RESOURCE_DEFAULT_MESSAGE, new HashMap<>());
    }


    @POST
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    /**
     * Renders the register.html
     *
     * @return
     */
    public TemplateInstance processCreationForm(@BeanParam Login register) {

        final Set<ConstraintViolation<Login>> violations = validator.validate(register);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {

            for (ConstraintViolation<Login> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return templates.register(Constants.REGISTER_RESOURCE_VALIDATION_FAILED, errors);

        } else {
            //Check if e-mail already registered

            Login login = Login.findByEmail(register.email);

            if (!(login.email == null) && BcryptUtil.matches(register.email, login.email)) {
                return templates.register(Constants.REGISTER_RESOURCE_ALREADY_REGISTERED, errors);
            }

            Login encryptedRegister = encrypt(register);
            encryptedRegister.persist();
            return templates.register(Constants.REGISTER_RESOURCE_REGISTRATION_SUCCESS, errors);
        }
    }

    private Login encrypt(Login toEncrypt) {
        Login encrypted = new Login();
        encrypted.email = BcryptUtil.bcryptHash(toEncrypt.email);
        encrypted.password = BcryptUtil.bcryptHash(toEncrypt.password);
        return encrypted;
    }


}
