(ns app.trackers.head)
;; Please don't judge me, I'm not proud of this
;; I promise I'll fix it later.

(def tracker (atom nil))

(def MOVEMENT_BUF_SIZE 5)
(def FROM_PAUSE_THRESHOLD 1)
(def FROM_MOVE_THRESHOLD 1)

(def motion (atom :pause))

;; Last depth of subject in camera
(def depth (atom nil))

;; Collection of the last MOVEMENT_BUF_SIZE number of z indexes found by head tracking listener
(def movements (atom []))

(defn median [xs]
  (let [sorted-xs (sort xs)
        length (count xs)
        mid-point (int (/ length 2))]
    (if (odd? length)
      (nth sorted-xs mid-point)
      (/ (+ (nth sorted-xs mid-point) (nth sorted-xs (dec mid-point))) 2))))

(defn get-next-action [previous-depth current-depth]
  (let [diff (- current-depth previous-depth)
        ;; When motion starts - require less movement to continue
        threshold (if (= :pause @motion) FROM_PAUSE_THRESHOLD FROM_MOVE_THRESHOLD)
        closer (> (- threshold) diff)
        further (> diff threshold)]
    (cond
      closer
      :play

      further
      :rewind

      (and
       (not further)
       (not closer))
      :pause)))

(defn handle-detection
  "Given a detection adds the z-index of the detected source
   to the movements vector. Once the movements vector reaches
   a length of X, select the median of the z-index values and
   consider this the next 'move' by the subject"
  [detection on-move-change]
  (let [mvs @movements
        pos (.-z detection)]
    (if (= (count mvs) MOVEMENT_BUF_SIZE)
      ;; Dispatch a movment update based on the median of the last X moves
      (let [smoothed-depth (median mvs)
            next-motion (get-next-action @depth smoothed-depth)]

        (if (not= next-motion @motion)
          (do
            (on-move-change next-motion)
            (reset! motion next-motion)))

        (reset! depth smoothed-depth)
        (reset! movements []))

      (swap! movements conj pos))))

(defn start! [{:keys [video canvas on-move-change]}] 
  (let [t (js/headtrackr.Tracker. #js {:ui false :debug canvas})]
    (.init t video canvas)

    (.addEventListener
       js/document
       "headtrackingEvent"
       #(handle-detection
         %
         on-move-change))
    
    (.start t)

    (reset! tracker t)))

