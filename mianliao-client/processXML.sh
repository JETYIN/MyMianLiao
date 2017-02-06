array=($(ls ./res/layout/*))
for i in "${array[@]}"; 
do python processXML.py $i; 
done