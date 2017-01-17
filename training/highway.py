#highway neural network trainer
import resources as r
r.log("MAGIC!")

from sys import argv
assert len(argv)==3
VERSION, SUBVERSION = argv[1:]

data_dir = 'dog_data'
batch_size = 64
nb_epoch = 500
samples_per_epoch = 2048
img_dim = 32
img_channels = 1

recurrent_size = 5096
highway_depth = 3
activation = 'sigmoid'
#optimizer = 'adam'
from keras.optimizers import SGD


if int(SUBVERSION) > 0:
    model = r.load(VERSION, int(SUBVERSION)-1)
else:
    from keras.models import Model
    from keras.layers import Input, Flatten, Dense, Highway
    
    in_layer = Input(shape=(1,img_dim,img_dim))
    flatten = Flatten()(in_layer)
    squishing_layer = Dense(recurrent_size)(flatten)
    
    recurrent_base = Highway(activation=activation)
    recurrent_layer = recurrent_base(squishing_layer)
    
    for _ in range(highway_depth-1):
        recurrent_layer = recurrent_base(recurrent_layer)
    
    predicting_layer = Dense(r.num_classes(data_dir), activation='softmax')(recurrent_layer)
    
    model = Model(in_layer, predicting_layer)

model.compile(loss='categorical_crossentropy', optimizer=SGD(lr=1e-3, momentum=0.8, decay=0.005), metrics=['accuracy'])

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
    verbose=1)

r.log("Training done. Saving everything...")
r.save(model, history.history, VERSION, SUBVERSION)

r.log("Done.")

