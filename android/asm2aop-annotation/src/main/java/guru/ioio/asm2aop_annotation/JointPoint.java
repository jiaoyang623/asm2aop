package guru.ioio.asm2aop_annotation;

public abstract class JointPoint {
    public Object[] args = null;
    public Object target = null;

    public abstract Object execute(Object[] args);

    public Object execute() {
        return execute(args);
    }
}
