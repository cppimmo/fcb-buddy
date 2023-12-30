(ns fcb-buddy.core
  "Application frames & entry point."
  (:require [clojure.core.cache :as cache]
            [clojure.java.browse :refer [browse-url]]
            [clojure.main :as m]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [cljfx.api :as fx]
            [fcb-buddy.midi :as midi]
            [fcb-buddy.prefs :as prefs]
            [fcb-buddy.utils :as utils]
            [environ.core :refer [env]])
  (:import [fcb_buddy MidiFootController Utils]
           [javafx.application Platform])
  (:gen-class))

(comment
  (let [root "https://mediadl.musictribe.com/media/PLM/data/docs/P0089/"]
    (defn- help-man [e]
      (letfn [(url [lang]
                (str root
                     "FCB1010_P0089_M_"
                     lang
                     ".pdf"))
              (language-dialog []
                (input "Choose a language."
                       :choices [{:lang "Chinese"    :url "CN"}
                                 {:lang "Danish"     :url "DK"}
                                 {:lang "Dutch"      :url "NL"}
                                 {:lang "English"    :url "EN"}
                                 {:lang "Finnish"    :url "FI"}
                                 {:lang "French"     :url "FR"}
                                 {:lang "German"     :url "DE"}
                                 {:lang "Greek"      :url "GR"}
                                 {:lang "Italian"    :url "IT"}
                                 {:lang "Japanese"   :url "JP"}
                                 {:lang "Portuguese" :url "PT"}
                                 {:lang "Spanish"    :url "ES"}
                                 {:lang "Swedish"    :url "SE"}]
                       :to-string :lang))]
        (-> (language-dialog)
            :url
            url
            browse-url)))
    (defn- help-quickstart [e]
      (let [f "QSG_BE_0715-AAA_FCB1010_WW.pdf"]
        (browse-url (str root f)))))

  (defn- get-menu-items
    [prefix]
    (vec (vals (utils/filter-by-prefix menu-items prefix)))))
;;; Stores application state.
(defrecord State [prefs])

(def *state
  (atom true))

(def renderer
  (fx/create-renderer))
(def *context
  (atom
   (fx/create-context {}
                      #(cache/lru-cache-factory % :threshold 4096))))

(defmulti event-handler :event/type)

(defmethod event-handler :default [e]
  (prn e))

(defmethod event-handler ::close [_]
  (println "Closing")
  (reset! *state false))

(def menu-actions
  {:file {:open (fn [_])
          :quit (fn [_] (Platform/exit))}
   :edit {:prefs (fn [_])}
   :midi nil
   :tools nil
   :help {:docs (fn [_]
                  (let [docs-url ""]
                    (browse-url docs-url)))
          :man (fn [_])
          :quick-start (fn [_])
          :about (fn [_]
                   (fx/create-component
                    {:fx/type :dialog
                     :showing true
                     :header-text "About"
                     :dialog-pane {:fx/type :dialog-pane
                                   :content-text
                                   (str/join "\n"
                                             [(let [project-version (:fcb-buddy-version env)]
                                                (str "FCB Buddy - Version " project-version))
                                              ""
                                              "Programming and configuration tool for FCB1010 MIDI Foot Controller."])
                                   :button-types [:close]}}))}})

(defn- separator-menu-item [_]
  {:fx/type :custom-menu-item
   :disable true
   :content {:fx/type :separator}})

(defn- menu-bar-view [_]
  (letfn [(get-action [& ks]
            (get-in menu-actions ks))]
    {:fx/type :menu-bar
     :menus [{:fx/type :menu
              :text "File"
              :items [{:fx/type :menu-item
                       :text "Open SYSEX"
                       :accelerator "Shortcut+O"
                       :on-action (get-action :file :open)}
                      {:fx/type separator-menu-item}
                      {:fx/type :menu-item
                       :text "Quit"
                       :accelerator "Shortcut+Q"
                       :on-action (get-action :file :quit)}]}
             {:fx/type :menu
              :text "Edit"
              :items [{:fx/type :menu-item
                       :text "Preferences"
                       :accelerator "Shortcut+P"
                       :on-action (get-action :edit :prefs)}]}
             {:fx/type :menu
              :text "MIDI"
              :items []}
             {:fx/type :menu
              :text "Tools"
              :items []}
             {:fx/type :menu
              :text "Help"
              :items [{:fx/type :menu-item
                       :text "Documentation"
                       :accelerator "F1"
                       :on-action (get-action :help :docs)}
                      {:fx/type :menu-item
                       :text "FCB1010 Manual"
                       :on-action (get-action :help :man)}
                      {:fx/type :menu-item
                       :text "FCB1010 Quickstart"
                       :on-action (get-action :help :quick-start)}
                      {:fx/type separator-menu-item}
                      {:fx/type :menu-item
                       :text "About"
                       :on-action (get-action :help :about)}]}]}))

(defn- root-view [_]
  {:fx/type :stage
   :showing true
   :title "FCB Buddy"
   :width 960
   :height 540
   :scene {:fx/type :scene
           :stylesheets #{(utils/resource-path "css/styles.css")}
           :root {:fx/type :v-box
                  :children
                  [{:fx/type menu-bar-view}
                   {:fx/type :tab-pane
                    :pref-width 960
                    :pref-height 540
                    :tabs [{:fx/type :tab
                            :text "Presets"
                            :closable false
                            :content {:fx/type :label
                                      :text "Hello, world!"}}
                           {:fx/type :tab
                            :text "MIDI Messages"
                            :closable false
                            :content {:fx/type :border-pane
                                      :center {:fx/type :text-area
                                               :editable false
                                               :text (str/join "\n" (take 25 (repeat "Hello, world!")))}
                                      :bottom {:fx/type :h-box
                                               :padding {:top 15.0
                                                         :bottom 15.0
                                                         :left 15.0
                                                         :right 15.0}
                                               :spacing 5.0
                                               :children [{:fx/type :button
                                                           :text "Resume"}
                                                          {:fx/type :button
                                                           :text "Pause"}]}}}]}]}}})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;(Platform/setImplicitExit true) ; Shut JavaFX runtime down when last window is closed
  (fx/create-app
   *context
   :event-handler event-handler
   :desc-fn root-view
   :renderer-middleware identity ; TODO: Add custom middleware (identity is the default).
   :renderer-error-handler (fn [e]
                             (if (instance? Exception e)
                               (fx/create-component ; Display exceptions in an alert dialog
                                {:fx/type :alert
                                 :alert-type :error
                                 :showing true
                                 :header-text "An error occurred!"
                                 :dialog-pane {:fx/type :dialog-pane
                                               :content-text (-> e Throwable->map m/ex-triage m/ex-str)
                                               :expandable-content {:fx/type :label
                                                                    :text (with-out-str (pp/pprint e))}
                                               :button-types [(javafx.scene.control.ButtonType. "Quit")
                                                              (javafx.scene.control.ButtonType. "Continue")]}})
                               (throw e)))))
