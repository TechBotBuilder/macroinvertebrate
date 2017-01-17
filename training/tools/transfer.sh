#!/bin/bash
dontdofirstthing=0

for FILENUM in $@
do
  if [ $dontdofirstthing == 0 ]; then
    dontdofirstthing=1
    continue
  fi
  FNAME=`ls ../training/$1/ | grep "^$FILENUM\."`
  if [[ $FNAME != "" ]]; then
    echo "Moving $FNAME"
    mv ../training/$1/$FNAME $1/$FNAME
  else
    echo "No matching files for $FILENUM"
  fi
done
