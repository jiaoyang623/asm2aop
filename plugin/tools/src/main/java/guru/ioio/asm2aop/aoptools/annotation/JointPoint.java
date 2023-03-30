package guru.ioio.asm2aop.aoptools.annotation;

public abstract class JointPoint {
    public Object[] args = null;
    public Object caller = null;
    public Object executor = null;

    public abstract Object execute(Object[] args);

    public Object execute() {
        return execute(args);
    }
}
