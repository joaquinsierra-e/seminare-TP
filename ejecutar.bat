@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

set JAR=lib\mysql-connector-j-8.4.0.jar
if not exist lib mkdir lib

if not exist "%JAR%" (
  echo Descargando el driver JDBC de MySQL...
  curl.exe -L -f -o "%JAR%" https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
  if errorlevel 1 (
    echo No se pudo descargar el driver. Copie mysql-connector-j-8.x.jar manualmente en la carpeta lib\
    del "%JAR%" 2>nul
    pause
    exit /b 1
  )
)

echo Compilando...
if exist out rmdir /s /q out
javac -encoding UTF-8 -cp "lib/*" -d out src\Main.java src\modelo\*.java src\controlador\*.java src\dao\*.java src\dao\memoria\*.java src\excepciones\*.java src\util\*.java
if errorlevel 1 (
  echo Error de compilacion.
  pause
  exit /b 1
)

echo Ejecutando (agregue --memoria para usar datos de demostracion sin MySQL)...
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out;lib/*" src.Main %*
pause
