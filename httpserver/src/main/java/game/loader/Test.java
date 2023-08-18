package game.loader;

public class Test {
    int i;

    public void addI() {
        i += 1;
//        System.out.println(i);
    }

    public static void main(String[] args) {
        try {
            test();
        } catch (Exception e) {
            System.out.println("sdf");
        }

    }

    public static String test() {
        String s = null;
        try {
            s.substring(0, s.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
