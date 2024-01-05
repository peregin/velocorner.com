import cv2
import sys
import numpy as np
import matplotlib.pyplot as plt
import os
from zipfile import ZipFile
import urllib
from urllib.request import urlretrieve

#  Tensorflow model ZOO
#  wget http://download.tensorflow.org/models/object_detection/ssd_mobilenet_v2_coco_2018_03_29.tar.gz

# Assets
#  wget https://www.dropbox.com/s/xoomeq2ids9551y/opencv_bootcamp_assets_NB13.zip

FONTFACE = cv2.FONT_HERSHEY_SIMPLEX
FONT_SCALE = 0.7
THICKNESS = 1


def detecting():
    # source = cv2.VideoCapture('/Users/levi/Downloads/velo/timot_lepcso1.MOV')
    # video = YouTubeVideo("XkJCvtCRdVM", width=1024, height=640)
    print('initializing...')
    load_models()
    load_assets()

    classFile = "coco_class_labels.txt"
    with open(classFile) as fp:
        labels = fp.read().split("\n")
    print('labels:')
    print(labels)

    print('loading model...')
    modelFile = os.path.join("models", "ssd_mobilenet_v2_coco_2018_03_29", "frozen_inference_graph.pb")
    configFile = os.path.join("models", "ssd_mobilenet_v2_coco_2018_03_29.pbtxt")
    # Read the Tensorflow network
    net = cv2.dnn.readNetFromTensorflow(modelFile, configFile)

    im = cv2.imread(os.path.join("images", "einsiedeln.jpg"))
    objects = detect_objects(net, im)
    display_objects(im, objects, labels)

    print('done...')


# For each file in the directory
def detect_objects(net, im, dim=300):
    # Create a blob from the image
    blob = cv2.dnn.blobFromImage(im, 1.0, size=(dim, dim), mean=(0, 0, 0), swapRB=True, crop=False)

    # Pass blob to the network
    net.setInput(blob)

    # Perform Prediction
    objects = net.forward()
    return objects


def display_text(im, text, x, y):
    # Get text size
    textSize = cv2.getTextSize(text, FONTFACE, FONT_SCALE, THICKNESS)
    dim = textSize[0]
    baseline = textSize[1]

    # Use text size to create a black rectangle
    cv2.rectangle(
        im,
        (x, y - dim[1] - baseline),
        (x + dim[0], y + baseline),
        (0, 0, 0),
        cv2.FILLED,
    )

    # Display text inside the rectangle
    cv2.putText(
        im,
        text,
        (x, y - 5),
        FONTFACE,
        FONT_SCALE,
        (0, 255, 255),
        THICKNESS,
        cv2.LINE_AA,
    )


def display_objects(im, objects, labels, threshold=0.25):
    rows = im.shape[0]
    cols = im.shape[1]

    # For every Detected Object
    for i in range(objects.shape[2]):
        # Find the class and confidence
        classId = int(objects[0, 0, i, 1])
        score = float(objects[0, 0, i, 2])

        # Recover original cordinates from normalized coordinates
        x = int(objects[0, 0, i, 3] * cols)
        y = int(objects[0, 0, i, 4] * rows)
        w = int(objects[0, 0, i, 5] * cols - x)
        h = int(objects[0, 0, i, 6] * rows - y)

        # Check if the detection is of good quality
        if score > threshold:
            display_text(im, "{}".format(labels[classId]), x, y)
            cv2.rectangle(im, (x, y), (x + w, y + h), (255, 255, 255), 2)

    # Convert Image to RGB since we are using Matplotlib for displaying image
    mp_img = cv2.cvtColor(im, cv2.COLOR_BGR2RGB)
    plt.figure(figsize=(30, 10))
    plt.imshow(mp_img)
    plt.show()


def load_models():
    if not os.path.isdir('models'):
        os.mkdir("models")
        os.chdir('models')
        print('downloading tensorflow models...')
        # Download the tensorflow Model
        urllib.request.urlretrieve(
            'http://download.tensorflow.org/models/object_detection/ssd_mobilenet_v2_coco_2018_03_29.tar.gz',
            'ssd_mobilenet_v2_coco_2018_03_29.tar.gz')

        # Uncompress the file
        os.system('tar -xvf ssd_mobilenet_v2_coco_2018_03_29.tar.gz')
        # Delete the tar.gz file
        os.remove('ssd_mobilenet_v2_coco_2018_03_29.tar.gz')
        # Come back to the previous directory
        os.chdir("..")


def load_assets():
    URL = r"https://www.dropbox.com/s/xoomeq2ids9551y/opencv_bootcamp_assets_NB13.zip?dl=1"
    asset_zip_path = os.path.join(os.getcwd(), "opencv_bootcamp_assets_NB13.zip")

    # Download if assets ZIP does not exist.
    if not os.path.exists(asset_zip_path):
        download_and_unzip(URL, asset_zip_path)


def download_and_unzip(url, save_path):
    print(f"Downloading and extracting assests....", end="")

    # Downloading zip file using urllib package.
    urlretrieve(url, save_path)

    try:
        # Extracting zip file using the zipfile package.
        with ZipFile(save_path) as z:
            # Extract ZIP file contents in the same directory.
            z.extractall(os.path.split(save_path)[0])

        print("Done")

    except Exception as e:
        print("\nInvalid file.", e)


if __name__ == '__main__':
    detecting()
