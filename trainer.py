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
from keras.layers import Dense, Activation

from keras.preprocessing.image import ImageDataGenerator

from keras.models import model_from_json

IMAGE_DIMENSION = 32
BATCH_SIZE = 32
NB_EPOCH = 100
HIDDEN_SIZE = 5000
DROPOUT = 0.3
VERSION = "0"
SUBVERSION = "0"

##First go - simple deep dense network
if __name__ == "__main__":
    print("Starting program...")
    #make a (1024 input) -> (5000 hidden) -> (5000 hidden) -> (50 output) network
    model = Sequential()
    model.add(Dense(HIDDEN_SIZE, input_dim = IMAGE_DIMENSION ** 2))
    model.add(Activation('relu'))
    model.add(Dropout(DROPOUT))
    model.add(Dense(HIDDEN_SIZE))
    model.add(Activation('relu'))
    model.add(Dropout(DROPOUT))
    model.add(Dense(50))#outputs to 50 categories
    model.add(Activation('softmax'))#only one true at a time
    model.compile(optimizer='adam', loss='categorical_crossentropy')#adam is rmsprop w/ momentum, apparently
    
    image_loader = ImageDataGenerator(
        rotation_range=45.0,
        width_shift_range=0.1,
        height_shift_range=0.1,
        shear_range=0.1,
        zoom_range=[1, 1.2],
        fill_mode='constant',
        cval=0,
        horizontal_flip=True,
        vertical_flip=True,
        )
    
    options = {
        'target_size': (IMAGE_DIMENSION,IMAGE_DIMENSION),
        'color_mode': 'grayscale',
        'class_mode': 'categorical',
        'batch_size': BATCH_SIZE
        }
    
    training_generator = image_loader.flow_from_directory('data/training', **options)
    
    validation_generator = image_loader.flow_from_directory('data/validation', **options)
    
    #test_generator = image_loader.flow_from_directory('data/test', **options)
    #we won't use this until the end.
    
    history = model.fit_generator(
        training_generator,
        samples_per_epoch=int(1000/BATCH_SIZE) * BATCH_SIZE, #whole number of batches
        nb_epoch=NB_EPOCH,
        validation_data=validation_generator,
        nb_validation_samples=100)
    
    model_architecture = model.to_json()
    with open('models/{}/architecture.json'.format(VERSION), 'w') as f:
        f.write(model_architecture)
    
    model.save_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, SUBVERSION))
    
    with open('models/{}/training_sessions/{}/history.txt'.format(VERSION, SUBVERSION), 'w') as f:
        f.write(history)
    
    
