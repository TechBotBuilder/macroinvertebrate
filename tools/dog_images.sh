#!/bin/bash
for directory in *
do
  mogrify -morphology Convolve DoG:0,1.8,2.4 $directory/*.*
  echo "Completed DOG on images in $directory"
done
