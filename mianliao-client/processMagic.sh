array=($(ls ./res/layout/*))
for i in "${array[@]}"; 
do python processMagic.py $i; 
done