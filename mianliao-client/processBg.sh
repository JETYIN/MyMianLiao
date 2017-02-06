array=($(ls ./res/layout/*activity_*))
for i in "${array[@]}";
do python processBg.py $i;
done