let ocv = null;

function timeoutWithRejection(ms) {
  return new Promise((_, reject) => {
    setTimeout(() => reject("timeout"), ms);
  });
}

function waitForOpencv(waitTimeMs = 30000) {
  return Promise.race([cv, timeoutWithRejection(waitTimeMs)]);
}

/**
 * This exists to capture all the events that are thrown out of the worker
 * into the worker. Without this, there would be no communication possible
 * with the project.
 */
onmessage = function (e) {
  switch (e.data.msg) {
    case "load": {
      // Import Webassembly script
      importScripts("./opencv.js");
      waitForOpencv()
        .then(opencv => {
          console.log("opencv => ", opencv);
          ocv = opencv;
          postMessage({ msg: e.data.msg });
        })
        .catch(err => {
          console.error("Failed to load opencv in webworker ", err);
        });
      break;
    }
    case "process": {
      // Process an image

      break;
    }
    default:
      break;
  }
};
