import cv2
import sys
import numpy as np
import matplotlib.pyplot as plt

# Set up tracker
tracker_types = [
    "BOOSTING",
    "MIL",
    "KCF",
    "CSRT",
    "TLD",
    "MEDIANFLOW",
    "GOTURN",
    "MOSSE",
]

# Change the index to change the tracker type
tracker_type = "MIL" # tracker_types[2]

# if tracker_type == "MIL":
#     tracker = cv2.legacy.TrackerMIL.create()
# elif tracker_type == "CSRT":
#     tracker = cv2.TrackerCSRT.create()
# elif tracker_type == "TLD":
#     tracker = cv2.legacy.TrackerTLD.create()
# elif tracker_type == "MEDIANFLOW":
#     tracker = cv2.legacy.TrackerMedianFlow.create()
# else:
#     tracker = cv2.TrackerGOTURN.create()


def drawRectangle(frame, bbox):
    p1 = (int(bbox[0]), int(bbox[1]))
    p2 = (int(bbox[0] + bbox[2]), int(bbox[1] + bbox[3]))
    cv2.rectangle(frame, p1, p2, (255, 0, 0), 2, 1)


def displayRectangle(frame, bbox):
    plt.figure(figsize=(20, 10))
    frameCopy = frame.copy()
    drawRectangle(frameCopy, bbox)
    frameCopy = cv2.cvtColor(frameCopy, cv2.COLOR_RGB2BGR)
    plt.imshow(frameCopy)
    plt.axis("off")


def drawText(frame, txt, location, color=(50, 170, 50)):
    cv2.putText(frame, txt, location, cv2.FONT_HERSHEY_PLAIN, 2, color, 3)


def tracking():
    source = cv2.VideoCapture('/Users/levi/Downloads/velo/timot_lepcso1.MOV')
    # video = YouTubeVideo("XkJCvtCRdVM", width=1024, height=640)

    win_name = 'Tracking Motion'
    cv2.namedWindow(win_name, cv2.WINDOW_NORMAL)

    tracker = cv2.TrackerCSRT.create()

    tracker_ready = False
    while cv2.waitKey(1) != 27:  # Escape
        has_frame, frame = source.read()
        if not has_frame:
            break

        if not tracker_ready:
            tracker.init(frame, (0, 0, frame.shape[1], frame.shape[0]))
            tracker_ready = True

        # Start timer
        timer = cv2.getTickCount()

        # Update tracker
        ok, bbox = tracker.update(frame)

        # Calculate Frames per second (FPS)
        fps = cv2.getTickFrequency() / (cv2.getTickCount() - timer)

        # Draw bounding box
        if ok:
            drawRectangle(frame, bbox)
        else:
            drawText(frame, "Tracking failure detected", (80, 140), (0, 0, 255))

            # Display Info
        drawText(frame, tracker_type + " Tracker", (80, 60))
        drawText(frame, "FPS : " + str(int(fps)), (80, 100))

        cv2.imshow(win_name, frame)

    source.release()
    cv2.destroyWindow(win_name)


if __name__ == '__main__':
    tracking()
