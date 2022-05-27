# MakePassword: A password generator

Passwords are generated from three pieces of information: the name of the
entity for which a password is being generated (public), a passphrase known
by the user (a relatively weak secret but not stored anywhere on the
computer), and a file containing random data (a strong secret but one that
might be compromised by an adversary who has taken over the machine).  It is
fine--but not necessary--to reuse the same passphrase for all of your
passwords.  Passwords can't be broken unless both of the two secrets are
compromised or the adversary can crack MD5. Using a stronger hash function
such as SHA-2 would be good, but the hash function is probably not the weak
point in this scheme.

Since the script takes several arguments, it may be convenient to create your
own script or alias that invokes this one and passes the appropriate key-file
as an argument. The accompanying script 'pw.release' can be used and
customized to your own requirements.

To create a key-file, I recommend the following command:

```
  head -c 500 < /dev/random  >  <filename>
```

Written by Andrew Myers, c. 2010. Version of Oct. 23, 2012.
