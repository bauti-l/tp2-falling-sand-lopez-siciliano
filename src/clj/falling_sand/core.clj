(ns falling-sand.core
  (:require [falling-sand.motor :as motor])
  (:import (fallingsand.ui VentanaPrincipal)
           (javafx.application Platform)))

(def ^:private ancho-grilla
  160)

(def ^:private alto-grilla
  120)

(defn- abrir-ventana
  [motor]
  (.mostrar (VentanaPrincipal. motor)))

(defn -main
  [& _args]
  (let [motor (motor/crear ancho-grilla alto-grilla)]
    (Platform/startup #(abrir-ventana motor))))