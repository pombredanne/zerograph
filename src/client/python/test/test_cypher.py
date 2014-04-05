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


if __name__ == "__main__":
    unittest.main()
