(ns fcb-buddy.midi
  "MIDI interface to the FCB1010."
  (:require [cljfx.api :as fx])
  (:import [fcb_buddy MidiFootController]
           [javafx.scene.control Dialog DialogEvent]
           [javax.sound.midi MidiSystem]))

(defn io-type->str
  "Return string representation of MIDI type :in or :out."
  [type]
  (condp = type
    :in "IN"
    :out "OUT"))

(defn- device-dialog
  "Dialog for selecting the MIDI device that represents the FCB1010.
  Returns selected choice map which contains device info that can be used to index the selected device."
  [type]
  ;; MidiDevice.Info members: description, name, vendor, & version
  (letfn [(item->str [choice]
            (str (:name choice) " | " (:vendor choice)))]
    (let [items (mapv bean (MidiSystem/getMidiDeviceInfo))
          type-str (io-type->str type)]
      (fx/create-component
       {:fx/type :choice-dialog
        :showing true
        :header-text (str "Pick a MIDI " type-str " Device")
        :content-text (str "Choose the MIDI " type-str " port representing your FCB1010")
        :on-close-request (fn [^DialogEvent e]
                            (when (nil? (.getResult ^Dialog (.getSource e)))
                              (.consume e)))
        :items (mapv item->str items)
        :selected-item (-> items first item->str)}))))

(defn in-device-dialog []
  (device-dialog :in))

(defn out-device-dialog []
  (device-dialog :out))
