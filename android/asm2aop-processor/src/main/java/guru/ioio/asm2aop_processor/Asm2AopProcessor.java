package guru.ioio.asm2aop_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import guru.ioio.asm2aop_annotation.Before;

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
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Before.class);
        String target = TARGET_PACKAGE + "." + TARGET_CLASS;
        for (Element e : elements) {
            // 找到对应的类，方法，注解
            ExecutableElement element = (ExecutableElement) e;
            TypeElement classElement = (TypeElement) element.getEnclosingElement();
            String value = element.getAnnotation(Before.class).value();
            String className = classElement.getQualifiedName().toString();
            String methodName = element.getSimpleName().toString();
            if (!className.equals(target)) {
                metaList.add(new MetaBean(className, methodName, value));
            }
        }
        try {
            generateFile(metaList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIsDone = true;
        return true;
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
            MethodSpec method = MethodSpec.methodBuilder(METHOD_PREFIX + count++)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addStatement(bean.className + " target=(" + bean.className + ")cache.get(" + bean.className + ".class)")
                    .addStatement("if(target==null) target=new " + bean.className + "()")
                    .addStatement("target." + bean.methodName + "()")
//                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                    .addAnnotation(AnnotationSpec.builder(Before.class)
                            .addMember("value", "$S", bean.query)
                            .build())
                    .build();
            classBuilder.addMethod(method);
        }

        JavaFile jFile = JavaFile.builder(TARGET_PACKAGE, classBuilder.build()).build();
        jFile.writeTo(mEnv.getFiler());
    }
}