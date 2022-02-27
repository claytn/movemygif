import "@tensorflow/tfjs-backend-cpu";
import "@tensorflow/tfjs-backend-webgl";
import cocoSsd from "@tensorflow-models/coco-ssd";

import { MOTION, streamWebcamToVideoElement } from "./utils";

export class PersonTracker {
  constructor({ video, onMoveChange, canvas }) {
    this.previousArea = null;
    this.previousMove = null;
    this.model = null;
    this.video = video;
    this.onMoveChange = onMoveChange;

    if (canvas) {
      this.canvas = canvas;
      this.ctx = this.canvas.getContext("2d");
      this.ctx.strokeStyle = "red";
      this.ctx.drawImage(this.video, 0, 0, this.canvas.width, this.canvas.height);
    }
  }

  calculateMotion(area) {
    // Distance needed to travel to be consider forward/reverse motion
    // should be determined by if the subject is already moving that direction
    const FORWARD_THRESHOLD = this.previousMove === MOTION.PLAY ? 200 : 600;
    const REVERSE_THRESHOLD = this.previousMove === MOTION.REWIND ? 200 : 600;

    if (this.previousArea - area > FORWARD_THRESHOLD) {
      return MOTION.REWIND;
    } else if (area - this.previousArea > REVERSE_THRESHOLD) {
      return MOTION.PLAY;
    } else {
      return MOTION.PAUSE;
    }
  }

  detectMovement() {
    if (!this.model) {
      throw new Error("Must load model before detection can be run");
    }

    if (this.canvas) {
      this.ctx.drawImage(this.video, 0, 0, this.canvas.width, this.canvas.height);
    }

    this.model.detect(this.video).then(predictions => {
      const people = predictions.filter(pred => pred.class === "person");
      if (people.length > 0) {
        const person = people[0];
        const x = person.bbox[0];
        const y = person.bbox[1];
        const width = person.bbox[2];
        const height = person.bbox[3];
        const area = width * height;

        if (this.previousArea !== null) {
          const move = this.calculateMotion(area);
          if (move !== this.previousMove) {
            this.onMoveChange(move);
          }
          this.previousMove = move;
        }

        this.previousArea = area;

        if (this.canvas) {
          this.ctx.strokeRect(x, y, width, height);
        }
      } else {
        // No detection
        if (this.previousMove !== MOTION.PAUSE) {
          this.onMoveChange(MOTION.PAUSE);
          this.previousMove = MOTION.PAUSE;
        }
        this.previousArea = null;
      }

      setTimeout(this.detectMovement.bind(this), 100);
    });
  }

  start() {
    return streamWebcamToVideoElement(this.video)
      .then(() => cocoSsd.load())
      .then(model => {
        this.model = model;
      })
      .then(() => {
        this.detectMovement();
      });
  }
}
