package edu.orgresource.extended;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.WebApplicationType;
import java.util.TimeZone;
import java.util.Arrays;
import edu.orgresource.extended.identity.C1BootstrapService;

@SpringBootApplication
@EnableScheduling
public class C1Application {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    ApplicationRunner offlineBootstrap(C1BootstrapService bootstrap) {
        return (ApplicationArguments args) -> {
            if (!args.containsOption("bootstrap")) return;
            String password = requiredSetting("EXT_BOOTSTRAP_PASSWORD");
            C1BootstrapService.BootstrapResult result = bootstrap.bootstrap(
                    requiredSetting("EXT_BOOTSTRAP_SLUG"), requiredSetting("EXT_BOOTSTRAP_ORG_NAME"),
                    requiredSetting("EXT_BOOTSTRAP_USERNAME"), requiredSetting("EXT_BOOTSTRAP_DISPLAY_NAME"), password);
            System.out.println("Extended C owner initialized for fictional workspace. Save these recovery codes now; they are shown once:");
            result.recoveryCodes().forEach(System.out::println);
        };
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(C1Application.class);
        if (Arrays.asList(args).contains("--bootstrap")) {
            app.setWebApplicationType(WebApplicationType.NONE);
            app.run(args).close();
        } else {
            app.run(args);
        }
    }

    private static String requiredSetting(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Required protected process setting is missing: " + key);
        return value;
    }
}
