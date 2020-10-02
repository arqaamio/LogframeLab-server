package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.UserNotFoundException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.UserRepository;
import com.arqaam.logframelab.util.Logging;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Qualifier("arqaamUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService, Logging {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Retrieves user details for authentication by username
     *
     * @param username Username
     * @return User details of the user with username
     * @throws UserNotFoundException When user with username is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findByUsernameWithGroupMemberships(username);
        if (user.isEmpty()) {
            logger().error("Failed to retrieve user with username: {}", username);
            throw new UserNotFoundException();
        }
        return user.get();
    }

}
