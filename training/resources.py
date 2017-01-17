#resources.py
#use `import resources as r`


def log(message):
    from datetime import datetime
    print("[{}] {}".format(str(datetime.now())[:19], message))

def num_classes(DATA_DIRECTORY):
    import os
    #http://stackoverflow.com/a/36150375 was helpful here
    return len(next(os.walk('{}/training'.format(DATA_DIRECTORY)))[1])

def image_generators(IMAGE_DIMENSION, BATCH_SIZE, DATA_DIRECTORY):
    from keras.preprocessing.image import ImageDataGenerator
    image_loader = ImageDataGenerator(
        rotation_range=35,#25,
        width_shift_range=0.1,#0.05,
        height_shift_range=0.1,#0.05,
        shear_range=0.05,
        zoom_range=0.25,#0.2,
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
    return training_generator, validation_generator

def safeify_dir(directory):
    #help from http://stackoverflow.com/a/273227
    import os
    if not os.path.exists(directory):
        os.makedirs(directory)

def save_model(model, VERSION, SUBVERSION):
    check_directories = (
        'models/{}'.format(VERSION),
        'models/{}/training_sessions'.format(VERSION),
        'models/{}/training_sessions/{}'.format(VERSION, SUBVERSION)
        )
    for checkdir in check_directories:
        safeify_dir(checkdir)
    model.save_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, SUBVERSION))

def save_architecture(model, VERSION):
    model_architecture = model.to_json()
    with open('models/{}/architecture.json'.format(VERSION), 'w') as f:
        f.write(model_architecture)

def save_history(history, VERSION, SUBVERSION):
    import dill
    with open('models/{}/training_sessions/{}/history.txt'.format(VERSION, SUBVERSION), 'wb') as f:
        dill.dump(history, f)

def save(model, history, VERSION, SUBVERSION):
    save_model(model, VERSION, SUBVERSION)
    save_architecture(model, VERSION)
    save_history(history, VERSION, SUBVERSION)

def load(VERSION, SUBVERSION):
    from keras.models import model_from_json
    with open('models/{}/architecture.json'.format(VERSION)) as f:
        model = model_from_json(f.read())
    model.load_weights('models/{}/training_sessions/{}/weights.hdf5'.format(VERSION, SUBVERSION))
    return model

def save_gridsearch(VERSION, results):
    safeify_dir("models/{}".format(VERSION))
    import dill
    try:
        with open("models/{}/results.pickle".format(VERSION), 'rb') as f:
            prev_results = dill.load(f)
    except:
        prev_results = []
    results = prev_results + results
    with open("models/{}/results.pickle".format(VERSION), 'wb') as f:
        dill.dump(results, f)



