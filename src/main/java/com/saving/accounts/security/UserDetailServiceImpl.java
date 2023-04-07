package com.saving.accounts.security;

import com.saving.accounts.model.User;
import com.saving.accounts.repository.iUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final iUserRepository userRepository;

    public UserDetailServiceImpl(iUserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String identification) throws UsernameNotFoundException {
        User user = userRepository
                .findOneByIdentification(identification)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + identification + "no existe..."));

        return new UserDetailsImpl(user);
    }
}
