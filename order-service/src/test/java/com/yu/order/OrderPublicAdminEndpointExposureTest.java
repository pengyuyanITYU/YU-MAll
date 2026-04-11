package com.yu.order;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderPublicAdminEndpointExposureTest {

    @Test
    void shouldNotExposePublicAdminControllers() throws ClassNotFoundException {
        List<String> adminMappings = findPublicAdminMappings("com.yu.order.controller");

        assertTrue(adminMappings.isEmpty(), () -> "公开 /admin 控制器未清理: " + adminMappings);
    }

    private static List<String> findPublicAdminMappings(String... basePackages) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> mappings = new ArrayList<>();
        for (String basePackage : basePackages) {
            for (var candidate : scanner.findCandidateComponents(basePackage)) {
                Class<?> controllerClass = ClassUtils.forName(candidate.getBeanClassName(), OrderPublicAdminEndpointExposureTest.class.getClassLoader());
                RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
                if (requestMapping == null) {
                    continue;
                }
                for (String path : resolvePaths(requestMapping)) {
                    if (path.startsWith("/admin")) {
                        mappings.add(controllerClass.getName() + " -> " + path);
                    }
                }
            }
        }
        return mappings;
    }

    private static String[] resolvePaths(RequestMapping requestMapping) {
        return requestMapping.path().length > 0 ? requestMapping.path() : requestMapping.value();
    }
}
