package guru.ioio.asm2aop.demo;

import java.util.Arrays;

import guru.ioio.asm2aop.aoptools.annotation.*;

@Asm2Aop
public class AspectDemo {
    @Before("execution void android.app.Activity.onCreate(android.os.Bundle)")
    public void beforeOnCreate() {
        System.out.println("asm2aop: before Activity.onCreated");
    }

    @Before("execution void android.app.Activity.onDestroy(..)")
    public void beforeOnDestroy() {
        System.out.println("asm2aop: before Activity.onDestroy");
    }

    @After("execution void android.app.Activity.onCreate(..)")
    public void afterOnCreate() {
        System.out.println("asm2aop: after Activity.onCreated");
    }

    @After("execution void android.app.Activity.onDestroy(..)")
    public void afterOnDestroy() {
        System.out.println("asm2aop: after Activity.onDestroy");
    }

//    @Around("execution * android.app.Activity.onResume(..)")
//    public void aroundOnResume() {
//        System.out.println("asm2aop: around Activity.onResume");
//    }

    @Around("execution * guru.ioio.asm2aop.demo.MainActivity.load(..)")
    public Object aroundLoad(JointPoint jp) {
        System.out.println("asm2aop: around MainActivity.load: " + jp.target + ", " + Arrays.toString(jp.args));
        return jp.execute();
    }
}
