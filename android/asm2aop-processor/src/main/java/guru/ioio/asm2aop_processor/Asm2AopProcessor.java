package guru.ioio.asm2aop_processor;

import com.google.auto.service.AutoService;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import guru.ioio.asm2aop_annotation.Asm2Aop;
import guru.ioio.asm2aop_annotation.Before;

@AutoService(Processor.class)
public class Asm2AopProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        System.out.println("APT init");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(Asm2Aop.class.getCanonicalName());
        hashSet.add(Before.class.getCanonicalName());

        return hashSet;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements1 = roundEnvironment.getElementsAnnotatedWith(Before.class);
        for (Element element : elements1) {
            Name simpleName = element.getSimpleName();
            System.out.println("APT process 2 " + simpleName);
        }
        return false;
    }
}
