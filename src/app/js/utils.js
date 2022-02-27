export const MOTION = {
  PLAY: "play",
  REWIND: "rewind",
  PAUSE: "pause",
};

export function streamWebcamToVideoElement(videoElement) {
  const constraints = {
    audio: false,
    facingMode: "user",
    video: { width: 250, height: 200 },
  };

  return new Promise(resolve => {
    navigator.mediaDevices.getUserMedia(constraints).then(stream => {
      videoElement.addEventListener("loadeddata", resolve);
      videoElement.srcObject = stream;
      videoElement.play();
    });
  });
}
