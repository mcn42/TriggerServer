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
    
    private static final long GREEN_LED_PULSE_WIDTH_MILLIS = 750L;
    private static final long BEEPER_PULSE_WIDTH_MILLIS = 1500L;
    
    private static final Pin ACCEPT_PIN_ID = RaspiPin.GPIO_04;
    private static final Pin BANKED_PIN_ID = RaspiPin.GPIO_05;
    private GpioPinDigitalOutput acceptPin = null;
    private GpioPinDigitalOutput bankedPin = null;
    
    private static final Pin GREEN_LED_PIN_ID = RaspiPin.GPIO_00;
    private static final Pin BEEPER_PIN_ID = RaspiPin.GPIO_01;
    private GpioPinDigitalOutput greenLedPin = null;
    private GpioPinDigitalOutput beeperPin = null;
    private boolean fareboxMode = false;
    
    private final GpioController gpio = GpioFactory.getInstance();
    
    public PiPulser(boolean fareboxMode) {
        super();
        this.fareboxMode = fareboxMode;
        if(this.fareboxMode) {
            setUpFareboxMode();
        } else {
            this.setUpTurnstileMode();
        }
        Utils.getLogger().info(String.format("Starting in %s mode",fareboxMode?"Farebox":"Turnstile"));
    }
    
    public void sendTurnstileSequence() {
        if(this.fareboxMode) throw new RuntimeException("Cannot send Turnstile sequence in Farebox mode");
        acceptPin.pulse(ACCEPT_PULSE_WIDTH_MILLIS, true); // set second argument to 'true' use a blocking call
        Utils.sleep(INTER_PULSE_GAP_MILLIS);
        bankedPin.pulse(BANKED_PULSE_WIDTH_MILLIS, true);
    }
    
    public void sendFareboxSequence() {
        if(!this.fareboxMode) throw new RuntimeException("Cannot send Farebox sequence in Turnstile mode");
        this.greenLedPin.pulse(GREEN_LED_PULSE_WIDTH_MILLIS,false);
        this.beeperPin.pulse(BEEPER_PULSE_WIDTH_MILLIS,true);
    }


    public boolean isFareboxMode() {
        return fareboxMode;
    }

    @PreDestroy
    public void preDestroy() {
        if(this.gpio != null) this.gpio.shutdown();
    }

    private void setUpTurnstileMode() {
        acceptPin = gpio.provisionDigitalOutputPin(ACCEPT_PIN_ID, "Accept", PinState.LOW);
        bankedPin = gpio.provisionDigitalOutputPin(BANKED_PIN_ID, "Banked", PinState.LOW);
        this.acceptPin.setShutdownOptions(true, PinState.LOW);
        this.bankedPin.setShutdownOptions(true, PinState.LOW);
    }

    private void setUpFareboxMode() {
        this.greenLedPin = gpio.provisionDigitalOutputPin(GREEN_LED_PIN_ID, "Green_LED", PinState.LOW);
        this.beeperPin = gpio.provisionDigitalOutputPin(BEEPER_PIN_ID, "Beeper", PinState.LOW);
        this.greenLedPin.setShutdownOptions(true, PinState.LOW);
        this.beeperPin.setShutdownOptions(true, PinState.LOW);
    }
}
