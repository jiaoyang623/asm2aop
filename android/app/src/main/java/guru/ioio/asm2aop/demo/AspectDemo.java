package guru.ioio.asm2aop.demo;

import guru.ioio.asm2aop_annotation.Asm2Aop;
import guru.ioio.asm2aop_annotation.Before;

@Asm2Aop
public class AspectDemo {
    @Before("execution android.app.Activity.onCreate(..)")
    public void beforeOnCreate() {
        System.out.println("asm2aop: Activity.onCreated");
    }

    @Before("execution android.app.Activity.onDestroy(..)")
    public void beforeOnDestroy() {
        System.out.println("asm2aop: Activity.onDestroy");
    }
}
