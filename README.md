# HildeGUARD

Need an extra layer of security for your SSH connections? HildeGUARD provides two-factor authentication for SSH without entering the PAM hell or performing other black magic.

![](https://github.com/scireum/hildeguard/raw/master/hildeguard_short.gif)

[See a longer Demo](https://github.com/scireum/hildeguard/blob/master/DEMO.md)

All you need to do is to obtain/compile the binary and set it as your login shell. Next is to create the file **~/.ssh/authorized_tokens** in the form:

```
<name> <secret> <ip> e.g.
Test JBSWY3DPEHPK3PXP 192.168.0.1/24
```

The IP address can be a single IP, a subnet given in CIDR notation or a list thereof separated by commas. If the users remote IP matches the IP/net listed here, the user may enter without further checks. Otherwise a one time password (TOTP), generated e.g. by Google Authenticator has to be entered.

This provides an alternative to "from" directives on SSH keys as it and gives you flexibility, security and convenience all at once.

> IMPORTANT:
>  * When testing/using HildeGUARD make sure you have a backup account with a default shell to logon in case of a bug! (Either in the software or your configuration). 
>  * It might be a good idea to keep a "backup" ssh connection open while testing hildeGUARD
>  * Make sure your server clock is NTP synced - TOTPs require an accurate clock as the time window is only 30 seconds


## License

HildeGUARD is licensed under the MIT License:

> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in
> all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
> THE SOFTWARE.
