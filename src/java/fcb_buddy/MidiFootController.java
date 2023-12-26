package fcb_buddy;

import javax.sound.midi.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public class MidiFootController {	
	public static boolean isIOMidiDevice(MidiDevice device) {
		return ((device instanceof Sequencer) || (device instanceof Synthesizer));
	}
	
	public static String getCommandName(int command) {
		return switch (command) {
			case ShortMessage.NOTE_OFF -> "NOTE_OFF";
			case ShortMessage.POLY_PRESSURE -> "POLY_PRESSURE";
			case ShortMessage.CONTROL_CHANGE -> "CONTROL_CHANGE";
			case ShortMessage.PROGRAM_CHANGE -> "PROGRAM_CHANGE";
			case ShortMessage.CHANNEL_PRESSURE -> "CHANNEL_PRESSURE";
			case ShortMessage.PITCH_BEND -> "PITCH_BEND";
			default ->"Unknown";
		};
	}
	
	public static MidiDevice[] devicesFromInfos(MidiDevice.Info[] infos) {
		var devices = new MidiDevice[infos.length];
		for (int i = 0; i < infos.length; i++) {
			try {
				devices[i] = MidiSystem.getMidiDevice(infos[i]);
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return devices;
	}
	
	public MidiFootController() {
		var deviceInfos = MidiSystem.getMidiDeviceInfo();
		//var devices = devicesFromInfos(deviceInfos);
		Predicate<MidiDevice> isPort = device -> isIOMidiDevice(device);
		
		var devices = Arrays.stream(devicesFromInfos(deviceInfos))
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
		
		
		CustomReceiver customReceiver = new CustomReceiver();
		
		Transmitter transmitter;
		try {
			device.open();
			
			transmitter = device.getTransmitter();
			transmitter.setReceiver(customReceiver);
			
			System.out.println("Press enter to quit.");
			System.in.read();
		} catch (MidiUnavailableException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		device.close();
	}

	static class CustomReceiver implements Receiver {
		@Override
		public void send(MidiMessage message, long timeStamp) {
			if (message instanceof ShortMessage) {
				ShortMessage shortMessage = (ShortMessage) message;
				int command = shortMessage.getCommand();
				int channel = shortMessage.getChannel();
				int data1 = shortMessage.getData1();
				int data2 = shortMessage.getData2();
				
				System.out.println();
				System.out.printf(
					"Command: %s(%d)\nChannel: %d\nData1: %d\nData2: %d",
					getCommandName(command), command, channel, data1, data2);
				System.out.println();
			}
		}

		@Override
		public void close() { }
	}
}
