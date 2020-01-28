mkdir build

mkdir dist

javac -cp src/br/uem/din/pc/libraries/mpj.jar src/br/uem/din/pc/model/*.java src/br/uem/din/pc/controller/*.java src/br/uem/din/pc/main/*.java -d build/

cd build

jar cmf ../manifest_compiler.mf ../dist/KMeans.jar br/uem/din/pc/main/*.class br/uem/din/pc/model/*.class br/uem/din/pc/controller/*.class

java -jar C:\mpj\lib\starter.jar -np 5 ../dist/KMeans.jar 1

pause