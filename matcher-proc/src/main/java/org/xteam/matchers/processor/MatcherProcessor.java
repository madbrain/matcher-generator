package org.xteam.matchers.processor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.xteam.matchers.Matchable;

import com.google.auto.service.AutoService;

@AutoService(Processor.class)
public class MatcherProcessor extends AbstractProcessor {

    public static class MatchableBean {

        private TypeElement typeElement;
        private List<BeanProperty> properties;

        public MatchableBean(TypeElement typeElement, List<BeanProperty> properties) {
            this.typeElement = typeElement;
            this.properties = properties;
        }

        public List<BeanProperty> getProperties() {
            return properties;
        }

        public String getName() {
            return typeElement.getSimpleName().toString();
        }

        public String getTypeName() {
            return typeElement.getQualifiedName().toString();
        }

    }

    public static class BeanProperty {

        private String name;
        private TypeMirror type;

        public BeanProperty(String name, TypeMirror type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getCapitalName() {
            return capitalize(name);
        }

        public String getType() {
            if (type.getKind().isPrimitive()) {
                switch (type.getKind()) {
                case INT:
                    return "Integer";
                default:
                    throw new RuntimeException("not complete");
                }
            }
            return type.toString();
        }

    }

    private Map<PackageElement, List<MatchableBean>> matchableClassesPerPackage = new HashMap<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Matchable.class.getName())));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateConfigFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }
        return true;
    }

    private void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Matchable.class);
        for (TypeElement typeElement : ElementFilter.typesIn(elements)) {
            processingEnv.getMessager().printMessage(Kind.NOTE, "processing " + typeElement.getQualifiedName());

            PackageElement packageElement = typeElement.getEnclosingElement().accept(
                    new SimpleElementVisitor7<PackageElement, Object>() {
                        @Override
                        public PackageElement visitPackage(PackageElement e, Object p) {
                            return e;
                        }
                    }, null);
            if (packageElement != null && !packageElement.isUnnamed()) {
                List<MatchableBean> matchableClasses = matchableClassesPerPackage.get(packageElement);
                if (matchableClasses == null) {
                    matchableClassesPerPackage.put(packageElement, matchableClasses = new ArrayList<>());
                }
                matchableClasses.add(new MatchableBean(typeElement, getProperties(typeElement)));
            }
        }
    }

    private List<BeanProperty> getProperties(TypeElement typeElement) {
        List<BeanProperty> properties = new ArrayList<>();
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            String methodName = executableElement.getSimpleName().toString();
            if (methodName.startsWith("get")) {
                properties.add(new BeanProperty(getPropertyName(methodName), executableElement.getReturnType()));
            }
        }
        return properties;
    }

    private String getPropertyName(String methodName) {
        return uncapitalize(methodName.substring(3));
    }

    private void generateConfigFiles() {
        Filer filer = processingEnv.getFiler();

        RuntimeInstance instance = new RuntimeInstance();
        instance.setProperty("resource.loader", "class");
        instance.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        instance.init();

        Template template = instance.getTemplate("templates/PackageMatcher.vm");

        for (PackageElement packageElement : matchableClassesPerPackage.keySet()) {
            String simplePackageName = packageElement.getSimpleName().toString();
            String packageName = packageElement.getQualifiedName().toString() + ".matchers";
            String packageMatcherName = capitalize(simplePackageName) + "Matchers";
            String sourceFile = packageName + "." + packageMatcherName;

            VelocityContext context = new VelocityContext();
            context.put("packageName", packageName);
            context.put("packageMatcherName", packageMatcherName);
            context.put("beans", matchableClassesPerPackage.get(packageElement));

            try {
                FileObject fileObject = filer.createSourceFile(sourceFile);
                Writer writer = new OutputStreamWriter(fileObject.openOutputStream(), "UTF-8");
                template.merge(context, writer);
                writer.close();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
            }
        }

    }

    private String uncapitalize(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
