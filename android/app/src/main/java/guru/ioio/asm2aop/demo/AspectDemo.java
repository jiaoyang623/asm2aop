package guru.ioio.asm2aop.demo;

import guru.ioio.asm2aop_annotation.After;
import guru.ioio.asm2aop_annotation.Around;
import guru.ioio.asm2aop_annotation.Asm2Aop;
import guru.ioio.asm2aop_annotation.Before;

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

    @Around("execution * android.app.Activity.onResume(..)")
    public void aroundOnResume() {
        System.out.println("asm2aop: around Activity.onResume");
    }
    @Around("execution * guru.ioio.asm2aop.demo.MainActivity.load(..)")
    public void aroundLoad() {
        System.out.println("asm2aop: around MainActivity.load");
    }
}
