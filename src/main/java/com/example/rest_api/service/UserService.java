package com.example.rest_api.service;

import com.example.rest_api.database.model.users.RoleEntity;
import com.example.rest_api.database.model.users.UserEntity;
import com.example.rest_api.database.repository.users.RoleRepository;
import com.example.rest_api.database.repository.users.UserRepository;
import com.example.rest_api.security.AuthenticatedUser;
import com.example.rest_api.security.PasswordGeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService extends OidcUserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /* Authentication methods */
    /**
     * Classic Auth.
     * @param email
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByEmail(email)
                /* TODO: Find a way to communicated this to the user */
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return entity.toAuthenticatedUser();
    }

    /**
     * Used for oAuth Auth.
     * @param userRequest
     * @return
     * @throws OAuth2AuthenticationException
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegates to the default OidcUserService for the basic OidcUser
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        // Check if user exists in DB, if not, create it
        Optional<UserEntity> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setPassword(PasswordGeneratorUtil.generate());
            newUser.setIsOAuthAccount(true);
            newUser.setRoles(roleRepository.findAllByName("USER"));
            this.save(newUser);
            return newUser.toAuthenticatedUser();
        }

        AuthenticatedUser authenticatedUser = optUser.get().toAuthenticatedUser();
        authenticatedUser.setIdToken(oidcUser.getIdToken());
        return authenticatedUser;
    }

    /* Queries */
    public UserEntity save(UserEntity user) {
        /* Encrypt password */
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Boolean existsByEmail(String email){
        return this.userRepository.existsByEmail(email);
    }

    public List<UserEntity> findAll() {
        return this.userRepository.findAll();
    }
    public UserEntity findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));}
    public UserEntity findByEmail(String email)
    {
        return userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Email not found with email: "+email));
    }
    @Transactional(transactionManager = "usersTransactionManager")
    public void removeRoleFromUser(Long userId, Long roleId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Remove the role from the user
        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            role.getUsers().remove(user);

            // Save the changes in both entities
            userRepository.save(user);
            roleRepository.save(role);
        }
    }
    public  boolean hasRole(UserEntity user , String roleName)
    {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }
}
