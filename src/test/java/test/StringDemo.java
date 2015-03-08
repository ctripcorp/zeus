package test;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class StringDemo {
    public static void main(String[] args) {
        int i = 0;
        long prev_time = System.currentTimeMillis();
        long time;

        for (i = 0; i < 100000; i++) {
            String s = "Blah" + i + "Blah";
        }
        time = System.currentTimeMillis() - prev_time;

        System.out.println("Time after for loop " + time);

        prev_time = System.currentTimeMillis();
        for (i = 0; i < 100000; i++) {
            String s = String.format("Blah %d Blah", i);
        }
        time = System.currentTimeMillis() - prev_time;
        System.out.println("Time after for loop " + time);

    }
}
