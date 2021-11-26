(ns app.pages.home
  (:require
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [app.pages.search :refer [search]]
   [app.pages.display :refer [display]]))



(declare home)
(defnc home []
  (let [[gif set-gif!] (hooks/use-state nil)]

    (d/div
     (d/div
      {:style {:position "absolute" :right 10 :top 5}}
      (d/p  "movemygif v0.1")
      (d/a {:href "https://github.com/claytn/movemygif" :style {:float "right"}} "source"))

     ;; When GIF loaded, show display page. Otherwise, assume search page
     ;; A+ routing if you ask me
     (if gif
       ($ display {:gif-frames gif})

       ($ search {:handle-selection! (fn [{:keys [frames url]}]
                                       (set-gif! frames))})))))