package presenter;

public class DigitalInkManger {

    private DigitalInkManger() {
    }

    private static volatile DigitalInkManger instance = null;

    public static DigitalInkManger getInstance() {
        if (instance == null) {
            synchronized (DigitalInkManger.class) {
                if (instance == null) {
                    instance = new DigitalInkManger();
                }
            }
        }
        return instance;
    }

}
