package com.yu.common.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RabbitMQConfigClasspathIsolationTest {

    @Test
    void shouldAllowIntrospectionWithoutAmqpDependencies() throws Exception {
        URL classesUrl = Path.of("target", "classes").toUri().toURL();

        try (FilteringClassLoader classLoader = new FilteringClassLoader(classesUrl)) {
            Class<?> configClass = classLoader.loadClass("com.yu.common.config.RabbitMQConfig");

            assertDoesNotThrow(configClass::getDeclaredMethods);
        }
    }

    private static final class FilteringClassLoader extends URLClassLoader {

        private FilteringClassLoader(URL classesUrl) {
            super(new URL[]{classesUrl}, RabbitMQConfigClasspathIsolationTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("org.springframework.amqp.")) {
                throw new ClassNotFoundException(name);
            }
            if (name.equals("com.yu.common.config.RabbitMQConfig")) {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loadedClass = findLoadedClass(name);
                    if (loadedClass == null) {
                        loadedClass = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(loadedClass);
                    }
                    return loadedClass;
                }
            }
            return super.loadClass(name, resolve);
        }

        @Override
        public void close() throws IOException {
            super.close();
        }
    }
}
