/*
 *Copyright 2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.uchicago.duo.security;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

enum DUOAuthority implements GrantedAuthority {
    ROLE_ANONYMOUS,
    ROLE_USER;

    @Override
    public String getAuthority() {
        return name();
    }
}
/**
 *
 * @author abrook
 */
public class DUOGrantedAUthoritiesMapper implements GrantedAuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<DUOAuthority> roles;
        roles = EnumSet.noneOf(DUOAuthority.class);
        boolean UserAccount = true;

        for (GrantedAuthority a: authorities) {
            if ("BSD$ ALL Service Accounts".equals(a.getAuthority())) {
                UserAccount = false;
            } else {
            }
        }
        if(UserAccount) {
            roles.add(DUOAuthority.ROLE_USER);
        } else {
            roles.add(DUOAuthority.ROLE_ANONYMOUS);
        }

        return roles;
    }
}
