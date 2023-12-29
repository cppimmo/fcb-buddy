package fcb_buddy;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class LoggingReciever implements Receiver {
	private boolean enabled;

	public LoggingReciever() {
		enabled = false;
	}
	
	@Override
	public void send(MidiMessage message, long timeStamp) {
		if ((message instanceof ShortMessage) && enabled) {
			ShortMessage shortMessage = (ShortMessage) message;
			int command = shortMessage.getCommand();
			int channel = shortMessage.getChannel();
			int data1 = shortMessage.getData1();
			int data2 = shortMessage.getData2();
			
			System.out.println();
			System.out.printf(
				"Command: %s(%d)\nChannel: %d\nData1: %d\nData2: %d",
				Utils.getCommandName(command), command, channel, data1, data2);
			System.out.println();
		}
	}
	
	@Override
	public void close() { }

	public void enable() { enabled = true; }
	public void disable() { enabled = false; }
}
