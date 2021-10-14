// File based on https://github.com/matt-way/gifuct-js/blob/master/demo/demo.js
// For the life of me I cannot understand why this library needs 3 canvas elements
// to paint a single fucking GIF frame

let c = null;
let ctx = null;
// gif patch canvas
const tempCanvas = document.createElement("canvas");
const tempCtx = tempCanvas.getContext("2d");
// full gif canvas
const gifCanvas = document.createElement("canvas");
const gifCtx = gifCanvas.getContext("2d");

let frameImageData;

function drawPatch(frame) {
  let dims = frame.dims;

  if (
    !frameImageData ||
    dims.width != frameImageData.width ||
    dims.height != frameImageData.height
  ) {
    tempCanvas.width = dims.width;
    tempCanvas.height = dims.height;
    frameImageData = tempCtx.createImageData(dims.width, dims.height);
  }

  // set the patch data as an override
  frameImageData.data.set(frame.patch);

  // draw the patch back over the canvas
  tempCtx.putImageData(frameImageData, 0, 0);
  gifCtx.drawImage(tempCanvas, dims.left, dims.top);

  const imageData = gifCtx.getImageData(0, 0, gifCanvas.width, gifCanvas.height);

  ctx.putImageData(imageData, 0, 0);
  ctx.drawImage(c, 0, 0);
}

let frames = [];
let frameIndex = 0;
let frameChange = 0; // 1 = play, 0 = pause, -1 = rewind

function renderFrame() {
  let start = new Date().getTime();

  if (frameIndex < 0) {
    frameIndex = frames.length - 1;
  } else if (frameIndex >= frames.length) {
    frameIndex = 0;
  }

  const frame = frames[frameIndex];
  frameIndex = frameIndex + frameChange;

  if (frame.disposalType === 2) {
    gifCtx.clearRect(0, 0, c.width, c.height);
  }

  // draw the patch
  drawPatch(frame);

  let end = new Date().getTime();
  let diff = end - start;

  setTimeout(function () {
    requestAnimationFrame(renderFrame);
  }, Math.max(0, Math.floor(frame.delay - diff)));
}

export function createGifPlayer(gifFrames, outputCanvas) {
  c = outputCanvas;
  ctx = c.getContext("2d");

  c.width = gifFrames[0].dims.width;
  c.height = gifFrames[0].dims.height;

  gifCanvas.width = c.width;
  gifCanvas.height = c.height;

  frames = gifFrames;

  return {
    start: () => {
      renderFrame();
    },
    pause: () => {
      frameChange = 0;
    },
    play: () => {
      frameChange = 1;
    },
    rewind: () => {
      frameChange = -1;
    },
  };
}
