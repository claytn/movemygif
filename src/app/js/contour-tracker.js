const scratch = document.createElement("canvas");
const scratchCtx = scratch.getContext("2d");

const THRESHOLD = 100;
const PLAY = "play",
  REWIND = "rewind",
  PAUSE = "pause";

function timeoutWithRejection(ms) {
  return new Promise((_, reject) => {
    setTimeout(() => reject("timeout"), ms);
  });
}

function waitForOpencv(waitTimeMs = 30000) {
  return Promise.race([cv, timeoutWithRejection(waitTimeMs)]);
}

const constraints = {
  audio: false,
  video: { width: 640, height: 480 },
};

function streamVideo(video) {
  return navigator.mediaDevices
    .getUserMedia(constraints)
    .then(stream => {
      video.srcObject = stream;
      video.play();
    })
    .catch(error => console.log("Error fetching video stream: ", error));
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

function detectMovement(cv, video, canvas, onMoveChange) {
  let lastFrame = null;
  let lastArea = null;

  return setInterval(() => {
    scratchCtx.drawImage(video, 0, 0, canvas.width, canvas.height);

    const src = cv.imread(scratch);
    const blurred = blur(cv, src);
    const grayed = grayScale(cv, blurred);
    const morphed = grayed;

    if (!lastFrame) {
      lastFrame = new cv.Mat();
      morphed.copyTo(lastFrame);
    }

    // Diff the two images
    const delta = new cv.Mat();
    cv.absdiff(morphed, lastFrame, delta);

    const thresh = new cv.Mat();
    cv.threshold(delta, thresh, 25, 255, cv.THRESH_BINARY);

    cv.imshow(canvas, thresh);

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

    if (lastArea) {
      let move = null;
      if (maxArea - lastArea > THRESHOLD) {
        move = PLAY;
      } else if (lastArea - maxArea > THRESHOLD) {
        move = REWIND;
      } else {
        move = PAUSE;
      }

      if (move) {
        onMoveChange(move);
      }
    }

    lastArea = maxArea;
  }, 100);
}

export function start(video, canvas, onMoveChange) {
  return streamVideo(video)
    .then(
      () =>
        new Promise(res => {
          console.log("Calibrating...");
          setTimeout(res, 2000);
        })
    )
    .then(() => waitForOpencv(30000))
    .then(cv => detectMovement(cv, video, canvas, onMoveChange))
    .catch(err => console.log("ouch an error ", err));
}
