package org.mtahq.pfc.turnstile;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class PiPulser {
    private static final long ACCEPT_PULSE_WIDTH_MILLIS = 40;
    private static final long BANKED_PULSE_WIDTH_MILLIS = 50;
    private static final long INTER_PULSE_GAP_MILLIS = 100;
    
    private static final Pin ACCEPT_PIN_ID = RaspiPin.GPIO_04;
    private static final Pin BANKED_PIN_ID = RaspiPin.GPIO_05;
        
    public PiPulser() {
        super();
    }
    
    public void sendOneSequence() {
        final GpioController gpio = GpioFactory.getInstance();
        
        final GpioPinDigitalOutput acceptPin = gpio.provisionDigitalOutputPin(ACCEPT_PIN_ID, "Accept", PinState.LOW);
        final GpioPinDigitalOutput bankedPin = gpio.provisionDigitalOutputPin(BANKED_PIN_ID, "Banked", PinState.LOW);
        
        acceptPin.pulse(ACCEPT_PULSE_WIDTH_MILLIS, true); // set second argument to 'true' use a blocking call
        Utils.sleep(INTER_PULSE_GAP_MILLIS);
        bankedPin.pulse(BANKED_PULSE_WIDTH_MILLIS, true);
        gpio.shutdown();
    }
}
