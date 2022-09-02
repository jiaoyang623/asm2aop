package guru.ioio.asm2aop.demo;

public class BaseBean {
    public String id = "id";

    public int create(Object a) {
        if (a == null) {
            return 0;
        } else {
            return 1;
        }
    }
}
