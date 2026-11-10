@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
rem Corre el programa contra MySQL con las respuestas de datos\demo_entrada.txt
rem y guarda todo lo que muestra la pantalla en capturas\salida_demo.txt (sirve como prueba repetible).
if not exist config\db.properties copy config\db.properties.ejemplo config\db.properties >nul
if exist out rmdir /s /q out
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -cp "lib/*" -d out @fuentes.txt
del fuentes.txt
if errorlevel 1 ( echo Error de compilacion. & pause & exit /b 1 )
if not exist capturas mkdir capturas
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out;lib/*" src.Main --mysql < datos\demo_entrada.txt > capturas\salida_demo.txt 2>&1
type capturas\salida_demo.txt
echo.
echo Salida guardada en capturas\salida_demo.txt
pause
