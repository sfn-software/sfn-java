# sfn-java
A small and fast utility to send files over network.

Help
----

Usage:

    siphon --listen [options]
    siphon --connect <address> [options]

`-l` and `-s` are aliases for `--listen`, `-c` is an alias for `--connect`.

Options:

* `--version`, `-v`: Show sfn version and exit.
* `--help`, `-h`: Show this text and exit.
* `--port`, `-p`: Use specified port. Defaults to 3214.
* `--file`, `-f`: Send specified files of directories after connection. Use "-f file1 -f file2" to send multiple files.
* `--directory`, `-d`: Use specified directory to store received files. Format is: /home/user/folder/.

Screenshot
----------

![image](https://raw.github.com/solkin/sfn-java/master/art/main.png)

Related projects
----------------

* [siphon](https://github.com/solkin/siphon) &mdash; compatible C implementation
* [sfn](https://github.com/m1kc/sfn) &mdash; compatible D implementation
