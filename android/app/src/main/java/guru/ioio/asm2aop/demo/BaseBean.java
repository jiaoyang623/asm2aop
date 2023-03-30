package guru.ioio.asm2aop.demo;

import java.io.PrintStream;

import guru.ioio.asm2aop.AopTarget;
import guru.ioio.asm2aop.aoptools.annotation.JointPoint;

public class BaseBean {
    public String id = "id";

    public int create(Object a) {
        if (a == null) {
            return 0;
        } else {
            return 1;
        }
    }

    public void run() {
        JointPoint jp = new JointPoint() {
            @Override
            public Object execute(Object[] objects) {
                return hookRun((PrintStream) executor, (int) objects[0]);
            }
        };
        jp.caller = this;
        jp.executor = System.out;
        jp.args = new Object[]{1};

        int out = (int) AopTarget.f7(jp);

        System.out.println(out);
    }

    public int hookRun(PrintStream ps, int arg) {
        ps.println(arg);
        return 0;
    }
}
