(ns app.gif)

(def not-nil? (complement nil?))

(defn play! [gp]
  (let [{:keys [gif interval]} @gp]
    (if (not-nil? interval)
      (js/clearInterval interval))
    
    (.play gif)

    (reset! gp {:gif gif})))

(defn pause! [gp]
  (let [{:keys [gif interval]} @gp]
    (if (not-nil? interval)
      (js/clearInterval interval))
    
    (.pause gif)
    
    (reset! gp {:gif gif})))

(defn rewind! [gp]
  (let [{:keys [gif interval]} @gp]
    (if (not-nil? interval)
      (js/clearInterval interval))

    (.pause gif)
    (swap! gp (fn [{:keys [gif]}]
                {:gif gif
                 :interval (js/setInterval (fn []
                                             (let [frame-count (.get_length gif)
                                                   current-frame (.get_current_frame gif)]
                                               (if (= current-frame 0)
                                                 (.move_to gif (- frame-count 1))
                                                 (.move_relative gif -1))))
                                           60)}))))

(defn create-gif-player! [{:keys [url image]}]
  (let [g (js/SuperGif. #js {:gif image
                             :loop_mode "auto"
                             :auto_play true
                             :draw_while_loading false})]

    (.load_url g url (fn []))

    (atom {:gif g})))

