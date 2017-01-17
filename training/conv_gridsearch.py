#convolutional neural network trainer
#much helped by https://github.com/fchollet/keras/blob/master/examples/cifar10_cnn.py
import resources as r
from keras.optimizers import SGD
r.log("MAGIC!")

from sys import argv
assert len(argv)==2
VERSION = argv[1]

data_dir = 'dog_data'
batch_size = 64

def build_model(conv_hidden_sizes, deep_hidden_sizes, img_dim, dropout, kernal_size, activation, pool_dim, l2):
    from keras.models import Sequential
    from keras.layers import Dense, Convolution2D, MaxPooling2D, Dropout, Activation, Flatten
    from keras.regularizers import activity_l2
    
    img_channels = 1
    
    model = Sequential()
    model.add(Convolution2D(conv_hidden_sizes[0], kernal_size, kernal_size, border_mode='same', 
        input_shape=(1, img_dim, img_dim), activity_regularizer=activity_l2(l2)))
    model.add(Activation(activation))
    model.add(Convolution2D(conv_hidden_sizes[0], kernal_size, kernal_size, activity_regularizer=activity_l2(l2)))
    model.add(Activation(activation))
    model.add(MaxPooling2D(pool_size=(pool_dim, pool_dim)))
    if dropout>0: model.add(Dropout(dropout))
    
    for size in conv_hidden_sizes[1:]:
        model.add(Convolution2D(size, kernal_size, kernal_size, border_mode='same', 
            activation=activation, activity_regularizer=activity_l2(l2)))
        model.add(Convolution2D(size, kernal_size, kernal_size, activation=activation, activity_regularizer=activity_l2(l2)))
        model.add(MaxPooling2D(pool_size=(pool_dim, pool_dim)))
        if dropout>0: model.add(Dropout(dropout))
    
    model.add(Flatten())
    for size in deep_hidden_sizes:
        model.add(Dense(size, activation=activation, activity_regularizer=activity_l2(l2)))
        if dropout>0: model.add(Dropout(dropout))
    
    model.add(Dense(r.num_classes(data_dir)))
    model.add(Activation('softmax'))
    return model

def compile_model(model, Optimizer, loss, **optimizer_ops):
    optimizer = Optimizer(**optimizer_ops)
    model.compile(loss=loss, optimizer=optimizer, metrics=['accuracy'])

def train_model(model, img_dim, epochs=3):
    training_generator, validation_generator = r.image_generators(img_dim, batch_size, data_dir)
    nb_validation_samples = validation_generator.N
    nb_training_samples = training_generator.N
    
    history = model.fit_generator(
        training_generator,
        samples_per_epoch = nb_training_samples,
        nb_epoch = epochs,
        validation_data = validation_generator,
        nb_val_samples = nb_validation_samples,
        verbose=0)
    h = history.history
    return {'loss':h['loss'][-1], 'val_loss':h['val_loss'][-1], 'acc':h['acc'][-1], 'val_acc':h['val_acc'][-1]}

results = []

for conv_size in [[32,64]]:
  for deep_size in [[512]]:
    for img_dim in [16,32]:
      for dropout in [0,0.5,0.75]:
        for kernal_size in [3,5]:
          for activation in ['relu','sigmoid']:
            for pool_dim in [2]:
              for l2 in [0,1e-5,1e-3]:
                ops=[conv_size, deep_size, img_dim, dropout, kernal_size, activation, pool_dim, l2]
                model = build_model(*ops)
                compile_model(model, SGD, 'categorical_crossentropy', lr=0.01, decay=1e-5, momentum=0.9, nesterov=True)
                result = train_model(model, img_dim)
                subversion = "c{}.d{}.i{}.D{}.k{}.a{}.p{}.l{}".format(*ops)
                results.append((subversion, result))

r.save_gridsearch(VERSION, results)

r.log("Done")

