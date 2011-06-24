# Datasource Ciphered Password example

This example shows how to use a ciphered password with an OpenEJB datasource.

It shows how to implement its own encryption too.

# Configuration

The configuration is simply a datasource configuration with an additionnal parameter
"PasswordCipher" to specify the encryption to use.

Example using Static3DES encryption:

        properties.setProperty("ProtectedDatasource", "new://Resource?type=DataSource");
        properties.setProperty("ProtectedDatasource.JdbcDriver", "org.hsqldb.jdbcDriver");
        properties.setProperty("ProtectedDatasource.JdbcUrl", "jdbc:hsqldb:mem:protected");
        properties.setProperty("ProtectedDatasource.UserName", "user");
        // the plain text password is "YouLLN3v3rFindM3"
        properties.setProperty("ProtectedDatasource.Password", "fEroTNXjaL5SOTyRQ92x3DNVS/ksbtgs");
        properties.setProperty("ProtectedDatasource.PasswordCipher", "Static3DES");
        properties.setProperty("ProtectedDatasource.JtaManaged", "true");


# Using its own implementation

The example implement a reverse encryption which simply reverse the password to encrypt/decrypt.

The implementation is done with commons-lang library:

    public static class ReverseEncryption implements PasswordCipher {
        @Override public char[] encrypt(String plainPassword) {
            return StringUtils.reverse(plainPassword).toCharArray();
        }

        @Override public String decrypt(char[] encryptedPassword) {
            return new String(encrypt(new String(encryptedPassword)));
        }
    }


To be functional it needs the file META-INF/org.apache.openejb.resource.jdbc.PasswordCipher/reverse.

The file name (reverse) define  the encryption name to use for the PasswordCipher parameter.

This file simply contains the implementation class of the encryption.

Then you simply declare this encryption for your datasource:

        properties.setProperty("ProtectedDatasource", "new://Resource?type=DataSource");
        properties.setProperty("ProtectedDatasource.JdbcDriver", "org.hsqldb.jdbcDriver");
        properties.setProperty("ProtectedDatasource.JdbcUrl", "jdbc:hsqldb:mem:protected");
        properties.setProperty("ProtectedDatasource.UserName", USER);
        properties.setProperty("ProtectedDatasource.Password", "3MdniFr3v3NLLuoY");
        properties.setProperty("ProtectedDatasource.PasswordCipher", "reverse");
        properties.setProperty("ProtectedDatasource.JtaManaged", "true");

# Documentation

For more information please see the [OpenEJB documentation](http://openejb.apache.org/3.0/datasource-password-encryption.html)

