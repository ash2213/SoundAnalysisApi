package dat.config;

import io.javalin.rendering.FileRenderer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

public class ThymeleafConfig {
    private static final TemplateEngine templateEngine = new TemplateEngine();

    static {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/"); // Ensure templates are inside `resources/templates/`
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCacheable(false); // Disable caching for development

        templateEngine.setTemplateResolver(templateResolver);
    }

    public static FileRenderer getFileRenderer() {
        return (filePath, model, ctx) -> {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables((Map<String, Object>) model);
            return templateEngine.process(filePath, thymeleafContext);
        };
    }
}