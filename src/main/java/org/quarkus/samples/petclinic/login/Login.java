package org.quarkus.samples.petclinic.login;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.FormParam;
import javax.persistence.Entity;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.elytron.security.common.BcryptUtil;


import java.util.List;


@Entity
@Table(name = "users")
public class Login extends PanacheEntity {


    @Column(name = "email")
    @NotEmpty
    @FormParam("email")
    public String email;

    @Column(name = "password")
    @NotEmpty
    @FormParam("password")
    public String password;

    @Override
    public String toString() {
        return "Login [email=" + email + ", password=" + password + "]";
    }

    public static Login findByEmail(String email) {

        List<Login> loginDetails = Login.listAll();

        for (Login login : loginDetails) {
            if (BcryptUtil.matches(email, login.email)) {
                return login;
            }
        }
        return new Login();
    }

}
