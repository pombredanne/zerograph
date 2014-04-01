from zerograph import Graph

g0 = Graph.zero("localhost")


def test_can_use_pointers():
    batch = g0.create_batch()
    a = batch.create_node()
    b = batch.create_node()
    batch.create_rel(a, b, "KNOWS")
    results = batch.submit()
    for result in results:
        print(result)
