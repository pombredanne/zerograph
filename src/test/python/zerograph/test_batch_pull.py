from unittest import main

from zerograph import BatchPull

from .helpers import ZerographTestCase


class BatchPullTestCase(ZerographTestCase):

    def test_can_batch_pull_nodes_and_rel(self):
        # Create and retrieve a couple of remote nodes and a rel
        a, ab, b = self.graph.execute("""\
        CREATE (a:Person {name:'Alice'})-[ab:KNOWS]->(b:Person {name:'Bob'})
        RETURN a, ab, b
        """).rows[0]
        # Check the name properties of the local objects
        assert a["name"] == "Alice"
        assert b["name"] == "Bob"
        assert ab.rels[0].type == "KNOWS"
        assert ab.rels[0]["since"] is None
        # Update the remote copies
        self.graph.execute("""\
        MATCH (a:Person {name:'Alice'})-[ab:KNOWS]->(b:Person {name:'Bob'})
        SET a.name = 'Alice Smith',
            b.name = 'Bob Jones',
            ab.since = 1999
        """)
        # Check the local copies haven't changed
        assert a["name"] == "Alice"
        assert b["name"] == "Bob"
        assert ab.rels[0].type == "KNOWS"
        assert ab.rels[0]["since"] is None
        # Batch pull the remote changes
        batch = BatchPull(self.graph)
        batch.add(a)
        batch.add(b)
        batch.add(ab)
        batch.submit()
        # Check the local copies have been updated
        assert a["name"] == "Alice Smith"
        assert b["name"] == "Bob Jones"
        assert ab.rels[0]["since"] == 1999


if __name__ == "__main__":
    main()
