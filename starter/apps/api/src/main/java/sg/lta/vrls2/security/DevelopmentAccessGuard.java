package sg.lta.vrls2.security;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** No production listener is allowed until authentication and admin authorisation are implemented. */
@Component
public class DevelopmentAccessGuard {
    public DevelopmentAccessGuard(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length != 1 || !"dev".equals(profiles[0])) {
            throw new IllegalStateException(
                    "Production authentication is not configured. Only the explicit dev profile may start.");
        }
    }
}
