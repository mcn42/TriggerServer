package org.mtahq.pfc.turnstile;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import javax.annotation.PreDestroy;

public class PiPulser {
    private static final long ACCEPT_PULSE_WIDTH_MILLIS = 40;
    private static final long BANKED_PULSE_WIDTH_MILLIS = 50;
    private static final long INTER_PULSE_GAP_MILLIS = 100;
    
    private static final Pin ACCEPT_PIN_ID = RaspiPin.GPIO_04;
    private static final Pin BANKED_PIN_ID = RaspiPin.GPIO_05;
    private GpioPinDigitalOutput acceptPin = null;
    private GpioPinDigitalOutput bankedPin = null;
    private final GpioController gpio = GpioFactory.getInstance();
        
    public PiPulser() {
        super();
        acceptPin = gpio.provisionDigitalOutputPin(ACCEPT_PIN_ID, "Accept", PinState.LOW);
        bankedPin = gpio.provisionDigitalOutputPin(BANKED_PIN_ID, "Banked", PinState.LOW);
    }
    
    public void sendOneSequence() {
        
        acceptPin.pulse(ACCEPT_PULSE_WIDTH_MILLIS, true); // set second argument to 'true' use a blocking call
        Utils.sleep(INTER_PULSE_GAP_MILLIS);
        bankedPin.pulse(BANKED_PULSE_WIDTH_MILLIS, true);
    }
    
    @PreDestroy
    public void preDestroy() {
        if(this.gpio != null) this.gpio.shutdown();
    }
}
