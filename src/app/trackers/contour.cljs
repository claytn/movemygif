(ns app.trackers.contour
  (:require ["../js/contour-tracker.js" :refer [ContourTracker]]))

(defn start! [{:keys [video canvas on-move-change]}]
  (let [t (ContourTracker. #js {:video video :canvas canvas :onMoveChange on-move-change})]
    (.start t)))

