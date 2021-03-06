(ns app.pages.display
  (:require
   [helix.core :refer [defnc]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [app.gif :as gif]
   [app.trackers.head :as htracker]
   [app.trackers.contour :as ctracker]
   [app.trackers.person :as ptracker]))


(declare display gif-url)

(defnc display [{:keys [gif-url]}]
  (let [video-ref (hooks/use-ref nil)
        scratch-ref (hooks/use-ref nil)
        image-ref (hooks/use-ref nil)
        [gp set-gp!] (hooks/use-state nil)
        [debugging set-debugging!] (hooks/use-state false)
        [tracker set-tracker!] (hooks/use-state :person)
        [tracking set-tracking!] (hooks/use-state false)
        start-tracking! (fn [] 
                          (gif/pause! gp)
                          (let [start! (case tracker
                                         :head htracker/start!
                                         :contour ctracker/start!
                                         :person ptracker/start!)]
                            (start! {:video (.-current video-ref)
                                     :canvas (.-current scratch-ref)
                                     :on-move-change (fn [move]
                                                       (case (keyword move)
                                                         :play (gif/play! gp)
                                                         :rewind (gif/rewind! gp)
                                                         :pause (gif/pause! gp)))}))
                          (set-tracking! true))]
    
    (hooks/use-effect
     :once 
     (let [gif-player (gif/create-gif-player! {:url gif-url :image (.-current image-ref)})]
       (set-gp! gif-player)))

    (d/div {:style {:display "flex" 
                    :flex 1 
                    :flex-direction "row" 
                    :justify-content (if tracking "center" "flex-start")
                    :background (if (and tracking (not debugging)) "#000" "inherit")}}

           (d/div {:style {:margin-top (if tracking "5%" "0")}}
            (d/img {:ref image-ref :rel:auto_play "1" :rel:rubbable "1"}))

           (d/div {:style {:display "flex" :flex-direction "column"}}

                  (if (not tracking)
                    (d/div {:style {:margin-left 10}}
                           (d/div {:style {:padding 10 :border "black solid 2px" :max-width 600}}
                                  (d/div
                                   (d/h2 {:style {:margin-top 0}} "Settings")
                                   (d/h3 {:class "settings-section-header"} "Tracker type:")
                                   (d/p
                                    {:class "settings-description"}
                                    "Determines the method to track movement in camera. Unsure which to choose? Test out both! 
                               Typically, the head tracker will work best for long-term use, but it requires good lighting and isn't as snappy as the contour tracker.
                               The contour tracker won't perform well overtime as the background changes subtly, but is much quicker to detect movement in the short-term.")
                                   (d/div
                                    (d/label {:for "head-tracker"} "Head tracker")
                                    (d/input {:type "radio" :name "head-tracker" :on-change #(set-tracker! :head) :checked (= tracker :head)})
                                    (d/br)
                                    (d/label {:for "contour-tracker"} "Contour tracker")
                                    (d/input {:type "radio" :name "contour-tracker" :on-change #(set-tracker! :contour) :checked (= tracker :contour)})
                                    (d/br)
                                    (d/label {:for "person-tracker"} "Person tracker")
                                    (d/input {:type "radio" :name "person-tracker" :on-change #(set-tracker! :person) :checked (= tracker :person)}))

                                   (d/h3 {:class "settings-section-header"} "Debugging:")
                                   (d/p {:class "settings-description"} "Want to see what's being tracked? Turn on debugging options!")
                                   (d/label {:for "head-tracker"} "Debugger Enabled")
                                   (d/input {:type "radio" :name "debug" :on-click #(set-debugging! not) :on-change (fn []) :checked debugging})))

                           (d/button {:class "btn"
                                      :style {:margin-top 10}
                                      :on-click start-tracking!}
                                     "Start Tracking"))))


           (d/div {:class "debugging-tools" :style {:display (if (and debugging tracking) "block" "none") :position "absolute" :bottom 0 :right 0}}

                  (d/p "Tracking Details")
                  (d/canvas {:id "output" :ref scratch-ref :style {:width "250px" :height "200px"}})

                  (d/p {:style {:margin-bottom 0}} "Video feed")
                  (d/video {:id "video-feed" :ref video-ref :style {:width "250px" :height "240px"}})))))