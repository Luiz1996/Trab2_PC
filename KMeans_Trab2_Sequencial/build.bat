mkdir build

mkdir dist

javac src/br/uem/din/pc/model/*.java src/br/uem/din/pc/controller/*.java src/br/uem/din/pc/main/*.java -d build/

cd build

jar cmf ../manifest_compiler.mf ../dist/KMeans.jar br/uem/din/pc/main/*.class br/uem/din/pc/model/*.class br/uem/din/pc/controller/*.class

java -jar ../dist/KMeans.jar 1

pause
