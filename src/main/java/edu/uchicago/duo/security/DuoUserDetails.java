/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uchicago.duo.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 *
 * @author abrook
 */
public class DuoUserDetails extends User {

    // extra instance variables
    final String fullname;
    final String email;

    DuoUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
        boolean credentialsNonExpired, boolean accountNonLocked,
        Collection<GrantedAuthority> authorities, String fullname,
        String email) {

        super(username, password, enabled, accountNonExpired, credentialsNonExpired,
            accountNonLocked, authorities);

        this.fullname = fullname;
        this.email = email;
    }

    public String getFullName(){
        return fullname;
    }

    public String getEmail() {
        return email;
    }
}
