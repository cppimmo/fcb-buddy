(defproject fcb-buddy "0.1.0-SNAPSHOT"
  :description "Programming and configuration tool for FCB1010 MIDI Foot Controller."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.cache "1.0.225"]
                 [cljfx "1.7.24"]
                 [environ "1.2.0"]]
  :plugins [[lein-environ "1.2.0"]
            [lein-pprint "1.3.2"]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :main ^:skip-aot fcb-buddy.core
  :target-path "target/%s"
  :profiles {
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
