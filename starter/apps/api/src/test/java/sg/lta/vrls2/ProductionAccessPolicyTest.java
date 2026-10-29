package sg.lta.vrls2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/** R-6.3: executable guard for the ADR's dev-only stance; no test-only security filter. */
class ProductionAccessPolicyTest {
    private final WebApplicationContextRunner app = new WebApplicationContextRunner()
            .withUserConfiguration(VrlsApplication.class)
            .withPropertyValues("vrls.data-dir=../../data");

    @ParameterizedTest
    @ValueSource(strings = {"prod", "prod,dev", ""})
    void refusesStartupOutsideExplicitDevWithoutProductionAuthentication(String profiles) {
        app.withPropertyValues("spring.profiles.active=" + profiles).run(context -> {
            assertThat(context).as("R-6.3 no anonymous production or implicit default startup").hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("auth");
        });
    }

    @Test
    void explicitLocalDevCanStartWithoutCredentials() {
        app.withPropertyValues("spring.profiles.active=dev").run(context -> assertThat(context).hasNotFailed());
    }
}
