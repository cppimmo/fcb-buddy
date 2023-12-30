package fcb_buddy;

import javax.sound.midi.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public class MidiFootController {
	public MidiFootController() {
		var deviceInfos = MidiSystem.getMidiDeviceInfo();
		//var devices = devicesFromInfos(deviceInfos);
		Predicate<MidiDevice> isPort = device -> Utils.isIOMidiDevice(device);
		
		var devices = Arrays.stream(Utils.devicesFromInfos(deviceInfos))
							//.filter(isPort.negate())
							.toArray(MidiDevice[]::new);
		// MidiInDevice
		// MidiOutDevice
		
		int index = 3; 
		System.out.println("Device index " + index + 
			" is a sequencer." + devices[index].getClass().getName());
		
		System.out.println("Displaying connected MIDI devices:");
		System.out.println(new String("=").repeat(70));
		for (int i = 0; i < devices.length; i++) {
			var info = deviceInfos[i];
			var device = devices[i];
			
			System.out.printf("Name: %s\nVender: %s\nVersion: %s\nDesc: %s\n",
				info.getName(),
				info.getVendor(),
				info.getVersion(),
				info.getDescription());
			System.out.println();
				
			var recieverCount = device.getMaxReceivers();
			var transmitterCount = device.getMaxTransmitters();
			
			if (recieverCount != 0)
				System.out.println("Device is a reciever!");
			if (transmitterCount != 0)
				System.out.println("Device is a transmitter!");
		}
		
		MidiDevice device = devices[3];
		Transmitter transmitter;
		try {
			device.open();
			
			transmitter = device.getTransmitter();
			transmitter.setReceiver(new LoggingReciever(System.out));
			
			System.out.println("Press enter to quit.");
			System.in.read();
		} catch (MidiUnavailableException | IOException e) {
			e.printStackTrace();
		}
		device.close();
	}
}