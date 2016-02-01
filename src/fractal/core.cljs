(ns fractal.core
  (:require [fractal.canvas :as canvas]
            [fractal.complex :as complex]))

(def ctx (canvas/get-ctx-by-id "canvas"))

(defn calculate-iteration-nr [p q K]
  (loop [x p
         y q
         k 0]
    (if (or (> k K)
            (> (complex/abs x y) 2.2))
      k
      (recur (+ p (- (* x x) (* y y))) (+ q (* 2 x y)) (inc k)))))

(defn draw-fractal [{:keys [window-start-x window-start-y window-end-x window-end-y canvas-width canvas-heidth K]}]
  (let [step-x (/ (- window-end-x window-start-x) canvas-width)
        scale-x (/ 1 step-x)
        step-y (/ (- window-end-y window-start-y) canvas-heidth)
        scale-y (/ 1 step-y)]
    (doseq [x (range window-start-x window-end-x step-x)
            y (range window-start-y window-end-y step-y)]
      (let [k (calculate-iteration-nr x y K)
            x-canvas (* (- x window-start-x) scale-x)
            y-canvas (* (- y window-start-y) scale-y)]
        (canvas/draw-point
          ctx
          (canvas/rgb (* k (/ 250 K)) 0 0)
          x-canvas
          y-canvas)))))

(draw-fractal {:window-start-x -2.5
               :window-start-y -1.5
               :window-end-x   0.5
               :window-end-y   1.5
               :canvas-width   300
               :canvas-heidth  300
               :K              25})
