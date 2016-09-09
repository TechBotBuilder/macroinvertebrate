#Macroinvertebrate classification system
#Using Artificial Neural Networks
#Specifically Recurrent Convolutional Networks
#By Maxwell Budd
#techbotbuilder.com/neuralnet

from math import log10

from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import Reshape
from keras.optimizers import SGD, RMSprop, Adamax

from keras.preprocessing.image import ImageDataGenerator

from keras.models import model_from_json

import os
import pickle

SAMPLES_PER_EPOCH = 2**11#=2048
NB_EPOCH = 4

NUM_VALIDATION_SAMPLES = 113#128

DATA_DIRECTORY = 'dog_data'#'data'

VERSION = 'gridsearch'

#http://stackoverflow.com/a/36150375 was helpful here
NB_CLASSES = len(next(os.walk('{}/training'.format(DATA_DIRECTORY)))[1])
#^counts how many directories are in ^

def log(message):
    from datetime import datetime
    print("[{}] {}".format(str(datetime.now())[:19], message))

params={
    "learning_rates":[2**-1,2**-4,2**-8],
    "hidden_sizes":[2**8,2**10,2**12],
    "batch_sizes":[2**4,2**6,2**8],
    "image_dimensions":[2**4,2**5,2**6],
    "activations":['sigmoid','relu'],
    "momentums":[0,0.5,0.99],
    "losses":['categorical_crossentropy','mean_squared_error','cosine_proximity'],
    "optimizers":[[SGD,['momentums', 'learning_rates']], [RMSprop, ['learning_rates']], [Adamax,[]]}

counter = 0

##First go - simple deep dense network
if __name__ == "__main__":
  results = []
  for optimizer_params in params['optimizers']:
    Optimizer, optimizer_args = optimizer_params
    for HIDDEN_SIZE in params['hidden_sizes']:
      for BATCH_SIZE in params['batch_sizes']:
        for IMAGE_DIMENSION in params['image_dimensions']:
          for activation in params['activations']:
            for loss in params['losses']:
              if 'learning_rates' in optimizer_args:
                for learning_rate in params['learning_rates']:
                  if 'momentums' in optimizer_args:
                    for momentum in params['momentums']:
                      results.append(train(Optimizer, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss, learning_rate, momentum))
                  else:
                    results.append(train(Optimizer, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss, learning_rate))
              else:
                results.append(train(Optimizer, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss))
  with open("models/{}/results.pickle".format(VERSION), 'wb') as f:
    pickle.dump(results, f)


image_loader = ImageDataGenerator(
    rotation_range=15,
    width_shift_range=0.1,
    height_shift_range=0.1,
    shear_range=0.1,
    zoom_range=[3/2, 2/3],
    fill_mode='constant',
    cval=0
    )

def train(Optimizer, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss, learning_rate=None, momentum=None):
    global counter
    counter = counter + 1
    SUBVERSION = (Optimizer.__name__ +
        "H{}.N{}.I{}.A{}.L{}".format(HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION,
        activation.__name__, loss) )
    if learning_rate:
        SUBVERSION = SUBVERSION + "l{}".format(abs(int(log10(learning_rate)/log10(2)))) #eg, add "lx" where learningrate was 2^-x
    if momentum:
        SUBVERSION = SUBVERSION + "m{}".format(momentum)
    
    log("Starting SVM #{:0>4}/2106: {} in training...".format(counter, SUBVERSION))
    #make a (image input) -> (Nonlinear high-dimensional kernal) -> (classification output) network
    model = Sequential()
    model.add(Reshape((IMAGE_DIMENSION**2,), input_shape=(1, IMAGE_DIMENSION, IMAGE_DIMENSION)))
    model.add(Dense(HIDDEN_SIZE, trainable=False))
    ## ^ https://keras.io/getting-started/faq/#how-can-i-freeze-keras-layers
    model.add(Activation(activation))
    model.add(Dense(NB_CLASSES))#outputs to some categories
    model.add(Activation('softmax'))#only one true at a time
    
    if learning_rate:
        if momentum:
            optimizer=Optimizer(lr=learning_rate, momentum=momentum)
        else:
            optimizer=Optimizer(lr=learning_rate)
    else:
        optimizer=Optimizer()
    model.compile(optimizer=optimizer, loss=loss, metrics=['accuracy'])
    
    options = {
        'target_size': (IMAGE_DIMENSION, IMAGE_DIMENSION),
        'color_mode': 'grayscale',
        'class_mode': 'categorical',
        'batch_size': BATCH_SIZE,
        'shuffle': True,
        'seed': 10001
        }
    
    training_generator = image_loader.flow_from_directory('{}/training'.format(DATA_DIRECTORY), **options)
    
    validation_generator = image_loader.flow_from_directory('{}/validation'.format(DATA_DIRECTORY), **options)
    
    #test_generator = image_loader.flow_from_directory('data/test', **options)
    #we won't use this until the end, once we've trained a few models.
    
    history = model.fit_generator(
        training_generator,
        samples_per_epoch = int(SAMPLES_PER_EPOCH / BATCH_SIZE) * BATCH_SIZE,
        nb_epoch = NB_EPOCH,
        validation_data = validation_generator,
        nb_val_samples = NUM_VALIDATION_SAMPLES,
        verbose=0)
    
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
    with open('models/{}/training_sessions/{}/architecture.json'.format(VERSION, SUBVERSION), 'w') as f:
        f.write(model_architecture)
    
    
    with open('models/{}/training_sessions/{}/history.txt'.format(VERSION, SUBVERSION), 'wb') as f:
        pickle.dump(history, f)
    
    log("Done.")
    h = history.history
    return (SUBVERSION, {'loss':h['loss'][-1], 'val_loss':h['val_loss'][-1], 'acc':h['acc'][-1], 'val_acc':h['val_acc'][-1]})
