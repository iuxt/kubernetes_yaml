sed 's#{namespace}#dev#g'  template.yaml |kubectl apply -f -
sed 's#{namespace}#test#g'  template.yaml |kubectl apply -f -
sed 's#{namespace}#prod#g'  template.yaml |kubectl apply -f -