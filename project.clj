(defproject falling-sand "0.1.0-SNAPSHOT"
  :description "Simulador de materiales (falling sand). TP2 de TB025 Paradigmas de Programacion, FIUBA."
  :dependencies [[org.clojure/clojure "1.12.6"]
                 [org.openjfx/javafx-controls "21.0.13"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :test-paths ["test"]
  :javac-options ["--release" "17" "-encoding" "UTF-8"]
  :main falling-sand.core)