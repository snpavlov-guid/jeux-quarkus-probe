package com.jeuxwebapi.auth;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakRoleAugmentor implements SecurityIdentityAugmentor {
    @ConfigProperty(name = "quarkus.oidc.client-id", defaultValue = "")
    String clientId;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (!(identity.getPrincipal() instanceof JsonWebToken)) {
            return Uni.createFrom().item(identity);
        }

        JsonWebToken jwt = (JsonWebToken) identity.getPrincipal();
        Set<String> roles = new LinkedHashSet<>();
        roles.addAll(extractRealmRoles(jwt));
        roles.addAll(extractResourceRoles(jwt));

        if (roles.isEmpty()) {
            return Uni.createFrom().item(identity);
        }

        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        roles.forEach(builder::addRole);
        return Uni.createFrom().item(builder.build());
    }

    private Set<String> extractRealmRoles(JsonWebToken jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmMap)) {
            return Set.of();
        }
        Object roles = realmMap.get("roles");
        return extractRoleList(roles);
    }

    private Set<String> extractResourceRoles(JsonWebToken jwt) {
        Object resourceAccess = jwt.getClaim("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> resourceMap)) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (Object entry : resourceMap.values()) {
            if (entry instanceof Map<?, ?> clientMap) {
                Object roles = clientMap.get("roles");
                result.addAll(extractRoleList(roles));
            }
        }
        return result;
    }

    private Set<String> extractRoleList(Object roles) {
        if (roles instanceof Collection<?> list) {
            Set<String> result = new LinkedHashSet<>();
            for (Object item : list) {
                if (item instanceof String role && !role.isBlank()) {
                    result.add(role);
                }
            }
            return result;
        }
        return Set.of();
    }
}
