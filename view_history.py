import pickle
from matplotlib import pyplot as plt
from sys import argv

#Command-line ops:
# <version> <subversion number> [-k (kernal size)] [-v (loss/acc)]

def avg(values):
    return sum(values)/len(values)

def smoothed(values, kernal=5):
    newvalues=values[:]
    for i in range(len(values)):
        surrounding = [i+k for k in range(-kernal+1,kernal) if i+k >=0 and i+k<len(values)]
        newvalues[i]=avg([values[pt] for pt in surrounding])
    return newvalues

assert 7>=len(argv)>=3
version = argv[1]
subversion = argv[2]
if '-k' in argv:
    smoothing=int(argv[argv.index('-k')+1])
else:
    smoothing=5
if '-v' in argv:
    what = argv[argv.index('-v')+1]
else:
    what = 'acc'

with open("models/{}/training_sessions/{}/history.txt".format(version,subversion), "rb") as f:
    res = pickle.load(f)

#check new/old-style history
res = getattr(res, 'history', res) #returns res.history if history in res, else res.

#see http://stackoverflow.com/a/4701285 - help to add legend
fig = plt.figure()
ax = plt.subplot(111)

val, = plt.plot(smoothed(res['val_'+what], smoothing),'r',label="Validation " + what.title())
train, = plt.plot(smoothed(res[what], smoothing),'g',label="Training " + what.title())

box = ax.get_position()
ax.set_position([box.x0, box.y0 + box.height * 0.1, box.width, box.height * 0.9])
ax.legend(bbox_to_anchor=(.5,-.05), loc='upper center', ncol=2)

plt.show()
