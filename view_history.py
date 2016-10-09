import pickle
from matplotlib import pyplot as plt
from sys import argv

#Command-line ops:
# <version> <subversion>

def avg(values):
    return sum(values)/len(values)

def smoothed(values, kernel=5):
    newvalues=values[:]
    oldvalues=values[:]
    for i in range(len(values)):
        surrounding = [i+k for k in range(-5,5) if i+k >=0 and i+k<len(values)]
        newvalues[i]=avg([oldvalues[pt] for pt in surrounding])
    return newvalues

assert 4>=len(argv)>=3
version = argv[1]
subversion = argv[2]
if len(argv)==4:
    smoothing=argv[3]
else:
    smoothing=5

with open("models/{}/training_sessions/{}/history.txt".format(version,subversion), "rb") as f:
    res = pickle.load(f).history

#see http://stackoverflow.com/a/4701285 - help to add legend
fig = plt.figure()
ax = plt.subplot(111)

val, = plt.plot(smoothed(res['val_acc'], smoothing),'r',label="Validation")
train, = plt.plot(smoothed(res['acc'], smoothing),'g',label="Training")

box = ax.get_position()
ax.set_position([box.x0, box.y0 + box.height * 0.1, box.width, box.height * 0.9])
ax.legend(bbox_to_anchor=(.5,-.05), loc='upper center', ncol=2)

plt.show()
