import pickle

#to do dictionary sorting quickly
import collections
import operator

def error(msg):
    
    exit(2) #https://docs.python.org/2/library/sys.html#sys.exit -> 2=command line syntax errors

def compare(version, sortby, num_output, output=False):
    #data like list of
    #(SUBVERSION, {'loss':#, 'val_loss':#, 'acc':#, 'val_acc':#})
    with open("models/{}/results.pickle".format(version), 'rb') as f:
        results = pickle.load(f)
    if sortby == 'combo':
        val_accs = dict([dp for dp in enumerate([(d[1]['acc']*d[1]['val_acc'])**0.5 for d in results])])
    else:
        val_accs = dict([dp for dp in enumerate([d[1][sortby] for d in results])])
    val_accs = sorted(val_accs.items(), key=operator.itemgetter(1), reverse=True)
    #^Help from http://stackoverflow.com/a/613218
    if output:
        for dp in val_accs[0:min(len(val_accs), num_output)]:
            d = results[dp[0]]
            print("{:45} acc={:.3}, val_acc={:.3}".format(d[0]+":", d[1]['acc'], d[1]['val_acc']))

if __name__ == "__main__":
    sortbyops = ('combo', 'acc', 'val_acc')
    from sys import argv
    n = 5
    sortby = sortbyops[0]
    if len(argv) < 2:
        error("Options:\n"
        + "<model_type> [sort_by [num_results]]")
    version = argv[1] + "_gridsearch"
    if len(argv) > 2:
        sortby = argv[2]
        if sortby not in sortbyops:
            msg = "Sort by one of:"
            for sb in sortbyops:
                msg = msg + "  '"+sb+"'"
            msg = msg + "."
            error(msg)
    if len(argv) > 3:
        try:
            n = int(argv[3])
        except:
            error("Third argument must be an integer. Given", argv[3] + ".")
    compare(version, sortby, n, True)
