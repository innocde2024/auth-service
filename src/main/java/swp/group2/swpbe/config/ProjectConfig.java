package swp.group2.swpbe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;
@Configuration
public class ProjectConfig {
 
  @Value("${allow.origin}")
  private String allowedOrigins;
  @Value("${cloudinary.apiKey}")
  private String cloudinaryApiKey;
  @Value("${cloudinary.apiSecret}")
  private String cloudinaryApiSecret;
  @Value("${cloudinary.name}")
  private String cloudinaryApiName;


  
  @Bean
   Cloudinary getCloudinary() {
    Map<String, Object> config = new HashMap<>();
    config.put("cloud_name", cloudinaryApiName);
    config.put("api_key", cloudinaryApiKey);
    config.put("api_secret", cloudinaryApiSecret);
    config.put("secure", true);
    return new Cloudinary(config);
  }


  @Bean
  WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD");
      }
    };
  }
}
