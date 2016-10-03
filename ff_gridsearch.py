#Macroinvertebrate classification system
#Using Artificial Neural Networks
#Gridsearch for ff network hyperparameters
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

STARTAT = 0

SAMPLES_PER_EPOCH = 2**11
NB_EPOCH = 3

NUM_VALIDATION_SAMPLES = 113

DATA_DIRECTORY = 'dog_data'

VERSION = 'ff_gridsearch'

#http://stackoverflow.com/a/36150375 was helpful here
NB_CLASSES = len(next(os.walk('{}/training'.format(DATA_DIRECTORY)))[1])
#^counts how many directories are in ^

def log(message):
    from datetime import datetime
    print("[{}] {}".format(str(datetime.now())[:19], message))

params={
    "learning_rates":[2**-3,2**-6],
    "hidden_sizes":[2**5,2**8,2**11],
    "num_hidden_layers":[2,4],
    "batch_sizes":[2**6,2**8],
    "image_dimensions":[2**4,2**5],
    "activations":['sigmoid','relu'],
    "momentums":[0,0.9],
    "losses":['categorical_crossentropy','mean_squared_error'],
    "optimizers":[[SGD,['momentums', 'learning_rates']], [RMSprop, ['learning_rates']], [Adamax,[]]]}

num_combos=1
for param in params:
    if param not in ("learning_rates", "momentums", "optimizers"):
        num_combos = num_combos * len(params[param])
num_combos = num_combos*(1+len(params['learning_rates'])*(1+len(params['momentums'])))

counter = 0
def inc():
    global counter
    counter = counter + 1

image_loader = ImageDataGenerator(
    rotation_range=15,
    width_shift_range=0.1,
    height_shift_range=0.1,
    shear_range=0.1,
    zoom_range=[3/2, 2/3],
    fill_mode='constant',
    cval=0
    )

def train(Optimizer, NUM_HIDDEN_LAYERS, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss, learning_rate=None, momentum=None):
    inc()
    SUBVERSION = (Optimizer.__name__ +
        "H{}.N{}.I{}.A{}.L{}".format(HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation[:4], loss[:4]) )
    if learning_rate:
        SUBVERSION = SUBVERSION + ".l{}".format(abs(int(log10(learning_rate)/log10(2)))) #eg, add "lx" where learningrate was 2^-x
    if momentum:
        SUBVERSION = SUBVERSION + ".m{}".format(momentum)
    SUBVERSION = SUBVERSION + ".D{}".format(NUM_HIDDEN_LAYERS)
    
    log("Starting FF #{:0>3}/{}: {} in training...".format(counter, num_combos, SUBVERSION))
    model = Sequential()
    model.add(Reshape((IMAGE_DIMENSION**2,), input_shape=(1, IMAGE_DIMENSION, IMAGE_DIMENSION)))
    for _ in range(NUM_HIDDEN_LAYERS):
        model.add(Dense(HIDDEN_SIZE))
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
    
    history = model.fit_generator(
        training_generator,
        samples_per_epoch = int(SAMPLES_PER_EPOCH / BATCH_SIZE) * BATCH_SIZE,
        nb_epoch = NB_EPOCH,
        validation_data = validation_generator,
        nb_val_samples = NUM_VALIDATION_SAMPLES,
        verbose=0)
    
    h = history.history
    log("Done: acc {:.3}".format(h['acc'][-1]))
    return (SUBVERSION, {'loss':h['loss'][-1], 'val_loss':h['val_loss'][-1], 'acc':h['acc'][-1], 'val_acc':h['val_acc'][-1]})

def save(results):
    try:
        with open("models/{}/results.pickle".format(VERSION), 'rb') as f:
            prev_results = pickle.load(f)
    except:
        prev_results = []
    results = prev_results + results
    with open("models/{}/results.pickle".format(VERSION), 'wb') as f:
        pickle.dump(results, f)

##First go - simple deep dense network
if __name__ == "__main__":
  check_directories = (
    'models/{}'.format(VERSION),
    'models/{}/training_sessions'.format(VERSION))
  #help with directories from http://stackoverflow.com/a/273227
  for checkdir in check_directories:
    if not os.path.exists(checkdir):
      os.makedirs(checkdir)
  
  results = []
  for optimizer_params in params['optimizers']:
    Optimizer, optimizer_args = optimizer_params
    for HIDDEN_SIZE in params['hidden_sizes']:
      for NUM_HIDDEN_LAYERS in params['num_hidden_layers']:
        for BATCH_SIZE in params['batch_sizes']:
          save(results)
          results = []
          for IMAGE_DIMENSION in params['image_dimensions']:
            for activation in params['activations']:
              for loss in params['losses']:
                if 'learning_rates' in optimizer_args:
                  for learning_rate in params['learning_rates']:
                    if 'momentums' in optimizer_args:
                      for momentum in params['momentums']:
                        if counter < STARTAT: inc()
                        else: results.append(train(Optimizer, NUM_HIDDEN_LAYERS, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss, learning_rate, momentum))
                    else:
                      if counter < STARTAT: inc()
                      else: results.append(train(Optimizer, NUM_HIDDEN_LAYERS, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss, learning_rate))
                else:
                  if counter < STARTAT: inc()
                  else: results.append(train(Optimizer, NUM_HIDDEN_LAYERS, HIDDEN_SIZE, BATCH_SIZE, IMAGE_DIMENSION, activation, loss))
  save(results)

