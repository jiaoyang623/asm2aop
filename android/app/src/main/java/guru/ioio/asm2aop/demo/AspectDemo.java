package guru.ioio.asm2aop.demo;

import guru.ioio.asm2aop_annotation.After;
import guru.ioio.asm2aop_annotation.Asm2Aop;
import guru.ioio.asm2aop_annotation.Before;

@Asm2Aop
public class AspectDemo {
//    @Before("execution void android.app.Activity.onCreate(..)")
//    public void beforeOnCreate() {
//        System.out.println("asm2aop: before Activity.onCreated");
//    }
//
//    @Before("execution void android.app.Activity.onDestroy(..)")
//    public void beforeOnDestroy() {
//        System.out.println("asm2aop: before Activity.onDestroy");
//    }

    @After("execution void android.app.Activity.onCreate(..)")
    public void afterOnCreate() {
        System.out.println("asm2aop: after Activity.onCreated");
    }

    @After("execution void android.app.Activity.onDestroy(..)")
    public void afterOnDestroy() {
        System.out.println("asm2aop: after Activity.onDestroy");
    }
}
