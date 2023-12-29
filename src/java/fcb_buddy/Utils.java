package fcb_buddy;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

public class Utils {
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
}
