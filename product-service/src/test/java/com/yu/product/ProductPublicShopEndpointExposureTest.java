package com.yu.product;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductPublicShopEndpointExposureTest {

    @Test
    void shouldExposePublicShopDetailEndpoint() throws ClassNotFoundException {
        assertTrue(hasShopDetailEndpoint(), "缺少公开店铺详情接口 /shops/{id}");
    }

    private static boolean hasShopDetailEndpoint() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        for (var candidate : scanner.findCandidateComponents("com.yu.item.controller")) {
            Class<?> controllerClass = ClassUtils.forName(candidate.getBeanClassName(), ProductPublicShopEndpointExposureTest.class.getClassLoader());
            RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
            if (requestMapping == null || !containsPath(resolvePaths(requestMapping), "/shops")) {
                continue;
            }
            for (Method method : controllerClass.getDeclaredMethods()) {
                GetMapping getMapping = AnnotatedElementUtils.findMergedAnnotation(method, GetMapping.class);
                if (getMapping != null && containsPath(resolvePaths(getMapping), "/{id}")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] resolvePaths(RequestMapping requestMapping) {
        return requestMapping.path().length > 0 ? requestMapping.path() : requestMapping.value();
    }

    private static String[] resolvePaths(GetMapping getMapping) {
        return getMapping.path().length > 0 ? getMapping.path() : getMapping.value();
    }

    private static boolean containsPath(String[] paths, String target) {
        for (String path : paths) {
            if (target.equals(path)) {
                return true;
            }
        }
        return false;
    }
}
