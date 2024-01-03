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


def detecting():
    # source = cv2.VideoCapture('/Users/levi/Downloads/velo/timot_lepcso1.MOV')
    # video = YouTubeVideo("XkJCvtCRdVM", width=1024, height=640)
    print('initializing...')

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

    print('done...')


if __name__ == '__main__':
    detecting()
