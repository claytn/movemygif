(ns app.trackers.contour
  (:require ["../js/contour-tracker.js" :refer [start]]))


(defn stop! []
  ;; Stop the stream & interval
  (println "IMPLEMENT STOP!"))

(defn start! [{:keys [video canvas on-move-change]}]
  (start video canvas on-move-change))

