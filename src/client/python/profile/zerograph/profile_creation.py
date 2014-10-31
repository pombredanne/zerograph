from __future__ import print_function

import random
from time import time
from uuid import uuid4

from zerograph import Graph, Batch


graph = None


def create(count):

    t = [time()]

    batch = Batch(graph)
    for i in range(count):
        batch.create_node(properties={"number": i, "uuid": uuid4().hex})

    t.append(time())

    nodes = list(batch.submit())

    t.append(time())

    rel_types = ["RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "INDIGO", "VIOLET"]
    batch = Batch(graph)
    for i in range(count):
        batch.create_rel(start=random.choice(nodes)._id,
                         end=random.choice(nodes)._id,
                         type=random.choice(rel_types))

    t.append(time())

    rels = list(batch.submit())

    t.append(time())

    print("")
    #print("Creation   : {:.3f}s/1000".format(1000 * (t[1] - t[0]) / count))
    print("Nodes : {:.3f}s/1000".format(1000 * (t[2] - t[1]) / count))
    print("Rels  : {:.3f}s/1000".format(1000 * (t[4] - t[3]) / count))



if __name__ == "__main__":
    import cProfile
    graph = Graph.open("localhost", port=47471)
    create(10000)
    try:
        cProfile.run("create(10000)")
    finally:
        Graph.drop("localhost", 47471)
