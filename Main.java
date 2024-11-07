import controller.Controller;
import view.TUI;

public class Main {
    public static void main(String[] args) throws Exception {
        Controller controller = new Controller("data/");
        TUI tui = new TUI(controller);
        while(true) {
            tui.processUserInput();
        }
    }
}
