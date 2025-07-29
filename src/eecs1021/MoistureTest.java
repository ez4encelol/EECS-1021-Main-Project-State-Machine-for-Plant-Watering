package eecs1021;
import org.firmata4j.I2CDevice;
import org.firmata4j.IODevice;
import org.firmata4j.PinEventListener;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import org.junit.Test;

import static eecs1021.Pins.*;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import org.firmata4j.Pin;

public class MoistureTest {

    @Test
    public void testReadMoistureLevel() throws IOException {
        // Initialize arduino
        var arduinoObject = new FirmataDevice("COM5");
        arduinoObject.start();
        try {
            arduinoObject.ensureInitializationIsDone();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Setup for Soil Moisture Sensor
        var soilMoisture = arduinoObject.getPin(A0);
        soilMoisture.setMode(Pin.Mode.ANALOG);

        // Initiate the Moisture class to start the test
        Moisture moisture = new Moisture(soilMoisture, null, null);

        // Generate a random analog value
        Random random = new Random();
        int analogValue = random.nextInt(1023); // Generate a random value between 0 and 1023

        // retrieve the value from the moisture sensor
        soilMoisture.setValue(analogValue);

        // Calculate expected voltage
        double expectedVolt = (analogValue / 1023.0) * 5.0;

        // Test the readMoistureLevel method
        double actualVolt = moisture.readMoistureLevel();

        // Check if the voltage is within an error margin of 0.01
        assertEquals(expectedVolt, actualVolt, 0.01);
    }
}