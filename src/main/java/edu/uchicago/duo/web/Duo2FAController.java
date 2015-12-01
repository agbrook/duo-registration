/**
 * Copyright 2014 University of Chicago
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
 * Author: Daniel Yu <danielyu@uchicago.edu>
 */
package edu.uchicago.duo.web;

import com.duosecurity.duoweb.DuoWeb;
import com.duosecurity.duoweb.DuoWebException;
import edu.uchicago.duo.domain.DuoAllIntegrationKeys;
import edu.uchicago.duo.domain.DuoPersonObj;
import edu.uchicago.duo.security.DUOGrantedAuthoritiesMapper;
import edu.uchicago.duo.security.DuoUserDetails;
import edu.uchicago.duo.service.DuoObjInterface;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping("/secure/2fa")
@SessionAttributes("DuoPerson")
public class Duo2FAController {

	protected final Log logger = LogFactory.getLog(getClass());
	private JSONObject result = null;
	private Date date = new Date();
	String name = null;
        //
        @Autowired(required = true)
        private DuoAllIntegrationKeys duoAllIKeys;
        //
	@Autowired
	private DuoObjInterface duoUsrService;
	//
	@Autowired
	private DuoObjInterface duoPhoneService;
	//
	@Autowired
	private DuoObjInterface duoTabletService;
	//
	@Autowired
	private DuoObjInterface duoTokenService;

        @RequestMapping(method = RequestMethod.GET)
	public String initForm(HttpServletRequest request, Principal principal, ModelMap model, @ModelAttribute DuoPersonObj duoperson, HttpSession session, SessionStatus status) {
            String userId = null;
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DuoUserDetails activeUser = (DuoUserDetails)auth.getPrincipal();
            duoperson.setUsername(activeUser.getUsername());
            
            if (session.getAttribute("duoUserId") == null) {
		userId = duoUsrService.getObjByParam(duoperson.getUsername(), null, "userId");
		if (userId == null) {
                    logger.info("2FA Info - Username:" + duoperson.getUsername() + " has not yet register with DUO!");
                    List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
                    authorities.add(DUOGrantedAuthoritiesMapper.DUOAuthority.ROLE_DUOAUTH);
                    Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),authorities);
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    logger.info("2FA Info - Adding ROLE_DUOAUTH to " + duoperson.getUsername());
                    return "redirect:/secure";		//return form view
		}
		logger.debug("2FA Debug - "+"Assigned UserID via DUO API Query");
            } else {
		userId = session.getAttribute("duoUserId").toString();
		logger.debug("2FA Debug - Assigned UserID via Session Variable");
            }
            
            duoperson.setUser_id(userId);
            duoperson.setPhones(duoPhoneService.getAllPhones(userId));
            duoperson.setTablets(duoTabletService.getAllTablets(userId));
            duoperson.setTokens(duoTokenService.getAllTokens(userId));

            //Initalize Model with some variables and push that into SessionAttribute
            model.addAttribute("DuoPerson", duoperson);

            if (duoperson.getPhones().isEmpty() && duoperson.getTablets().isEmpty() && duoperson.getTokens().isEmpty()) {
                
                List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
                authorities.add(DUOGrantedAuthoritiesMapper.DUOAuthority.ROLE_DUOAUTH);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),authorities);
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                logger.info("2FA Info - Adding ROLE_DUOAUTH to " + duoperson.getUsername());
		return "redirect:/secure";
            } else {        
                String DuoReq = DuoWeb.signRequest(duoAllIKeys.getAuthikeys().getIkey(), duoAllIKeys.getAuthikeys().getSkey(), duoAllIKeys.getAuthikeys().getAkey(), duoperson.getUsername());
                String DuoHost = duoAllIKeys.getAuthikeys().getHostkey();

                model.addAttribute("DuoReq",DuoReq);
                model.addAttribute("DuoHost",DuoHost);
                model.addAttribute("mintime", date);
                logger.info("2FA Info - Sending " + duoperson.getUsername() + " to DUO2FA screen");
                return "Duo2FA";
            }
	}
        
        @RequestMapping(method= RequestMethod.POST)
        public String Duo2FACheck(HttpServletRequest request, HttpServletResponse response, Principal principal, HttpSession session,
                @ModelAttribute DuoPersonObj duoperson,
                @RequestParam("signedDuoResponse") final String signedDuoResponse)
			throws ServletException, IOException, JSONException, Exception {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DuoUserDetails activeUser = (DuoUserDetails)auth.getPrincipal();

            logger.debug("2FA Debug - Signed response from Duo web form is: " + signedDuoResponse);
            
            if(signedDuoResponse == null) {
                logger.error("2FA Error - Received null response from Duo web form.  User possibly has an existing authN window open in another tab.");
                request.logout();
                return "redirect:/";
            }
            String DuoAPIResponse = null;
            try{
                DuoAPIResponse = DuoWeb.verifyResponse(duoAllIKeys.getAuthikeys().getIkey(), duoAllIKeys.getAuthikeys().getSkey(), duoAllIKeys.getAuthikeys().getAkey(), signedDuoResponse);
            }catch(DuoWebException | NoSuchAlgorithmException | InvalidKeyException dwe){
                logger.warn(dwe.getMessage(),dwe);
            }catch(IOException ioe){
                logger.error(ioe.getMessage(), ioe);
                throw new RuntimeException(ioe);
            }
            logger.debug("2FA DEBUG - Result of the verification of the response from Duo is: " + DuoAPIResponse);
            
            // If the response is null or doesn't equal the current username, then logout the user
            if(DuoAPIResponse == null || !DuoAPIResponse.equalsIgnoreCase(activeUser.getUsername())) {
                request.logout();
                return "redirect:/";
            } else {
                List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
                authorities.add(DUOGrantedAuthoritiesMapper.DUOAuthority.ROLE_DUOAUTH);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),authorities);
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                logger.info("2FA Info - Adding ROLE_DUOAUTH to " + activeUser.getUsername());
                return "redirect:/secure";
            }
        } 
}