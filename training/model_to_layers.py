###
# Convert a model's weights to binary data usable by the mobile app
###

from sys import argv

if len(argv) != 3:
    print("Arguments should be: model_name model_version")
    quit()

from keras.models import model_from_json

MODEL_PATH = "models/{}".format(argv[1])
SAVE_PATH = MODEL_PATH+"/training_sessions/{}".format(argv[2])

with open(MODEL_PATH+"/architecture.json", "r") as f:
    model = model_from_json(f.read())
model.load_weights(SAVE_PATH+"/weights.hdf5")
weights = model.get_weights()

def toShort(value):
    return int(value*2048).to_bytes(2, byteorder='big', signed=True)

import os
if not os.path.exists(SAVE_PATH+"/layers"):
    os.makedirs(SAVE_PATH+"/layers")

#weights[2k] : layer k weights : shaped like (layerinsize, layeroutsize)
#weights[2k+1] : layer k biases : shaped like (layeroutsize,)
for layernum in range(len(weights)//2):
    layerweight = weights[2*layernum]
    layerbias = weights[2*layernum+1]
    with open(SAVE_PATH+"/layers/layer{}".format(layernum+1), "wb") as f:
        layer_in_size = len(layerweight)
        layer_out_size = len(layerbias)
        for output_rownum in range(layer_out_size):
            for output_colnum in range(layer_in_size):
                f.write(toShort(layerweight[output_colnum][output_rownum]))
            f.write(toShort(layerbias[output_rownum]))

print("Layers have been written to {}/layers".format(SAVE_PATH))

