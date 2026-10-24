package com.recap.replay.camera;

public interface CameraController {
    void update(float partialTicksPassed);

    void increaseSpeed();
    void decreaseSpeed();
}
