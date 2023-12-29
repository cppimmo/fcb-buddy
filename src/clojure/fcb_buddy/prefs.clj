(ns fcb-buddy.prefs
  "Application preferences frames & utilities."
  (:require [cljfx.api :as fx])
  (:import [fcb_buddy MidiFootController]
           [javax.sound.midi MidiSystem]))

(defrecord Preferences [])

(defn load-config
  []
  (letfn [(find-cfg []
            )]
    ))

(defn save-config
  []
  )

(defn root-view
  []
  )
