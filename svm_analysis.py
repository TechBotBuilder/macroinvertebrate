#analysis of gridsearched data for svm architecture.

def magic(data):
    print("In first half: {}".format(sum(data[:len(data)//2])))
    print("In last half:  {}".format(sum(data[len(data)//2:])))


def count(data, what):
    yeps = [1 if x==what else 0 for x in data]
    magic(yeps)

with open("models/svm_gridsearch/results.pickle", "rb") as f:
    svmres = pickle.load(f)
svmres.sort(key=lambda x: x[1]['loss'])

losses = [s[s.index('.L')+2:s.index('.L')+6] for s in [x[0] for x in svmres]]
splitpt = sum([1 for x in losses if x=='mean'])

