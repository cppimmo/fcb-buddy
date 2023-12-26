(ns fcb-buddy.options
  (:use [seesaw.core])
  (:import [fcb_buddy MidiFootController]
           [javax.sound.midi MidiSystem]))

(defn midi-device-dialog
  "Dialog for selecting the MIDI device that represents the FCB1010.
  Returns selected choice map which contains device info that can be used to index the selected device."
  []
  (input "Pick the MIDI OUT port representing your FCB1010"
         :title "Pick a MIDI Device"
         :choices (mapv (fn [info]
                          {:name (.getName info)
                           :vendor (.getVendor info)})
                        (MidiSystem/getMidiDeviceInfo))
         :to-string (fn [choice]
                      (str (:name choice) " | " (:vendor choice)))))

