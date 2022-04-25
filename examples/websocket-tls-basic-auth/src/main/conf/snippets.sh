# Generate self-signed certifacte store
keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks -validity 36000

# Extract CERT
openssl pkcs12 -info -nodes -in keystore.jks -nokeys 2>&1| sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > server.crt

# Extract Key
openssl pkcs12 -info -nodes -in keystore.jks -nocerts 2>&1| sed -ne '/-BEGIN PRIVATE KEY-/,/-END PRIVATE KEY-/p' > server.key