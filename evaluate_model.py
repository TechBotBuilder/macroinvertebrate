##evaluate_model.py
##Evaluate the classification accuracy of model
# run like $python evaluate_model.py <model_name> [<subversion>]

from keras.preprocessing.image import ImageDataGenerator
from keras.models import model_from_json
import os

from sys import argv
assert len(argv)==3
VERSION = argv[1]
SUBVERSION = argv[2]

DATA_DIRECTORY = 'dog_data'
NB_CLASSES = len(next(os.walk('{}/training'.format(DATA_DIRECTORY)))[1])
BATCH_SIZE = 64

with open('models/{}/architecture.json'.format(VERSION)) as f:
    model = model_from_json(f.read())
model.load_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, SUBVERSION))
model.compile(optimizer='sgd', loss='categorical_crossentropy', metrics=['accuracy'])
IMAGE_DIMENSION = model.layers[0].batch_input_shape[2]
image_loader = ImageDataGenerator()
options = {
    'target_size': (IMAGE_DIMENSION, IMAGE_DIMENSION),
    'color_mode': 'grayscale',
    'class_mode': 'categorical',
    'batch_size': BATCH_SIZE,
    'shuffle': False
    }
test_generator = image_loader.flow_from_directory('{}/test'.format(DATA_DIRECTORY), **options)
accuracies = model.evaluate_generator(test_generator, val_samples=test_generator.N)
print("Test accuracy: {}\tTest loss: {}".format(round(accuracies[1],4), round(accuracies[0],4)))
