package guru.ioio.asm2aop.demo;

public class AlphaBean extends BaseBean {
    public String getData(Object... args) {
        return id;
    }

    public void test(int a) {
        Object[] list = new Object[99];
        list[0] = a;
        getData("1", "2", "3");
    }

    public Object toObject(boolean i) {
        return i;
    }

    public Object toObject(char i) {
        return i;
    }

    public Object toObject(byte i) {
        return i;
    }

    public Object toObject(short i) {
        return i;
    }

    public Object toObject(long i) {
        return i;
    }

    public Object toObject(float i) {
        return i;
    }

    public Object toObject(double i) {
        return i;
    }

    public void call(Object[] args) {
        exe((boolean) args[0], (short) args[1], (long) args[2], (float) args[3], (double) args[4]);
    }

    public void exe(boolean a, short b, long c, float d, double e) {

    }
}
