package vp04.pantic.test3.App.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import vp04.pantic.test3.App.model.user.SecurityUser;
import vp04.pantic.test3.App.security.TokenUtils;
import vp04.pantic.test3.App.service.UserDetailsServiceImpl;
import vp04.pantic.test3.App.web.dto.LoginDTO;
import vp04.pantic.test3.App.web.dto.UserDTO;

@RestController
public class UserController {

	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Autowired
	TokenUtils tokenUtils;
	
	@RequestMapping(value = "/api/login", method = RequestMethod.POST)
	public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        try {
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					loginDTO.getUsername(), loginDTO.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails details = userDetailsService.loadUserByUsername(loginDTO.getUsername());
            return new ResponseEntity<String>(tokenUtils.generateToken(details), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<String>("Invalid login", HttpStatus.BAD_REQUEST);
        }
	}
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	@RequestMapping(value = "/api/register", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        SecurityUser user = new SecurityUser();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        
        user = userDetailsService.register(user, userDTO.getRoles());
        
        if (user != null) {
        	return new ResponseEntity<>(new UserDTO(user), HttpStatus.CREATED); 
        } else { // ako vec postoji korisnik sa tim korisnickim imenom
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }        
	}
}
