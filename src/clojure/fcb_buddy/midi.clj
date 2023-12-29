(ns fcb-buddy.midi
  "MIDI interface to the FCB1010."
  (:require [cljfx.api :as fx])
  (:import [fcb_buddy MidiFootController]
           [javafx.scene.control Dialog DialogEvent]
           [javax.sound.midi MidiSystem]))

(defn- device-dialog
  "Dialog for selecting the MIDI device that represents the FCB1010.
  Returns selected choice map which contains device info that can be used to index the selected device."
  [type]
  (comment (input "Pick the MIDI OUT port representing your FCB1010"
                  :title "Pick a MIDI Device"
                  ;; MidiDevice.Info members: description, name, vendor, & version
                  :choices (mapv bean (MidiSystem/getMidiDeviceInfo))
                  :to-string (fn [choice]
                               (str (:name choice) " | " (:vendor choice)))))
  (letfn [(io->str [type]
            (condp = type
              :in "IN"
              :out "OUT"))]
    (fx/create-component
     {:fx/type :choice-dialog
      :showing true
      :header-text (str "Pick a MIDI " (io->str type) " Device")
      :content-text (str "Choose the MIDI "
                         (io->str type)
                         " port representing your FCB1010")
      :on-close-request (fn [^DialogEvent e]
                          (if (nil? (.getResult ^Dialog (.getSource e)))
                            (.consume e)
                            (.getResult ^Dialog (.getSource e))))
                         
      :items (mapv bean (MidiSystem/getMidiDeviceInfo))})))

(defn in-device-dialog [_]
  (device-dialog :in))

(defn out-device-dialog [_]
  (device-dialog :out))
