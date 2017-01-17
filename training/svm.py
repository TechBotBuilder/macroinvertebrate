#Macroinvertebrate classification system
#Using Artificial Neural Networks
#Specifically Recurrent Convolutional Networks
#By Maxwell Budd
#techbotbuilder.com/neuralnet

from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import Reshape
from keras.optimizers import SGD, Adamax

from keras.preprocessing.image import ImageDataGenerator

from keras.models import model_from_json

import os
import pickle

IMAGE_DIMENSION = 32
BATCH_SIZE = 64
SAMPLES_PER_EPOCH = 2**11
NB_EPOCH = 100

VERSION = "svm_grid_3"
SUBVERSION = 0

HIDDEN_SIZE = 2**10 #1024

NUM_VALIDATION_SAMPLES = 113#128

DATA_DIRECTORY = 'dog_data'#'data'

#http://stackoverflow.com/a/36150375 was helpful here
NB_CLASSES = len(next(os.walk('{}/training'.format(DATA_DIRECTORY)))[1])
#^counts how many directories are in ^

def log(message):
    from datetime import datetime
    print("[{}] {}".format(str(datetime.now())[:19], message))


##First go - simple deep dense network
if __name__ == "__main__":
    log("Starting SVM Training...")
    if int(SUBVERSION)==0:
        #make a (image input) -> (Nonlinear high-dimensional kernal) -> (classification output) network
        model = Sequential()
        model.add(Reshape((IMAGE_DIMENSION**2,), input_shape=(1, IMAGE_DIMENSION, IMAGE_DIMENSION)))
        model.add(Dense(HIDDEN_SIZE, trainable=False))
        ## ^ https://keras.io/getting-started/faq/#how-can-i-freeze-keras-layers
        model.add(Activation('sigmoid'))
        model.add(Dense(NB_CLASSES))#outputs to some categories
        model.add(Activation('softmax'))#only one true at a time
    else:
        with open('models/{}/architecture.json'.format(VERSION)) as f:
            model = model_from_json(f.read())
        model.load_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, int(SUBVERSION)-1))
    
    optimizer=Adamax()
    model.compile(optimizer=optimizer, loss='categorical_crossentropy', metrics=['accuracy'])
    
    image_loader = ImageDataGenerator(
        rotation_range=15,
        width_shift_range=0.1,
        height_shift_range=0.1,
        shear_range=0.1,
        zoom_range=[3/2, 2/3],
        fill_mode='constant',
        cval=0
        )
    
    options = {
        'target_size': (IMAGE_DIMENSION, IMAGE_DIMENSION),
        'color_mode': 'grayscale',
        'class_mode': 'categorical',
        'batch_size': BATCH_SIZE
        }
    
    training_generator = image_loader.flow_from_directory('{}/training'.format(DATA_DIRECTORY), **options)
    
    validation_generator = image_loader.flow_from_directory('{}/validation'.format(DATA_DIRECTORY), **options)
    
    history = model.fit_generator(
        training_generator,
        samples_per_epoch = int(SAMPLES_PER_EPOCH / BATCH_SIZE) * BATCH_SIZE,
        nb_epoch = NB_EPOCH,
        validation_data = validation_generator,
        nb_val_samples = NUM_VALIDATION_SAMPLES)
    
    log("Training done. Saving everything...")
    
    check_directories = (
        'models/{}'.format(VERSION),
        'models/{}/training_sessions'.format(VERSION),
        'models/{}/training_sessions/{}'.format(VERSION, SUBVERSION)
        )
    #help from http://stackoverflow.com/a/273227
    for checkdir in check_directories:
        if not os.path.exists(checkdir):
            os.makedirs(checkdir)
    
    model.save_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, SUBVERSION))
    
    model_architecture = model.to_json()
    with open('models/{}/architecture.json'.format(VERSION), 'w') as f:
        f.write(model_architecture)
    
    
    with open('models/{}/training_sessions/{}/history.txt'.format(VERSION, SUBVERSION), 'wb') as f:
        pickle.dump(history, f)
    
    log("Done.")
