# HildeGUARD
Signs public keys (id_rsa.pub) usign a CA certificate to permit ssh access to
users authenticated via LDAP (Active Directory).

The certificate automatically contains all LDAP groups which start with ssh-
to permit fine grained access control.

# HildeGUARD installation

```bash
mkdir -p /hildeguard/data
cd /hildeguard/data
# setup a SSH CA without a phasephrase (empty)
ssh-keygen -C CA -f ca
cd /hildeguard
touch instance.conf

```

Create a extra user with a password inside your Windows ADFS / LDAP.

Use the created instance.conf within /hildeguard to configure HildeGUARD to use the new credentials:
```
ldap {
    server = "ldap://192.168.0.1"
    ssl = false
    user = "hildeguard-ssh"
    password = "foobar"
    userSuffix = "@example.com"
    searchBase = "dc=example,dc=com"
}
```
 HildeGUARD will create a new SSH host key on the first connect. It is stored within /hildeguard as sshd.key.
 
 HildeGUARD runs inside the Docker container as user ID 2000
 
 ```
 chown -R 2000:2000 /hildeguard
 ```
 
 Start the HildeGUARD docker container:
 
 ```
 docker run -p 2222:2222 -v /hildeguard/data:/home/sirius/data -v /hildeguard/instance.conf:/home/sirius/instance.conf  scireum/hildeguard:1.0.2
 ```


The software is licensed under the MIT license.
