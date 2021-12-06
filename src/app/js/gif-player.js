// Player based on gifuct's demo: https://github.com/matt-way/gifuct-js/blob/master/demo/demo.js

let c = null;
let ctx = null;

const tempCanvas = document.createElement("canvas");
const tempCtx = tempCanvas.getContext("2d");

let frameImageData;

function drawPatch(frame) {
  const dims = frame.dims;
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

  ctx.drawImage(tempCanvas, 0, 0, c.width, c.height);
}

let frames = [];
let frameIndex = 0;
let frameChange = 0; // 1 = play, 0 = pause, -1 = rewind

function renderFrame() {
  if (frameIndex < 0) {
    frameIndex = frames.length - 1;
  } else if (frameIndex >= frames.length) {
    frameIndex = 0;
  }

  const frame = frames[frameIndex];
  frameIndex = frameIndex + frameChange;

  drawPatch(frame);
}

export function createGifPlayer({
  frames: gifFrames,
  canvas: outputCanvas,
  fitToScreen,
}) {
  c = outputCanvas;
  ctx = c.getContext("2d");

  const gifHeight = gifFrames[0].dims.height;
  const gifWidth = gifFrames[0].dims.width;

  if (fitToScreen) {
    const windowHeight = window.innerHeight;
    const windowWidth = window.innerWidth;
    if (windowHeight < windowWidth) {
      c.height = windowHeight;
      c.width = (windowHeight * gifWidth) / gifHeight;
    } else {
      c.width = windowWidth;
      c.height = (windowWidth * gifHeight) / gifWidth;
    }
  } else {
    c.width = gifFrames[0].dims.width;
    c.height = gifFrames[0].dims.height;
  }

  const delay = gifFrames[0].delay;

  frames = gifFrames;

  return {
    start: () => {
      return setInterval(() => {
        requestAnimationFrame(renderFrame);
      }, delay);
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
