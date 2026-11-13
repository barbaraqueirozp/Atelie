package atelie_api.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serve as fotos enviadas pelo app.
 * Um arquivo salvo em  demo/uploads/obras/abc.jpg
 * fica disponível em   http://localhost:8080/uploads/obras/abc.jpg
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {

    public static final Path PASTA_UPLOADS = Path.of("uploads").toAbsolutePath();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(PASTA_UPLOADS.toUri().toString());
    }
}