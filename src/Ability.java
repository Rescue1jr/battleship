import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Ability {

    private int powerPoints = 0;
    private Label display;

    public Ability() {
        display = new Label("Power Points: 0");
        display.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    }

    public Label getDisplay() {
        return display;
    }

    public void addPoints(int amount) {
        powerPoints += amount;
        updateDisplay();
    }

    public boolean canUseSonar() {
        return powerPoints >= 10;
    }

    public void useSonar() {
        powerPoints -= 10;
        updateDisplay();
    }

    private void updateDisplay() {
        display.setText("Power Points: " + powerPoints);
    }
}