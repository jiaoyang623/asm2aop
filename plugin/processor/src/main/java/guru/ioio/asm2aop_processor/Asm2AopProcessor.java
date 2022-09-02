package guru.ioio.asm2aop_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import guru.ioio.asm2aop.aoptools.annotation.After;
import guru.ioio.asm2aop.aoptools.annotation.Around;
import guru.ioio.asm2aop.aoptools.annotation.Before;
import guru.ioio.asm2aop.aoptools.annotation.JointPoint;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

@AutoService(Processor.class)
public class Asm2AopProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    private ProcessingEnvironment mEnv = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mEnv = processingEnv;
        System.out.println("APT init");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
//        hashSet.add(Asm2Aop.class.getCanonicalName());
        hashSet.add(Before.class.getCanonicalName());
        hashSet.add(After.class.getCanonicalName());
        hashSet.add(Around.class.getCanonicalName());

        return hashSet;
    }

    private static final String TARGET_PACKAGE = "guru.ioio.asm2aop";
    private static final String TARGET_CLASS = "AopTarget";
    private boolean mIsDone = false;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (mIsDone) return true;
        System.out.println(set);
        List<MetaBean> metaList = new ArrayList<>();
        makeMetaBean(roundEnvironment, metaList, Before.class);
        makeMetaBean(roundEnvironment, metaList, After.class);
        makeMetaBean(roundEnvironment, metaList, Around.class);
        try {
            generateFile(metaList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIsDone = true;
        return true;
    }

    private String getValue(Annotation annotation) {
        if (annotation instanceof Before) {
            return ((Before) annotation).value();
        } else if (annotation instanceof After) {
            return ((After) annotation).value();
        } else if (annotation instanceof Around) {
            return ((Around) annotation).value();
        } else {
            return null;
        }
    }

    private <T extends Annotation> void makeMetaBean(
            RoundEnvironment roundEnvironment,
            List<MetaBean> metaList, Class<T> annotationClass) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotationClass);
        String target = TARGET_PACKAGE + "." + TARGET_CLASS;
        for (Element e : elements) {
            // 找到对应的类，方法，注解
            ExecutableElement element = (ExecutableElement) e;
            TypeElement classElement = (TypeElement) element.getEnclosingElement();
            String value = getValue(element.getAnnotation(annotationClass));
            String className = classElement.getQualifiedName().toString();
            String methodName = element.getSimpleName().toString();
            if (!className.equals(target) && value != null) {
                metaList.add(new MetaBean(className, methodName, value, annotationClass));
            }
        }
    }

    private static final String METHOD_PREFIX = "f";

    private void generateFile(List<MetaBean> list) throws IOException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(TARGET_CLASS)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//                .superclass(BaseAopTarget.class);
        FieldSpec field = FieldSpec.builder(HashMap.class, "cache")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", HashMap.class)
                .build();
        classBuilder.addField(field);

        int count = 0;
        for (MetaBean bean : list) {
            MethodSpec method;//                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
            if (bean.annotation == Around.class) {
                method = MethodSpec.methodBuilder(METHOD_PREFIX + count++)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(JointPoint.class, "jointPoint")
                        .returns(Object.class)
                        .addStatement("if(cache.get(" + bean.className + ".class)==null)cache.put(" + bean.className + ".class,new " + bean.className + "())")
                        .addStatement(bean.className + " target=(" + bean.className + ")cache.get(" + bean.className + ".class)")
                        .addStatement("return target." + bean.methodName + "(jointPoint)")
                        .addAnnotation(AnnotationSpec.builder(bean.annotation)
                                .addMember("value", "$S", bean.query)
                                .build())
                        .build();
            } else {
                method = MethodSpec.methodBuilder(METHOD_PREFIX + count++)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addStatement("if(cache.get(" + bean.className + ".class)==null)cache.put(" + bean.className + ".class,new " + bean.className + "())")
                        .addStatement(bean.className + " target=(" + bean.className + ")cache.get(" + bean.className + ".class)")
                        .addStatement("target." + bean.methodName + "()")
                        .addAnnotation(AnnotationSpec.builder(bean.annotation)
                                .addMember("value", "$S", bean.query)
                                .build())
                        .build();
            }
            classBuilder.addMethod(method);
        }

        JavaFile jFile = JavaFile.builder(TARGET_PACKAGE, classBuilder.build()).build();
        jFile.writeTo(mEnv.getFiler());
    }
}
