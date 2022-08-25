package guru.ioio.asm2aop_processor;

import java.lang.annotation.Annotation;

public class MetaBean {
    public MetaBean(String className, String methodName, String query, Class<? extends Annotation> annotation) {
        this.className = className;
        this.methodName = methodName;
        this.query = query;
        this.annotation = annotation;
    }

    public String className;
    public String methodName;
    public String query;
    public Class<? extends Annotation> annotation;
}
