(ns app.pages.home
  (:require
   [helix.core :refer [defnc]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [app.gif :as gif]
   [app.tracker :as tracker]))

(declare home)
(defnc home []


  (let [[search set-search!] (hooks/use-state "")
        [gp set-gp!] (hooks/use-state nil)
        [tracking set-tracking!] (hooks/use-state false)
        [error set-error!] (hooks/use-state nil)
        toggle-tracking! (fn []
                           (if tracking
                             (tracker/stop!)

                             (tracker/start! {:video (js/document.getElementById "video-feed")
                                              :canvas (js/document.getElementById "output")
                                              :on-move-change (fn [move]
                                                                (case move
                                                                  :play (.play gp)
                                                                  :rewind (.rewind gp)
                                                                  :pause (.pause gp)))}))
                           (set-tracking! (fn [t] (not t))))]


    (d/div
     (d/div
      {:style {:position "absolute" :right 10 :top 5}}
      (d/p  "movemygif v0.1")
      (d/a {:href "" :style {:float "right"}} "source"))

     (if gp
       (d/div
        (if (not tracking)
          (d/button {:class "tracking-btn"
                     :on-click toggle-tracking!}
                    "Start Tracking"))
        (d/p "For best experience use in a well lit room and start by keeping around 3 feet away from camera"))





       ;; Show interaction tools if the gif player hasn't been loaded.
       (d/div {:class "interaction-tools"}
              (d/form
               {:on-submit (fn [e]
                             (.preventDefault e)

                             (let [gpp (gif/create-gif-player search)]
                               (-> gpp
                                   (.then (fn [gp]
                                            (.start gp)
                                            (set-gp! gp)
                                            (set-error! nil)))
                                   (.catch (fn [err]
                                             (set-error! err)))))

                             (set-search! ""))}

               (d/h1 {:class "gif-search-heading"} "Paste GIF URL below:")
               (d/div {:class "gif-search-wrapper"}
                      (d/input {:class "gif-search"
                                :name "url"
                                :placeholder "(https://media.giphy.com/media/.../giphy.gif)"
                                :value search
                                :on-change #(set-search! (.. %
                                                             -target
                                                             -value))})
                      (d/button {:class "search-submit" :type "submit"} "Search"))


               (if error
                 (d/p {:class "error"} "Error fetching gif. Try another URL."))

               (d/p "Don't want to go find a GIF? Copy this URL to test it out:")
               (d/a {:href "https://media.giphy.com/media/3o7buijTqhjxjbEqjK/giphy.gif"} "https://media.giphy.com/media/3o7buijTqhjxjbEqjK/giphy.gif"))))

     (d/video {:id "video-feed" :width 400 :height 300})
     (d/canvas {:id "output" :width 400 :height 300})
     (d/canvas {:id "gif-display" :style {:display (if gp "block" "none")}}))))