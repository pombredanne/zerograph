from unittest import main

from zerograph import BatchCreate, Node, Path

from .helpers import ZerographTestCase


class BatchCreateTestCase(ZerographTestCase):

    def test_can_batch_create_new_nodes(self):
        batch = BatchCreate(self.graph)
        batch.add(Node(name="Alice"))
        batch.add(Node(name="Bob"))
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 2
        assert result_list[0] == Node(name="Alice")
        assert result_list[0].bound
        assert result_list[1] == Node(name="Bob")
        assert result_list[1].bound

    def test_can_batch_create_existing_nodes(self):
        result = self.graph.execute("""\
        CREATE (a {name:'Alice'}), (b {name:'Bob'})
        RETURN a, b
        """)
        a, b = next(iter(result))
        b["age"] = 44
        batch = BatchCreate(self.graph)
        batch.add(a)
        batch.add(b)
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 2
        assert result_list[0] == Node(name="Alice")
        assert result_list[0].bound
        assert result_list[0] is a
        assert result_list[1] == Node(name="Bob", age=44)
        assert result_list[1].bound
        assert result_list[1] is b

    def test_can_create_path_when_just_one_unbound_node(self):
        batch = BatchCreate(self.graph)
        batch.add(Path({"name": "Alice"}))
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 1
        assert result_list[0] == Path({"name": "Alice"})
        assert all(map(lambda x: x.bound, result_list[0].nodes))
        assert all(map(lambda x: x.bound, result_list[0].rels))

    def test_can_create_path_when_just_one_bound_node(self):
        result = self.graph.execute("""\
        CREATE (a {name:'Alice'})
        RETURN a
        """)
        a, = next(iter(result))
        batch = BatchCreate(self.graph)
        batch.add(Path(a))
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 1
        assert result_list[0] == Path(a)
        assert all(map(lambda x: x.bound, result_list[0].nodes))
        assert all(map(lambda x: x.bound, result_list[0].rels))

    def test_can_batch_create_new_paths(self):
        batch = BatchCreate(self.graph)
        batch.add(Path({"name": "Alice"}, "KNOWS", {"name": "Bob"}))
        batch.add(Path({"name": "Alice"}, "KNOWS", {"name": "Bob"}))
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 2
        assert result_list[0] == Path({"name": "Alice"}, "KNOWS", {"name": "Bob"})
        assert all(map(lambda x: x.bound, result_list[0].nodes))
        assert all(map(lambda x: x.bound, result_list[0].rels))
        assert result_list[1] == Path({"name": "Alice"}, "KNOWS", {"name": "Bob"})
        assert all(map(lambda x: x.bound, result_list[1].nodes))
        assert all(map(lambda x: x.bound, result_list[1].rels))
        assert result_list[0] is not result_list[1]

    def test_can_batch_create_partially_existing_paths(self):
        result = self.graph.execute("""\
        CREATE (a {name:'Alice'})
        RETURN a
        """)
        a, = next(iter(result))
        batch = BatchCreate(self.graph)
        batch.add(Path(a, "KNOWS", {"name": "Bob"}))
        batch.add(Path(a, "KNOWS", {"name": "Bob"}))
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 2
        assert result_list[0] == Path(a, "KNOWS", {"name": "Bob"})
        assert all(map(lambda x: x.bound, result_list[0].nodes))
        assert all(map(lambda x: x.bound, result_list[0].rels))
        assert result_list[1] == Path(a, "KNOWS", {"name": "Bob"})
        assert all(map(lambda x: x.bound, result_list[1].nodes))
        assert all(map(lambda x: x.bound, result_list[1].rels))
        assert result_list[0] is not result_list[1]

    def test_can_batch_create_fully_existing_paths(self):
        result = self.graph.execute("""\
        CREATE (a:Person {name:'Alice'})-[ab:KNOWS]->(b:Person {name:'Bob'})
        RETURN a, ab, b
        """)
        a, ab, b = next(iter(result))
        batch = BatchCreate(self.graph)
        batch.add(ab)
        batch.add(ab)
        result = batch.submit()
        result_list = list(result)
        assert len(result_list) == 2
        assert result_list[0] == ab
        assert all(map(lambda x: x.bound, result_list[0].nodes))
        assert all(map(lambda x: x.bound, result_list[0].rels))
        assert result_list[1] == ab
        assert all(map(lambda x: x.bound, result_list[1].nodes))
        assert all(map(lambda x: x.bound, result_list[1].rels))
        assert result_list[0] is not result_list[1]


if __name__ == "__main__":
    main()
