call generateKeyPair.bat serveralias serverPassword serverStore.jks keystorePass serverKey.rsa
call generateKeyPair.bat clientalias  clientPassword  clientStore.jks keystorePass clientKey.rsa
keytool -import -alias serveralias -file serverKey.rsa -keystore clientStore.jks -storepass keystorePass -noprompt
keytool -import -alias clientalias -file clientKey.rsa -keystore serverStore.jks -storepass keystorePass -noprompt
