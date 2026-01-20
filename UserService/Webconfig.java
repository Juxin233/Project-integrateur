@Configuration
public class WebConfig implements WebMvcConfigurer {

    public WebConfig() {
        System.out.println(" WebConfig LOADED ");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println(" addCorsMappings CALLED");

        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}