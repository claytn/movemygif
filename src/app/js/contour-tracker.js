const THRESHOLD = 100;
const PLAY = "play";
const REWIND = "rewind";
const PAUSE = "pause";

function timeoutWithRejection(ms) {
  return new Promise((_, reject) => {
    setTimeout(() => reject("timeout"), ms);
  });
}

function waitForOpencv(waitTimeMs = 30000) {
  return Promise.race([cv, timeoutWithRejection(waitTimeMs)]);
}

function grayScale(cv, image) {
  const dst = new cv.Mat();
  cv.cvtColor(image, dst, cv.COLOR_RGBA2GRAY, 0);
  return dst;
}

function blur(cv, image) {
  const dst = new cv.Mat();
  const ksize = new cv.Size(21, 21);
  cv.GaussianBlur(image, dst, ksize, 0, 0, cv.BORDER_DEFAULT);
  return dst;
}

export class ContourTracker {
  constructor({ video, canvas, onMoveChange }) {
    this.cv = null;
    this.scratch = document.createElement("canvas");
    this.scratchCtx = this.scratch.getContext("2d");
    this.previousArea = null;
    this.previousFrame = null;

    this.video = video;
    this.canvas = canvas;
    this.onMoveChange = onMoveChange;
  }

  streamVideo() {
    // Stream camera feed to video element
    const constraints = {
      audio: false,
      video: { width: 640, height: 480 },
    };

    return navigator.mediaDevices
      .getUserMedia(constraints)
      .then(stream => {
        this.video.srcObject = stream;
        this.video.play();
      })
      .catch(error => console.log("Error fetching video stream: ", error));
  }

  captureImage() {
    this.scratchCtx.drawImage(this.video, 0, 0, this.canvas.width, this.canvas.height);
  }

  detectMovement() {
    this.captureImage();

    const cv = this.cv;
    const src = cv.imread(this.scratch);
    const blurred = blur(cv, src);
    const grayed = grayScale(cv, blurred);
    const morphed = grayed;

    if (!this.previousFrame) {
      this.previousFrame = new cv.Mat();
      morphed.copyTo(this.previousFrame);
    }

    const delta = new cv.Mat();
    cv.absdiff(morphed, this.previousFrame, delta);

    const thresh = new cv.Mat();
    cv.threshold(delta, thresh, 25, 255, cv.THRESH_BINARY);

    // TODO: ONLY PERFORM THIS ACTION IN DEBUG MODE.
    cv.imshow(this.canvas, thresh);

    const contours = new cv.MatVector();
    const hierarchy = new cv.Mat();
    cv.findContours(thresh, contours, hierarchy, cv.RETR_LIST, cv.CHAIN_APPROX_SIMPLE);

    let maxArea = 0;
    for (let i = 0; i < contours.size(); i++) {
      const cnt = contours.get(i);
      const area = cv.contourArea(cnt);
      if (area > maxArea) {
        maxArea = area;
      }
    }

    if (this.previousArea) {
      let move = null;
      if (maxArea - this.previousArea > THRESHOLD) {
        move = PLAY;
      } else if (this.previousArea - maxArea > THRESHOLD) {
        move = REWIND;
      } else {
        move = PAUSE;
      }

      this.onMoveChange(move);
    }

    this.previousArea = maxArea;
  }

  start() {
    this.streamVideo()
      // Allow video to start streaming
      .then(() => new Promise(res => setTimeout(res, 2000)))
      .then(() => waitForOpencv(30000))
      .then(cv => {
        this.cv = cv;
      })
      .then(() => {
        this.interval = setInterval(() => {
          this.detectMovement();
        }, 100);
      });
  }

  stop() {
    clearInterval(this.interval);
  }
}
