package eecs1021;
// Libraries
import org.firmata4j.I2CDevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import java.io.IOException;
import java.util.Timer;
// Pin imports
import static eecs1021.Pins.A0;
import static eecs1021.Pins.D2;
import static eecs1021.Pins.I2C;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize the Arduino Board
        var arduinoObject = new FirmataDevice("COM5");
        arduinoObject.start();
        arduinoObject.ensureInitializationIsDone();

        // Setup for water pump
        var waterPump = arduinoObject.getPin(D2);
        waterPump.setMode(Pin.Mode.OUTPUT);

        // Setup for Soil Moisture Sensor
        var soilMoisture = arduinoObject.getPin(A0);
        soilMoisture.setMode(Pin.Mode.ANALOG);

        // Create I2CDevice to initialize OLED display
        I2CDevice i2cObject = arduinoObject.getI2CDevice(I2C);
        SSD1306 oledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
        oledObject.init();

        // Used to plot each point in a graph per second and detect the moisture of the soil
        Timer pumpTimer = new Timer();
        var Moist = new Moisture(soilMoisture, waterPump, oledObject);
        pumpTimer.schedule(Moist,0,1000);
    }
}
