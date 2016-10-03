from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import Reshape
from keras.optimizers import SGD

from keras.preprocessing.image import ImageDataGenerator
from keras.models import model_from_json

import os
import pickle

NB_EPOCH = 100
SAMPLES_PER_EPOCH = 2048

NUM_VALIDATION_SAMPLES = 113

DATA_DIRECTORY = 'dog_data'

#http://stackoverflow.com/a/36150375 was helpful here
NB_CLASSES = len(next(os.walk('{}/training'.format(DATA_DIRECTORY)))[1])
#^counts how many directories are in ^

def log(message):
    from datetime import datetime
    print("[{}] {}".format(str(datetime.now())[:19], message))

def parse_subversion(subversion_descriptor):
    sv=subversion_descriptor
    def whereis(letter, after=0, suppress_error=False):
        pos_letter = sv.find(letter, after)
        return pos_letter
        #if pos_letter==-1
        #    if suppress_error: return False
        #    else:
        #       print("Invalid subversion string:\n"
        #        + "Could not find {} in {}".format(letter, sv))
        #        exit(2)
        #else: return pos_letter
    positions = {letter:whereis(letter) for letter in "HNIAL"}
    results = {}
    results['optimizer'] = sv[:positions['H']].lower()
    results['hidden_size'] = int(sv[positions['H']+1:positions['N']-1])
    results['batch_size'] = int(sv[positions['N']+1:positions['I']-1])
    results['image_dimension'] = int(sv[positions['I']+1:positions['A']-1])
    results['activation'] = sv[positions['A']+1:positions['L']-1]
    if results['activation'] == 'sigm': results['activation'] = 'sigmoid'
    results['loss'] = sv[positions['L']+1:positions['L']+5] #loss is 4 chars long
    if results['loss'] == 'cate': results['loss'] = "categorical_crossentropy"
    if results['loss'] == 'mean': results['loss'] = "mean_squared_error"
    pos_l = whereis('l', positions['L'], True)
    if pos_l != -1: results['learning_rate'] = 2**-int(sv[pos_l+1:pos_l+2])
    pos_m = whereis('m', pos_l, True)
    if pos_m != -1: results['momentum'] = float(sv[pos_m+1:pos_m+4])
    pos_D = whereis('D', positions['L'])
    results['deepness'] = int(sv[pos_D+1:pos_D+2])
    return results

if __name__ == "__main__":
    log("Starting program...")
    from sys import argv
    if len(argv) != 3:
        print("Usage: python ff.py <version (like ff_grid_0)> <gridsearch_descriptor_string>")
        exit(2)
    VERSION = argv[1]
    SUBVERSION = 0
    subversion_details = argv[2]
    ops = parse_subversion(subversion_details)
    IMAGE_DIMENSION = ops['image_dimension']
    BATCH_SIZE = ops['batch_size']
    NUM_HIDDEN_LAYERS = ops['deepness']
    HIDDEN_SIZE = ops['hidden_size']
    #make a (image input) -> (NUM_HIDDEN_LAYERS * )(hidden) -> (classification output) network
    model = Sequential()
    model.add(Reshape((IMAGE_DIMENSION**2,), input_shape=(1, IMAGE_DIMENSION, IMAGE_DIMENSION)))
    for _ in range(NUM_HIDDEN_LAYERS):
        model.add(Dense(HIDDEN_SIZE))
        model.add(Activation(ops['activation']))
    model.add(Dense(NB_CLASSES))
    model.add(Activation('softmax'))
    
    optimizer=ops['optimizer']
    if optimizer=='sgd' and 'learning_rate' in ops:
        if 'momentum' in ops:
            optimizer = SGD(lr=ops['learning_rate'], momentum=ops['momentum'])
        else:
            optimizer = SGD(lr=ops['learning_rate'])
    model.compile(optimizer=optimizer, loss=ops['loss'], metrics=['accuracy'])
    
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
