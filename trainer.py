#Macroinvertebrate classification system
#Using Artificial Neural Networks
#Specifically Recurrent Convolutional Networks
#By Maxwell Budd
#techbotbuilder.com/neuralnet

#from keras.models import Model
#from keras.layers import Input
#from keras.layers import LSTM
#from keras.layers import merge
from keras.models import Sequential
from keras.layers import Dense, Activation, Dropout
from keras.layers import Reshape
from keras.optimizers import SGD

from keras.preprocessing.image import ImageDataGenerator

from keras.models import model_from_json

import os
import pickle

IMAGE_DIMENSION = 64#32#16#
BATCH_SIZE = 42#64#32#128#256#64
NB_EPOCH = 40
SAMPLES_PER_EPOCH = 4096##2048##1028

VERSION = "10"
SUBVERSION = "1"

NUM_HIDDEN_LAYERS = 2#3#2#
HIDDEN_SIZE = 2**12#2**13#4096#1024#
DROPOUT = 0#0.5

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
    log("Starting program...")
    if SUBVERSION=="0":
        #make a (image input) -> (NUM_HIDDEN_LAYERS * )(hidden) -> (classification output) network
        model = Sequential()
        model.add(Reshape((IMAGE_DIMENSION**2,), input_shape=(1, IMAGE_DIMENSION, IMAGE_DIMENSION)))
        for _ in range(NUM_HIDDEN_LAYERS):
            model.add(Dense(HIDDEN_SIZE))
            model.add(Activation('relu'))
            model.add(Dropout(DROPOUT))
        model.add(Dense(NB_CLASSES))#outputs to some categories
        model.add(Activation('softmax'))#only one true at a time
    else:
        with open('models/{}/architecture.json'.format(VERSION)) as f:
            model = model_from_json(f.read())
        model.load_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, int(SUBVERSION)-1))
    
    #optimizer=SGD(lr=0.001, momentum=0.6, decay=0.0, nesterov=True)
    optimizer='adam'
    #optimizer='rmsprop'
    #optimizer=SGD(lr=0.01, momentum=0.9)
    model.compile(optimizer=optimizer, loss='categorical_crossentropy', metrics=['accuracy'])
    #adam is rmsprop w/ momentum, apparently
    #but let's try sgd for the moment
    
    image_loader = ImageDataGenerator(
        rotation_range=15,#45.0,
        width_shift_range=0.1,
        height_shift_range=0.1,
        shear_range=0.1,
        zoom_range=[3/2, 2/3],
        fill_mode='constant',
        cval=0
        #,        #horizontal_flip=True,        #vertical_flip=True
        #we don't want horizontal or vertical flip because of the difference
        #between right and left-handed snails
        )
    
    options = {
        'target_size': (IMAGE_DIMENSION, IMAGE_DIMENSION),
        'color_mode': 'grayscale',
        'class_mode': 'categorical',
        'batch_size': BATCH_SIZE
        }
    
    training_generator = image_loader.flow_from_directory('{}/training'.format(DATA_DIRECTORY), **options)#, save_to_dir='sample_preprocessed_data')
    
    #validation_image_loader = ImageDataGenerator()
    validation_generator = image_loader.flow_from_directory('{}/validation'.format(DATA_DIRECTORY), **options)
    
    #test_generator = image_loader.flow_from_directory('data/test', **options)
    #we won't use this until the end, once we've trained a few models.
    
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
    
    print("Done.")
