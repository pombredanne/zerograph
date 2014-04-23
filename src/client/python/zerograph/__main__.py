import sys

from . import Graph


def main():
    command = sys.argv[1]
    if command == "open":
        port = int(sys.argv[2])
        Graph.open(port=port)
    elif command == "drop":
        port = int(sys.argv[2])
        Graph.open(port=port).drop()
    elif command == "shell":
        from .shell import main
        main()
    else:
        raise ValueError("Unknown command")


if __name__ == "__main__":
    main()
