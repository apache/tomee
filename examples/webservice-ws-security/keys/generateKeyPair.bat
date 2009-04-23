rem @echo off
echo alias %1
echo keypass %2
echo keystoreName %3
echo KeyStorePass %4
echo keyName %5

echo keyName %5
keytool -genkey -alias %1 -keypass %2 -keystore %3 -storepass %4  -dname "cn=%1" -keyalg RSA
keytool -selfcert -alias %1 -keystore %3 -storepass %4 -keypass %2
keytool -export -alias %1 -file %5 -keystore %3 -storepass %4
