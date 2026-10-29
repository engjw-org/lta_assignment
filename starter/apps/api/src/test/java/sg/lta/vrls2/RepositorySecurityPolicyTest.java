package sg.lta.vrls2;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Scoped regression checks, not a credential/history scanner or a security certification. */
class RepositorySecurityPolicyTest {
    private final Path starter = Path.of("../..").toAbsolutePath().normalize();

    @Test
    void configurationDoesNotAssignLiteralCredentials() throws Exception {
        List<Path> files = productionFiles();
        Pattern assignment = Pattern.compile(
                "(?im)^\\s*(?:-\\s*)?[\\w.-]*(?:password|passwd|secret|api[_-]?key|signing[_-]?key|access[_-]?token)\\s*[:=]\\s*(.*?)\\s*$");
        for (Path file : files) {
            if (!file.toString().matches(".*\\.(yml|yaml|properties)$")) continue;
            var values = assignment.matcher(Files.readString(file));
            while (values.find()) {
                String value = values.group(1).replaceAll("^[\"']|[\"']$", "");
                // No fallback literal is allowed inside an environment substitution.
                assertThat(value.isBlank() || value.matches("\\$\\{[A-Z][A-Z0-9_]*}"))
                        .as("R-6.5 literal credential assignment in %s (value withheld)", starter.relativize(file)).isTrue();
            }
        }
    }

    @Test
    void productionSourcesContainNoRecognisablePrivateKeysOrProviderTokens() throws Exception {
        Pattern token = Pattern.compile("-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"
                + "|\\bAKIA[0-9A-Z]{16}\\b|\\bgh[pousr]_[A-Za-z0-9]{36,}\\b"
                + "|\\bgithub_pat_[A-Za-z0-9_]{40,}\\b");
        for (Path file : productionFiles()) {
            assertThat(token.matcher(Files.readString(file)).find())
                    .as("R-6.5 credential signature in %s (value withheld)", starter.relativize(file)).isFalse();
        }
    }

    @Test
    void angularProductionSourcesHaveNoUnreviewedHtmlOrSanitizerBypass() throws Exception {
        for (Path file : productionFiles()) {
            if (!file.startsWith(starter.resolve("apps/web/src"))) continue;
            String source = Files.readString(file);
            assertThat(source).as("R-6.2 unsafe rendering primitive in %s", starter.relativize(file))
                    .doesNotContain("innerHTML", "outerHTML", "bypassSecurityTrust", "insertAdjacentHTML");
        }
    }

    private List<Path> productionFiles() throws Exception {
        List<Path> files = new ArrayList<>();
        for (String root : List.of("apps/api/src/main", "apps/web/src")) {
            try (var paths = Files.walk(starter.resolve(root))) {
                files.addAll(paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().matches(".*\\.(java|ts|html|css|json|yml|yaml|properties)$"))
                        .filter(path -> !path.toString().endsWith(".spec.ts")).toList());
            }
        }
        for (String file : List.of("docker-compose.yml", "apps/api/Dockerfile", "apps/web/Dockerfile")) {
            files.add(starter.resolve(file));
        }
        assertThat(files).isNotEmpty();
        return files;
    }
}
