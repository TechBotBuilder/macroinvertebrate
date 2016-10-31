from keras.models import Sequential
from keras.layers import Dense, Activation, Dropout
from keras.layers import Reshape
from keras.optimizers import SGD
import resources as r

NB_EPOCH = 200
SAMPLES_PER_EPOCH = 2048
NUM_VALIDATION_SAMPLES = 113

DATA_DIRECTORY = 'dog_data'

NB_CLASSES = r.num_classes(data_dir)

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
    letters = "HNIAL"
    positions = {}
    for letter_index in range(len(letters)):
        positions[letters[letter_index]] = whereis(letters[letter_index], after=positions.get(letters[letter_index-1], 0))
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
    pos_d = whereis('d', pos_D, True)
    if pos_d != -1: results['dropout'] = float(sv[pos_d+1:pos_d+4])
    return results

if __name__ == "__main__":
    r.log("Starting program...")
    from sys import argv
    if len(argv) != 3:
        print("Usage: python ff.py <version (like ff_grid_0)> <gridsearch_descriptor_string>")
        exit(2)
    VERSION = argv[1]
    SUBVERSION = 1
    subversion_details = argv[2]
    ops = parse_subversion(subversion_details)
    IMAGE_DIMENSION = ops['image_dimension']
    BATCH_SIZE = ops['batch_size']
    NUM_HIDDEN_LAYERS = ops['deepness']
    HIDDEN_SIZE = ops['hidden_size']
    if int(SUBVERSION) == 0:
        #make a (image input) -> (NUM_HIDDEN_LAYERS * )(hidden) -> (classification output) network
        model = Sequential()
        model.add(Reshape((IMAGE_DIMENSION**2,), input_shape=(1, IMAGE_DIMENSION, IMAGE_DIMENSION)))
        for _ in range(NUM_HIDDEN_LAYERS):
            model.add(Dense(HIDDEN_SIZE))
            model.add(Activation(ops['activation']))
            if 'dropout' in ops:
                model.add(Dropout(ops['dropout']))
        model.add(Dense(NB_CLASSES))
        model.add(Activation('softmax'))
    else:
        model = r.load(VERSION, int(SUBVERSION)-1)
    
    optimizer=ops['optimizer']
    if optimizer=='sgd' and 'learning_rate' in ops:
        if 'momentum' in ops:
            optimizer = SGD(lr=ops['learning_rate'], momentum=ops['momentum'])
        else:
            optimizer = SGD(lr=ops['learning_rate'])
    model.compile(optimizer=optimizer, loss=ops['loss'], metrics=['accuracy'])
    
    training_generator, validation_generator = r.image_generators(IMAGE_DIMENSION, BATCH_SIZE, DATA_DIRECTORY)
    
    history = model.fit_generator(
        training_generator,
        samples_per_epoch = int(SAMPLES_PER_EPOCH / BATCH_SIZE) * BATCH_SIZE,
        nb_epoch = NB_EPOCH,
        validation_data = validation_generator,
        nb_val_samples = NUM_VALIDATION_SAMPLES,
        verbose=0)
    
    r.log("Training done. Saving everything...")
    r.save(model, history, VERSION, SUBVERSION)
    
    r.log("Done.")
