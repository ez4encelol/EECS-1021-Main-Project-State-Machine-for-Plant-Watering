package eecs1021;
// Libraries
import org.firmata4j.Pin;
import org.firmata4j.ssd1306.SSD1306;
import edu.princeton.cs.introcs.StdDraw;
import java.io.IOException;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;

public class Moisture extends TimerTask {
    // Parameters for the state machine
    final double drySoil = 3.3;
    final double moistSoil = 2.6;
    final double notMoistEnough = (drySoil-moistSoil)/2 + moistSoil;
    // Variables for constructor
    private final Pin moistPin;
    private final Pin waterPin;
    private final SSD1306 oledDisplay;
    // Arraylist to contain voltage values to plot points on the graph
    private final List<Double> voltageArray;
    // Used to prevent the graph from closing and opening
    private boolean GraphSetup = false;
    // Constructor class for the sensor, water pump, and oled display
    Moisture(Pin sensor, Pin pump, SSD1306 oled) {
        this.moistPin = sensor;
        this.waterPin = pump;
        this.oledDisplay = oled;
        this.voltageArray = new ArrayList<>();

    }

    // Stores the analogue value from the sensor into the arraylist, then converts it into volts in a separate variable
    // to display the value on the oled display
    public double readMoistureLevel() {
        int analogValue = (int) moistPin.getValue();
        double voltage = (analogValue / 1023.0) * 5.0;
        voltageArray.add(voltage);
        return voltage;
    }
    // Sets up the dimensions of the graph
    private void setupGraph() {
        if (!GraphSetup) {
            StdDraw.setCanvasSize(800, 600);
            StdDraw.setXscale(-11, 100);
            StdDraw.setYscale(-1, 5);
            GraphSetup = true;
        }
    }
    // Plot points on the graph in real-time
    private void plotGraph() {
        StdDraw.clear();
        StdDraw.setPenRadius(0.03);
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.line(2, 0, 2, 5); // Y line
        StdDraw.line(3, 0, 100, 0); // X line
        StdDraw.text(50, -0.5, "Time[s]"); // X label
        StdDraw.text(-10, 2.5, "Voltage[v]"); // Y label
        double spaceOut = 100.0 / (voltageArray.size() + 1); // Used to space out each point to prevent crunching
        for (int i = 0; i < voltageArray.size(); i++) {
            double x = (i + 1) * spaceOut;
            double y = voltageArray.get(i);
            StdDraw.point(x, y);
        }
        // Draw a number line on y-axis
        for (int i = 0; i <= 5; i++) {
            StdDraw.text(-2, i, Integer.toString(i));
        }
        // Draw a number line on x-axis
        for (int i = 0; i <= voltageArray.size(); i++) {
            double x = (i + 1) * spaceOut; // Adjust x-coordinate
            StdDraw.text(x, -0.3, Integer.toString(i));
        }
        StdDraw.show();
    }
    // Runs the state machine, and displays the voltage and current state of the system
    @Override
    public void run() {
        try {
            setupGraph();
            double moistureLevel = readMoistureLevel();
            System.out.println("Volts: " + (String.format("%.2f",moistureLevel)));
            if (moistureLevel >= drySoil) { // If the soil is dry, begin pumping water
                waterPin.setValue(1);
                Thread.sleep(100);
                waterPin.setValue(0);
                oledDisplay.getCanvas().drawString(0,10, " Soil is Dry, pumping...");
                oledDisplay.getCanvas().drawString(0,0, "Volts: " + (String.format("%.2f",moistureLevel)));
                oledDisplay.display();
                oledDisplay.clear();
            } else if (moistureLevel <= notMoistEnough && moistureLevel > moistSoil){
                // If the soil is not moist enough, continue pumping water
                waterPin.setValue(1);
                Thread.sleep(100);
                waterPin.setValue(0);
                oledDisplay.getCanvas().drawString(0,10, "Soil isn't wet enough, continue pumping..");
                oledDisplay.getCanvas().drawString(0,0, "Volts: " + String.format("%.2f",moistureLevel));
                oledDisplay.display();
                oledDisplay.clear();
            }
            else{ // Otherwise, once the soil is moist enough, stop pumping water
                waterPin.setValue(0);
                oledDisplay.getCanvas().drawString(0,10, "Soil is wet, stop pumping...");
                oledDisplay.getCanvas().drawString(0,0, "Volts: " + String.format("%.2f",moistureLevel));
                oledDisplay.display();
                oledDisplay.clear();
            }
            plotGraph();
            // Handle the IOException
        } catch (IOException ex) {
            System.out.println("State Machine Malfunction");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
