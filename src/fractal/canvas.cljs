(ns fractal.canvas)

(defn get-canvas-by-id [id]
  (.getElementById js/document id))

(defn get-ctx-by-id [id]
  (.getContext (get-canvas-by-id id) "2d"))

(defn rgb [r g b]
  (str "rgb(" r "," g "," b ")"))

(defn draw-line
  ([ctx x0 y0 x1 y1]
   (draw-line ctx x0 y0 x1 y1 1))
  ([ctx x0 y0 x1 y1 width]
   (.moveTo ctx x0 y0)
   (.lineTo ctx x1 y1)
   (.-lineWidth width)
   (.stroke ctx)))

(defn fill-rect [ctx color x0 y0 x1 y1]
  (set! (. ctx -fillStyle) color)
  (.fillRect ctx x0 y0 x1 y1))
