# Zerograph

## Server

To run the server:

```bash
$ gradle run
```

### Storage

If running as root, the databases will be stored in ``/var/zerograph`` by
default. For other users, the ``$HOME/.zerograph`` directory will be used.
These defaults can be overridden using the ``ZG_STORAGE_PATH`` environment
variable.

### Service

The central Zerograph service instance listens on port 47470. Databases can be
started on any other ports as required.


## Client

To run the Python test shell:

```bash
$ pip3 install pyzmq
$ cd src/client/python
$ python3 -m zerograph.repl
```
