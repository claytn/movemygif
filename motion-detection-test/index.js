/**
 * TODOS:
 * - run opencv using wasm or webassembly
 * - re-write the below using the average background
 * - how do I measure metrics inside the browser?
 *   what metrics do I even care about here?
 */

class CV {
  /**
   * We will use this method privately to communicate with the worker and
   * return a promise with the result of the event. This way we can call
   * the worker asynchronously.
   */
  _dispatch(event) {
    const { msg } = event;
    this._status[msg] = ["loading"];
    this.worker.postMessage(event);
    return new Promise((res, rej) => {
      let interval = setInterval(() => {
        const status = this._status[msg];
        if (status[0] === "done") res(status[1]);
        if (status[0] === "error") rej(status[1]);
        if (status[0] !== "loading") {
          delete this._status[msg];
          clearInterval(interval);
        }
      }, 50);
    });
  }

  /**
   * First, we will load the worker and capture the onmessage
   * and onerror events to always know the status of the event
   * we have triggered.
   *
   * Then, we are going to call the 'load' event, as we've just
   * implemented it so that the worker can capture it.
   */
  load() {
    this._status = {};
    this.worker = new Worker("./cv.worker.js"); // load worker

    // Capture events and save [status, event] inside the _status object
    this.worker.onmessage = e => (this._status[e.data.msg] = ["done", e]);
    this.worker.onerror = e => (this._status[e.data.msg] = ["error", e]);
    return this._dispatch({ msg: "load" });
  }
}

// Manual implementation of accumulateWeighted from https://stackoverflow.com/questions/63889719/how-to-add-accumulateweighted-support-to-opencv-js
cv.lerp = function (lerpFromMat, lerpToMat, lerpResult, amount) {
  // TODO: args safety check (including constraining amount)
  if (lerpToMat.cols === 0) {
    console.log("");
    lerpFromMat.copyTo(lerpResult);
  } else if (lerpFromMat.cols === 0) {
    lerpToMat.copyTo(lerpResult);
  } else {
    cv.addWeighted(lerpFromMat, amount, lerpToMat, 1.0 - amount, 0.0, lerpResult);
  }
};

// super simplified alias, skipping mask for now
cv.accumulateWeighted = function (newMat, accumulatorMat, alpha) {
  cv.lerp(accumulatorMat, newMat, accumulatorMat, alpha);
};

const constraints = {
  audio: false,
  video: { width: 640, height: 480 },
};

const video = document.getElementById("video");

navigator.mediaDevices
  .getUserMedia(constraints)
  .then(stream => {
    video.srcObject = stream;
    video.play();
    start();
  })
  .catch(error => console.log("Error fetching video stream: ", error));

const inputCanvas = document.getElementById("input-canvas");
inputCanvas.width = 640;
inputCanvas.height = 480;
const inCtx = inputCanvas.getContext("2d");

const outputCanvas = document.getElementById("output-canvas");
outputCanvas.width = 640;
outputCanvas.height = 480;
const outCtx = outputCanvas.getContext("2d");

function capture() {
  inCtx.drawImage(video, 0, 0, inputCanvas.width, inputCanvas.height);
}

function grayScale(image) {
  const dst = new cv.Mat();
  cv.cvtColor(image, dst, cv.COLOR_RGBA2GRAY, 0);
  return dst;
}

function blur(image) {
  const dst = new cv.Mat();
  const ksize = new cv.Size(21, 21);
  // You can try more different parameters
  cv.GaussianBlur(image, dst, ksize, 0, 0, cv.BORDER_DEFAULT);
  return dst;
}

let lastFrame = null;
let settingFrameCount = 10;
let lastArea = null;
let frameCapturedCount = 0;
const RESET_LAST_FRAME_COUNT = 25;
const areaHeader = document.getElementById("area");

function loop() {
  capture();
  const src = cv.imread("input-canvas");
  const grayed = grayScale(src);
  const blurred = blur(grayed);
  const dst = blurred;

  if (!lastFrame) {
    lastFrame = new cv.Mat();
    dst.copyTo(lastFrame);
  }

  // Diff the two images
  const delta = new cv.Mat();
  cv.absdiff(dst, lastFrame, delta);

  // cv.imshow("output-canvas", delta);
  // Converts all pixels to black/white depending on a threshold
  const thresh = new cv.Mat();
  cv.threshold(delta, thresh, 25, 255, cv.THRESH_BINARY);

  cv.imshow("output-canvas", thresh);

  const contours = new cv.MatVector();
  const hierarchy = new cv.Mat();
  cv.findContours(thresh, contours, hierarchy, cv.RETR_LIST, cv.CHAIN_APPROX_SIMPLE);

  let maxArea = 0;
  let largestContour = null;
  for (let i = 0; i < contours.size(); i++) {
    const cnt = contours.get(i);
    const area = cv.contourArea(cnt);
    if (area > maxArea) {
      maxArea = area;
      largestContour = cnt;
    }
  }

  areaHeader.innerHTML = maxArea + "";
  if (lastArea) {
    if (maxArea > lastArea) {
      console.log("closer");
      areaHeader.innerHTML = "closer";
    } else if (maxArea < lastArea) {
      console.log("further");
      areaHeader.innerHTML = "further";
    } else {
      console.log("still");
    }
  }
  const diff = maxArea - lastArea;
  ///console.log(`${diff > 0 ? `+${diff}` : diff}`);
  lastArea = maxArea;

  // if (frameCapturedCount++ >= RESET_LAST_FRAME_COUNT) {
  //   // lastFrame = dst;
  //   frameCapturedCount = 0;
  // }
}

function start() {
  console.log("calibrating...");
  avg = new cv.Mat();
  setTimeout(() => {
    setInterval(loop, 100);
  }, 2000);
}

// let cv = new CV();
// cv.load().then(x => console.log("x=", x));
