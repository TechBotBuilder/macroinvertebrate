#convnet.py
import resources as r
from keras.models import Sequential
from keras.layers import Dense, Convolution2D, MaxPooling2D, Dropout, Activation, Flatten
from keras.optimizers import SGD
r.log("MAGIC!")

from sys import argv
assert len(argv)==3
VERSION, SUBVERSION = argv[1:]

data_dir = 'dog_data'
batch_size = 128
nb_epoch = 20
samples_per_epoch = 2048

img_dim = 32
img_channels = 1
c_h_size = [32, 64]
d_h_size = [512]
dropout = 0.5
k_size = 3
activation = 'relu'
pool_dim = 2

model = Sequential()
model.add(Convolution2D(c_h_size[0], k_size, k_size, border_mode='same', input_shape=(1, img_dim, img_dim)))
model.add(Activation(activation))
model.add(Convolution2D(c_h_size[0], k_size, k_size))
model.add(Activation(activation))
model.add(MaxPooling2D(pool_size=(pool_dim, pool_dim)))
model.add(Dropout(dropout))

for size in c_h_size[1:]:
    model.add(Convolution2D(size, k_size, k_size, border_mode='same'))
    model.add(Activation(activation))
    model.add(Convolution2D(size, k_size, k_size))
    model.add(Activation(activation))
    model.add(MaxPooling2D(pool_size=(pool_dim, pool_dim)))
    model.add(Dropout(dropout))

model.add(Flatten())
for size in d_h_size:
    model.add(Dense(size))
    model.add(Activation('relu'))
    model.add(Dropout(dropout))

model.add(Dense(r.num_classes(data_dir)))
model.add(Activation('softmax'))

optimizer = SGD(lr=0.01, decay=1e-6, momentum=0.9, nesterov=True)
model.compile(loss='categorical_crossentropy', optimizer=optimizer, metrics=['accuracy'])

training_generator, validation_generator = r.image_generators(img_dim, batch_size, data_dir)
nb_validation_samples = validation_generator.N
nb_training_samples = training_generator.N

r.log("Starting training...")
history = model.fit_generator(
    training_generator,
    samples_per_epoch = nb_training_samples, #int(samples_per_epoch / batch_size) * batch_size,
    nb_epoch = nb_epoch,
    validation_data = validation_generator,
    nb_val_samples = nb_validation_samples,
    verbose=0)

r.log("Training done. Saving everything...")
r.save(model, history, VERSION, SUBVERSION)

r.log("Done.")

