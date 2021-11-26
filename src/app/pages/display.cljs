(ns app.pages.display
  (:require
   [helix.core :refer [defnc]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [app.gif :as gif]
   [app.trackers.head :as htracker]
   [app.trackers.contour :as ctracker]))

(declare display gif-frames)

(defnc display [{:keys [gif-frames]}]

  (let [canvas-ref (hooks/use-ref nil)
        video-ref (hooks/use-ref nil)
        scratch-ref (hooks/use-ref nil)
        [debugging set-debugging!] (hooks/use-state true)
        [gp set-gp!] (hooks/use-state nil)
        [tracking set-tracking!] (hooks/use-state false)
        [tracker set-tracker!] (hooks/use-state :head)
        toggle-tracking! (fn []

                           (if tracking
                             (ctracker/stop!)

                             (let [start! (if (= tracker :head) htracker/start! ctracker/start!)]
                               (start! {:video (.-current video-ref)
                                        :canvas (.-current scratch-ref)
                                        :on-move-change (fn [move]

                                                          (case (keyword move)
                                                            :play (.play gp)
                                                            :rewind (.rewind gp)
                                                            :pause (.pause gp)))})))
                           (set-tracking! (fn [t] (not t))))]

    ;; TODO: FIGURE OUT THE SCHEDULING OF RENDER ELEMENTS VS use-effect being called
    (hooks/use-effect
     :always
     (if (nil? gp)
       (let [gif-player (gif/create-gif-player gif-frames (.-current canvas-ref))]
         (.start gif-player)
         (set-gp! gif-player))))

    (d/div {:style {:display "flex" :flex-direction "row"}}

           (d/canvas {:id "gif-display" :ref canvas-ref :style {:display "block"}})

           (d/div {:style {:display "flex" :flex-direction "column"}}

                  (if (not tracking)
                    (d/div
                     (d/div {:class "customize-menu"}
                            (d/button {:class "tracking-btn"
                                       :on-click toggle-tracking!}
                                      "Start Tracking"))
                     (d/div {:margin-top 50}
                            (d/h4 "Tracker type:")
                            (d/div
                             (d/input {:type "radio" :name "head-tracker" :on-change #(set-tracker! :head) :checked (= tracker :head)})
                             (d/label {:for "head-tracker"} "Head tracker")
                             (d/br)
                             (d/input {:type "radio" :name "contour-tracker" :on-change #(set-tracker! :contour) :checked (= tracker :contour)})
                             (d/label {:for "contour-tracker"} "Contour tracker"))))))




           (d/div {:class "debugging-tools" :style {:display (if debugging "block" "none") :position "absolute" :bottom 0 :right 0}}
                  (d/p "Live feed")
                  (d/video {:id "video-feed" :ref video-ref :style {:width "100px"}})
                  (d/p "Tracking Details")
                  (d/canvas {:id "output" :ref scratch-ref :style {:width "100px"}})))))