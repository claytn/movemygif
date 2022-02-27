(ns app.trackers.person
  (:require
   ["../js/person-tracker.js" :refer [PersonTracker]]))

(defn start! [{:keys [video canvas on-move-change]}]
  (let [t (PersonTracker.  #js {:video video :canvas canvas :onMoveChange on-move-change})]
    (-> (.start t)
        (.catch (fn [err]
                  (js/alert (.-message err)))))))