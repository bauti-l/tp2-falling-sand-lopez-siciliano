(ns falling-sand.motor
  "Motor de la aplicación: el estado de la simulación y la implementación de la
  interfaz Java `fallingsand.Motor` que usa la capa de JavaFX
  (docs/arquitectura.md, secciones 4, 5 y 6). Es el único namespace de Clojure
  con estado y efectos.

  El estado es un mapa con dos referencias:
    :mundo            agent con el mundo inmutable: el mapa de
                      `falling-sand.simulacion` (`:grilla` y `:paso`) más
                      `:en-pausa`.
    :pulso-pendiente  atom con true mientras haya un paso de pulso encolado
                      que todavía no empezó.

  Cada cambio del mundo (pintar, avanzar, limpiar, pausar) es una transición
  `mundo -> mundo` que se le envía al agent con `send`. Las transiciones son
  funciones puras; el pulso envuelve la suya en `atender-pulso`, que además
  baja la bandera `:pulso-pendiente`. El agent ejecuta sus acciones de a una,
  en el orden en que llegaron y fuera del hilo de JavaFX: no se pierde lo
  pintado ni el avance, y ningún método bloquea al que lo llama, que es el
  hilo de JavaFX. `cuadro()` lee el último mundo calculado sin esperar.

  `detener()` también se encola: le envía al agent `apagar`, que llama a
  `shutdown-agents` recién cuando terminaron las acciones anteriores. Así
  ningún paso queda a medio calcular cuando se apagan los hilos.

  El agent usa `:error-mode :continue`: si una transición lanza una excepción,
  se imprime en `*err*`, el mundo queda como estaba y las siguientes
  transiciones corren igual.

  No importa JavaFX."
  (:require [falling-sand.grilla :as grilla]
            [falling-sand.materiales :as materiales]
            [falling-sand.pincel :as pincel]
            [falling-sand.simulacion :as simulacion])
  (:import (fallingsand Cuadro Motor)
           (java.io PrintWriter Writer)))

(def ^:private nombres-de-materiales
  "Nombres de los materiales en el orden del selector de la interfaz
  (enunciado 3), como strings: el valor de `materiales()`. Es un vector, que ya
  es una `java.util.List`."
  (mapv name materiales/orden-selector))

(def ^:private color-de
  "Mapa de cada material a su color ARGB como `int` de 32 bits con signo, que
  es como lo guarda el arreglo `int[]` de `fallingsand.Cuadro`. Se calcula una
  sola vez a partir de los `:color` de `materiales/materiales`, para que
  `cuadro()` no convierta en cada llamada."
  (into {} (map (fn [[material {:keys [color]}]] [material (unchecked-int color)]))
        materiales/materiales))
