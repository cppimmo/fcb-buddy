(ns fcb-buddy.core
  (:use [seesaw.core])
  (:require [fcb-buddy.options :as options])
  (:import [fcb_buddy MidiFootController Utils])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (comment (invoke-later
             (-> (frame :title "FCB Buddy",
                        :content "Hello, world!",
                        :on-close :exit)
                 pack!
                 show!)))
  (comment (new MidiFootController))
  (options/midi-device-dialog))

