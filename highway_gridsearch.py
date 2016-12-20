#highway neural network gridsearch
import resources as r
from sys import argv
assert len(argv)==2
VERSION = argv[1]

r.log("Starting gridsearch v:{}!".format(VERSION))

data_dir = 'dog_data'
batch_size = 64
nb_epoch = 3
samples_per_epoch = 2048
img_dim = 32
img_channels = 1

backup_period = 10 #save every ten models
START = 140 #our program crashed in model 121 last time.

#can do this outside of model generation since image dimension and batch size are being held constant
training_generator, validation_generator = r.image_generators(img_dim, batch_size, data_dir)
nb_validation_samples = validation_generator.N
nb_training_samples = training_generator.N


highway_depth_ops=[3,5,8,13,20]
recurrent_size_ops=[64,512,1024,2048,5096]
activation_ops=['relu','sigmoid']
optimizer_ops=['rmsprop','sgd','adam']

ops = [highway_depth_ops, recurrent_size_ops, activation_ops, optimizer_ops]
format_str = "d{}.s{}.a{}.o{}"


def build_model(highway_depth, recurrent_size, activation):
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
    
    return Model(in_layer, predicting_layer)

def optimize(model, optimizer):
    model.compile(loss='categorical_crossentropy', optimizer=optimizer, metrics=['accuracy'])

def test_hyperparams(highway_depth, recurrent_size, activation, optimizer):
    model = build_model(highway_depth, recurrent_size, activation)
    optimize(model, optimizer)
    h=model.fit_generator(
        training_generator,
        samples_per_epoch = nb_training_samples,
        nb_epoch = nb_epoch,
        validation_data = validation_generator,
        nb_val_samples = nb_validation_samples,
        verbose=0).history
    return {'loss':h['loss'][-1], 'val_loss':h['val_loss'][-1], 'acc':h['acc'][-1], 'val_acc':h['val_acc'][-1]}


num_models = 1
for i in range(len(ops)):
    num_models *= len(ops[i])
results = []
print("Gridsearching over {} hyperparameter combinations...".format(num_models))
for model_num in range(START, num_models):
    combo = [0]*len(ops)
    n = model_num
    i=0
    while n>0:
        slots = len(ops[i])
        combo[i]= n % slots
        n = n // slots
        i += 1
    model_ops = [ops[i][combo[i]] for i in range(len(ops))]
    subversion = format_str.format(*model_ops)
    result = test_hyperparams(*model_ops)
    results.append((subversion, result))
    if model_num % backup_period == 1:
        r.save_gridsearch(VERSION, results)
        results = []

r.save_gridsearch(VERSION, results)

r.log("Done")

