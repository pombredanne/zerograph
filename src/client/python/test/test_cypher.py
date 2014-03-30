from zerograph import Graph

g1 = Graph.open("localhost", 47471)
result = g1.execute("MATCH (n) RETURN n")
