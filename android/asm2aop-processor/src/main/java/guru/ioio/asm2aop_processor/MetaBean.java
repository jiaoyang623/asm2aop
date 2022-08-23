package guru.ioio.asm2aop_processor;

public class MetaBean {
    public MetaBean(String className, String methodName, String query) {
        this.className = className;
        this.methodName = methodName;
        this.query = query;
    }

    public String className;
    public String methodName;
    public String query;
}
