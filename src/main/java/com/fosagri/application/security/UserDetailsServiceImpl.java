package com.fosagri.application.security;

import com.fosagri.application.model.Authority;
import com.fosagri.application.model.Utilisateur;
import com.fosagri.application.repository.UtilisateurRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public UserDetailsServiceImpl(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByUsernameWithAuthorities(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        if (!utilisateur.isEnabled()) {
            throw new UsernameNotFoundException("Le compte utilisateur est désactivé: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (utilisateur.getAuthorities() != null) {
            for (Authority authority : utilisateur.getAuthorities()) {
                String role = authority.getRole();
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }

        // Default role if no authorities
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(
            utilisateur.getUsername(),
            utilisateur.getPassword(),
            utilisateur.isEnabled(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        );
    }
}
