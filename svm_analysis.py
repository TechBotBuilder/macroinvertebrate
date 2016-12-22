#analysis of gridsearched data for svm architecture.
import pickle

def magic(data):
    print("In first half: {}".format(sum(data[:len(data)//2])))
    print("In last half:  {}".format(sum(data[len(data)//2:])))

def count(data, what):
    yeps = [1 if x==what else 0 for x in data]
    magic(yeps)


with open("models/svm_gridsearch/results.pickle", "rb") as f:
    svmres = pickle.load(f)
svmres.sort(key=lambda x: x[1]['loss']) #very important :)

losses = [s[s.index('.L')+2:s.index('.L')+6] for s in [x[0] for x in svmres]]
##splitpt = sum([1 for x in losses if x=='mean'])
###all mse losses are much less than cce losses, so this is safe.
##meandata = svmres[:splitpt]
##catedata = svmres[splitpt:]
#but the below is much more resiliant
def loss(s):
    return s[s.index('.L')+2:s.index('.L')+6]
meandata = [x for x in svmres if loss(x[0])=='mean']
catedata = [x for x in svmres if loss(x[0])=='cate']

def itercount(data, name):
    for x in set(data):
        print("For {} {}".format(name, x))
        count(data, x)

def evaluate(data):
    names = [x[0] for x in data]
    optimizers = [s[:s.index('H')] for s in names]
    itercount(optimizers, "optimizer")
    hiddensizes = [s[s.index('H')+1:s.index('.N')] for s in names]
    itercount(hiddensizes, "hidden size")
    batchsizes = [s[s.index('.N')+2:s.index('.I')] for s in names]
    itercount(batchsizes, "batch size")
    imgsizes = [s[s.index('.I')+2:s.index('.A')] for s in names]
    itercount(imgsizes, "image size")
    activations = [s[s.index('.A')+2:s.index('.L')] for s in names]
    itercount(activations, "activation")
    sgd_names = [s[0] for s in data if "SGD" in s[0]]
    sgd_momentums = ['0' if '.m' not in s else ('0.5' if '.m0.5' in s else '0.9') for s in sgd_names]
    itercount(sgd_momentums, "(SGD-only) momentum")
    sgd_lrs = [s[s.index('.l')+2:s.index('.l')+3] for s in sgd_names]
    itercount(sgd_lrs, "(SGD-only) learning rate of (1/2) ^")
    rms_names = [s[0] for s in data if "RMSprop" in s[0]]
    rms_lrs = [s[s.index('.l')+2:s.index('.l')+4] for s in rms_names]
    itercount(rms_lrs, "(RMS-only) learning rate of (1/2) ^")
    

print("Mean-squared-error results: ")
evaluate(meandata)

print("Categorical crossentropy results: ")
evaluate(catedata)

