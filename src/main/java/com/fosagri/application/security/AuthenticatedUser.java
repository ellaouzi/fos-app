package com.fosagri.application.security;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.Utilisateur;
import com.fosagri.application.repository.UtilisateurRepository;
import com.fosagri.application.service.AdhAgentService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthenticationContext authenticationContext;
    private final AdhAgentService adhAgentService;

    public AuthenticatedUser(AuthenticationContext authenticationContext,
                            UtilisateurRepository utilisateurRepository,
                            AdhAgentService adhAgentService) {
        this.utilisateurRepository = utilisateurRepository;
        this.authenticationContext = authenticationContext;
        this.adhAgentService = adhAgentService;
    }

    @Transactional
    public Optional<Utilisateur> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
            .flatMap(userDetails -> utilisateurRepository.findByUsernameWithAuthorities(userDetails.getUsername()));
    }

    @Transactional
    public Optional<AdhAgent> getLinkedAgent() {
        Optional<Utilisateur> userOpt = get();
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        Utilisateur user = userOpt.get();

        // Try to find agent by adhid first
        if (user.getAdhid() != null && !user.getAdhid().trim().isEmpty()) {
            Optional<AdhAgent> agent = adhAgentService.findByIdAdh(user.getAdhid());
            if (agent.isPresent()) {
                return agent;
            }
        }

        // Try to find by CIN
        if (user.getCin() != null && !user.getCin().trim().isEmpty()) {
            Optional<AdhAgent> agent = adhAgentService.findByCin(user.getCin());
            if (agent.isPresent()) {
                return agent;
            }
        }

        return Optional.empty();
    }

    public void logout() {
        authenticationContext.logout();
    }

    public boolean isAuthenticated() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).isPresent();
    }

    public Optional<String> getUsername() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
            .map(UserDetails::getUsername);
    }

    public boolean isAdherent() {
        return getLinkedAgent().isPresent();
    }
}
