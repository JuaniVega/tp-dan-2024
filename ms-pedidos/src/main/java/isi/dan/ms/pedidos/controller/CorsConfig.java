package isi.dan.ms.pedidos.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**") // Permitir CORS en todos los endpoints bajo /api
				.allowedOrigins("http://localhost:3000") // Origen permitido
				.allowedMethods("GET", "POST", "PUT", "DELETE") // Métodos HTTP permitidos
				.allowedHeaders("*") // Headers permitidos
				.allowCredentials(true); // Permitir credenciales (cookies, headers de autenticación)
	}
}