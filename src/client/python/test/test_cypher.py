import unittest

from zerograph import Node, Table

from .helpers import ZerographTestCase


class CypherTestCase(ZerographTestCase):

    def test_simple_statement(self):
        result = self.graph.execute("CREATE (a:Person {name:'Alice'}) RETURN a")
        assert isinstance(result, Table)
        assert result.columns == ["a"]
        assert result.rows == [
            [Node({"Person"}, {"name": "Alice"})],
        ]

    def test_statement_with_parameters(self):
        result = self.graph.execute("CREATE (a:Person {name:{name}}) RETURN a", {"name": "Alice"})
        assert isinstance(result, Table)
        assert result.columns == ["a"]
        assert result.rows == [
            [Node({"Person"}, {"name": "Alice"})],
        ]

    def test_statement_with_multiple_parameters(self):
        param_sets = [{"name": "Alice"}, {"name": "Bob"}, {"name": "Carol"}]
        results = self.graph.execute("CREATE (a:Person {name:{name}}) RETURN a", *param_sets)
        assert len(results) == len(param_sets)
        for i, result in enumerate(results):
            assert isinstance(result, Table)
            assert result.columns == ["a"]
            assert result.rows == [
                [Node({"Person"}, param_sets[i])],
            ]


if __name__ == "__main__":
    unittest.main()
