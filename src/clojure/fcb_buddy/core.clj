(ns fcb-buddy.core
  "Application frames & entry point."
  (:use [seesaw.core]
        [seesaw.widgets.log-window])
  (:require [clojure.core.cache :as cache]
            [clojure.java.browse :refer [browse-url]]
            [clojure.main :as m]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [cljfx.api :as fx]
            ;;[cljfx.controls :as controls]
            ;;[cljfx.stylesheet :as style]
            [fcb-buddy.midi :as midi]
            [fcb-buddy.prefs :as prefs]
            [fcb-buddy.utils :as utils]
            [environ.core :refer [env]])
  (:import [fcb_buddy MidiFootController Utils])
  (:gen-class))

(defn- file-open [e]
  nil)

(defn- file-quit [e]
  (invoke-later (dispose! e)))

(defn- edit-prefs [e]
  nil)

(defn- help-docs [e]
  (let [docs-url ""]
    (browse-url docs-url)))

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

(defn- help-about [e]
  (invoke-later
    (-> (dialog :content
                (str/join "\n"
                          [(let [project-version (:fcb-buddy-version env)]
                             (str "FCB Buddy - Version " project-version))
                           "Programming and configuration tool for FCB1010 MIDI Foot Controller."]))
        pack!
        show!)))

;; TODO: Fix since map order is not guarunteed.
(def menu-items
  {:file-open (action :handler file-open :name "Open" :tip "Open SYSEX file." :key "menu O")
   :file-sep1 (separator)               ; First file menu seperator
   :file-quit (action :handler file-quit :name "Quit" :tip "Quit FCB Buddy." :key "menu Q")
   :edit-prefs (action :handler edit-prefs :name "Preferences" :tip "Edit the user preferences." :key "menu P")
   :help-docs (action :handler help-docs :name "Documentation" :tip "View the online documentation." :key "F1")
   :help-man (action :handler help-man :name "FCB1010 Manual" :tip "View the FCB1010 manual.")
   :help-quickstart (action :handler help-quickstart :name "FCB1010 Quickstart" :tip "View the FCB1010 quickstart manual.")
   :help-sep1 (separator)               ; First help menu separator
   :help-about (action :handler help-about :name "About" :tip "")})

(defn- get-menu-items
  [prefix]
  (vec (vals (utils/filter-by-prefix menu-items prefix))))

(defn make-frame
  []
  (frame
   :title "FCB Buddy"
   :size [640 :by 480]
   ;;:on-close :exit
   :menubar (menubar
             :items [(menu :text "File" :items (get-menu-items :file))
                     (menu :text "Edit" :items (get-menu-items :edit))
                     (menu :text "MIDI" :items (get-menu-items :midi))
                     (menu :text "Tools" :items (get-menu-items :tools))
                     (menu :text "Help" :items (get-menu-items :help))])
   :content
   (tabbed-panel :placement :top
                 :overflow :scroll
                 :tabs [{:title "Presets"
                         :tip "Edit presets."
                         :content "Hello, world!"}
                        {:title "MIDI Messages"
                         :tip "View MIDI message log."
                         :content (border-panel
                                   :north (scrollable (log-window :id :log-window
                                                                  :limit nil
                                                                  ))
                                   :south (horizontal-panel :items [(button :id :log-resume :text "Resume")
                                                                    (button :id :log-pause :text "Pause")]))}])))

(defn- logger
  [lw message go]
  (log lw (str (java.util.Date.) message)))

(defn- add-behaviors
  [frame]
  (let [{:keys [log-window start stop limit limit?]} (group-by-id frame)]
    (listen
     limit?
     :selection (fn [_] (config! log-window :limit (if (value limit?)
                                                     (value limit)))))
    (listen stop
            :action (fn [_] nil))
    (listen
     start
     :action (fn [_]
               (logger log-window "Hello, world!" true))))
  frame)

(def renderer
  (fx/create-renderer))

(def *state
  (atom true))

(def *context
  (atom
   (fx/create-context {}
                      #(cache/lru-cache-factory % :threshold 4096))))

(defmulti event-handler :event/type)

(defmethod event-handler :default [e]
  (prn e))

(defmethod event-handler ::close [_]
  (reset! *state false))

(def menu-actions
  {:file {:open (fn [_])
          :quit (fn [_])}
   :edit {:prefs (fn [_])}
   :midi nil
   :tools nil
   :help {:docs (fn [_])
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

(defn- menu-bar [_]
  (letfn [(get-action [& ks]
            (get-in menu-actions ks))]
    {:fx/type :menu-bar
     :menus [{:fx/type :menu
              :text "File"
              :items [{:fx/type :menu-item
                       :text "Open SYSEX"
                       :accelerator "Shortcut+O"
                       :on-action (get-action :file :open)}
                      {:fx/type :menu-item
                       :disable true}
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
                      {:fx/type :menu-item
                       :disable true}
                      {:fx/type :menu-item
                       :text "About"
                       :on-action (get-action :help :about)}]}]}))

(defn- root [_]
  {:fx/type :stage
   :showing true
   :title "FCB Buddy"
   :width 960
   :height 540
   :scene {:fx/type :scene
           :stylesheets #{(utils/resource-path "css/styles.css")}
           :root {:fx/type :v-box
                  :children [{:fx/type menu-bar}
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
                                      :content {:fx/type :label
                                                :text "Hello, world!"}}]}]}}})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (comment (invoke-later
             (-> (make-frame)
                 ;;add-behaviors
                 show!)))
  (fx/create-app
   *context
   :event-handler event-handler
   :desc-fn root
   :renderer-middleware identity ; TODO: Custom middleware.
   :renderer-error-handler (fn [e]
                             (if (instance? Exception e)
                               (fx/create-component
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

