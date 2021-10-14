(ns app.pages.home
  (:require
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["face-api.js" :as faceapi]
   [app.gif :refer [gif-player]]
   [app.face-detect :refer [listen-to-video-stream get-face-detection load-faceapi-models]]
   [app.use-interval :refer [use-interval]]))

(def THRESHOLD 5)

(defn max-by [f xs]
  (reduce f xs))

(def ACTION_BUFFER_LENG 5)
(def action-history (atom []))
(defn handle-detection [detection {:keys [action subject-height]} set-state!]
  (if-not detection
    ;; Update last action to pause
    (swap! action-history (fn [prev] (conj (butlast prev) :pause)))

    (let [box (.-_box detection)
          height (.-_height box)
          previous-height subject-height]

      (if subject-height
        (let [add-action! #(swap! action-history (fn [prev] (if (>= (count prev) ACTION_BUFFER_LENG)
                                                      ;; Only keep 5 elements at a time
                                                              (conj (butlast prev) %)
                                                              (conj prev %))))
              closer (> (- height previous-height) THRESHOLD)
              further (> (- previous-height height) THRESHOLD)]

          (cond
            closer
            (add-action! :play)

            further
            (add-action! :rewind)

            (and
             (not further)
             (not closer))
            (add-action! :pause))
          
          (println @action-history)

          (let [kv-pair
                (max-by (fn [[k1 v1 :as x] [k2 v2 :as y]]
                          (cond
                            (> v1 v2) x
                            (> v2 v1) y
                            :else x))
                        (frequencies @action-history))
                next-action (if (nil? kv-pair) nil (first kv-pair))]
            (if (not (nil? next-action))
              (set-state! (fn [prev] (assoc prev :action next-action)))))))

      (set-state! (fn [prev] (assoc prev :subject-height height))))))


(defn draw-detection [detection video canvas]
  (if detection
    (let [resizedDetection
          (.resizeResults faceapi detection #js{:width (.-width video)
                                                :height (.-height video)})]
      (.drawDetections (.-draw faceapi) canvas resizedDetection))))

(declare home)
(defnc home []
  (let [[state set-state!] (hooks/use-state {:action :pause
                                             :subject-height nil})]

    (hooks/use-effect
     []
     (-> (listen-to-video-stream (js/document.getElementById "video-feed"))
         (.then load-faceapi-models)))

    (use-interval
     (fn []
       ;; TODO: Check that the video element has data to be processed
       (let [video (js/document.getElementById "video-feed")]

         (-> (get-face-detection video)
             (.then (fn [detection]
                      (let [canvas (js/document.getElementById "output")
                            ctx (.getContext canvas "2d")]

                    ;; draw detection?
                        (.drawImage ctx video 0 0 (.-width video) (.-height video))
                        (draw-detection detection video canvas)

                        ;; SETTING STATE HERE SLOWS THIS SHIT DOWN A LOT.
                        (set-state! (fn [prev] (assoc prev :action :woof)))
                        ;; (if detection
                        ;;   (let [previous-height (:subject-height state)
                        ;;         box (.-_box detection)
                        ;;         height (.-_height box)

                        ;;         closer (> (- height previous-height) THRESHOLD)
                        ;;         further (> (- previous-height height) THRESHOLD)]

                        ;;     (cond
                        ;;       closer
                        ;;       (set-state! (fn [prev] (assoc prev :action :play)))

                        ;;       further
                        ;;       (set-state! (fn [prev] (assoc prev :action :rewind)))

                        ;;       (and
                        ;;        (not further)
                        ;;        (not closer))
                        ;;       (set-state! (fn [prev] (assoc prev :action :pause))))
                            
                        ;;     (set-state! (fn [prev] (assoc prev :subject-height height))))

                        ;;   (set-state! (fn [prev] (assoc prev :action :pause))))

                    ;; pass detection into handle detection
                        ;; (handle-detection detection state set-state!)
                        ))))))
     60)

    (d/div
     (d/input {:type "button" :on-click (fn [] (set-state! (fn [prev] (assoc prev :action :play)))) :value "play"})
     (d/input {:type "button" :on-click (fn [] (set-state! (fn [prev] (assoc prev :action :pause)))) :value "pause"})
     (d/input {:type "button" :on-click (fn [] (set-state! (fn [prev] (assoc prev :action :rewind)))) :value "rewind"})

     (d/code (str "Action: " (:action state)))
     (d/br)
     (d/video {:id "video-feed" :width 400 :height 300 :autoPlay true :style {:display "none"}})
     (d/canvas {:id "output" :width 400 :height 300})
     ($ gif-player {:src "https://media.giphy.com/media/Vfhj19PusenfO/giphy.gif" :action (:action state)}))))
