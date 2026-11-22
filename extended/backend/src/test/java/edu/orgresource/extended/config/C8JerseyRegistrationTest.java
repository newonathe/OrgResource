package edu.orgresource.extended.config;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;
import edu.orgresource.extended.rest.*;

class C8JerseyRegistrationTest {
    // Independent executable check of Jersey registration; the JSON inventory is
    // checked against source separately by scripts/verify-c8-routes.mjs.
    private static final Map<Class<?>, Integer> EXPECTED = Map.ofEntries(
        Map.entry(AllocationResource.class, 9),
        Map.entry(C1AuthResource.class, 4),
        Map.entry(C1GovernanceResource.class, 8),
        Map.entry(C1InvitationResource.class, 1),
        Map.entry(C1SessionResource.class, 7),
        Map.entry(DemoMetadataResource.class, 1),
        Map.entry(DepartmentResource.class, 4),
        Map.entry(InventoryResource.class, 24),
        Map.entry(ProjectResource.class, 4),
        Map.entry(ProjectWorkResource.class, 8),
        Map.entry(ReportingResource.class, 3),
        Map.entry(ResourceRequirementResource.class, 4),
        Map.entry(ResourceResource.class, 7),
        Map.entry(V2DepartmentResource.class, 3),
        Map.entry(WorkspacePageResource.class, 6)
    );

    @Test
    void allFifteenResourceClassesAndNinetyThreeOperationsAreRegistered() {
        ResourceConfig config = new C1JerseyConfiguration().c1ResourceConfig();
        Set<Class<?>> registeredResources = new HashSet<>();
        for (Class<?> type : config.getClasses()) {
            if (type.getPackageName().equals("edu.orgresource.extended.rest")
                    && type.isAnnotationPresent(Path.class)) {
                registeredResources.add(type);
            }
        }
        assertEquals(EXPECTED.keySet(), registeredResources, "Missing or unexpected Jersey resource");

        Set<String> keys = new HashSet<>();
        int count = 0;
        for (Map.Entry<Class<?>, Integer> expected : EXPECTED.entrySet()) {
            Class<?> type = expected.getKey();
            Path root = type.getAnnotation(Path.class);
            assertNotNull(root, "Missing resource @Path: " + type.getSimpleName());
            Produces produces = type.getAnnotation(Produces.class);
            assertNotNull(produces, "Missing response media type: " + type.getSimpleName());
            assertTrue(Set.of(produces.value()).contains(MediaType.APPLICATION_JSON));
            int perClass = 0;
            for (Method method : type.getDeclaredMethods()) {
                String verb = null;
                for (Annotation annotation : method.getAnnotations()) {
                    HttpMethod http = annotation.annotationType().getAnnotation(HttpMethod.class);
                    if (http == null) continue;
                    assertNull(verb, "Multiple HTTP verbs on " + type.getSimpleName() + "." + method.getName());
                    verb = http.value();
                }
                if (verb == null) continue;
                Path suffix = method.getAnnotation(Path.class);
                String fullPath = "/api" + root.value() + (suffix == null ? "" : suffix.value());
                assertTrue(keys.add(verb + " " + fullPath), "Duplicate HTTP route: " + verb + " " + fullPath);
                perClass++;
            }
            assertEquals(expected.getValue(), perClass, "Route count drift in " + type.getSimpleName());
            count += perClass;
        }
        assertEquals(93, count);
    }
}
