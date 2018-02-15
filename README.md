# HildeGUARD
Signs public keys (id_rsa.pub) usign a CA certificate to permit ssh access to
users authenticated via LDAP (Active Directory).

The certificate automatically contains all LDAP groups which start with ssh-
to permit fine grained access control.

The software is licensed under the MIT license.
