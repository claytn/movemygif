(ns app.face-detect
  (:require
   ["face-api.js" :as faceapi]))

(def MODELS "/models")


(defn get-video-stream []
  (if js/navigator.mediaDevices
    (.getUserMedia js/navigator.mediaDevices #js{:video true :audio false})))

(defn load-faceapi-models []
  (js/Promise.all [(.loadSsdMobilenetv1Model faceapi MODELS)
                   (.loadTinyFaceDetectorModel faceapi MODELS)]))

(defn get-face-detection [video]
  (.detectSingleFace faceapi video))

(defn listen-to-video-stream [video-el]
  (-> (get-video-stream)
      (.then
       (fn [stream]
         (set! (.-srcObject video-el) stream)))))

(defn end-video-stream [video-el]
  (set! (.-srcObject video-el) nil))
