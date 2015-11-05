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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 *
 * @author abrook
 */
public class DuoUserDetailsContextMapper implements UserDetailsContextMapper {
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public UserDetails mapUserFromContext(DirContextOperations dco, String string, Collection<? extends GrantedAuthority> authorities) {

        String fullname =  dco.getStringAttribute("name");
        String email = dco.getStringAttribute("mail");
        String username = dco.getStringAttribute("samaccountname");
        return new DuoUserDetails(username, " ", true, true, true, true, (Collection<GrantedAuthority>) authorities, fullname, email);
        // " " is used as password because null values can't be passed
        // Could or should this be a DuoPersonObj ?
    }

    @Override
    public void mapUserToContext(UserDetails ud, DirContextAdapter dca) {
        throw new UnsupportedOperationException("Only retrieving data from AD is currently supported");
    }
}
